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
package io.github.hasithaa.diagram.integration;

import io.github.hasithaa.diagram.flowchart.Edge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractCompositeInOperation extends AbstractOperation implements CompositeInOperations {

    List<Operation> incomingOperations = new ArrayList<>();
    List<Edge> flowchartEdges = null;

    public AbstractCompositeInOperation(int id, String description) {
        super(id, description);
    }

    public List<Edge> getFlowchartEdges() {
        if (flowchartEdges != null) {
            return flowchartEdges;
        }
        List<Edge> flowchartEdges = new ArrayList<>();
        flowchartEdges.addAll(super.getFlowchartEdges());
        for (Operation operation : incomingOperations) {
            flowchartEdges.add(new Edge(operation.getFlowchartNode(), this.getFlowchartNode()));
        }
        flowchartEdges = Collections.unmodifiableList(flowchartEdges);
        return flowchartEdges;
    }

    @Override
    public List<Operation> incomingOperations() {
        return Collections.unmodifiableList(incomingOperations);
    }

    @Override
    public void addIncomingOperation(Operation operation) {
        this.incomingOperations.add(operation);
    }
}
