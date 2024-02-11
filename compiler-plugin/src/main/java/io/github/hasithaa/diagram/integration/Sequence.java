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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sequence implements Scope {

    private final List<Operation> operations = new ArrayList<>();
    private final Operation source, target;
    boolean generated = false;
    private String label = null;
    private boolean special = false;

    public Sequence(Operation source, Operation target) {
        this.source = source;
        this.target = target;
    }

    public void addOperation(Operation operation) {
        if (operations.isEmpty()) {
            operation.setPreviousOperation(source);
            this.operations.add(operation);
        } else {
            Operation lastOperation = operations.get(operations.size() - 1);
            if (lastOperation == operation.getParent()) {
                lastOperation.setNextOperation(null);
                operation.setPreviousOperation(null);
            } else {
                lastOperation.setNextOperation(operation);
                operation.setPreviousOperation(lastOperation);
            }
            operation.setNextOperation(target);
            this.operations.add(operation);
        }
    }

    public boolean isSpecial() {
        return special;
    }

    public void setSpecial(boolean special) {
        this.special = special;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Operation getTarget() {
        return target;
    }

    public List<Operation> getOperations() {
        return Collections.unmodifiableList(operations);
    }
}
