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

public class Flags {

    public static final int PUBLIC = 1;
    public static final int PRIVATE = 1 << 1;
    public static final int FINAL = 1 << 2;
    public static final int CONST = 1 << 3;
    public static final int READONLY = 1 << 4;

    public static final int CHECKED = 1 << 7;
    public static final int CHECKED_PANIC = 1 << 8;
    public static final int LAX = 1 << 10;

    public static final int RESOURCE = 1 << 10;
    public static final int REMOTE = 1 << 11;
    public static final int ISOLATED = 1 << 12;
    public static final int TRANSACTIONAL = 1 << 13;

    public static final int Client = 1 << 15;
    public static final int SERVICE = 1 << 16;
    public static final int FUNCTION = 1 << 17;
    public static final int METHOD = 1 << 18;

    public static final int NEW = 1 << 20;
    public static final int ASSIGNED = 1 << 21;
    public static final int IGNORE = 1 << 22;

    private static boolean isSetFlag(int flag, int value) {
        return (flag & value) == flag;
    }

    public static int setFlag(int flag, int value) {
        return value | flag;
    }

    public static int unsetFlag(int flag, int value) {
        return value & ~flag;
    }

}
