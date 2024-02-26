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
import io.github.hasithaa.diagram.model.Diagram;
import io.github.hasithaa.diagram.model.Model;
import io.github.hasithaa.diagram.model.Node;
import io.github.hasithaa.diagram.model.Subgraph;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public interface DiagramSerializer {

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
        sb.append("<div class=\"container\">\n");
        for (Diagram diagram : model.getDiagrams()) {
            sb.append("    <h2>").append("[").append(diagram.getDiagramType()).append("] ").append(diagram.getLabel())
              .append(
                      "</h2>\n");
            sb.append("    <div class=\"row row-cols-2\">\n");
            sb.append("         <div class=\"col\">\n");
            sb.append("             <h3>Overview</h3>\n");
            sb.append(getDiagramOverview(diagram));
            sb.append("         </div>\n");
            sb.append("         <div class=\"diagram col\">\n");
            sb.append("             <h3>Diagram</h3>\n");
            sb.append("             <div class=\"mermaid\">\n");
            sb.append(diagram.getMermaidString(1));
            sb.append("             </div>\n");
            sb.append("         </div>\n");
            sb.append("    </div>\n");
            sb.append("    <hr>\n");
        }
        sb.append("</div>\n");
        sb.append("    <script src=\"https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js\"></script>\n");
        sb.append("    <script src=\"https://cdn.jsdelivr.net/npm/bootstrap@5.0.2/dist/js/bootstrap.bundle.min.js\">")
          .append("</script>\n");
        sb.append("    <script>mermaid.initialize({startOnLoad:true});</script>\n");
        sb.append("</body>\n");
        sb.append("</html>");
        return sb.toString();
    }

    private static String getDiagramOverview(Diagram diagram) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h4>Main Flow</h4><br>\n");
        getOverview(diagram.getNodes(), sb, 1, "overview-" + diagram.getLabel(), true);
        if (!diagram.getSubgraphs().isEmpty()) {
            sb.append("<h4>Async Flows</h4><br>\n");
            for (Subgraph subgraph : diagram.getSubgraphs()) {
                sb.append("<strong>").append(subgraph.getLabel()).append("</strong>\n");
                getOverview(subgraph.getNodes(), sb, 2, "overview-" + subgraph.getLabel(), true);
            }
        }
        return sb.toString();
    }

    private static void getOverview(List<Node> nodeList, StringBuilder sb, int level, String id, boolean show) {
        final String ws = "    ".repeat(level);
        sb.append(ws).append("<ul ").append("id=\"").append(id).append("\" ");
        if (!show) {
            sb.append("class=\"collapse\"");
        }
        sb.append(">\n");
        for (Node node : nodeList) {
            if (node.getHeading() == null || node.getHeading().isEmpty()) {
                continue;
            }
            sb.append(ws).append("<li>\n");
            String icon = Arrays.stream(node.getIcon().split(" ")).map(s -> "<i class=\"fa " + s + "\"></i>")
                                .reduce("", String::concat);
            sb.append(ws).append(icon).append(" ").append(node.getHeading()).append("\n");
            if (node.hasChildren()) {
                for (Map.Entry<String, List<Node>> entry : node.getChildren().entrySet()) {
                    sb.append(ws).append("<div>\n");
                    sb.append(ws)
                      .append("<a href=\"")
                      .append("#overview-").append(entry.getKey())
                      .append("\" data-bs-toggle=\"collapse\" aria-expanded=\"false\" class=\"dropdown-toggle\">")
                      .append(icon).append(" ").append(entry.getKey()).append("</a>\n");
                    getOverview(entry.getValue(), sb, level + 1, "overview-" + entry.getKey(), false);
                    sb.append(ws).append("</div>\n");
                }
            }
            sb.append(ws).append("</li>\n");
        }
        sb.append(ws).append("</ul>\n");
    }

}
