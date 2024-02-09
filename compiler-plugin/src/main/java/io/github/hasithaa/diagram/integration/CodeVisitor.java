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
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.BlockStatementNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.ExpressionFunctionBodyNode;
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
import io.ballerina.projects.plugins.CompilationAnalysisContext;
import io.github.hasithaa.diagram.flowchart.FlowChart;
import io.github.hasithaa.diagram.integration.templates.End;
import io.github.hasithaa.diagram.integration.templates.Expression;
import io.github.hasithaa.diagram.integration.templates.LibraryCall;
import io.github.hasithaa.diagram.integration.templates.NetworkCall;
import io.github.hasithaa.diagram.integration.templates.Sequence;
import io.github.hasithaa.diagram.integration.templates.Start;
import io.github.hasithaa.diagram.integration.templates.Switch;
import io.github.hasithaa.diagram.integration.templates.SwitchMerge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Stack;


public class CodeVisitor extends NodeVisitor {

    final List<FlowChart> flowCharts = new ArrayList<>();
    final CompilationAnalysisContext ctx;
    final ModuleId moduleId;
    final SemanticModel semanticModel;

    // Data
    Sequence base;
    Stack<Sequence> sequences;
    Stack<Operation> composite;
    int count = 0;
    IOperation current = null;


    public CodeVisitor(CompilationAnalysisContext ctx, ModuleId moduleId, SemanticModel semanticModel) {
        this.ctx = ctx;
        this.moduleId = moduleId;
        this.semanticModel = semanticModel;
        base = new Sequence();
    }

    private void genFlowChart(Sequence sb, FlowChart flowChart) {
        for (Operation operation : sb.getOperations()) {
            flowChart.add(operation.getFlowchartNode());
            if (operation instanceof CompositeOutOperation) {
                for (Sequence sequence : ((CompositeOutOperation) operation).outgoingSequence()) {
                    genFlowChart(sequence, flowChart);
                }
            }
            operation.getFlowchartEdges().forEach(flowChart::add);
        }
    }

    private FlowChart newFlowchart(String name) {
        base = new Sequence();
        sequences = new Stack<>();
        sequences.push(base);
        composite = new Stack<>();
        count = 0;
        return new FlowChart(name);
    }

    public List<FlowChart> getFlowCharts() {
        return Collections.unmodifiableList(flowCharts);
    }


    @Override
    public void visit(FunctionDefinitionNode node) {
        FlowChart flowChart = newFlowchart(node.functionName().toString());
        flowCharts.add(flowChart);
        super.visit(node);
        genFlowChart(base, flowChart);
    }

    @Override
    public void visit(FunctionBodyBlockNode node) {
        sequences.push(base);
        base.addOperation(new Start(count++, null));
        super.visit(node);
        base.addOperation(new End(count++, null));
    }

    public void visit(ExpressionFunctionBodyNode node) {
        // Identify this as a data transformation
        flowCharts.remove(flowCharts.size() - 1);
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
        if (handleExternalLibraryCall(node)) {
            current.done = true;
        }
        // Don't do anything further
    }

    @Override
    public void visit(FunctionCallExpressionNode node) {
        if (handleExternalLibraryCall(node)) {
            current.done = true;
        }
        // Don't do anything further
    }

    @Override
    public void visit(RemoteMethodCallActionNode node) {
        handleNetworkCall(node, node.methodName().toString());
    }

    @Override
    public void visit(ClientResourceAccessActionNode node) {
        handleNetworkCall(node, node.methodName().toString());
    }

    // Utils

    private boolean handleExternalLibraryCall(Node node) {
        Optional<Symbol> symbol = semanticModel.symbol(node);
        if (symbol.isPresent() && symbol.get().getModule().isPresent()) {

            ModuleSymbol moduleSymbol = symbol.get().getModule().get();
            String orgName = moduleSymbol.id().orgName();
            String moduleName = moduleSymbol.id().moduleName();
            // TODO Improve this
            if (orgName.equals("ballerina")) {
                switch (moduleName) {
                    case "http":
                    case "log":
                    case "io":
                        LibraryCall libraryCall = new LibraryCall(count++, moduleName);
                        sequences.peek().addOperation(libraryCall);
                        return true;
                    default:
                }
            }
        }
        return false;
    }

    private void handleNetworkCall(Node node, String methodName) {
        Optional<Symbol> symbol = semanticModel.symbol(node);
        if (symbol.isPresent() && symbol.get().getModule().isPresent()) {
            ModuleSymbol moduleSymbol = symbol.get().getModule().get();
            String moduleName = moduleSymbol.id().moduleName();
            NetworkCall networkCall = new NetworkCall(count++, moduleName + " " + symbol.get().getName().get());
            sequences.peek().addOperation(networkCall);
            current.done = true;
        }
    }

    private void handleExpressionCase() {
        if (current == null || current.done) {
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
