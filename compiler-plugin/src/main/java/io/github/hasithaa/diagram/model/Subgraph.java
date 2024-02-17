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

public class Subgraph implements JsonElement, MermaidElement, Linkable {

    final SubgraphKind kind;
    private final String iId;
    String label;
    List<Node> nodes = new ArrayList<>();
    List<Edge> edges = new ArrayList<>();

    public Subgraph(String iId, SubgraphKind kind) {
        this.iId = iId;
        this.kind = kind;
    }

    @Override
    public String getIId() {
        return iId;
    }

    @Override
    public String getJsonString(int wsCount) {
        String ws = getWs(wsCount);
        StringBuilder json = new StringBuilder();
        json.append(ws).append("{\n");
        json.append(ws).append("  \"kind\": \"").append(kind).append("\",\n");
        json.append(ws).append("  \"label\": \"").append(label).append("\",\n");
        json.append(ws).append("  \"iId\": \"").append(iId).append("\",\n");
        json.append(ws).append("  \"nodes\": [\n");
        for (Node node : nodes) {
            json.append(node.getJsonString(wsCount + 3)).append(",\n");
        }
        if (!nodes.isEmpty()) {
            json.deleteCharAt(json.length() - 2);
        }
        json.append(ws).append("  ]\n");
        json.append(ws).append("}");

        return json.toString();
    }

    @Override
    public String getMermaidString(int wsCount) {
        String ws = getWs(wsCount);
        StringBuilder mermaid = new StringBuilder();
        mermaid.append(ws).append("subgraph ").append(iId).append("[").append(label).append("]\n");
        mermaid.append(ws).append("direction ").append("TB").append("\n");
        for (Node node : nodes) {
            mermaid.append(node.getMermaidString(wsCount + 2));
        }
        mermaid.append(ws).append("end\n");
        edges.forEach(edge -> mermaid.append(ws).append(edge.getMermaidString(wsCount)));
        return mermaid.toString();
    }

    enum SubgraphKind {
        WORKER, ENDPOINT
    }
}
