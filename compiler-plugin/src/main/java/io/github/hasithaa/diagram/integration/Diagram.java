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

public class Diagram {

    private final List<DPath> paths = new ArrayList<>();
    private Sequence base = null;
    private final String name;

    Diagram(String name, Sequence base) {
        this.name = name;
        this.base = base;
    }

    public void generatePaths() {
        generatePaths(base, null);
    }

    private void generatePaths(Sequence sequence, String startingLabel) {
        if (sequence == null || sequence.generated) {
            return;
        }
        int index = 0;
        for (Operation operation : sequence.getOperations()) {
            if (operation.previousOperation() != null) {
                if (startingLabel != null && index == 0) {
                    paths.add(new DPath(operation.previousOperation(), operation, startingLabel));
                } else {
                    paths.add(new DPath(operation.previousOperation(), operation));
                }
            }
            if (operation instanceof AbstractCompositeOutOperation outOperation) {
                for (Sequence childSeq : outOperation.outgoingSequence()) {
                    if (childSeq.getOperations().isEmpty()) {
                        paths.add(new DPath(operation, childSeq.getTarget(), childSeq.getLabel()));
                    }
                    generatePaths(childSeq, childSeq.getLabel());
                    childSeq.generated = true;
                }
            } else if (operation instanceof AbstractCompositeInOperation inOperation) {
                for (Sequence childSeq : inOperation.incomingSequence()) {
                    if (!childSeq.getOperations().isEmpty()) {
                        generatePaths(childSeq, childSeq.getLabel());
                        int lastIndex = childSeq.getOperations().size() - 1;
                        paths.add(new DPath(childSeq.getOperations().get(lastIndex), operation));
                    }
                    childSeq.generated = true;
                }
            }
            index++;
        }
    }

    public void addPath(DPath path) {
        paths.add(path);
    }

    public List<DPath> getPaths() {
        return Collections.unmodifiableList(paths);
    }

    public String getName() {
        return name;
    }

    public Sequence getBaseSequence() {
        return base;
    }
}
