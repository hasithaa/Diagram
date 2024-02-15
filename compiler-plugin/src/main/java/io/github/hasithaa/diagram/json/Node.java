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

import io.ballerina.tools.text.LineRange;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Node implements JsonElement {

    String label;
    Kind kind;
    Optional<String> subkind = Optional.empty();
    Optional<String> sublabel = Optional.empty();
    Map<String, FormData> formData = new LinkedHashMap<>();
    LineRange lineRange;

    Optional<Node> parent = Optional.empty();
    Optional<Map<String, List<Node>>> children = Optional.empty();
    List<Edge> edges = new ArrayList<>();

    String iId;
    boolean editable = true;

    @Override
    public String getJsonString(int wsCount) {
        String ws = " ".repeat(wsCount * 4);
        StringBuilder json = new StringBuilder();
        json.append(ws).append("{\n");
        json.append(ws).append("  \"label\": \"").append(label).append("\",\n");
        json.append(ws).append("  \"kind\": \"").append(kind).append("\",\n");
        subkind.ifPresent(s -> json.append(ws).append("  \"subkind\": \"").append(s).append("\",\n"));
        sublabel.ifPresent(s -> json.append(ws).append("  \"sublabel\": \"").append(s).append("\",\n"));
        json.append(ws).append("  \"formData\": {\n");
        for (Map.Entry<String, FormData> entry : formData.entrySet()) {
            json.append(ws).append("    \"").append(entry.getKey()).append("\": ").append(
                    entry.getValue().getJsonString(wsCount + 1)).append(",\n");
        }
        if (!formData.isEmpty()) {
            json.deleteCharAt(json.length() - 2);
        }
        json.append(ws).append("  },\n");
        json.append(ws).append(" \"lineRange\": {\n");
        json.append(ws).append("    \"fileName\": \"").append(lineRange.fileName()).append("\",\n");
        json.append(ws).append("    \"startLine\": ").append(lineRange.startLine().line()).append(",\n");
        json.append(ws).append("    \"endLine\": ").append(lineRange.endLine().line()).append("\n");
        json.append(ws).append("  },\n");

        parent.ifPresent(node -> json.append(ws).append("  \"parent\": \"").append(node.iId).append("\",\n"));
        children.ifPresent(map -> {
            json.append(ws).append("  \"children\": {\n");
            for (Map.Entry<String, List<Node>> entry : map.entrySet()) {
                json.append(ws).append("    \"").append(entry.getKey()).append("\": [\n");
                for (Node node : entry.getValue()) {
                    json.append(node.getJsonString(wsCount + 2)).append(",\n");
                }
                if (!entry.getValue().isEmpty()) {
                    json.deleteCharAt(json.length() - 2);
                }
                json.append(ws).append("    ],\n");
            }
            if (!map.isEmpty()) {
                json.deleteCharAt(json.length() - 2);
            }
            json.append(ws).append("  },\n");
        });
        json.append(ws).append("  \"edges\": [\n");
        for (Edge edge : edges) {
            json.append(edge.getJsonString(wsCount + 1)).append(",\n");
        }
        if (!edges.isEmpty()) {
            json.deleteCharAt(json.length() - 2);
        }
        json.append(ws).append("  ],\n");
        json.append(ws).append("  \"iId\": \"").append(iId).append("\",\n");
        json.append(ws).append("  \"editable\": ").append(editable).append("\n");
        json.append(ws).append("}");
        return json.toString();
    }

    enum Kind {
        // Events
        TRIGGER, NETWORK_EVENT, FUNCTION_START,

        // Control flow
        IF, CLONE, WAIT, RETURN, FOREACH, WHILE, CONTINUE, BREAK, WORKER_RETURN,

        // Data Operations
        DATA_MAPPING, DATA_NEW_MESSAGE, DATA_UPDATE, DATA_VARIABLE,

        // Network Operations
        NETWORK_RESOURCE_CALL, NETWORK_REMOTE_CALL,

        // LIBRARY FUNCTIONS
        LIBRARY_FUNCTION,

        // Terminal
        END,

        // Node Connection
        KONNECTOR, // Intentionally misspelled to avoid conflict with Connectors
    }
}
