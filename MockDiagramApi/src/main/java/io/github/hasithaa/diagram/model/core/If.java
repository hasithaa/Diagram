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
package io.github.hasithaa.diagram.model.core;

import io.github.hasithaa.diagram.model.Branch;
import io.github.hasithaa.diagram.model.Expression;
import io.github.hasithaa.diagram.model.ExpressionTypeKind;
import io.github.hasithaa.diagram.model.ILineRange;
import io.github.hasithaa.diagram.model.INode;
import io.github.hasithaa.diagram.model.INodeKind;

public class If extends INode<If.Properties> {

    public static final String IF = "If";
    public static final String THEN = "Then";
    public static final String ELSE = "Else";
    public static final String CONDITION = "Condition";
    public static final String BOOLEAN = "boolean";
    public static final String BOOLEAN_CONDITION = "Boolean Condition";


    public If() {
        this("id", ILineRange.DEFAULT, new Properties(), 0, new Branch[]{
                new Branch(Branch.BranchKind.BLOCK, THEN), new Branch(Branch.BranchKind.BLOCK, ELSE)});
    }

    public If(String id, ILineRange lineRange, Properties properties, int flags, Branch[] branches) {
        super(id, IF, INodeKind.IF, lineRange, properties, flags, branches);
        if (branches.length == 0 || branches.length > 2) {
            throw new IllegalArgumentException("If node should have 1 or 2 branches");
        }
        if (!THEN.equals(branches[0].getLabel())) {
            throw new IllegalArgumentException("If first branch should be Called 'Then'");
        }
        if (branches.length == 2 && ELSE.equals(branches[1].getLabel())) {
            throw new IllegalArgumentException("If second branch should be called 'Else'");
        }
    }

    public String toSourceCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("if ").append(this.getProperties().condition.toSourceCode()).append(" {\n");
        if (!this.getBranches()[0].getChildren().isEmpty()) {
            for (INode<?> child : this.getBranches()[0].getChildren()) {
                sb.append("    ").append(child.toSourceCode()).append("\n");
            }
        }
        if (this.getBranches().length == 2 && !this.getBranches()[1].getChildren().isEmpty()) {
            Branch elseBranch = this.getBranches()[1];
            if (elseBranch.getChildren().get(0).getKind() == INodeKind.IF) {
                sb.append("} else ").append(elseBranch.getChildren().get(0).toSourceCode());
            } else {
                sb.append("} else {\n");
                for (INode<?> child : elseBranch.getChildren()) {
                    sb.append("    ").append(child.toSourceCode()).append("\n");
                }
            }
        }
        sb.append("}");
        return sb.toString();
    }

    public static class Properties extends INode.Properties {
        private final ConditionExpression condition;

        public Properties() {
            this.condition = new ConditionExpression();
            this.condition.setValue("true");
        }

        public Properties(ConditionExpression condition) {
            this.condition = condition;
        }

        public ConditionExpression getCondition() {
            return condition;
        }
    }

    public static class ConditionExpression extends Expression {
        public ConditionExpression() {
            super(CONDITION, ExpressionTypeKind.BTYPE, BOOLEAN_CONDITION, BOOLEAN);
        }
    }
}
