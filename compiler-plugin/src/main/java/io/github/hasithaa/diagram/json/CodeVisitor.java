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
package io.github.hasithaa.diagram.json;

import io.ballerina.compiler.api.SemanticModel;
import io.ballerina.compiler.api.symbols.AbsResourcePathAttachPoint;
import io.ballerina.compiler.api.symbols.FunctionSymbol;
import io.ballerina.compiler.api.symbols.LiteralAttachPoint;
import io.ballerina.compiler.api.symbols.MethodSymbol;
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
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionDefinitionNode;
import io.ballerina.compiler.syntax.tree.MappingConstructorExpressionNode;
import io.ballerina.compiler.syntax.tree.ModuleVariableDeclarationNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ServiceDeclarationNode;
import io.ballerina.compiler.syntax.tree.StatementNode;
import io.ballerina.compiler.syntax.tree.SyntaxKind;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;

import java.util.Optional;
import java.util.Stack;

public class CodeVisitor extends NodeVisitor {

    private final SemanticModel semanticModel;
    private final ModelBuilder modelBuilder = new ModelBuilder();
    private final Stack<StatementNode> stmtNodeStack = new Stack<>();
    private final Stack<Symbol> symbolStack = new Stack<>();

    public CodeVisitor(SemanticModel semanticModel) {
        this.semanticModel = semanticModel;

    }

    public void visit(ServiceDeclarationNode stNode) {
        symbolStack.push(semanticModel.symbol(stNode).orElseThrow());
        super.visit(stNode);
        symbolStack.pop();
    }

    public void visit(FunctionDefinitionNode stNode) {
        Optional<Symbol> symbol = semanticModel.symbol(stNode);
        if (stNode.functionBody().kind() == SyntaxKind.EXPRESSION_FUNCTION_BODY) {
            symbol.ifPresent(modelBuilder::addDataMapping);
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
            modelBuilder.startChildFlow("Network Event");
            // TODO : improve this further
            String methodName = resourceSymbol.getName().orElse("Unknown");
            diagram.setLabel(methodName + " " + resourceSymbol.resourcePath());
            node = modelBuilder.addNode(Node.Kind.NETWORK_EVENT);
            modelBuilder.addFormData("Network", new FormData("Method").setTypeKind(FormData.FormDataTypeKind.IDENTIFIER)
                                                                      .setValue(methodName));
            modelBuilder.addFormData("Network", new FormData("Path").setTypeKind(FormData.FormDataTypeKind.STRING)
                                                                    .setValue(
                                                                            resourceSymbol.resourcePath().toString()));
            extractServiceDeclarationFormData();
        } else if (symbol.get() instanceof FunctionSymbol functionSymbol) {
            if (functionSymbol instanceof MethodSymbol methodSymbol) {
                if (methodSymbol.qualifiers().contains(Qualifier.REMOTE)) {
                    modelBuilder.startChildFlow("Network Event");
                    extractServiceDeclarationFormData();
                } else {
                    modelBuilder.startChildFlow("Method Call");
                }
            } else {
                modelBuilder.startChildFlow("Function Call");
            }
            // TODO : Function Call Event
            diagram.setLabel(functionSymbol.getName().orElse("Unknown"));
            node = modelBuilder.addNode(Node.Kind.FUNCTION_START);
            modelBuilder.addFormData("Function", new FormData("Name").setTypeKind(FormData.FormDataTypeKind.IDENTIFIER)
                                                                     .setValue(functionSymbol.getName()
                                                                                             .orElse("Unknown")));
        } else {
            throw new IllegalStateException("Unexpected value functionSymbol");
        }
        node.lineRange = stNode.lineRange();
        extractFunctionSymbolFormData((FunctionSymbol) symbol.get());
        super.visit(stNode);
        modelBuilder.endChildFlow();
        symbolStack.pop();
    }

    public void visit(VariableDeclarationNode stNode) {
        stmtNodeStack.push(stNode);
        super.visit(stNode);
        if (stmtNodeStack.peek() == stNode) {
            handleDefaultExpressionNode(stNode);
        }
    }

    public void visit(AssignmentStatementNode stNode) {
        stmtNodeStack.push(stNode);
        super.visit(stNode);
        if (stmtNodeStack.peek() == stNode) {
            handleDefaultExpressionNode(stNode);
        }
    }

    public void visit(ExpressionStatementNode stNode) {
        stmtNodeStack.push(stNode);
        super.visit(stNode);
        if (stmtNodeStack.peek() == stNode) {
            handleDefaultExpressionNode(stNode);
        }
    }

    public void visit(MappingConstructorExpressionNode stNode) {
        // New Message
        Node node = modelBuilder.addNode(Node.Kind.DATA_NEW_MESSAGE);
        boolean localStatement = findAndUpdateParentLocalStatement(node, stNode);
        if (!localStatement) {
            // Safely ignore the node.
            return;
        }
        FormData formData = new FormData("Data");
        formData.setTypeKind(FormData.FormDataTypeKind.MAPPING);
        semanticModel.symbol(stNode).ifPresent(symbol -> {
            if (symbol instanceof TypeSymbol tSymbol) {
                formData.addAllowedTypes(tSymbol.signature());
            }
        });
        formData.setValue(stNode.toSourceCode());
    }


    // Utility methods

    private void handleDefaultExpressionNode(StatementNode stNode) {
        // TODO : Improve this further.
        Node node = modelBuilder.addNode(Node.Kind.EXPRESSION);
        handleStatementNode(node, stNode);
    }

    private boolean findAndUpdateParentLocalStatement(Node node, io.ballerina.compiler.syntax.tree.Node source) {
        io.ballerina.compiler.syntax.tree.Node stNode = source;
        while (stNode != null) {
            if (stNode instanceof StatementNode) {
                node.lineRange = stNode.lineRange();
                if (stmtNodeStack.peek() != stNode) {
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
            if (symbol.kind() == SymbolKind.VARIABLE) {
                VariableSymbol variableSymbol = (VariableSymbol) symbol;
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