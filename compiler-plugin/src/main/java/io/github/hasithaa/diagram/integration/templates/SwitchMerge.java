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

import io.github.hasithaa.diagram.integration.AbstractCompositeInOperation;
import io.github.hasithaa.diagram.integration.AbstractOperation;
import io.github.hasithaa.diagram.integration.TemplateKind;
import io.github.hasithaa.diagram.integration.UnEditable;

public class SwitchMerge extends AbstractCompositeInOperation implements UnEditable {

    public SwitchMerge(int id, AbstractOperation parent) {
        super(id, parent);
    }

    @Override
    public TemplateKind getKind() {
        return TemplateKind.SWITCH_MERGE;
    }

    @Override
    public String icon() {
        return "⚪";
    }

    @Override
    public String getFlowChartDisplayContent() {
        return icon();
    }

    public String getSimpleFlowChartDisplayContent() {
        return icon();
    }
}
