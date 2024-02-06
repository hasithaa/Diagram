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

public class BasicNode implements FlowchartComponent, IdentifiableComponent {

    protected final String identifier;
    protected final String description;
    protected final NodeKind kind;

    public BasicNode(String identifier, NodeKind kind) {
        this.identifier = identifier;
        this.description = null;
        this.kind = kind;
    }

    public BasicNode(String identifier, String description, NodeKind kind) {
        this.identifier = identifier;
        this.description = description;
        this.kind = kind;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String generateMermaidSyntax(int index) {
        final String ws = "  ".repeat(index);
        if (description == null) {
            return ws + identifier;
        }
        return ws + identifier + kind.start + "\"" + description.trim() + "\"" + kind.end + "\n";
    }
}
