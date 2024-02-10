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

public abstract class AbstractCompositeInOperation extends AbstractOperation implements CompositeInOperations {

    List<Sequence> incomingSequences = new ArrayList<>();
    List<Edge> flowchartEdges = null;

    public AbstractCompositeInOperation(int id) {
        super(id);
    }

    public AbstractCompositeInOperation(int id, AbstractOperation parent) {
        super(id, parent);
    }

    public List<Edge> getFlowchartEdges() {
        if (flowchartEdges != null) {
            return flowchartEdges;
        }
        final List<Edge> flowchartEdges = new ArrayList<>();
        // Link the last operation of each sequence to the current operation
        for (Sequence sequence : incomingSequences) {
            if (!sequence.getOperations().isEmpty()) {
                int lastIndex = sequence.getOperations().size() - 1;
                flowchartEdges.add(
                        new Edge(sequence.getOperations().get(lastIndex).getFlowchartNode(), this.getFlowchartNode(),
                                 sequence.getLabel()));
            } else {
                // Implicit Edge.
                flowchartEdges.add(
                        new Edge(sequence.getSource().getFlowchartNode(), this.getFlowchartNode(),
                                 sequence.getLabel()));
            }
        }
        flowchartEdges.addAll(super.getFlowchartEdges());
        this.flowchartEdges = Collections.unmodifiableList(flowchartEdges);
        return flowchartEdges;
    }

    @Override
    public List<Sequence> incomingSequence() {
        return Collections.unmodifiableList(incomingSequences);
    }

    public void addIncomingSequence(Sequence sequence) {
        this.incomingSequences.add(sequence);
    }
}
