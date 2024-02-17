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

import java.util.Optional;

public class Edge implements JsonElement, MermaidElement {

    final String iId;
    final Linkable source;
    final Linkable target;
    String label = null;
    EdgeKind kind = EdgeKind.DEFAULT;

    Edge(String iId, Linkable source, Linkable target) {
        this.iId = iId;
        this.source = source;
        this.target = target;
    }

    @Override
    public String getJsonString(int wsCount) {
        String ws = getWs(wsCount);
        StringBuilder json = new StringBuilder();
        json.append(ws).append("{\n");
        Optional.ofNullable(label).ifPresent(s -> json.append(ws).append("  \"label\": \"").append(s).append("\",\n"));
        Optional.ofNullable(source).ifPresent(node -> json.append(ws).append("  \"source\": \"").append(node.getIId())
                                                          .append("\",\n"));
        Optional.ofNullable(target).ifPresent(node -> json.append(ws).append("  \"target\": \"").append(node.getIId())
                                                          .append("\",\n"));
        json.append(ws).append("  \"kind\": \"").append(kind).append("\",\n");
        json.append(ws).append("  \"iId\": \"").append(iId).append("\"\n");
        json.append(ws).append("}");
        return json.toString();
    }

    @Override
    public String getMermaidString(int wsCount) {
        String ws = getWs(wsCount);
        StringBuilder mermaid = new StringBuilder();
        String connection = " ==> ";
        if (kind == EdgeKind.IMPLICIT) {
            connection = " --> ";
        } else if (kind == EdgeKind.HIDDEN) {
            connection = " ~~~ ";
        } else if (kind == EdgeKind.OPTIONAL) {
            connection = " -.-> ";
        } else if (kind == EdgeKind.UNREACHABLE) {
            connection = " x-.-x ";
        }
        if (source != null && target != null) {
            mermaid.append(ws).append(source.getIId()).append(connection);
            if (label != null) {
                mermaid.append("|").append(label).append("|");
            }
            mermaid.append(target.getIId()).append("\n");
        }
        return mermaid.toString();
    }

    enum EdgeKind {
        DEFAULT, IMPLICIT, HIDDEN, OPTIONAL, UNREACHABLE
    }


}
