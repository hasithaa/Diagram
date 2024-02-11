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

import io.github.hasithaa.diagram.integration.AbstractCompositeInOperation;
import io.github.hasithaa.diagram.integration.AbstractCompositeOutOperation;
import io.github.hasithaa.diagram.integration.DPath;
import io.github.hasithaa.diagram.integration.Diagram;
import io.github.hasithaa.diagram.integration.Operation;
import io.github.hasithaa.diagram.integration.Sequence;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface FlowChartGenerator {

    static FlowChart generateFlowChart(Diagram diagram, List<Sequence> eps, boolean simple) {

        Map<Operation, BasicNode> nodeMap = new HashMap<>();
        FlowChart flowChart = new FlowChart(diagram.getName());
        generateNodes(diagram.getBaseSequence(), flowChart, nodeMap, simple);
        generateEPs(eps, flowChart, nodeMap, simple);
        for (DPath dPath : diagram.getPaths()) {
            Edge component = new Edge(nodeMap.get(dPath.source), nodeMap.get(dPath.target), dPath.label);
            component.setStyle(dPath.pathType.name());
            component.setArrowHead(dPath.arrowHead);
            flowChart.add(component);
        }
        return flowChart;
    }

    static void generateEPs(List<Sequence> eps, FlowChart flowChart, Map<Operation, BasicNode> nodeMap,
                            boolean simple) {
        int count = 0;
        for (Sequence ep : eps) {
            SubGraph subGraph = new SubGraph("Connector" + count, ep.getLabel());
            for (Operation operation : ep.getOperations()) {
                BasicNode component = new BasicNode(operation.getNodeId(),
                                                    simple ? operation.getSimpleFlowChartDisplayContent() :
                                                    operation.getFlowChartDisplayContent(), NodeKind.PROCESS);
                nodeMap.put(operation, component);
                subGraph.add(component);
            }
            flowChart.add(subGraph);
            count++;
        }
    }

    static void generateNodes(Sequence sequence, FlowChart flowChart, Map<Operation, BasicNode> nodeMap,
                              boolean simple) {
        if (sequence == null) {
            return;
        }
        for (Operation operation : sequence.getOperations()) {

            NodeKind process;
            switch (operation.getKind()) {
                case SWITCH_MERGE, HIDDEN -> process = NodeKind.CONNECTOR;
                case START -> process = NodeKind.START;
                case END -> process = NodeKind.TERMINAL;
                default -> process = NodeKind.PROCESS;
            }
            BasicNode basicNode = new BasicNode(operation.getNodeId(),
                                                simple ? operation.getSimpleFlowChartDisplayContent() :
                                                operation.getFlowChartDisplayContent(), process);
            nodeMap.put(operation, basicNode);
            flowChart.add(basicNode);
            if (operation instanceof AbstractCompositeOutOperation outOperation) {
                for (Sequence outSeq : outOperation.outgoingSequence()) {
                    generateNodes(outSeq, flowChart, nodeMap, simple);
                }
            } else if (operation instanceof AbstractCompositeInOperation inOperation) {
                for (Sequence inSeq : inOperation.incomingSequence()) {
                    generateNodes(inSeq, flowChart, nodeMap, simple);
                }
            }
        }
    }
}
