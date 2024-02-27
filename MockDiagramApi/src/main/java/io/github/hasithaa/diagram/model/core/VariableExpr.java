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
import io.github.hasithaa.diagram.model.Flags;

public class VariableExpr extends Expression {

    public static final String VARIABLE = "Variable";
    public static final String RESULT_VARIABLE = "Result Variable";

    public VariableExpr() {
        this("json", "var1", true, false);
    }

    public VariableExpr(String type, String value) {
        this(type, value, true, false);
    }

    public VariableExpr(String type, String value, boolean newVar, boolean ignore) {
        super(VARIABLE, ExpressionTypeKind.BTYPE, RESULT_VARIABLE);
        this.setType(type);
        this.setValue(value);
        int flag = getFlags();
        flag = newVar ? Flags.setFlag(Flags.NEW, flag) : Flags.unsetFlag(Flags.NEW, flag);
        flag = newVar ? Flags.unsetFlag(Flags.ASSIGNED, flag) : Flags.setFlag(Flags.ASSIGNED, flag);
        flag = ignore ? Flags.setFlag(Flags.IGNORE, flag) : Flags.unsetFlag(Flags.IGNORE, flag);
        setFlags(flag);
    }

    @Override
    public String toSourceCode() {
        throw new UnsupportedOperationException();
    }
}
