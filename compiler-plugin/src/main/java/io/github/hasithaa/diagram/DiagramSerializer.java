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
package io.github.hasithaa.diagram;

import io.ballerina.projects.Project;
import io.ballerina.projects.util.ProjectPaths;
import io.github.hasithaa.diagram.flowchart.FlowChart;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public interface DiagramSerializer {

    static void serialize(List<FlowChart> flowCharts, Project project) throws IOException {
        Path packageRootPath = ProjectPaths.packageRoot(project.sourceRoot());
        Path diagramDirectory = packageRootPath.resolve("diagrams");
        if (!Files.exists(diagramDirectory)) {
            Files.createDirectory(diagramDirectory);
        }
        for (FlowChart flowChart : flowCharts) {
            Path diagramPath = diagramDirectory.resolve(flowChart.getName() + ".md");
            if (!Files.exists(diagramPath)) {
                Files.createFile(diagramPath);
            }
            Files.writeString(diagramPath, getFlowChartDoc(flowChart), StandardOpenOption.TRUNCATE_EXISTING);
        }
    }

    static String getFlowChartDoc(FlowChart flowChart) {
        StringBuilder sb = new StringBuilder();
        sb.append("# Flowchart\n\n");
        sb.append("```mermaid\n");
        sb.append(flowChart.generateMermaidSyntax());
        sb.append("```\n");
        return sb.toString();
    }
}
