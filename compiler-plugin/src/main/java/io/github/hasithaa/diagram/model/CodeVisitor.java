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

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AbsResourcePathAttachPoint;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.LiteralAttachPoint;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.ModuleSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.Qualifier;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.CheckExpressionNode;
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.IfElseStatementNode;
import io.ballerina.compiler.syntax.tree.ListConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.TemplateExpressionNode;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Stack;

public class CodeVisitor extends NodeVisitor {

    private final SemanticModel semanticModel;
    private final ModelBuilder modelBuilder = new ModelBuilder();
    private final Stack<StatementNode> stmtNodeStack = new Stack<>();
    private final Stack<Symbol> symbolStack = new Stack<>();
    private final List<Symbol> dataMapping = new ArrayList<>();

    public CodeVisitor(SemanticModel semanticModel, String name) {
        this.semanticModel = semanticModel;
        modelBuilder.getModel().setLabel(name);
    }

    public Model getModel() {
        return modelBuilder.getModel();
    }

    public void visit(ServiceDeclarationNode stNode) {
        symbolStack.push(semanticModel.symbol(stNode).orElseThrow());
        super.visit(stNode);
        symbolStack.pop();
    }

    public void visit(FunctionDefinitionNode stNode) {
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
        symbolStack.push(symbol.get());

        Diagram diagram = modelBuilder.addDiagram();
        Node node;
        if (symbol.get() instanceof ResourceMethodSymbol resourceSymbol) {
//            modelBuilder.startChildFlow("Network Event");
            // TODO : improve this further
            String methodName = resourceSymbol.getName().orElse("Unknown");
            diagram.setLabel(methodName + " " + resourceSymbol.resourcePath().signature());
            node = modelBuilder.addNewNode(Node.Kind.NETWORK_EVENT);
            node.label = "Network Event";
            modelBuilder.addFormData("Network", new FormData("Method").setTypeKind(FormData.FormDataTypeKind.IDENTIFIER)
                                                                      .setValue(methodName));
            modelBuilder.addFormData("Network", new FormData("Path").setTypeKind(FormData.FormDataTypeKind.STRING)
                                                                    .setValue(
                                                                            resourceSymbol.resourcePath().signature()));
            extractServiceDeclarationFormData();
        } else if (symbol.get() instanceof FunctionSymbol functionSymbol) {
            if (functionSymbol instanceof MethodSymbol methodSymbol) {
                if (methodSymbol.qualifiers().contains(Qualifier.REMOTE)) {
//                    modelBuilder.startChildFlow("Network Event");
                    extractServiceDeclarationFormData();
                } else {
//                    modelBuilder.startChildFlow("Method Call");
                }
            } else {
//                modelBuilder.startChildFlow("Function Call");
            }
            diagram.setLabel(functionSymbol.getName().orElse("Unknown"));
            node = modelBuilder.addNewNode(Node.Kind.FUNCTION_START);
            if (functionSymbol.getName().isPresent() && functionSymbol.getName().get().equals("main")) {
                node.label = "Trigger";
            } else {
                node.label = "Function Start";
            }
            modelBuilder.addFormData("Function", new FormData("Name").setTypeKind(FormData.FormDataTypeKind.IDENTIFIER)
                                                                     .setValue(functionSymbol.getName()
                                                                                             .orElse("Unknown")));
        } else {
            throw new IllegalStateException("Unexpected value functionSymbol");
        }
        node.lineRange = stNode.lineRange();
        extractFunctionSymbolFormData((FunctionSymbol) symbol.get());
        super.visit(stNode);
        Node end = modelBuilder.createNode(Node.Kind.END);
//        modelBuilder.endChildFlow(node, end);
        modelBuilder.addNode(end, false);
        symbolStack.pop();
    }


    // Statement Nodes

