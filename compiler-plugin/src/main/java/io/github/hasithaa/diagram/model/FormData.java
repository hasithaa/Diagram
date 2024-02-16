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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FormData implements JsonElement {

    private final String label;
    private final List<String[]> allowedTypes = new ArrayList<>();
    private final List<String> possibleValues = new ArrayList<>();
    private final List<String> flags = new ArrayList<>();
    private int index = -1;
    private FormDataTypeKind typeKind = FormDataTypeKind.STRING;
    private String value = "";
    private boolean editable = true;

    FormData(String label) {
        this.label = label;
    }

    public FormData setIndex(int index) {
        this.index = index;
        return this;
    }

    public FormData setTypeKind(FormDataTypeKind typeKind) {
        this.typeKind = typeKind;
        return this;
    }

    public FormData addAllowedTypes(String... types) {
        allowedTypes.add(types);
        return this;
    }

    public FormData setValue(String value) {
        this.value = value;
        return this;
    }

    public FormData addPossibleValues(String... values) {
        Collections.addAll(possibleValues, values);
        return this;
    }

    public FormData setEditable(boolean editable) {
        this.editable = editable;
        return this;
    }

    public FormData addFlags(String... flags) {
        Collections.addAll(this.flags, flags);
        return this;
    }

    @Override
    public String getJsonString(int wsCount) {
        String ws = getWs(wsCount);
        StringBuilder json = new StringBuilder();
        json.append(ws).append("{\n");
        json.append(ws).append("  \"index\": ").append(index).append(",\n");
        json.append(ws).append("  \"label\": \"").append(label).append("\",\n");
        json.append(ws).append("  \"type\": \"").append(typeKind).append("\",\n");
        json.append(ws).append("  \"allowedTypes\": [\n");
        for (String[] types : allowedTypes) {
            json.append(ws).append("    [\n");
            for (String type : types) {
                json.append(ws).append("      \"").append(type).append("\",\n");
            }
            json.deleteCharAt(json.length() - 2);
            json.append(ws).append("    ],\n");
        }
        if (!allowedTypes.isEmpty()) {
            json.deleteCharAt(json.length() - 2);
        }
        json.append(ws).append("  ],\n");
        json.append(ws).append("  \"value\": \"").append(encodedString(value)).append("\",\n");
        json.append(ws).append("  \"possibleValues\": [\n");
        for (String possibleValue : possibleValues) {
            json.append(ws).append("    \"").append(encodedString(possibleValue)).append("\",\n");
        }
        if (!possibleValues.isEmpty()) {
            json.deleteCharAt(json.length() - 2);
        }
        json.append(ws).append("  ],\n");
        json.append(ws).append("  \"editable\": ").append(editable).append(",\n");
        json.append(ws).append("  \"flags\": [\n");
        for (String flag : flags) {
            json.append(ws).append("    \"").append(flag).append("\",\n");
        }
        if (!flags.isEmpty()) {
            json.deleteCharAt(json.length() - 2);
        }
        json.append(ws).append("  ]\n");
        json.append(ws).append("}");
        return json.toString();
    }

    enum FormDataTypeKind {
        INT, FLOAT, DECIMAL, BOOLEAN, STRING, BYTE_ARRAY, NIL, XML, MAPPING, ARRAY, TABLE, IDENTIFIER, DEFAULT, ENUM
    }

    enum RepeatType {
        ANY, OPTIONAL, REQUIRED,
    }
}
