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

import io.github.hasithaa.diagram.model.Expression;
import io.github.hasithaa.diagram.model.ExpressionTypeKind;
import io.github.hasithaa.diagram.model.ILineRange;
import io.github.hasithaa.diagram.model.INode;
import io.github.hasithaa.diagram.model.INodeKind;

public class Return extends INode<Return.Properties> {


    public static final String RESULT = "Result";
    public static final String RESULT_VALUE = "Result Value";
    public static final String RETURN = "Return";

    public Return() {
        this("id", ILineRange.DEFAULT, new Properties(), 0);
    }

    public Return(String id, ILineRange lineRange, Properties properties, int flags) {
        super(id, RETURN, INodeKind.RETURN, lineRange, properties, flags);
    }

    @Override
    public String toSourceCode() {
        StringBuilder sb = new StringBuilder();
        sb.append("return");
        if (this.getProperties().expression != null) {
            sb.append(" ").append(getProperties().expression.toSourceCode());
        }
        sb.append(";");
        return sb.toString();
    }

    public static class Properties extends INode.Properties {
        private final ReturnExpression expression;

        public Properties() {
            this.expression = new ReturnExpression();
            expression.setType("()");
            expression.setValue("()");
        }

        public Properties(ReturnExpression expr) {
            this.expression = expr;
        }

        public ReturnExpression getExpr() {
            return expression;
        }
    }

    public static class ReturnExpression extends Expression {
        public ReturnExpression() {
            super(RESULT, ExpressionTypeKind.BTYPE, RESULT_VALUE);
        }
    }
}
