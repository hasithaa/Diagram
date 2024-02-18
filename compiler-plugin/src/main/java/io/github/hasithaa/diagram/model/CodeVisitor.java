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

import io.ballerina.compiler.api.ModuleID;
import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.WorkerSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.ForkStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NamedWorkerDeclarationNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.ReturnStatementNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.WaitFieldNode;
import io.ballerina.compiler.syntax.tree.WaitFieldsListNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

public class CodeVisitor extends NodeVisitor {

    private final SemanticModel semanticModel;
    private final ModelBuilder modelBuilder;
    private final Stack<StatementNode> stmtNodeStack = new Stack<>();
    private final List<Symbol> dataMapping = new ArrayList<>();
    private final Map<WorkerSymbol, Subgraph> workerSymbols = new HashMap<>();
    private final FormBuilder formBuilder;
    private String moduleID;

    public CodeVisitor(SemanticModel semanticModel, String name) {
        this.semanticModel = semanticModel;
        modelBuilder = new ModelBuilder();
        modelBuilder.setLabel(name);
        formBuilder = new FormBuilder(modelBuilder, semanticModel);
    }

    public Model getModel() {
        return modelBuilder.getModel();
    }

    public void visit(ServiceDeclarationNode stNode) {
        formBuilder.addSymbolEntry(stNode);
        super.visit(stNode);
        formBuilder.removeSymbolEntry(stNode);
    }

    private void setCurrentModule(FunctionDefinitionNode stNode) {
        if (moduleID != null) {
            return;
        }
        semanticModel.symbol(stNode).ifPresent(symbol -> {
            if (symbol instanceof FunctionSymbol functionSymbol) {
                ModuleSymbol moduleSymbol = functionSymbol.getModule().get();
                this.moduleID = moduleSymbol.id().toString();
            }
        });
    }

    public void visit(FunctionDefinitionNode stNode) {
        setCurrentModule(stNode); // Find a way to improve this.

        workerSymbols.clear();
        Optional<Symbol> symbol = semanticModel.symbol(stNode);
        if (stNode.functionBody().kind() == SyntaxKind.EXPRESSION_FUNCTION_BODY) {
            symbol.ifPresent(dataMapping::add);
            return;
        }
        if (stNode.functionBody().kind() != SyntaxKind.FUNCTION_BODY_BLOCK) {
            return;
        }
        // New Diagram
        if (symbol.isEmpty()) {
            return;
        }
        formBuilder.addSymbolEntry(stNode);

        Diagram diagram = modelBuilder.addDiagram();
        Node node;
        if (symbol.get() instanceof ResourceMethodSymbol resourceSymbol) {
            // TODO : improve this further
            String methodName = resourceSymbol.getName().orElse("Unknown");
            diagram.setLabel(methodName + " " + resourceSymbol.resourcePath().signature());
            diagram.setDiagramType(Diagram.DiagramType.API);

            node = modelBuilder.addNewNode(Node.Kind.NETWORK_EVENT);
            node.label = "Network Event";

            formBuilder.handleResourceFunction(resourceSymbol);
            formBuilder.extractServiceDeclarationFormData();
        } else if (symbol.get() instanceof FunctionSymbol functionSymbol) {
            diagram.setLabel(functionSymbol.getName().orElse("Unknown"));

            node = modelBuilder.addNewNode(Node.Kind.FUNCTION_START);
            if (functionSymbol instanceof MethodSymbol methodSymbol) {
                if (methodSymbol.qualifiers().contains(Qualifier.REMOTE)) {
                    diagram.setDiagramType(Diagram.DiagramType.RPC);

                    node.label = "RPC";

                    formBuilder.extractServiceDeclarationFormData();
                } else {
                    diagram.setDiagramType(Diagram.DiagramType.FUNCTION);

                    node.label = "Function Start";
                }
            } else {
                if (functionSymbol.getName().isPresent() && functionSymbol.getName().get().equals("main")) {
                    diagram.setDiagramType(Diagram.DiagramType.TRIGGER);

                    node.label = "Trigger";
                    node.kind = Node.Kind.TRIGGER;
                } else {
                    diagram.setDiagramType(Diagram.DiagramType.FUNCTION);

                    node.label = "Function Start";
                    node.kind = Node.Kind.FUNCTION_START;
                }
            }
            formBuilder.handleFunction(functionSymbol);
        } else {
            throw new IllegalStateException("Unexpected value functionSymbol");
        }
        node.lineRange = stNode.lineRange();
        formBuilder.extractFunctionSymbolFormData((FunctionSymbol) symbol.get());
        super.visit(stNode);
        Node end = modelBuilder.createNode(Node.Kind.END);
        modelBuilder.addNode(end, false);
        formBuilder.removeSymbolEntry(stNode);
    }


