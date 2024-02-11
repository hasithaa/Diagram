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
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeDescKind;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ChildNodeEntry;
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.ExpressionFunctionBodyNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionBodyBlockNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.compiler.syntax.tree.NodeList;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.NonTerminalNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.github.hasithaa.diagram.integration.templates.End;
import io.github.hasithaa.diagram.integration.templates.Expression;
import io.github.hasithaa.diagram.integration.templates.External;
import io.github.hasithaa.diagram.integration.templates.Hidden;
import io.github.hasithaa.diagram.integration.templates.LibraryCall;
import io.github.hasithaa.diagram.integration.templates.NetworkCall;
import io.github.hasithaa.diagram.integration.templates.Start;
import io.github.hasithaa.diagram.integration.templates.Switch;
import io.github.hasithaa.diagram.integration.templates.SwitchMerge;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


public class CodeVisitor extends NodeVisitor {

    private final List<Diagram> diagrams = new ArrayList<>();
    private final SemanticModel semanticModel;

    // Data
    Sequence base;
    Stack<Sequence> sequences;
    int count = 0;
    Stack<IOperation> iOperations;
    Map<VariableSymbol, Sequence> globalEp = new HashMap();

    public CodeVisitor(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;
        base = new Sequence(null, null);
    }


    public List<Diagram> getDiagrams() {
        return Collections.unmodifiableList(diagrams);
    }

    public List<Sequence> getEp() {
        return new ArrayList<>(globalEp.values());
    }

    @Override
    public void visit(FunctionDefinitionNode node) {
        Diagram diagram = newDiagram(node.functionName().toString());
        super.visit(node);
        diagram.generatePaths();
    }

    private Diagram newDiagram(String name) {
        base = new Sequence(null, null);
        sequences = new Stack<>();
        iOperations = new Stack<>();
        sequences.push(base);
        count = 0;
        Diagram diagram = new Diagram(name, base);
        diagrams.add(diagram);
        return diagram;
    }

    @Override
    public void visit(FunctionBodyBlockNode node) {
        base.addOperation(new Start(count++));
        super.visit(node);
        base.addOperation(new End(count++));
    }

    public void visit(ExpressionFunctionBodyNode node) {
        // Identify this as a data transformation
        diagrams.remove(diagrams.size() - 1);
    }

    @Override
    public void visit(NamedWorkerDeclarationNode node) {
        super.visit(node);
    }

    @Override
    public void visit(ModuleVariableDeclarationNode node) {

        semanticModel.symbol(node.typedBindingPattern().bindingPattern()).ifPresent(symbol -> {
            if (symbol.kind() == SymbolKind.VARIABLE) {
                VariableSymbol variableSymbol = (VariableSymbol) symbol;
                if (variableSymbol.typeDescriptor().typeKind() == TypeDescKind.TYPE_REFERENCE) {
                    ModuleSymbol moduleSymbol = variableSymbol.typeDescriptor().getModule().get();

                    Optional<Symbol> typeByName = semanticModel.types().getTypeByName(moduleSymbol.id().orgName(),
                                                                                      moduleSymbol.id().moduleName(),
                                                                                      moduleSymbol.id().version(),
                                                                                      variableSymbol.typeDescriptor()
                                                                                                    .getName().get());
                    if (typeByName.isPresent()) {
                        Symbol typeSymbol = typeByName.get();
                        if (typeSymbol.kind() == SymbolKind.CLASS && ((ClassSymbol) typeSymbol).qualifiers().contains(
                                Qualifier.CLIENT)) {
                            External newEx = new External(count++);
                            newEx.setHeading(
                                    moduleSymbol.id().moduleName() + ":" + typeSymbol.getName().orElse("Unknown"));
                            Sequence connector = new Sequence(null, null);
                            connector.setLabel("📡 Connector");
                            newEx.addFormData("Name", variableSymbol.getName().orElse("Unknown Variable"));
                            globalEp.put(variableSymbol, connector);
                            connector.addOperation(newEx);
                        }
                    }
                }
            }
        });
    }

    // Statements

