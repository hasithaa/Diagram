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

import java.util.Objects;

public class Edge implements FlowchartComponent {

    private final FlowchartComponent source;
    private final FlowchartComponent target;
    private final String label;

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

    @Override
    public String generateMermaidSyntax(int index) {
        final String ws = "  ".repeat(index);
        if (source instanceof IdentifiableComponent && target instanceof IdentifiableComponent) {
            StringBuilder sb = new StringBuilder();
            sb.append(ws).append(((IdentifiableComponent) source).getIdentifier()).append(" --> ");
            if (label != null) {
                sb.append("|").append(label).append("| ");
            }
            sb.append(((IdentifiableComponent) target).getIdentifier()).append("\n");
            return sb.toString();
        }
        return "";
    }

    @Override
    public boolean equals(Object o) {
        // Let's ignore the label when comparing edges for now.
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Edge edge = (Edge) o;
        return Objects.equals(source, edge.source) && Objects.equals(target, edge.target);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, target);
    }
}
