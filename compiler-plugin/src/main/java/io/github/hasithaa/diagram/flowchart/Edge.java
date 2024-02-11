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
package io.github.hasithaa.diagram.flowchart;

public class Edge implements FlowchartComponent {

    private final FlowchartComponent source;
    private final FlowchartComponent target;
    private final String label;
    private String style = "SOLID";
    private boolean arrowHead = true;

    public Edge(FlowchartComponent source, FlowchartComponent target) {
        this.source = source;
        this.target = target;
        this.label = null;
        if (source == target) {
            throw new IllegalArgumentException("Source and target cannot be the same");
        }
    }

    public Edge(FlowchartComponent source, FlowchartComponent target, String label) {
        this.source = source;
        this.target = target;
        this.label = label;
        if (source == target) {
            throw new IllegalArgumentException("Source and target cannot be the same");
        }
    }

    public void setStyle(String style) {
        this.style = style;
    }

    @Override
    public String generateMermaidSyntax(int index) {
        final String ws = "  ".repeat(index);
        if (source instanceof IdentifiableComponent && target instanceof IdentifiableComponent) {
            StringBuilder sb = new StringBuilder();
            sb.append(ws).append(((IdentifiableComponent) source).getIdentifier()).append(" ").append(
                    getConnectionStyle()).append(" ");
            if (label != null) {
                sb.append("|").append(label).append("| ");
            }
            sb.append(((IdentifiableComponent) target).getIdentifier()).append("\n");
            return sb.toString();
        }
        return "";
    }

    private String getConnectionStyle() {
        if (arrowHead) {
            return switch (style) {
                case "DOTTED" -> "-.->";
                case "STRONG" -> "==>";
                case "HIDDEN" -> "~~~";
                default -> "-->";
            };
        } else {
            return switch (style) {
                case "DOTTED" -> "-.-";
                case "STRONG" -> "===";
                case "HIDDEN" -> "~~~";
                default -> "---";
            };
        }
    }

    public void setArrowHead(boolean arrowHead) {
        this.arrowHead = arrowHead;
    }
}
