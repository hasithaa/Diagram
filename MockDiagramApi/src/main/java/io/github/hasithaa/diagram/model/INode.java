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


public abstract class INode<T extends INode.Properties> {

    private final String id;
    private final String label;
    private final INodeKind kind;
    private final ILineRange lineRange;
    private final boolean returning;
    private final boolean fixed;
    private final T nodeProperties;
    private final Branch[] branches;
    private final int flags;

    public INode(String id, String label, INodeKind kind, ILineRange lineRange, T properties, int flags) {
        this.id = id;
        this.label = label;
        this.kind = kind;
        this.lineRange = lineRange;
        this.returning = false;
        this.fixed = false;
        this.nodeProperties = properties;
        this.branches = null;
        this.flags = flags;
    }

    public INode(String id, String label, INodeKind kind, ILineRange lineRange, T properties, int flags,
                 Branch[] branches) {
        this.id = id;
        this.label = label;
        this.kind = kind;
        this.lineRange = lineRange;
        this.returning = false;
        this.fixed = false;
        this.nodeProperties = properties;
        this.branches = branches;
        this.flags = flags;
    }

    public INode(String id, String label, INodeKind kind, ILineRange lineRange, T properties, int flags,
                 Branch[] branches, boolean returning, boolean fixed) {
        this.id = id;
        this.label = label;
        this.kind = kind;
        this.lineRange = lineRange;
        this.returning = returning;
        this.fixed = fixed;
        this.nodeProperties = properties;
        this.branches = branches;
        this.flags = flags;
    }

    public String id() {
        return id;
    }

    public String label() {
        return label;
    }

    public ILineRange lineRange() {
        return lineRange;
    }

    public Boolean fixed() {
        return fixed;
    }

    public Boolean returning() {
        return returning;
    }

    public T getProperties() {
        return nodeProperties;
    }

    public Branch[] getBranches() {
        return branches;
    }

    public INodeKind getKind() {
        return kind;
    }

    public int getFlags() {
        return flags;
    }

    public abstract String toSourceCode();

    public abstract static class Properties {}
}
