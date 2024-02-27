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
package io.github.hasithaa.diagram.model.library;

import io.github.hasithaa.diagram.model.Expression;
import io.github.hasithaa.diagram.model.ExpressionTypeKind;
import io.github.hasithaa.diagram.model.ILineRange;
import io.github.hasithaa.diagram.model.INode;
import io.github.hasithaa.diagram.model.INodeKind;
import io.github.hasithaa.diagram.model.core.VariableExpr;

public class HttpGet extends INode<HttpGet.Properties> {

    public static final String HTTP_GET_CALL = "HTTP GET Call";
    public static final String CLIENT = "Client";
    public static final String HTTP_CLIENT_CONNECTION = "Http Client Connection";
    public static final String HTTP_CLIENT = "http:Client";
    public static final String PATH = "Path";
    public static final String PATH_OF_THE_RESOURCE = "Path of the resource";
    public static final String STRING = "string";
    public static final String HEADERS = "Headers";
    public static final String THE_ENTITY_HEADERS = "The entity headers";
    public static final String MAPPING = "map<string|string[]>";
    public static final String TARGET_TYPE = "http:Response|anydata";
    public static final String RESULT_TYPE = "Result Type";
    public static final String TARGET_TYPE_LABEL = "Target Type";
    public static final String JSON = "json";

    public HttpGet() {
        this("id", ILineRange.DEFAULT, new Properties());
    }

    public HttpGet(String id, ILineRange lineRange, Properties properties) {
        super(id, HTTP_GET_CALL, INodeKind.LIBRARY_CALL_HTTP_GET, lineRange, properties, 0);
    }

    @Override
    public String toSourceCode() {
        return null;
    }

    public static class Properties extends INode.Properties {

        private final ClientExpr client;
        private final PathExpr path;
        private final HeadersExpr headers;
        private final TargetTypeExpr targetType;
        private final VariableExpr variable;

        public Properties() {
            this.client = new ClientExpr();
            this.path = new PathExpr();
            this.headers = null;
            this.targetType = new TargetTypeExpr();
            this.variable = new VariableExpr();
        }

        public Properties(ClientExpr client, PathExpr path, HeadersExpr headers, TargetTypeExpr targetType,
                          VariableExpr variable) {
            this.client = client;
            this.path = path;
            this.headers = headers;
            this.targetType = targetType;
            this.variable = variable;
        }
    }

    public static class ClientExpr extends Expression {
        public ClientExpr() {
            super(CLIENT, ExpressionTypeKind.IDENTIFIER, HTTP_CLIENT_CONNECTION, HTTP_CLIENT);
            this.setValue("cl");
        }

        @Override
        public String toSourceCode() {
            throw new UnsupportedOperationException();
        }
    }

    public static class PathExpr extends Expression {

        public PathExpr() {
            super(PATH, ExpressionTypeKind.BTYPE, PATH_OF_THE_RESOURCE, STRING);
        }
    }

    public static class HeadersExpr extends Expression {

        public HeadersExpr() {
            super(HEADERS, ExpressionTypeKind.BTYPE, THE_ENTITY_HEADERS, MAPPING, true, true, null);
        }
    }

    public static class TargetTypeExpr extends Expression {

        public TargetTypeExpr() {
            super(TARGET_TYPE_LABEL, ExpressionTypeKind.BTYPE, RESULT_TYPE, TARGET_TYPE, true, false, JSON);
        }
    }

}
