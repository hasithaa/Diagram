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

public enum TemplateKind {

    AGGREGATE("Aggregate"),
    CLONE("Clone"),
    CODE_BLOCK("CodeBlock"),
    CONVERSION("Data Conversion"),
    END("End"),
    EXPRESSION("Expression"),
    FOREACH("ForEach"),
    LIBRARY_CALL("Library Call"),
    NETWORK_CALL("Network Call"),
    NEW_PAYLOAD("New Payload"),
    SWITCH("Switch"),
    TRANSFORM("Transform"),
    START("Start"),
    NETWORK_EVENT("Network\nEvent"),
    SWITCH_MERGE("Switch Merge"),
    HIDDEN("Hidden"),
    ;

    final String longName;

    TemplateKind(String longName) {
        this.longName = longName;
    }
}