    // Statement Nodes

    public void visit(IfElseStatementNode stNode) {
        Node ifNode = modelBuilder.addNewNode(Node.Kind.IF);
        ifNode.lineRange = stNode.lineRange();
        Node mergeNode = modelBuilder.createNode(Node.Kind.KONNECTOR);
        mergeNode.lineRange = stNode.lineRange();
        ifNode.label = "If";

        formBuilder.handleExpression(stNode.condition(), "Condition", FormData.FormDataTypeKind.BOOLEAN);

        modelBuilder.startChildFlow("Then");
        stNode.ifBody().accept(this);
        modelBuilder.endChildFlow(ifNode, mergeNode, "Then");

        if (stNode.elseBody().isPresent()) {
            modelBuilder.startChildFlow("Else");
            stNode.elseBody().get().accept(this);
            modelBuilder.endChildFlow(ifNode, mergeNode, "Else");
        }
        modelBuilder.addNode(mergeNode, true);
    }

    @Override
    public void visit(ForkStatementNode stNode) {
        Node forkNode = modelBuilder.addNewNode(Node.Kind.CLONE);
        forkNode.lineRange = stNode.lineRange();
        forkNode.label = "Fork";
        for (NamedWorkerDeclarationNode workerDeclaration : stNode.namedWorkerDeclarations()) {
            workerDeclaration.accept(this);
        }
    }

    public void visit(NamedWorkerDeclarationNode stNode) {
        Subgraph subgraph = modelBuilder.startSubGraph(stNode.workerName().text());
        semanticModel.symbol(stNode).ifPresent(symbol -> {
            if (symbol instanceof WorkerSymbol workerSymbol) {
                workerSymbols.put(workerSymbol, subgraph);
            }
        });
        super.visit(stNode);
        modelBuilder.endSubGraph();
    }

    public void visit(VariableDeclarationNode stNode) {
        stmtNodeStack.push(stNode);
        super.visit(stNode);
        if (!stmtNodeStack.empty() && stmtNodeStack.peek() == stNode) {
            handleDefaultExpressionNode(stNode);
        }
    }

    public void visit(AssignmentStatementNode stNode) {
        stmtNodeStack.push(stNode);
        super.visit(stNode);
        if (!stmtNodeStack.empty() && stmtNodeStack.peek() == stNode) {
            handleDefaultExpressionNode(stNode);
        }
    }

    public void visit(ExpressionStatementNode stNode) {
        stmtNodeStack.push(stNode);
        super.visit(stNode);
        if (!stmtNodeStack.empty() && stmtNodeStack.peek() == stNode) {
            handleDefaultExpressionNode(stNode);
        }
    }

    public void visit(ReturnStatementNode stNode) {
        stmtNodeStack.push(stNode);
        super.visit(stNode);
        if (!stmtNodeStack.empty() && stmtNodeStack.peek() == stNode) {
            handleDefaultExpressionNode(stNode);
        }
    }

    // Deciding Expression Nodes

