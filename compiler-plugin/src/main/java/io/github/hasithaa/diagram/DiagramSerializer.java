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
import io.github.hasithaa.diagram.CodeAnalyzer.DiagramFile;
import io.github.hasithaa.diagram.flowchart.FlowChart;
import io.github.hasithaa.diagram.model.Diagram;
import io.github.hasithaa.diagram.model.Model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

public interface DiagramSerializer {

    static void serialize(List<DiagramFile> diagrams, Project project) throws IOException {
        Path packageRootPath = ProjectPaths.packageRoot(project.sourceRoot());
        Path diagramDirectory = packageRootPath.resolve("diagrams");
        if (!Files.exists(diagramDirectory)) {
            Files.createDirectory(diagramDirectory);
        }
        for (DiagramFile diagram : diagrams) {
            Path diagramPath = diagramDirectory.resolve(diagram.name() + ".md");
            if (!Files.exists(diagramPath)) {
                Files.createFile(diagramPath);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("# ").append(diagram.name()).append("\n\n");
            diagram.list().forEach(fc -> sb.append(getFlowChartDoc(fc)));
            Files.writeString(diagramPath, sb.toString(), StandardOpenOption.TRUNCATE_EXISTING);
        }

    }

    static String getFlowChartDoc(FlowChart flowChart) {
        String sb = "\n## Diagram\n\n```mermaid\n" +
                flowChart.generateMermaidSyntax() +
                "```\n---\n";
        return sb;
    }

    static void serialize(Model model, Project project) throws IOException {
        Path packageRootPath = ProjectPaths.packageRoot(project.sourceRoot());
        Path diagramDirectory = packageRootPath.resolve("diagrams");
        if (!Files.exists(diagramDirectory)) {
            Files.createDirectory(diagramDirectory);
        }
        Path diagramPath = diagramDirectory.resolve("diagram.json");
        if (!Files.exists(diagramPath)) {
            Files.createFile(diagramPath);
        }
        Files.writeString(diagramPath, model.getJsonString(0), StandardOpenOption.TRUNCATE_EXISTING);
    }

    static void serializeHTML(Model model, Project project) throws IOException {
        Path packageRootPath = ProjectPaths.packageRoot(project.sourceRoot());
        Path diagramDirectory = packageRootPath.resolve("diagrams");
        if (!Files.exists(diagramDirectory)) {
            Files.createDirectory(diagramDirectory);
        }
        Path diagramPath = diagramDirectory.resolve("diagram.html");
        if (!Files.exists(diagramPath)) {
            Files.createFile(diagramPath);
        }
        Files.writeString(diagramPath, getHtmlString(model), StandardOpenOption.TRUNCATE_EXISTING);
    }

    static String getHtmlString(Model model) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n").append("<html>\n");
        sb.append("<head>\n");
        sb.append("    <meta charset=\"utf-8\">\n");
        sb.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n");
        sb.append(
                "    <link href=\"https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/css/bootstrap.min.css\" " +
                        "rel=\"stylesheet\" />");
        sb.append(
                "    <link href=\"https://cdnjs.cloudflare.com/ajax/libs/font-awesome/6.5.1/css/all.min.css\" " +
                        "rel=\"stylesheet\" />");
        sb.append("</head>\n");
        sb.append("<body>\n");
        sb.append("    <h1>").append(model.getLabel()).append("</h1>\n");
        sb.append("    <p><button class=\"btn btn-primary\" type=\"button\" data-bs-toggle=\"collapse\"")
          .append(" data-bs-target=\".forms\" aria-expanded=\"false\">Toggle Data</button></p>\n");
        for (Diagram diagram : model.getDiagrams()) {
            sb.append("    <div class=\"diagram forms show\" \">\n");
            sb.append("    <div class=\"card card-body\">\n");
            sb.append("    <h2>").append(diagram.getLabel()).append("</h2>\n");
            sb.append("    <div class=\"mermaid\">\n");
            sb.append(diagram.getMermaidString(1));
            sb.append("    </div>\n");
            sb.append("    </div>\n");
            sb.append("    </div>\n");
        }
        sb.append("    <script src=\"https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js\"></script>\n");
        sb.append("    <script src=\"https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js\">")
          .append("</script>\n");
        sb.append("    <script>mermaid.initialize({startOnLoad:true});</script>\n");
        sb.append("</body>\n");
        sb.append("</html>");
        return sb.toString();
    }

}