    public void visit(IfElseStatementNode stNode) {
        Node ifNode = modelBuilder.addNewNode(Node.Kind.IF);
        ifNode.lineRange = stNode.lineRange();
        Node mergeNode = modelBuilder.createNode(Node.Kind.KONNECTOR);
        mergeNode.lineRange = stNode.lineRange();
        ifNode.label = "If";
        FormData formData = new FormData("Condition");
        formData.setTypeKind(FormData.FormDataTypeKind.BOOLEAN);
        formData.setValue(stNode.condition().toSourceCode());
        modelBuilder.addFormData("Condition", formData);

        modelBuilder.startChildFlow("Then");
        stNode.ifBody().accept(this);
        modelBuilder.endChildFlow(ifNode, mergeNode);

        if (stNode.elseBody().isPresent()) {
            modelBuilder.startChildFlow("Else");
            stNode.elseBody().get().accept(this);
            modelBuilder.endChildFlow(ifNode, mergeNode);
        }
        modelBuilder.addNode(mergeNode, true);
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

    // Deciding Expression Nodes

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
    }

    public void visit(FunctionCallExpressionNode stNode) {
        handleFunctionCall(stNode);
    }

    public void visit(RemoteMethodCallActionNode stNode) {
        handleNetworkCall(stNode);
    }

    public void visit(ClientResourceAccessActionNode stNode) {
        handleNetworkCall(stNode);
    }

    // Utility methods

    private void handleDefaultExpressionNode(StatementNode stNode) {
        // TODO : Improve this further.
        Node node = modelBuilder.addNewNode(Node.Kind.EXPRESSION);
        node.label = "Expression";
        node.lineRange = stNode.lineRange();
        handleStatementNode(node, stNode);
    }

    private void handleNewMessage(ExpressionNode stNode) {
        FormData.FormDataTypeKind typeKind;
        switch (stNode.kind()) {
            case MAPPING_CONSTRUCTOR:
                typeKind = FormData.FormDataTypeKind.MAPPING;
                break;
            case LIST_CONSTRUCTOR:
                typeKind = FormData.FormDataTypeKind.ARRAY;
                break;
            case XML_TEMPLATE_EXPRESSION:
                typeKind = FormData.FormDataTypeKind.XML;
                break;
            case STRING_TEMPLATE_EXPRESSION:
                typeKind = FormData.FormDataTypeKind.STRING;
                break;
            case BYTE_ARRAY_LITERAL:
                typeKind = FormData.FormDataTypeKind.BYTE_ARRAY;
                break;
            default:
                return;
        }
        Node node = modelBuilder.addNewNode(Node.Kind.DATA_NEW_MESSAGE);
        node.label = "New Message";
        boolean localStatement = findAndUpdateParentLocalStatement(node, stNode);
        if (!localStatement) {
            // Safely ignore the node.
            return;
        }
        FormData formData = new FormData("Data");
        formData.setTypeKind(typeKind);
        semanticModel.symbol(stNode).ifPresent(symbol -> {
            if (symbol instanceof TypeSymbol tSymbol) {
                formData.addAllowedTypes(tSymbol.signature());
            }
        });
        formData.setValue(stNode.toSourceCode());
    }

    private void handleFunctionCall(ExpressionNode stNode) {
        // TODO : Improve this further.
        Optional<Symbol> optionalSymbol = semanticModel.symbol(stNode);
        if (!optionalSymbol.isPresent()) {
            return;
        }
        if (optionalSymbol.get() instanceof FunctionSymbol functionSymbol) {
            ModuleSymbol moduleSymbol = functionSymbol.getModule().get();
            String moduleName = moduleSymbol.id().moduleName();
            Node node;
            if (moduleName.equals(".")) {
                // Local Function Call
                if (dataMapping.contains(functionSymbol)) {
                    node = modelBuilder.addNewNode(Node.Kind.DATA_MAPPING);
                    node.label = "Data Mapping";
                } else {
                    node = modelBuilder.addNewNode(Node.Kind.LIBRARY_FUNCTION);
                    node.label = "Library Call";
                }
            } else {
                node = modelBuilder.addNewNode(Node.Kind.LIBRARY_FUNCTION);
                node.label = "Library Call";
            }

            boolean localStatement = findAndUpdateParentLocalStatement(node, stNode);
            if (!localStatement) {
                // Safely ignore the node.
                return;
            }
        } else {
            throw new IllegalStateException("Unexpected value functionSymbol");
        }
        // TODO: Handel Function call parameters
        FormData formData = new FormData("expression");
        formData.setTypeKind(FormData.FormDataTypeKind.IDENTIFIER);
        formData.setValue(stNode.toSourceCode());
        modelBuilder.addFormData("Params", formData);
    }