    @Override
    public void visit(IfElseStatementNode node) {
        iOperations.push(new IOperation(node));
        Switch aSwitch = new Switch(count++);
        SwitchMerge switchMerge = new SwitchMerge(count++, aSwitch);
        sequences.peek().addOperation(aSwitch);
        sequences.peek().addOperation(switchMerge);

        aSwitch.setHeading("If");
        aSwitch.addFormData("Condition", sanitizeExpression(node.condition().toSourceCode()));
        node.condition().accept(this);

        Sequence thenSequence = new Sequence(aSwitch, switchMerge);
        thenSequence.setLabel("Then");
        aSwitch.addOutgoingSequence(thenSequence);
        sequences.push(thenSequence);
        node.ifBody().accept(this);
        sequences.pop();

        Sequence elseSequence = new Sequence(aSwitch, switchMerge);
        elseSequence.setLabel("Else");
        aSwitch.addOutgoingSequence(elseSequence);
        sequences.push(elseSequence);
        if (node.elseBody().isPresent()) {
            node.elseBody().get().accept(this);
        }
        sequences.pop();

        aSwitch.outgoingSequence().forEach(switchMerge::addIncomingSequence);
        iOperations.pop();
    }

    @Override
    public void visit(ExpressionStatementNode node) {
        iOperations.push(new IOperation(node));
        super.visit(node);
        handleDefaultExpression(node.expression());
        iOperations.pop();
    }

    @Override
    public void visit(AssignmentStatementNode node) {
        iOperations.push(new IOperation(node));
        super.visit(node);
        handleDefaultExpression(node.expression());
        semanticModel.symbol(node.varRef()).ifPresent(symbol -> {
            if (symbol.kind() == SymbolKind.VARIABLE) {
                VariableSymbol variableSymbol = (VariableSymbol) symbol;
                Variable variable = new Variable(
                        sanitizeExpression(variableSymbol.getName().orElse("Unknown Variable")), false,
                        sanitizeExpression(variableSymbol.typeDescriptor().signature()));
                iOperations.peek().operation.addVariable(variable);
            }
        });
        iOperations.pop();
    }

    @Override
    public void visit(VariableDeclarationNode node) {
        iOperations.push(new IOperation(node));
        super.visit(node);
        handleDefaultExpression(node.initializer().orElse(null));
        semanticModel.symbol(node.typedBindingPattern().bindingPattern()).ifPresent(symbol -> {
            if (symbol.kind() == SymbolKind.VARIABLE) {
                VariableSymbol variableSymbol = (VariableSymbol) symbol;
                Variable variable = new Variable(
                        sanitizeExpression(variableSymbol.getName().orElse("Unknown Variable")), true,
                        sanitizeExpression(variableSymbol.typeDescriptor().signature()));
                iOperations.peek().operation.addVariable(variable);
            }
        });
        iOperations.pop();
    }

    // Expressions

    @Override
    public void visit(CheckExpressionNode node) {
        if (!iOperations.empty()) {
            iOperations.peek().checked = true;
        }
        super.visit(node);
    }

    @Override
    public void visit(MethodCallExpressionNode node) {
        if (handleExternalLibraryCall(node)) {
        }
        // Don't do anything further
    }

    @Override
    public void visit(FunctionCallExpressionNode node) {
        if (handleExternalLibraryCall(node)) {
        }
        // Local function call. So CodeBlock.

    }

    @Override
    public void visit(RemoteMethodCallActionNode node) {
        handleNetworkCall(node, node.expression(), node.methodName().toString());
    }

    @Override
    public void visit(ClientResourceAccessActionNode node) {
        handleNetworkCall(node, node.expression(), node.methodName().toString());
    }

    // Utils

    private boolean handleExternalLibraryCall(Node node) {
        Optional<Symbol> symbol = semanticModel.symbol(node);
        // TODO: Following logic is demonstration purpose only. This should be improved and completed.
        if (symbol.isPresent()) {
            Symbol funcSymbol = symbol.get();
            if (funcSymbol.getModule().isPresent()) {
                String functionName = funcSymbol.getName().orElse("Unknown Function");
                ModuleSymbol moduleSymbol = funcSymbol.getModule().get();
                String moduleName = moduleSymbol.id().moduleName();
                LibraryCall libraryCall = new LibraryCall(count++);
                libraryCall.setHeading(moduleName + ":" + functionName);
                libraryCall.setComment(node.lineRange().fileName() + ":" + node.lineRange().startLine());

                if (funcSymbol instanceof FunctionSymbol functionSymbol) {
                    extractFunctionCallFormData((NonTerminalNode) node, libraryCall, functionSymbol);
                }
                if (iOperations.peek().checked) {
                    libraryCall.setFailOnError();
                }

                sequences.peek().addOperation(libraryCall);
                iOperations.peek().done(libraryCall);
                return true;
            }
            // TODO: What if local method call ?
        }
        return false;
    }

    private String sanitizeExpression(String str) {
        String newStr = str;
        if (str.length() > 15) {
            newStr = newStr.substring(0, 15) + "...";
        }
        newStr = newStr.replace("\n", "").replace("\r", "").replace("\"", "").replace("'", "&apos;");
        return newStr;
    }

