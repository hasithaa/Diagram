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
import io.github.hasithaa.diagram.integration.templates.Sequence;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class AbstractCompositeOutOperation extends AbstractOperation implements CompositeOutOperation {

    List<Sequence> outgoingOperations = new ArrayList<>();
    List<Edge> flowchartEdges = null;

    public AbstractCompositeOutOperation(int id) {
        super(id);
    }

    public List<Edge> getFlowchartEdges() {
        if (flowchartEdges != null) {
            return flowchartEdges;
        }
        final List<Edge> flowchartEdges = new ArrayList<>();
        // Link the first operation of each sequence to the current operation
        for (Sequence sequence : outgoingOperations) {
            if (!sequence.getOperations().isEmpty()) {
                flowchartEdges.add(
                        new Edge(this.getFlowchartNode(), sequence.getOperations().get(0).getFlowchartNode(),
                                 sequence.getLabel()));
            } else {
                // Implicit Edge.
                flowchartEdges.add(new Edge(this.getFlowchartNode(), sequence.getTarget().getFlowchartNode(),
                                            sequence.getLabel()));
            }
        }
        flowchartEdges.addAll(super.getFlowchartEdges());
        this.flowchartEdges = Collections.unmodifiableList(flowchartEdges);
        return flowchartEdges;
    }

    @Override
    public List<Sequence> outgoingSequence() {
        return Collections.unmodifiableList(outgoingOperations);
    }

    @Override
    public void addOutgoingSequence(Sequence operation) {
        this.outgoingOperations.add(operation);
    }
}