    private void handleNetworkCall(ExpressionNode stNode) {
        // TODO: Improve this further.
        Optional<Symbol> optionalSymbol = semanticModel.symbol(stNode);
        if (!optionalSymbol.isPresent()) {
            return;
        }
        if (optionalSymbol.get() instanceof MethodSymbol methodSymbol) {
            Node.Kind kind = Node.Kind.NETWORK_RESOURCE_CALL;
            if (stNode.kind() == SyntaxKind.REMOTE_METHOD_CALL_ACTION) {
                kind = Node.Kind.NETWORK_REMOTE_CALL;
            }
            Node node = modelBuilder.addNewNode(kind);
            node.label = "Network Call";
            boolean localStatement = findAndUpdateParentLocalStatement(node, stNode);
            if (!localStatement) {
                // Safely ignore the node.
                return;
            }
            // TODO: Handel Function call parameters
            FormData formData = new FormData("expression");
            formData.setTypeKind(FormData.FormDataTypeKind.IDENTIFIER);
            formData.setValue(stNode.toSourceCode());
            modelBuilder.addFormData("Params", formData);
        }
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
                FormData formData = new FormData("Fail On Error");
                formData.setTypeKind(FormData.FormDataTypeKind.BOOLEAN);
                formData.setValue("true");
                modelBuilder.addFormData("Error", formData);
            }
            stNode = stNode.parent();
        }
        if (stNode == null) {
            throw new IllegalStateException("Parent statement not found for the node: " + source);
        }
        handleStatementNode(node, (StatementNode) stNode);
        return true;
    }

    private void handleStatementNode(Node node, StatementNode stNode) {
        if (stNode instanceof VariableDeclarationNode stmt) {
            handleVariableDeclarationFormData(node, stmt);
        } else if (stNode instanceof AssignmentStatementNode stmt) {
            handleAssignmentStatementFormData(node, stmt);
        } else if (stNode instanceof ExpressionStatementNode stmt) {
            handleExpressionStatementNode(node, stmt);
        }
    }

    private void handleVariableDeclarationFormData(Node node, VariableDeclarationNode stNode) {
        semanticModel.symbol(stNode.typedBindingPattern().bindingPattern()).ifPresent(symbol -> {
            if (symbol.kind() == SymbolKind.VARIABLE) {
                VariableSymbol variableSymbol = (VariableSymbol) symbol;
                FormData formData = new FormData("result");
                formData.setTypeKind(getFormDataType(variableSymbol.typeDescriptor()));
                formData.addAllowedTypes(variableSymbol.typeDescriptor().signature());
                formData.setValue(variableSymbol.getName().orElse("Unknown Variable"));
                formData.addFlags("Local");
                formData.addFlags("New");
                modelBuilder.addFormData("Variables", formData);
            }
        });
    }

    private void handleAssignmentStatementFormData(Node node, AssignmentStatementNode stNode) {
        semanticModel.symbol(stNode.varRef()).ifPresent(symbol -> {
            if (symbol instanceof VariableSymbol variableSymbol) {
                FormData formData = new FormData("result");
                formData.setTypeKind(getFormDataType(variableSymbol.typeDescriptor()));
                formData.addAllowedTypes(variableSymbol.typeDescriptor().signature());
                formData.setValue(variableSymbol.getName().orElse("Unknown Variable"));
                formData.addFlags("Update");
                modelBuilder.addFormData("Variables", formData);
            }
        });
    }

    private void handleExpressionStatementNode(Node node, ExpressionStatementNode stNode) {
        // DO NOTHING for now. Improve this further.
    }

    private void extractFunctionSymbolFormData(FunctionSymbol functionSymbol) {
        functionSymbol.typeDescriptor().params().ifPresent(parameterSymbols -> {
            int index = 0;
            for (ParameterSymbol param : parameterSymbols) {
                modelBuilder.addFormData("Params", new FormData(param.getName().orElse("Param" + index))
                        .setTypeKind(FormData.FormDataTypeKind.IDENTIFIER)
                        .setValue(param.typeDescriptor().signature()));
            }
        });
        functionSymbol.typeDescriptor().restParam().ifPresent(param -> {
            modelBuilder.addFormData("Params", new FormData(param.getName().orElse("RestParam")).setTypeKind(
                                                                                                        FormData.FormDataTypeKind.IDENTIFIER).setValue(param.typeDescriptor().signature())
                                                                                                .addFlags("rest"));
        });
        functionSymbol.typeDescriptor().returnTypeDescriptor().ifPresent(returnType -> {
            modelBuilder.addFormData("Return", new FormData("Return").setTypeKind(FormData.FormDataTypeKind.IDENTIFIER)
                                                                     .setValue(returnType.signature()));
        });
        functionSymbol.qualifiers().forEach(q -> modelBuilder.addFormData("Qualifiers", new FormData("Qualifier")
                .setTypeKind(FormData.FormDataTypeKind.IDENTIFIER).setValue(q.toString())));
    }

    private void extractServiceDeclarationFormData() {
        symbolStack.stream().filter(symbol -> symbol instanceof ServiceDeclarationSymbol).map(
                symbol -> (ServiceDeclarationSymbol) symbol).forEach(
                serviceSymbol -> serviceSymbol.attachPoint().ifPresent(attachPoint -> {
                    FormData formData = new FormData("AttachPoint");
                    if (attachPoint instanceof AbsResourcePathAttachPoint attachPointSymbol) {
                        formData.setTypeKind(FormData.FormDataTypeKind.DEFAULT);
                        formData.setValue(attachPointSymbol.segments().stream().reduce("", (s1, s2) -> s1 + "/" + s2));
                        formData.setEditable(false);
                    } else if (attachPoint instanceof LiteralAttachPoint literalAttachPoint) {
                        formData.setTypeKind(FormData.FormDataTypeKind.STRING);
                        formData.setValue(literalAttachPoint.literal());
                        formData.setEditable(false);
                    }
                    modelBuilder.addFormData("Service", formData);
                }));
        // TODO : Improve this further.
    }

    private FormData.FormDataTypeKind getFormDataType(TypeSymbol tSymbol) {
        // This logic seems not right. It should be improved. Talk to Manu.
        return switch (tSymbol.typeKind()) {
            case INT, INT_SIGNED8, INT_SIGNED16, INT_SIGNED32, INT_UNSIGNED8, INT_UNSIGNED16, INT_UNSIGNED32, BYTE ->
                    FormData.FormDataTypeKind.INT;
            case FLOAT -> FormData.FormDataTypeKind.FLOAT;
            case DECIMAL -> FormData.FormDataTypeKind.DECIMAL;
            case BOOLEAN -> FormData.FormDataTypeKind.BOOLEAN;
            case STRING -> FormData.FormDataTypeKind.STRING;
            case XML -> FormData.FormDataTypeKind.XML;
            case NIL -> FormData.FormDataTypeKind.NIL;
            case ARRAY, TUPLE -> FormData.FormDataTypeKind.ARRAY;
            case JSON, MAP, RECORD -> FormData.FormDataTypeKind.MAPPING;
            case TABLE -> FormData.FormDataTypeKind.TABLE;
            case UNION -> FormData.FormDataTypeKind.ENUM;
            default -> FormData.FormDataTypeKind.DEFAULT;
        };
    }
}