    private void handleNetworkCall(Node node, Node expr, String methodName) {
        Optional<Symbol> symbol = semanticModel.symbol(node);
        if (symbol.isPresent() && symbol.get().getModule().isPresent()) {
            if (symbol.get() instanceof MethodSymbol methodSymbol) {
                ModuleSymbol moduleSymbol = methodSymbol.getModule().get();
                String moduleName = moduleSymbol.id().moduleName();
                NetworkCall networkCall = new NetworkCall(count++);
                sequences.peek().addOperation(networkCall);
                iOperations.peek().done(networkCall);

                networkCall.addFormData("Method", methodName);

                extractFunctionCallFormData((NonTerminalNode) node, networkCall, methodSymbol);
                if (iOperations.peek().checked) {
                    networkCall.setFailOnError();
                }
                Diagram diagram = this.diagrams.get(diagrams.size() - 1);


                semanticModel.symbol(expr).ifPresent(varSym -> {
                    if (varSym instanceof VariableSymbol variableSymbol) {
                        networkCall.setHeading(moduleName + " " + variableSymbol.typeDescriptor().getName()
                                                                                .orElse("Unknown Variable"));
                        if (globalEp.containsKey(variableSymbol)) {
                            // What if no operations ? (ep visited later)
                            Operation ep = globalEp.get(variableSymbol).getOperations().get(0);
                            diagram.addPath(new DPath(networkCall, ep, "🔗", DPath.PathType.STRONG));
                            diagram.addPath(new DPath(ep, networkCall, null, DPath.PathType.DOTTED, false));
                        }
                    }
                });
            }
        }
    }

    private void extractFunctionCallFormData(NonTerminalNode node, Operation operation, FunctionSymbol functionSymbol) {
        final List<String> params = new ArrayList<>();
        functionSymbol.typeDescriptor().params().ifPresent(parameterSymbols -> {
            for (ParameterSymbol parameterSymbol : parameterSymbols) {
                params.add(parameterSymbol.getName().orElse("Param" + params.size()) + "(" +
                                   sanitizeExpression(parameterSymbol.typeDescriptor().signature()) + ")");
            }
        });
        AtomicBoolean hasRestParam = new AtomicBoolean(false);
        functionSymbol.typeDescriptor().restParam().ifPresent(parameterSymbol -> {
            hasRestParam.set(true);
            params.add(parameterSymbol.getName().orElse("Rest") + "(" +
                               sanitizeExpression(parameterSymbol.typeDescriptor().signature()) + ")");
        });
        AtomicInteger paramCount = new AtomicInteger();
        for (ChildNodeEntry nodeEntry : node.childEntries()) {
            if (nodeEntry.name().equals("arguments") && nodeEntry.node().isPresent()) {
                if (nodeEntry.isList()) {
                    NodeList<Node> nodes = nodeEntry.nodeList();
                    nodes.stream().filter(n -> n.kind() == SyntaxKind.POSITIONAL_ARG).forEach(n -> {
                        operation.addFormData(params.get(paramCount.get()), sanitizeExpression(n.toSourceCode()));
                        // Hack to support rest params
                        if (hasRestParam.get() && paramCount.get() < params.size() - 1) {
                            paramCount.getAndIncrement();
                        }
                    });
                } else {
                    Node param = nodeEntry.node().get();
                    operation.addFormData(params.get(paramCount.get()), sanitizeExpression(param.toSourceCode()));
                }
            }
        }
        functionSymbol.typeDescriptor().returnTypeDescriptor().ifPresent(returnType -> {
            operation.addFormData("Return", sanitizeExpression(returnType.signature()));
        });
    }

    private void handleDefaultExpression(ExpressionNode rhs) {
        if (iOperations.empty() || iOperations.peek().isDone()) {
            return;
        }

        if (rhs == null) {
            Hidden hidden = new Hidden(count++, "🆕");
            sequences.peek().addOperation(hidden);
            hidden.setHeading("Var");
            iOperations.peek().done(hidden);
            return;
        }
        Expression expression = new Expression(count++);
        expression.setHeading("Expression");
        expression.addFormData("Expression", sanitizeExpression(rhs.toSourceCode()));
        if (iOperations.peek().checked) {
            expression.setFailOnError();
        }
        sequences.peek().addOperation(expression);
        iOperations.peek().done(expression);
    }

    static final class IOperation {

        final Stack<Node> units = new Stack<>();
        boolean checked = false;
        Operation operation;
        private boolean isDone = false;

        IOperation(Node node) {
            units.push(node);
        }

        void done(Operation operation) {
            this.operation = operation;
            isDone = true;
        }

        boolean isDone() {
            return isDone;
        }

    }
}
