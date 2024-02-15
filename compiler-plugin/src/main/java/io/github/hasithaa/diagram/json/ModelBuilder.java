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

import io.ballerina.compiler.api.symbols.Symbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class ModelBuilder {

    private final Model model;
    private final List<Symbol> dataMapping;

    int diagramCount = 0;
    int edgeCount = 0;
    int nodeCount = 0;
    Stack<List<Node>> currentNodeList;

    ModelBuilder() {
        this.model = new Model();
        this.dataMapping = new ArrayList<>();
        this.currentNodeList = new Stack<>();
    }

    public void addDataMapping(Symbol symbol) {
        this.dataMapping.add(symbol);
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

    public Node addNode(Node.Kind kind) {
        Node node = new Node();
        node.iId = "Node" + nodeCount;
        node.kind = kind;
        List<Node> nodes = this.model.diagrams.get(this.model.diagrams.size() - 1).nodes;
        nodes.add(node);
        currentNodeList.peek().add(node);
        nodeCount++;
        return node;
    }

    public void startChildFlow(String label) {
        Node lastElement = currentNodeList.peek().get(currentNodeList.peek().size() - 1);
        List<Node> children = new ArrayList<>();
        lastElement.getChildren().put(label, children);
        currentNodeList.push(children);
    }

    public void endChildFlow() {
        currentNodeList.pop();
    }

    public void addFormData(String key, FormData formData) {
        this.model.diagrams.get(this.model.diagrams.size() - 1).nodes.get(nodeCount - 1).formData.put(key, formData);
    }

}
