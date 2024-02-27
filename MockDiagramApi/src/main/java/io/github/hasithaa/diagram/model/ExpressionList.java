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

import java.util.List;

public class ExpressionList {
    private final transient String key;
    private final String type;
    private final List<Expression> expressions;
    private final boolean optional;

    public ExpressionList(String key, String type, List<Expression> expressions, boolean optional) {
        this.key = key;
        this.type = type;
        this.expressions = expressions;
        this.optional = optional;
    }

    public String getKey() {
        return key;
    }

    public String getType() {
        return type;
    }

    public List<Expression> getExpressions() {
        return expressions;
    }

    public boolean isOptional() {
        return optional;
    }

}
