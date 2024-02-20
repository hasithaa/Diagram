/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.github.hasithaa.diagram.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ModelBuilder {

    private final Model model;

    int diagramCount = 0;
    int edgeCount = 0;
    int nodeCount = 0;
    int subGraphCount = 0;
    int scopeCount = 0;
    Stack<Scope> currentNodeList;
    Diagram currentDiagram = null;

    ModelBuilder() {
        this.model = new Model();
        this.currentNodeList = new Stack<>();
    }

    public Model getModel() {
        optimize();
        return model;
    }

    private void optimize() {
        for (Diagram diagram : model.diagrams) {
            optimizeNodes(diagram.nodes);
        }
    }

    private void optimizeNodes(List<Node> nodes) {
        for (Node node : nodes) {
            if (!node.incomingEdges.isEmpty() && node.incomingEdges.stream().allMatch(
                    edge -> edge.kind == Edge.EdgeKind.UNREACHABLE)) {
                node.edges.forEach(edge -> edge.kind = Edge.EdgeKind.UNREACHABLE);
                node.terminal = true;
            }
            node.getChildren().forEach((label, children) -> optimizeNodes(children));
        }
    }

    public Diagram addDiagram() {
        Diagram diagram = new Diagram();
        currentDiagram = diagram;
        diagram.iId = "Diagram" + diagramCount;
        this.model.diagrams.add(diagram);
        nodeCount = 0;
        edgeCount = 0;
        subGraphCount = 0;
        currentNodeList = new Stack<>();
        currentNodeList.push(new Scope("default", diagram.nodes));
        return diagram;
    }

    public Node createNode(Node.Kind kind) {
        Node node = new Node(currentDiagram.iId + "Node" + nodeCount++);
        node.kind = kind;
        return node;
    }

    public void addNode(Node node, boolean partial) {
        List<Node> nodes = currentNodeList.peek().nodes;
        if (!nodes.isEmpty() && !partial) {
            Node lastNode = nodes.get(nodes.size() - 1);
            addEdge(lastNode, node);
        }
        node.setScope(currentNodeList.peek().scope);
        nodes.add(node);
    }

    public Node addNewNode(Node.Kind kind) {
        Node node = createNode(kind);
        addNode(node, false);
        return node;
    }

    public Edge addEdge(Node source, Subgraph target) {
        Edge edge = new Edge(currentDiagram.iId + "Edge" + edgeCount++, source, target);
        source.edges.add(edge);
        return edge;
    }

    public Edge addEdge(Subgraph source, Node target) {
        Edge edge = new Edge(currentDiagram.iId + "Edge" + edgeCount++, source, target);
        source.edges.add(edge);
        target.incomingEdges.add(edge);
        return edge;
    }

    public Edge addEdge(Node source, Node target) {
        Edge edge = new Edge(currentDiagram.iId + "Edge" + edgeCount++, source, target);
        source.edges.add(edge);
        if (target != null) {
            target.incomingEdges.add(edge);
        }
        return edge;
    }

    public Subgraph startSubGraph(String label) {
        Subgraph subGraph = new Subgraph(currentDiagram.iId + "SubGraph" + subGraphCount++,
                                         Subgraph.SubgraphKind.WORKER);
        subGraph.label = label;
        getCurrentDiagram().subgraphs.add(subGraph);
        if (!currentNodeList.peek().nodes.isEmpty()) {
            Node lastElement = currentNodeList.peek().nodes.get(currentNodeList.peek().nodes.size() - 1);
            lastElement.getSubgraphMap().put(label, subGraph);
            Edge sourceEdge = addEdge(lastElement, subGraph);
            sourceEdge.kind = Edge.EdgeKind.IMPLICIT;
            sourceEdge.label = "Start";
        }
        currentNodeList.push(new Scope("scope" + scopeCount++, subGraph.nodes));
        addNewNode(Node.Kind.ASYNC_START);
        return subGraph;
    }

    public void endSubGraph() {
        currentNodeList.pop();
    }

    public void startChildFlow(String label) {
        List<Node> children = new ArrayList<>();
        if (!currentNodeList.peek().nodes.isEmpty()) {
            Node lastElement = currentNodeList.peek().nodes.get(currentNodeList.peek().nodes.size() - 1);
            lastElement.getChildren().put(label, children);
        }
        currentNodeList.push(new Scope("scope" + scopeCount++, children));
    }

    public void endChildFlow(Node source, Node target, String label) {
        List<Node> nodes = currentNodeList.pop().nodes;
        if (!nodes.isEmpty()) {
            Node firstElement = nodes.get(0);
            Edge sourceEdge = addEdge(source, firstElement);
            sourceEdge.label = label;

            Node lastElement = nodes.get(nodes.size() - 1);
            Edge targetEdge = addEdge(lastElement, target);
            if (lastElement.terminal || lastElement.returnable) {
                targetEdge.kind = Edge.EdgeKind.UNREACHABLE;
            }
        } else {
            Edge edge = addEdge(source, target);
            edge.kind = Edge.EdgeKind.IMPLICIT;
        }
    }

    private Diagram getCurrentDiagram() {
        if (currentDiagram == null) {
            currentDiagram = this.model.diagrams.get(model.diagrams.size() - 1);
        }
        return currentDiagram;
    }

    public void addFormData(String key, FormData formData) {
        List<Node> peek = currentNodeList.peek().nodes;
        if (!peek.get(peek.size() - 1).formData.containsKey(key)) {
            peek.get(peek.size() - 1).formData.put(key, new ArrayList<>());
        }
        peek.get(peek.size() - 1).formData.get(key).add(formData);

    }

    public void setLabel(String name) {
        this.model.setLabel(name);
    }

    record Scope(String scope, List<Node> nodes) {
    }
}
