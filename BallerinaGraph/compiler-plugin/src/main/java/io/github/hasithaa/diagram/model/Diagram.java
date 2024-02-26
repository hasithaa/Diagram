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
import java.util.Collections;
import java.util.List;

public class Diagram implements JsonElement, MermaidElement {

    DiagramType diagramType;
    List<Node> nodes = new ArrayList<>();
    List<Subgraph> subgraphs = new ArrayList<>();
    String iId;
    private String label;

    public DiagramType getDiagramType() {
        return diagramType;
    }

    void setDiagramType(DiagramType diagramType) {
        this.diagramType = diagramType;
    }

    @Override
    public String getJsonString(int wsCount) {
        String ws = getWs(wsCount);
        StringBuilder json = new StringBuilder();
        json.append(ws).append("{\n");
        json.append(ws).append("  \"nodes\": [\n");
        for (Node node : nodes) {
            json.append(node.getJsonString(wsCount + 2)).append(",\n");
        }
        if (!nodes.isEmpty()) {
            json.deleteCharAt(json.length() - 2);
        }
        json.append(ws).append("  ],\n");
        json.append(ws).append("  \"subgraphs\": [\n");
        for (Subgraph subgraph : subgraphs) {
            json.append(subgraph.getJsonString(wsCount + 2)).append(",\n");
        }
        if (!subgraphs.isEmpty()) {
            json.deleteCharAt(json.length() - 2);
        }
        json.append(ws).append("  ],\n");
        json.append(ws).append("  \"label\": \"").append(label).append("\",\n");
        json.append(ws).append("  \"iId\": \"").append(iId).append("\"\n");
        json.append(ws).append("}");
        return json.toString();
    }

    public String getLabel() {
        return label;
    }

    void setLabel(String label) {
        this.label = label;
    }

    @Override
    public String getMermaidString(int wsCount) {
        String ws = getWs(wsCount);
        StringBuilder mermaid = new StringBuilder();
        mermaid.append(ws).append("flowchart TB").append("\n");
        for (Node node : nodes) {
            mermaid.append(node.getMermaidString(wsCount + 2));
        }
        for (Subgraph subgraph : subgraphs) {
            mermaid.append(subgraph.getMermaidString(wsCount + 2));
        }
        return mermaid.toString();
    }

    public List<Node> getNodes() {
        return Collections.unmodifiableList(nodes);
    }

    public List<Subgraph> getSubgraphs() {
        return Collections.unmodifiableList(subgraphs);
    }

    enum DiagramType {
        API,
        RPC,
        TRIGGER,
        FUNCTION,
    }
}
