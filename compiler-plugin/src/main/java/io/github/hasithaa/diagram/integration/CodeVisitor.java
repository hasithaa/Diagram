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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.github.hasithaa.diagram.flowchart.FlowChart;
import io.github.hasithaa.diagram.integration.templates.Clone;
import io.github.hasithaa.diagram.integration.templates.End;
import io.github.hasithaa.diagram.integration.templates.Expression;
import io.github.hasithaa.diagram.integration.templates.NetworkCall;
import io.github.hasithaa.diagram.integration.templates.Sequence;
import io.github.hasithaa.diagram.integration.templates.Start;

import java.util.Stack;

public class CodeVisitor extends NodeVisitor {

    FlowChart flowChart = new FlowChart();
    final SyntaxNodeAnalysisContext ctx;
    final ModuleId moduleId;
    final SemanticModel semanticModel;
    final Sequence base;
    Stack<Sequence> sequences = new Stack<>();
    Stack<Node> statement = new Stack<>();
    Stack<Clone> parallel = new Stack<>();
    int count = 0;

    public CodeVisitor(SyntaxNodeAnalysisContext ctx, ModuleId moduleId, SemanticModel semanticModel) {
        this.ctx = ctx;
        this.moduleId = moduleId;
        this.semanticModel = semanticModel;
        base = new Sequence();
    }

    @Override
    public void visit(FunctionDefinitionNode node) {
        super.visit(node);
        // TODO: Generate the diagram

    }

    public String getDiagram() {
        StringBuilder sb = new StringBuilder();
        sb.append("# Flowchart\n\n");
        sb.append("```mermaid\n");
        sb.append(flowChart.generateMermaidSyntax());
        sb.append("```\n");
        return sb.toString();
    }

    @Override
    public void visit(FunctionBodyBlockNode node) {
        sequences.push(base);
        base.addOperation(new Start(count++, "Start"));
//        super.visit(node);
        base.addOperation(new End(count++, "End"));
    }

    @Override
    public void visit(NamedWorkerDeclarationNode node) {
        Sequence sequence = new Sequence();
        sequences.push(sequence);
        super.visit(node);
    }

    @Override
    public void visit(AssignmentStatementNode node) {
        statement.add(node);
        super.visit(node);
        // TODO: Capture Variable
        if (!statement.isEmpty()) {
            Expression expression = new Expression(count++, "Expression");
            sequences.peek().addOperation(expression);
        }
        statement.empty();
    }

    @Override
    public void visit(VariableDeclarationNode node) {
        statement.add(node);
        super.visit(node);
        // TODO: Capture Variable
        if (!statement.isEmpty()) {
            Expression expression = new Expression(count++, "Expression");
            sequences.peek().addOperation(expression);
        }
        statement.empty();
    }

    @Override
    public void visit(RemoteMethodCallActionNode node) {
        // Make a NetworkCall
        NetworkCall networkCall = new NetworkCall(count++, node.methodName().toString());
        sequences.peek().addOperation(networkCall);
        statement.empty();
    }

    @Override
    public void visit(ClientResourceAccessActionNode node) {
        NetworkCall networkCall = new NetworkCall(count++, node.methodName().toString());
        sequences.peek().addOperation(networkCall);
        statement.empty();
    }
}