    @Override
    public void visit(WaitFieldsListNode stNode) {
        Node node = modelBuilder.addNewNode(Node.Kind.WAIT);
        node.label = "Wait for All";
        for (io.ballerina.compiler.syntax.tree.Node waitFieldNode : stNode.waitFields()) {
            Optional<Symbol> symbol = semanticModel.symbol(waitFieldNode);
            if (symbol.isPresent() && symbol.get() instanceof WorkerSymbol workerSymbol) {
                Subgraph subgraph = workerSymbols.get(workerSymbol);
                if (subgraph != null) {
                    Edge edge = modelBuilder.addEdge(subgraph, node);
                    edge.kind = Edge.EdgeKind.OPTIONAL;
                }
            }
        }
        boolean localStatement = findAndUpdateParentLocalStatement(node, stNode);
        if (!localStatement) {
            // Safely ignore the node.
        }
        // TODO : Extract Form Data
    }

    @Override
    public void visit(WaitFieldNode stNode) {
        Node node = modelBuilder.addNewNode(Node.Kind.WAIT);
        node.label = "Wait";
        Optional<Symbol> symbol = semanticModel.symbol(stNode);
        if (symbol.isPresent() && symbol.get() instanceof WorkerSymbol workerSymbol) {
            Subgraph subgraph = workerSymbols.get(workerSymbol);
            if (subgraph != null) {
                Edge edge = modelBuilder.addEdge(subgraph, node);
                edge.kind = Edge.EdgeKind.IMPLICIT;
            }
        }
        boolean localStatement = findAndUpdateParentLocalStatement(node, stNode);
        if (!localStatement) {
            // Safely ignore the node.
        }
        // TODO : Extract Form Data
    }

    public void visit(MappingConstructorExpressionNode stNode) {
        // New Message
        handleNewMessage(stNode);
    }

    public void visit(ListConstructorExpressionNode stNode) {
        // New Message
        handleNewMessage(stNode);
    }

    public void visit(TemplateExpressionNode stNode) {
        // New Message
        handleNewMessage(stNode);
    }

    public void visit(MethodCallExpressionNode stNode) {
        handleFunctionCall(stNode);
        formBuilder.handleMethodCall(stNode);
    }

    public void visit(FunctionCallExpressionNode stNode) {
        handleFunctionCall(stNode);
        formBuilder.handleFunctionCall(stNode);
    }

    public void visit(RemoteMethodCallActionNode stNode) {
        handleNetworkCall(stNode);
        formBuilder.handleRemoteCall(stNode);
    }

    public void visit(ClientResourceAccessActionNode stNode) {
        handleNetworkCall(stNode);
        formBuilder.handleClientCall(stNode);
    }

    // Utility methods

    private void handleDefaultExpressionNode(StatementNode stNode) {
        // TODO : Improve this further.
        Node node = modelBuilder.addNewNode(Node.Kind.EXPRESSION);
        node.label = "Expression";
        node.lineRange = stNode.lineRange();
        handleStatementNode(node, stNode, true);
        stNode.children().forEach(child -> {
            if (child instanceof ExpressionNode expressionNode) {
                formBuilder.handleExpression(expressionNode, "Expression", FormData.FormDataTypeKind.DEFAULT);
            }
        });
    }

    private void handleNewMessage(ExpressionNode stNode) {
        Node node = modelBuilder.addNewNode(Node.Kind.DATA_NEW_MESSAGE);
        FormData.FormDataTypeKind typeKind;
        switch (stNode.kind()) {
            case MAPPING_CONSTRUCTOR:
                typeKind = FormData.FormDataTypeKind.MAPPING;
                node.subKind = "JSON";
                break;
            case LIST_CONSTRUCTOR:
                typeKind = FormData.FormDataTypeKind.ARRAY;
                node.subKind = "Array";
                break;
            case XML_TEMPLATE_EXPRESSION:
                typeKind = FormData.FormDataTypeKind.XML;
                node.subKind = "XML";
                break;
            case STRING_TEMPLATE_EXPRESSION:
                typeKind = FormData.FormDataTypeKind.STRING;
                node.subKind = "String";
                break;
            case BYTE_ARRAY_LITERAL:
                typeKind = FormData.FormDataTypeKind.BYTE_ARRAY;
                node.subKind = "Bytes";
                break;
            default:
                return;
        }
        node.label = "New Message";
        formBuilder.handleExpression(stNode, "Data", typeKind);
        findAndUpdateParentLocalStatement(node, stNode);
    }

