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
package io.github.hasithaa.diagram.model.events;

import io.github.hasithaa.diagram.model.Expression;
import io.github.hasithaa.diagram.model.ExpressionTypeKind;
import io.github.hasithaa.diagram.model.ILineRange;
import io.github.hasithaa.diagram.model.INode;
import io.github.hasithaa.diagram.model.INodeKind;

public class HttpApi extends INode<HttpApi.Properties> {

    public static final String HTTP_METHOD = "Http Method";
    public static final String HTTP_METHOD_DOC = "Http Method";
    public static final String PATH = "Path";
    public static final String RESOURCE_PATH = "Resource Path";
    public static final String HTTP_API = "HTTP API";

    public HttpApi() {
        this("id", ILineRange.DEFAULT, new Properties(), 0);
    }

    public HttpApi(String id, ILineRange lineRange, Properties properties, int flags) {
        super(id, HTTP_API, INodeKind.EVENT_HTTP_API, lineRange, properties, flags, null, false, true);
    }

    @Override
    public String toSourceCode() {
        return null;
    }

    public static class Properties extends INode.Properties {
        private final MethodExpression method;
        private final PathExpression path;

        public Properties() {
            this.method = new MethodExpression();
            this.path = new PathExpression();
        }

        public Properties(MethodExpression method, PathExpression path) {
            this.method = method;
            this.path = path;
        }

        public MethodExpression getMethod() {
            return method;
        }

        public PathExpression getPath() {
            return path;
        }
    }

    public static class MethodExpression extends Expression {
        public MethodExpression() {
            super(HTTP_METHOD, ExpressionTypeKind.IDENTIFIER, HTTP_METHOD_DOC);
        }
    }

    public static class PathExpression extends Expression {
        public PathExpression() {
            super(PATH, ExpressionTypeKind.URI_PATH, RESOURCE_PATH);
        }
    }

}
