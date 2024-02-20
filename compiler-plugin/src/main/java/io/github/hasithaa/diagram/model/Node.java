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

import io.ballerina.tools.text.LineRange;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Node implements JsonElement, MermaidElement, Linkable {

    private final String iId;
    String label;
    Kind kind;
    String subKind = null;
    String subLabel = null;
    Map<String, List<FormData>> formData = new LinkedHashMap<>();
    LineRange lineRange;
    Node parent = null;
    List<Edge> edges = new ArrayList<>();
    boolean editable = true;
    boolean terminal = false;
    boolean returnable = false;
    // Temporary list to hold incoming edges for optimization
    List<Edge> incomingEdges = new ArrayList<>();
    private Map<String, List<Node>> children = null;
    private Map<String, Subgraph> subgraphMap = null;

    Node(String iId) {
        this.iId = iId;
    }

    @Override
    public String getIId() {
        return iId;
    }

    @Override
    public String getJsonString(int wsCount) {
        if (terminal && !returnable) {
            return "";
        }
        String ws = getWs(wsCount);
        StringBuilder json = new StringBuilder();
        json.append(ws).append("{\n");
        json.append(ws).append("  \"label\": \"").append(label).append("\",\n");
        json.append(ws).append("  \"kind\": \"").append(kind).append("\",\n");
        Optional.ofNullable(subKind).ifPresent(s -> json.append(ws).append("  \"subkind\": \"").append(s)
                                                        .append("\",\n"));
        Optional.ofNullable(subLabel).ifPresent(s -> json.append(ws).append("  \"sublabel\": \"").append(s)
                                                         .append("\",\n"));
        json.append(ws).append("  \"formData\": {\n");
        for (Map.Entry<String, List<FormData>> entry : formData.entrySet()) {
            json.append(ws).append("    \"").append(entry.getKey()).append("\": [\n");
            for (FormData formData : entry.getValue()) {
                json.append(formData.getJsonString(wsCount + 3)).append(",\n");
            }
            if (!entry.getValue().isEmpty()) {
                json.deleteCharAt(json.length() - 2);
            }
            json.append(ws).append("    ],\n");
        }
        if (!formData.isEmpty()) {
            json.deleteCharAt(json.length() - 2);
        }
        json.append(ws).append("  },\n");
        if (lineRange == null) {
            json.append(ws).append("  \"lineRange\": null,\n");
        } else {
            json.append(ws).append("  \"lineRange\": {\n");
            json.append(ws).append("    \"fileName\": \"").append(lineRange.fileName()).append("\",\n");
            json.append(ws).append("    \"startLine\": ").append(lineRange.startLine().line()).append(",\n");
            json.append(ws).append("    \"endLine\": ").append(lineRange.endLine().line()).append("\n");
            json.append(ws).append("  },\n");
        }
        Optional.ofNullable(parent).ifPresent(node -> json.append(ws).append("  \"parent\": \"").append(node.iId)
                                                          .append("\",\n"));
        if (children != null) {
            json.append(ws).append("  \"children\": {\n");
            for (Map.Entry<String, List<Node>> entry : children.entrySet()) {
                json.append(ws).append("    \"").append(entry.getKey()).append("\": [\n");
                for (Node node : entry.getValue()) {
                    json.append(node.getJsonString(wsCount + 3)).append(",\n");
                }
                if (!entry.getValue().isEmpty()) {
                    json.deleteCharAt(json.length() - 2);
                }
                json.append(ws).append("    ],\n");
            }
            if (!children.isEmpty()) {
                json.deleteCharAt(json.length() - 2);
            }
            json.append(ws).append("  },\n");
        }
        json.append(ws).append("  \"edges\": [\n");
        for (Edge edge : edges) {
            json.append(edge.getJsonString(wsCount + 2)).append(",\n");
        }
        if (!edges.isEmpty()) {
            json.deleteCharAt(json.length() - 2);
        }
        json.append(ws).append("  ],\n");
        json.append(ws).append("  \"iId\": \"").append(iId).append("\",\n");
        json.append(ws).append("  \"terminal\": ").append(terminal).append(",\n");
        json.append(ws).append("  \"returnable\": ").append(returnable).append(",\n");
        json.append(ws).append("  \"editable\": ").append(editable).append("\n");
        json.append(ws).append("}");
        return json.toString();
    }

    public boolean hasChildren() {
        return children != null && !children.isEmpty();
    }

    public Map<String, List<Node>> getChildren() {
        if (children == null) {
            children = new LinkedHashMap<>();
        }
        return children;
    }

    public Map<String, Subgraph> getSubgraphMap() {
        if (subgraphMap == null) {
            subgraphMap = new LinkedHashMap<>();
        }
        return subgraphMap;
    }

    @Override
    public String getMermaidString(int wsCount) {
        if (terminal && !returnable) {
            return "";
        }
        String ws = getWs(wsCount);
        StringBuilder mermaid = new StringBuilder();
        mermaid.append(ws).append(iId).append(getMermaidNode()).append("\n");
        Optional.ofNullable(children).ifPresent(map -> map.forEach((label, nodes) -> {
            nodes.forEach(node -> mermaid.append(node.getMermaidString(wsCount)));
        }));
        edges.forEach(edge -> mermaid.append(edge.getMermaidString(wsCount)));
        return mermaid.toString();
    }

    private String getMermaidNode() {
        StringBuilder sb = new StringBuilder();
        sb.append(getNodeStart()).append("\"");
        sb.append(getIcon().replaceAll("fa-", "fa:fa-"));
        String heading = getHeading();
        if (heading != null && !heading.isEmpty()) {
            sb.append("<br><strong>").append(getHeading()).append("</strong>");
        }
        String subHeading = getSubHeading();
        if (subHeading != null && !subHeading.isEmpty()) {
            sb.append("<br>").append(subHeading);
        }
        if (!formData.isEmpty()) {
            sb.append("<br><table class=\"table\">");
            if (formData.containsKey("Network")) {
                sb.append("<tr><td><strong>Network</strong></td></tr>");
                formData.getOrDefault("Network", new ArrayList<>()).forEach(
                        formData -> sb.append(formData.getMermaidString(0)));
            }
            if (formData.containsKey("Expressions")) {
                sb.append("<tr><td><strong>Expressions</strong></td></tr>");
                formData.getOrDefault("Expressions", new ArrayList<>()).forEach(
                        formData -> sb.append(formData.getMermaidString(0)));
            }
            if (formData.containsKey("Variables")) {
                sb.append("<tr><td><strong>Variables</strong></td></tr>");
                formData.getOrDefault("Variables", new ArrayList<>()).forEach(
                        formData -> sb.append(formData.getMermaidString(0)));
            }
            sb.append("</table>");
        }
        sb.append("\"").append(getNodeEnd());
        return sb.toString();
    }

    private String getNodeStart() {
        if (kind == Kind.KONNECTOR || kind == Kind.ASYNC_START) {
            return "((";
        } else if (kind == Kind.END) {
            return "(((";
        }
        return "[";
    }

    private String getNodeEnd() {
        if (kind == Kind.KONNECTOR || kind == Kind.ASYNC_START) {
            return "))";
        } else if (kind == Kind.END) {
            return ")))";
        }
        return "]";
    }

    public String getIcon() {
        String icon = switch (kind) {
            case IF -> "fa-code-merge";
            case CLONE -> "fa-clone";
            case WAIT -> "fa-clock";
            case NETWORK_EVENT -> "fa-network-wired fa-arrow-right-to-bracket";
            case TRIGGER -> "fa-arrow-right-to-bracket";
            case FUNCTION_START -> "fa-turn-down";
            case NETWORK_REMOTE_CALL, NETWORK_RESOURCE_CALL -> "fa-right-from-bracket fa-network-wired";
            case KONNECTOR -> "fa-code-branch";
            case LIBRARY_FUNCTION -> "fa-cogs";
            case DATA_MAPPING -> "fa-timeline";
            case DATA_CONVERT -> "fa-file-code fa-right-left fa-file-csv";
            case END -> "fa-stop";
            case DATA_NEW_MESSAGE -> "fa-envelope";
            case DATA_VALIDATION -> "fa-envelope-circle-check";
            case KNOWN_FUNCTION_CALL -> "fa-gear";
            case RETURN -> "fa-turn-up";
            case ASYNC_START -> "fa-play";
            case ASYNC_RETURN -> "fa-arrow-up-right-from-square";
            default -> "fa-gears";
        };
        if (returnable && kind != Kind.RETURN && kind != Kind.ASYNC_RETURN) {
            icon = icon + " fa-turn-up";
        }
        return icon;
    }

    public String getHeading() {
        return switch (kind) {
            case DATA_NEW_MESSAGE -> "New " + subKind;
            case KONNECTOR, ASYNC_START -> "";
            default -> label;
        };
    }

    private String getSubHeading() {
        return subLabel;
    }

    enum Kind {
        // Events
        TRIGGER, NETWORK_EVENT, FUNCTION_START, ASYNC_START,

        // Control flow
        IF, CLONE, WAIT, RETURN, FOREACH, WHILE, CONTINUE, BREAK, ASYNC_RETURN,

        // Data Operations
        DATA_MAPPING, DATA_NEW_MESSAGE, DATA_UPDATE, DATA_VARIABLE,

        // Data Conversion
        DATA_CONVERT, DATA_VALIDATION,

        // Network Operations
        NETWORK_RESOURCE_CALL, NETWORK_REMOTE_CALL,

        // LIBRARY FUNCTIONS
        LIBRARY_FUNCTION, KNOWN_FUNCTION_CALL,

        // Terminal
        END,

        // ERROR Handling
        DO,

        // Node Connection
        KONNECTOR, // Intentionally misspelled to avoid conflict with Connectors

        // Default,
        EXPRESSION, CODE_BLOCK// This is the default kind for all other nodes
    }
}
