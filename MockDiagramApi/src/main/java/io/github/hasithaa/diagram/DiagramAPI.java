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
package io.github.hasithaa.diagram;

import io.github.hasithaa.diagram.model.INode;
import io.github.hasithaa.diagram.model.core.If;
import io.github.hasithaa.diagram.model.core.Return;
import io.github.hasithaa.diagram.model.events.HttpApi;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DiagramAPI {

    public static void getDiagram() {

    }

    public static void getDiagramNodeDescription() {
        Map<String, List<INode<?>>> diagramNodeDescription = new LinkedHashMap<>();

        List<INode<?>> core = new ArrayList<>();
        diagramNodeDescription.put("Core", core);
        core.add(new If());
        core.add(new Return());

        List<INode<?>> events = new ArrayList<>();
        diagramNodeDescription.put("Events", events);
        core.add(new HttpApi());
    }

    public static void getSourceCode() {

    }
}