    private void handleFunctionCall(ExpressionNode stNode) {
        // TODO : Improve this further.
        Optional<Symbol> optionalSymbol = semanticModel.symbol(stNode);
        if (!optionalSymbol.isPresent()) {
            return;
        }

        if (optionalSymbol.get() instanceof FunctionSymbol functionSymbol) {
            ModuleSymbol moduleSymbol = functionSymbol.getModule().get();
            String moduleName = moduleSymbol.id().toString();
            Node node;
            if (moduleName.equals(this.moduleID) && !(functionSymbol instanceof MethodSymbol)) {
                // Local Function Call
                if (dataMapping.contains(functionSymbol)) { // Improve this. If order is not correct, this will fail.
                    node = modelBuilder.addNewNode(Node.Kind.DATA_MAPPING);
                    node.label = "Data Mapping Activity";
                } else {
                    node = modelBuilder.addNewNode(Node.Kind.CODE_BLOCK);
                    node.label = "Code Block";
                }
            } else {
                node = handleKnownFunctionCall(functionSymbol, moduleSymbol.id());
            }
            findAndUpdateParentLocalStatement(node, stNode);
            return;
        }
        throw new IllegalStateException("Unexpected value functionSymbol");
    }

    private Node handleKnownFunctionCall(FunctionSymbol functionSymbol, ModuleID moduleID) {
        // Improve this further with Ballerina Connectors.
        if (!moduleID.orgName().equals("ballerina")) {
            Node node = modelBuilder.addNewNode(Node.Kind.LIBRARY_FUNCTION);
            node.label = "Library Call";
            return node;
        }
        Node node = null;
        if (moduleID.moduleName().equals("xmldata")) {
            if (functionSymbol.getName().isPresent()) {
                switch (functionSymbol.getName().get()) {
                    case "fromXml", "toJson", "toXml" -> {
                        node = modelBuilder.addNewNode(Node.Kind.DATA_CONVERT);
                        node.label = "Data Conversion";
                    }
                }
            }
        } else if (moduleID.moduleName().equals("http")) {
            // Improve this further like above
            node = modelBuilder.addNewNode(Node.Kind.KNOWN_FUNCTION_CALL);
            node.subKind = "HTTP";
            node.label = "HTTP Utility";
        } else if (moduleID.moduleName().equals("sql")) {
            // Improve this further like above
            node = modelBuilder.addNewNode(Node.Kind.KNOWN_FUNCTION_CALL);
            node.subKind = "SQL";
            node.label = "SQL Call";
        } else if (moduleID.moduleName().equals("lang.value")) {
            if (functionSymbol.getName().isPresent()) {
                switch (functionSymbol.getName().get()) {
                    case "cloneWithType", "ensureType" -> {
                        node = modelBuilder.addNewNode(Node.Kind.DATA_VALIDATION);
                        node.label = "Data Schema Validation";
                    }
                }
            }
        } else if (moduleID.moduleName().equals("lang.array")) {
            node = modelBuilder.addNewNode(Node.Kind.KNOWN_FUNCTION_CALL);
            node.subKind = "ARRAY";
            node.label = "Array Operation";
        } else if (moduleID.moduleName().equals("lang.string")) {
            node = modelBuilder.addNewNode(Node.Kind.KNOWN_FUNCTION_CALL);
            node.subKind = "STRING";
            node.label = "String Operation";
        } else if (moduleID.moduleName().equals("lang.map")) {
            node = modelBuilder.addNewNode(Node.Kind.KNOWN_FUNCTION_CALL);
            node.subKind = "MAP";
            node.label = "Mapping Value Operation";
        } else if (moduleID.moduleName().equals("lang.xml")) {
            node = modelBuilder.addNewNode(Node.Kind.KNOWN_FUNCTION_CALL);
            node.subKind = "XML";
            node.label = "XML Operation";
        } // TODO : ADD more
        if (node == null) {
            node = modelBuilder.addNewNode(Node.Kind.LIBRARY_FUNCTION);
            node.label = functionSymbol.getName().orElse("Unknown Function");
        }
        return node;
    }

