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

import io.github.hasithaa.diagram.flowchart.BasicNode;
import io.github.hasithaa.diagram.flowchart.Edge;
import io.github.hasithaa.diagram.flowchart.FlowchartComponent;

import java.util.List;

public abstract class AbstractOperation implements Operation {

    final private int id;
    final private String description;
    private AbstractOperation next, previous;
    private FlowchartComponent flowchartComponent = null;
    private List<Edge> flowchartEdges = null;
    private boolean failOnError = false;

    public AbstractOperation(int id, String description) {
        this.id = id;
        this.description = description;
    }

    @Override
    public Operation nextOperation() {
        return next;
    }

    @Override
    public void setNextOperation(Operation nextOperation) {
        this.next = (AbstractOperation) nextOperation;
    }

    @Override
    public String getComment() {
        return null;
    }

    @Override
    public Operation previousOperation() {
        return previous;
    }

    @Override
    public void setPreviousOperation(Operation previousOperation) {
        this.previous = (AbstractOperation) previousOperation;
    }

    @Override
    public FlowchartComponent getFlowchartNode() {
        if (flowchartComponent == null) {
            flowchartComponent = new BasicNode(getNodeId(id), getDisplayDescription(description),
                                               getFlowchartNodeKind());
        }
        return flowchartComponent;
    }

    @Override
    public List<Edge> getFlowchartEdges() {
        // TODO : Fix this, this lead to duplicate edges
        if (flowchartEdges != null) {
            return flowchartEdges;
        }
        if (previous == null && next == null) {
            flowchartEdges = List.of();
        } else if (previous == null) {
            flowchartEdges = List.of(new Edge(getFlowchartNode(), next.getFlowchartNode()));
        } else if (next == null) {
            flowchartEdges = List.of(new Edge(previous.getFlowchartNode(), getFlowchartNode()));
        } else {
            flowchartEdges = List.of(new Edge(previous.getFlowchartNode(), getFlowchartNode()),
                                     new Edge(getFlowchartNode(), next.getFlowchartNode()));
        }
        return flowchartEdges;
    }

    @Override
    public String icon() {
        return "⚙️";
    }

    @Override
    public String getNodeId(int sequence) {
        return "Node" + sequence;
    }

    @Override
    public String getDisplayDescription(String description) {
        StringBuilder displayDescription = new StringBuilder();
        if (description == null) {
            displayDescription.append(icon()).append(" ").append(getKind().name);
        } else {
            displayDescription.append("[").append(icon()).append(" ").append(getKind().name).append("] ").append(
                    description);
        }
        if (failOnError) {
            displayDescription.append(" ⚠");
        }
        return displayDescription.toString();
    }

    @Override
    public void setFailOnError() {
        this.failOnError = true;
    }


}
