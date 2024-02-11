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

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public abstract class AbstractOperation implements Operation {

    private final int id;
    private final List<Map.Entry<String, String>> formData;
    private final AbstractOperation parent;
    private String heading;
    private String comment;
    private AbstractOperation next, previous;
    private boolean failOnError = false;
    private List<Variable> variables = null;

    public AbstractOperation(int id) {
        this.id = id;
        formData = new ArrayList<>();
        this.parent = null;
    }

    public AbstractOperation(int id, AbstractOperation parent) {
        this.id = id;
        formData = new ArrayList<>();
        this.parent = parent;
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
        return comment;
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
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
    public String icon() {
        return "⚙️";
    }

    @Override
    public String getNodeId() {
        return getKind().name() + id;
    }

    @Override
    public String getFlowChartDisplayContent() {
        StringBuilder displayDescription = new StringBuilder();
        if (heading == null) {
            displayDescription.append(icon());
        } else {
            displayDescription.append(icon()).append(" ").append(heading.replace("\"", "&quot;"));
        }
        if (!formData.isEmpty()) {
            displayDescription.append("\n");
            displayDescription.append("<table>");
            for (Map.Entry<String, String> entry : formData) {
                displayDescription.append("<tr><td>").append(entry.getKey()).append("</td><td>").append(
                        entry.getValue()).append("</td></tr>");
            }
            displayDescription.append("</table>");
        }
        if (variables != null) {
            displayDescription.append("<strong>Variables:</strong>");
            displayDescription.append("<table>");
            for (Variable variable : variables) {
                displayDescription.append("<tr><td>");
                displayDescription.append(variable.type()).append("</td><td>");
                displayDescription.append(variable.name()).append("</td><td>");
                displayDescription.append(variable.newVar() ? "🆕" : "🔄").append("</td></tr>");
            }
            displayDescription.append("</table>");
        }
        if (failOnError) {
            displayDescription.append("<p>⚠\uFE0F Fail on Error</p>");
        }
        return displayDescription.toString();
    }

    @Override
    public void setFailOnError() {
        this.failOnError = true;
    }

    @Override
    public String getHeading() {
        return heading;
    }

    @Override
    public void setHeading(String heading) {
        this.heading = heading;
    }

    @Override
    public void addFormData(String data, String value) {
        formData.add(new AbstractMap.SimpleEntry<>(data, value));
    }

    @Override
    public List<Map.Entry<String, String>> getFormData() {
        return Collections.unmodifiableList(formData);
    }

    @Override
    public AbstractOperation getParent() {
        return parent;
    }

    @Override
    public void addVariable(Variable variable) {
        if (variables == null) {
            variables = new ArrayList<>();
        }
        variables.add(variable);
    }
}