    private void handleNetworkCall(ExpressionNode stNode) {
        // TODO: Improve this further.
        Optional<Symbol> optionalSymbol = semanticModel.symbol(stNode);
        if (!optionalSymbol.isPresent()) {
            return;
        }
        Node node;
        if (stNode instanceof RemoteMethodCallActionNode) {
            node = modelBuilder.addNewNode(Node.Kind.NETWORK_RESOURCE_CALL);
            node.label = "Resource Call";
        } else if (stNode instanceof ClientResourceAccessActionNode) {
            node = modelBuilder.addNewNode(Node.Kind.NETWORK_REMOTE_CALL);
            node.label = "Remote Call";
        } else {
            throw new IllegalStateException("Unexpected value stNode");
        }
        if (optionalSymbol.get() instanceof MethodSymbol methodSymbol) {
            StringBuilder sb = new StringBuilder();
            methodSymbol.getModule().ifPresent(moduleSymbol -> sb.append(moduleSymbol.id().moduleName()));
            node.subLabel = sb.append(" ").append(methodSymbol.getName().orElse("Unknown Network Call")).toString();
        }
        findAndUpdateParentLocalStatement(node, stNode);
    }

    private boolean findAndUpdateParentLocalStatement(Node node, io.ballerina.compiler.syntax.tree.Node source) {
        io.ballerina.compiler.syntax.tree.Node stNode = source;
        while (stNode != null) {
            if (stNode instanceof StatementNode) {
                node.lineRange = stNode.lineRange();
                if (!stmtNodeStack.empty() && stmtNodeStack.peek() != stNode) {
                    throw new IllegalStateException("Node stack is not in sync with the source node: " + source);
                }
                stmtNodeStack.pop();
                break;
            }
            if (stNode instanceof ModuleVariableDeclarationNode) {
                return false;
            }
            if (stNode instanceof CheckExpressionNode) {
                formBuilder.handleFailOnError();
            }
            stNode = stNode.parent();
        }
        if (stNode == null) {
            throw new IllegalStateException("Parent statement not found for the node: " + source);
        }
        handleStatementNode(node, (StatementNode) stNode, false);
        return true;
    }

    private void handleStatementNode(Node node, StatementNode stNode, boolean defaultCase) {
        if (stNode instanceof VariableDeclarationNode stmt) {
            formBuilder.handleVariableDeclarationFormData(stmt);
        } else if (stNode instanceof AssignmentStatementNode stmt) {
            formBuilder.handleAssignmentStatementFormData(stmt);
        } else if (stNode instanceof ExpressionStatementNode stmt) {
            formBuilder.handleExpressionStatementFromData(stmt);
        } else if (stNode instanceof ReturnStatementNode stmt) {

            // Fix incorrect node kind
            if (defaultCase) {

                io.ballerina.compiler.syntax.tree.Node parent = stmt;
                boolean insideWorker = false;
                while (parent != null) {
                    if (parent instanceof NamedWorkerDeclarationNode) {
                        insideWorker = true;
                        break;
                    }
                    parent = parent.parent();
                }
                if (insideWorker) {
                    node.kind = Node.Kind.ASYNC_RETURN;
                    node.label = "Async Result";
                } else {
                    node.kind = Node.Kind.RETURN;
                    node.label = "Return";
                }
            }
            node.terminal = true;
            node.returnable = true;
        }
    }


}