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
package io.github.hasithaa.diagram.json;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FormData implements JsonElement {

    private final String label;
    private int index = -1;
    private FormDataType type = FormDataType.STRING;
    private final List<FormDataType[]> allowedTypes = new ArrayList<>();
    private String value = "";
    private final List<String> possibleValues = new ArrayList<>();
    private boolean editable = true;
    private final List<String> flags = new ArrayList<>();

    FormData(String label) {
        this.label = label;
    }

    public FormData setIndex(int index) {
        this.index = index;
        return this;
    }

    public FormData setType(FormDataType type) {
        this.type = type;
        return this;
    }

    public FormData addAllowedTypes(FormDataType... types) {
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
        String ws = " ".repeat(wsCount * 4);
        StringBuilder json = new StringBuilder();
        json.append(ws).append("{\n");
        json.append(ws).append("  \"index\": ").append(index).append(",\n");
        json.append(ws).append("  \"label\": \"").append(label).append("\",\n");
        json.append(ws).append("  \"type\": \"").append(type).append("\",\n");
        json.append(ws).append("  \"allowedTypes\": [\n");
        for (FormDataType[] types : allowedTypes) {
            json.append(ws).append("    [");
            for (FormDataType type : types) {
                json.append("\"").append(type).append("\", ");
            }
            json.deleteCharAt(json.length() - 2);
            json.append("],\n");
        }
        if (!allowedTypes.isEmpty()) {
            json.deleteCharAt(json.length() - 2);
        }
        json.append(ws).append("  ],\n");
        json.append(ws).append("  \"value\": \"").append(value).append("\",\n");
        json.append(ws).append("  \"possibleValues\": [\n");
        for (String possibleValue : possibleValues) {
            json.append(ws).append("    \"").append(possibleValue).append("\",\n");
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

    enum FormDataType {
        INT, FLOAT, DECIMAL, BOOLEAN, STRING, NIL, XML, JSON, MAPPING, ARRAY, TABLE, IDENTIFIER, DEFAULT, ENUM
    }

    enum RepeatType {
        ANY, OPTIONAL, REQUIRED,
    }
}
