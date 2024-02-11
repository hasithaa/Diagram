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

import java.util.ArrayList;
import java.util.List;

public class SubGraph implements FlowchartComponent, IdentifiableComponent {

    protected final String identifier;
    protected final String description;
    private final List<FlowchartComponent> components = new ArrayList<>();

    public SubGraph(String identifier, String description) {
        this.identifier = identifier;
        this.description = description;
    }

    public void add(FlowchartComponent component) {
        components.add(component);
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String generateMermaidSyntax(int index) {
        final String ws = "  ".repeat(index);
        StringBuilder builder = new StringBuilder();
        builder.append(ws).append("subgraph ").append(identifier).append(" [\"").append(description).append("\"]\n");
        builder.append(ws).append("direction ").append("TB").append("\n");
        for (FlowchartComponent component : components) {
            builder.append(component.generateMermaidSyntax(index + 1));
        }
        builder.append("end\n");
        return builder.toString();
    }
}
