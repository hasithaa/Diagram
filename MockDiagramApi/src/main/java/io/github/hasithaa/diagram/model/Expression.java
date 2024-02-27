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

public abstract class Expression {

    private final String label;
    private final ExpressionTypeKind typeKind;
    private final boolean editable;
    private final boolean optional;
    private final String documentation;
    private String type;
    private int flags;
    private String value;

    public Expression(String label, ExpressionTypeKind typeKind, String documentation) {
        this.label = label;
        this.type = null;
        this.typeKind = typeKind;
        this.editable = true;
        this.optional = false;
        this.value = null;
        this.documentation = documentation;
    }

    public Expression(String label, ExpressionTypeKind typeKind, String documentation, String type) {
        this.label = label;
        this.type = type;
        this.typeKind = typeKind;
        this.editable = true;
        this.optional = false;
        this.value = null;
        this.documentation = documentation;
    }

    public Expression(String label, ExpressionTypeKind typeKind, String documentation, String type, boolean editable,
                      boolean optional, String value) {
        this.label = label;
        this.type = type;
        this.typeKind = typeKind;
        this.editable = editable;
        this.optional = optional;
        this.value = value;
        this.documentation = documentation;
    }

    public String getLabel() {
        return label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ExpressionTypeKind getTypeKind() {
        return typeKind;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isOptional() {
        return optional;
    }

    public String getDocumentation() {
        return documentation;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String toSourceCode() {
        return value;
    }

    public int getFlags() {
        return flags;
    }

    public void setFlags(int flags) {
        this.flags = flags;
    }
}
