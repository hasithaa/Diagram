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
package io.github.hasithaa.diagram.model;

import java.util.ArrayList;
import java.util.List;

public class Branch {

    private final BranchKind kind;
    private final String label;
    private final List<? extends INode<?>> children = new ArrayList<>();

    public Branch(BranchKind kind, String label) {
        this.kind = kind;
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public BranchKind getKind() {
        return kind;
    }

    public List<? extends INode<?>> getChildren() {
        return children;
    }

    public enum BranchKind {
        BLOCK,
    }
}
