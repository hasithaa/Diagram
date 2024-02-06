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
package io.github.hasithaa.diagram.integration.templates;

import io.github.hasithaa.diagram.flowchart.NodeKind;
import io.github.hasithaa.diagram.integration.AbstractOperation;
import io.github.hasithaa.diagram.integration.BBKind;

public class LibraryCall extends AbstractOperation {

    public LibraryCall(int id, String description) {
        super(id, description);
    }

    @Override
    public NodeKind getFlowchartNodeKind() {
        return NodeKind.CUSTOM_PROCESS;
    }

    @Override
    public BBKind getKind() {
        return BBKind.LIBRARY_CALL;
    }

    @Override
    public String icon() {
        return "🧩";
    }
}