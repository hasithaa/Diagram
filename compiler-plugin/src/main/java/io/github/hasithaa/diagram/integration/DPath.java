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

public class DPath {

    public Operation source, target;
    public String label = null;
    public PathType pathType = PathType.SOLID;
    public boolean arrowHead = true;

    public DPath(Operation source, Operation target) {
        this.source = source;
        this.target = target;
    }

    public DPath(Operation source, Operation target, String label) {
        this.source = source;
        this.target = target;
        this.label = label;
    }

    public DPath(Operation source, Operation target, String label, PathType pathType) {
        this.source = source;
        this.target = target;
        this.label = label;
        this.pathType = pathType;
    }

    public DPath(Operation source, Operation target, String label, PathType pathType, boolean arrowHead) {
        this.source = source;
        this.target = target;
        this.label = label;
        this.pathType = pathType;
        this.arrowHead = arrowHead;
    }

    public enum PathType {
        SOLID, DOTTED, STRONG, HIDDEN
    }

}
