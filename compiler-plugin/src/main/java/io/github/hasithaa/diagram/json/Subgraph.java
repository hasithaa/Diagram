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

public class Subgraph implements JsonElement {

    final SubgraphKind kind;
    String iid;
    String label;
    List<Node> nodes = new ArrayList<>();

    public Subgraph(SubgraphKind kind) {
        this.kind = kind;
    }

    @Override
    public String getJsonString(int wsCount) {
        String ws = " ".repeat(wsCount * 4);
        StringBuilder json = new StringBuilder();
        json.append(ws).append("{\n");
        json.append(ws).append("  \"kind\": \"").append(kind).append("\",\n");
        json.append(ws).append("  \"label\": \"").append(label).append("\",\n");
        json.append(ws).append("  \"iId\": \"").append(iid).append("\",\n");
        json.append(ws).append("  \"nodes\": [\n");
        for (Node node : nodes) {
            json.append(node.getJsonString(wsCount + 1)).append(",\n");
        }
        if (!nodes.isEmpty()) {
            json.deleteCharAt(json.length() - 2);
        }
        json.append(ws).append("  ]\n");
        json.append(ws).append("}");

        return json.toString();
    }

    enum SubgraphKind {
        WORKER, ENDPOINT
    }
}
