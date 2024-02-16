package io.github.hasithaa.diagram;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.CompilationAnalysisContext;
import io.github.hasithaa.diagram.flowchart.FlowChart;
import io.github.hasithaa.diagram.model.CodeVisitor;
import io.github.hasithaa.diagram.model.Model;

import java.util.List;

public class CodeAnalyzer<T> implements AnalysisTask<CompilationAnalysisContext> {

    @Override
    public void perform(CompilationAnalysisContext ctx) {

        if (ctx.compilation().diagnosticResult().hasErrors()) {
            return;
        }
        try {
            ModuleId currentModuleId = ctx.currentPackage().getDefaultModule().moduleId();
            SemanticModel semanticModel = ctx.compilation().getSemanticModel(currentModuleId);
            CodeVisitor codeVisitor = new CodeVisitor(semanticModel, currentModuleId.moduleName());
            ctx.currentPackage().getDefaultModule().documentIds().forEach(documentId -> {
                ctx.currentPackage().getDefaultModule().document(documentId).syntaxTree().rootNode().accept(
                        codeVisitor);
            });

//            try {
////                List<Sequence> epSequences = codeVisitor.getEp();
////                List<DiagramFile> diagramFiles = new ArrayList<>();
////                for (Diagram diagram : codeVisitor.getDiagrams()) {
////
////                    DiagramFile diagramFile = new DiagramFile(diagram.getName(), List.of(
////                            FlowChartGenerator.generateFlowChart(diagram, epSequences, true),
////                            FlowChartGenerator.generateFlowChart(diagram, epSequences, false)));
////                    diagramFiles.add(diagramFile);
////                }
////                DiagramSerializer.serialize(diagramFiles, ctx.currentPackage().project());
//            } catch (IOException e) {
//                throw new RuntimeException("Error occurred while generating the diagram for the function:", e);
//            }

            Model model = codeVisitor.getModel();
            DiagramSerializer.serialize(model, ctx.currentPackage().project());
            DiagramSerializer.serializeHTML(model, ctx.currentPackage().project());
        } catch (Exception e) {
            throw new RuntimeException("Error occurred while generating the diagram for the function:", e);
        }
    }

    record DiagramFile(String name, List<FlowChart> list) {}
}
