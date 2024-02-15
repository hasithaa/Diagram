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
package io.github.hasithaa.diagram.json;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class ModelBuilder {

    private final Model model;

    int diagramCount = 0;
    int edgeCount = 0;
    int nodeCount = 0;
    Stack<List<Node>> currentNodeList;

    ModelBuilder() {
        this.model = new Model();
        this.currentNodeList = new Stack<>();
    }

    public Diagram addDiagram() {
        Diagram diagram = new Diagram();
        diagram.iId = "Diagram" + diagramCount;
        this.model.diagrams.add(diagram);
        nodeCount = 0;
        edgeCount = 0;
        currentNodeList = new Stack<>();
        currentNodeList.push(diagram.nodes);
        return diagram;
    }

    public Node createNode(Node.Kind kind) {
        Node node = new Node();
        node.iId = "Node" + nodeCount++;
        node.kind = kind;
        return node;
    }

    public void addNode(Node node, boolean partial) {
        List<Node> nodes = currentNodeList.peek();
        if (!nodes.isEmpty() || !partial) {
            Node lastNode = nodes.get(nodes.size() - 1);
            Edge edge = new Edge("Edge" + edgeCount++);
            edge.source = Optional.of(lastNode);
            edge.target = Optional.of(node);
            lastNode.edges.add(edge);
        }
        nodes.add(node);
    }

    public Node addNewNode(Node.Kind kind) {
        Node node = createNode(kind);
        addNode(node, false);
        return node;
    }

    public void startChildFlow(String label) {
        Node lastElement = currentNodeList.peek().get(currentNodeList.peek().size() - 1);
        List<Node> children = new ArrayList<>();
        lastElement.getChildren().put(label, children);
        currentNodeList.push(children);
    }

    public void endChildFlow(Node source, Node target) {
        List<Node> nodes = currentNodeList.pop();
        if (!nodes.isEmpty()) {
            Node firstElement = nodes.get(0);
            Edge sourceEdge = new Edge("Edge" + edgeCount++);
            sourceEdge.source = Optional.of(source);
            sourceEdge.target = Optional.of(firstElement);
            source.edges.add(sourceEdge);

            Node lastElement = nodes.get(nodes.size() - 1);
            Edge targetEdge = new Edge("Edge" + edgeCount++);
            targetEdge.source = Optional.of(lastElement);
            targetEdge.target = Optional.of(target);
            lastElement.edges.add(targetEdge);
        } else {
            Edge edge = new Edge("Edge" + edgeCount++);
            edge.source = Optional.of(source);
            edge.target = Optional.of(target);
            edge.kind = Edge.EdgeKind.IMPLICIT;
            source.edges.add(edge);
        }
    }

    private Diagram getCurrentDiagram() {
        return this.model.diagrams.get(model.diagrams.size() - 1);
    }

    public void addFormData(String key, FormData formData) {
        getCurrentDiagram().nodes.get(nodeCount - 1).formData.put(key, formData);
    }

}
