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
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.projects.ModuleId;
import io.ballerina.projects.plugins.SyntaxNodeAnalysisContext;
import io.github.hasithaa.diagram.flowchart.FlowChart;
import io.github.hasithaa.diagram.integration.templates.End;
import io.github.hasithaa.diagram.integration.templates.Expression;
import io.github.hasithaa.diagram.integration.templates.NetworkCall;
import io.github.hasithaa.diagram.integration.templates.Sequence;
import io.github.hasithaa.diagram.integration.templates.Start;
import io.github.hasithaa.diagram.integration.templates.Switch;
import io.github.hasithaa.diagram.integration.templates.SwitchMerge;

import java.util.Stack;

public class CodeVisitor extends NodeVisitor {

    final SyntaxNodeAnalysisContext ctx;
    final ModuleId moduleId;
    final SemanticModel semanticModel;
    final Sequence base;

    // Data
    FlowChart flowChart = new FlowChart();
    Stack<Sequence> sequences = new Stack<>();
    Stack<Operation> composite = new Stack<>();
    int count = 0;
    IOperation current = null;

    public String getDiagram() {

        genDiagram(base);

        StringBuilder sb = new StringBuilder();
        sb.append("# Flowchart\n\n");
        sb.append("```mermaid\n");
        sb.append(flowChart.generateMermaidSyntax());
        sb.append("```\n");
        return sb.toString();
    }

    public void genDiagram(Sequence sb) {
        for (Operation operation : sb.getOperations()) {
            flowChart.add(operation.getFlowchartNode());
            if (operation instanceof CompositeOutOperation) {
                for (Sequence sequence : ((CompositeOutOperation) operation).outgoingSequence()) {
                    genDiagram(sequence);
                }
            }
            operation.getFlowchartEdges().forEach(flowChart::add);
        }
    }

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

    @Override
    public void visit(FunctionBodyBlockNode node) {
        sequences.push(base);
        base.addOperation(new Start(count++, null));
        super.visit(node);
        base.addOperation(new End(count++, null));
    }

    @Override
    public void visit(NamedWorkerDeclarationNode node) {
        Sequence sequence = new Sequence();
        sequences.push(sequence);
        super.visit(node);
    }

    // Statements

    @Override
    public void visit(IfElseStatementNode node) {
        Switch aSwitch = new Switch(count++, "If");
        composite.push(aSwitch);
        sequences.peek().addOperation(aSwitch);
        super.visit(node);
        SwitchMerge switchMerge = new SwitchMerge(count++, null);
        composite.pop();
        aSwitch.outgoingSequence().forEach(switchMerge::addIncomingSequence);
        if (aSwitch.outgoingSequence().size() == 1) {
            // This will be a simple if case
            sequences.peek().addOperation(switchMerge);
        } else {
            sequences.peek().addCompositeOperationEnd(switchMerge);
        }
    }

    @Override
    public void visit(BlockStatementNode node) {
        if (!(composite.peek() instanceof AbstractCompositeOutOperation)) {
            throw new IllegalStateException("BlockStatementNode can only be a child of AbstractCompositeOutOperation");
        }
        AbstractCompositeOutOperation outOperation = (AbstractCompositeOutOperation) composite.peek();
        Sequence sequence = new Sequence();
        outOperation.addOutgoingSequence(sequence);
        sequences.push(sequence);
        super.visit(node);
        sequences.pop();
    }

    @Override
    public void visit(ExpressionStatementNode node) {
        current = new IOperation(node);
        super.visit(node);
        handleExpressionCase();
    }

    @Override
    public void visit(AssignmentStatementNode node) {
        current = new IOperation(node);
        super.visit(node);
        // TODO: Capture Variable
        handleExpressionCase();
    }

    @Override
    public void visit(VariableDeclarationNode node) {
        current = new IOperation(node);
        super.visit(node);
        // TODO: Capture Variable
        handleExpressionCase();
    }

    // Expressions

    @Override
    public void visit(CheckExpressionNode node) {
        current.checked = true;
        super.visit(node);
    }

    @Override
    public void visit(MethodCallExpressionNode node) {

    }

    @Override
    public void visit(FunctionCallExpressionNode node) {

    }

    @Override
    public void visit(RemoteMethodCallActionNode node) {
        // Make a NetworkCall
        NetworkCall networkCall = new NetworkCall(count++, node.methodName().toString());
        sequences.peek().addOperation(networkCall);
        current.units.empty();
    }

    @Override
    public void visit(ClientResourceAccessActionNode node) {
        NetworkCall networkCall = new NetworkCall(count++, node.methodName().toString());
        sequences.peek().addOperation(networkCall);
        current.units.empty();
    }

    // Utils

    private void handleExpressionCase() {
        if (current == null || current.units.isEmpty()) {
            return;
        }

        Expression expression = new Expression(count++, "Expression");
        if (current.checked) {
            expression.setFailOnError();
        }
        sequences.peek().addOperation(expression);
    }

    static final class IOperation {

        final Stack<Node> units = new Stack<>();
        boolean checked = false;
        boolean done = false;

        IOperation(Node node) {
            units.push(node);
        }
    }
}
