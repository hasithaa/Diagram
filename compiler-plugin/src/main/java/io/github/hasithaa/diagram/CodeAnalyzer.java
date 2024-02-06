package io.github.hasithaa.diagram;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.plugins.AnalysisTask;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.github.hasithaa.diagram.integration.CodeVisitor;

import java.io.IOException;

public class CodeAnalyzer<T> implements AnalysisTask<SyntaxNodeAnalysisContext> {

    @Override
    public void perform(SyntaxNodeAnalysisContext ctx) {

        if (ctx.syntaxTree().hasDiagnostics()) {
            return;
        }
        if (ctx.node().kind() == SyntaxKind.FUNCTION_DEFINITION) {
            ModuleId currentModuleId = ctx.moduleId();
            SemanticModel semanticModel = ctx.compilation().getSemanticModel(currentModuleId);
            CodeVisitor codeVisitor = new CodeVisitor(ctx, currentModuleId, semanticModel);
            ctx.node().accept(codeVisitor);
            String fileName = ((FunctionDefinitionNode) ctx.node()).functionName().toString() + ".md";
            try {
                DiagramSerializer.serialize(fileName, codeVisitor.getDiagram(), ctx.currentPackage().project());
            } catch (IOException e) {
                throw new RuntimeException("Error occurred while generating the diagram for the function: " +
                                                   ((FunctionDefinitionNode) ctx.node()).functionName().toString());
            }
        }

    }

}
