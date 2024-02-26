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
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.ResourceMethodSymbol;
import io.ballerina.compiler.api.symbols.ServiceDeclarationSymbol;
import io.ballerina.compiler.api.symbols.Symbol;
import io.ballerina.compiler.api.symbols.SymbolKind;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.VariableSymbol;
import io.ballerina.compiler.syntax.tree.AssignmentStatementNode;
import io.ballerina.compiler.syntax.tree.ClientResourceAccessActionNode;
import io.ballerina.compiler.syntax.tree.ExpressionNode;
import io.ballerina.compiler.syntax.tree.ExpressionStatementNode;
import io.ballerina.compiler.syntax.tree.FunctionArgumentNode;
import io.ballerina.compiler.syntax.tree.FunctionCallExpressionNode;
import io.ballerina.compiler.syntax.tree.MethodCallExpressionNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.RemoteMethodCallActionNode;
import io.ballerina.compiler.syntax.tree.RestArgumentNode;
import io.ballerina.compiler.syntax.tree.SeparatedNodeList;
import io.ballerina.compiler.syntax.tree.VariableDeclarationNode;

import java.util.Optional;
import java.util.Stack;

public class FormBuilder {

    private final ModelBuilder modelBuilder;
    private final SemanticModel semanticModel;
    private final Stack<SymbolEntry> symbolStack = new Stack<>();

    FormBuilder(ModelBuilder modelBuilder, SemanticModel semanticModel) {
        this.modelBuilder = modelBuilder;
        this.semanticModel = semanticModel;
    }

    void addSymbolEntry(io.ballerina.compiler.syntax.tree.Node node) {
        SymbolEntry symbolEntry = new SymbolEntry(node, semanticModel.symbol(node).orElse(null));
        symbolStack.push(symbolEntry);
    }

    void removeSymbolEntry(io.ballerina.compiler.syntax.tree.Node node) {
        if (symbolStack.isEmpty() || symbolStack.peek().node() != node) {
            throw new IllegalStateException("Invalid symbol stack operation");
        }
        symbolStack.pop();
    }

    void handleFunction(FunctionSymbol symbol) {
        FormData formData = new FormData("Name");
        formData.setTypeKind(FormData.FormDataTypeKind.IDENTIFIER);
        formData.setValue(symbol.getName().orElse("Unknown"));
        modelBuilder.addFormData("Function", formData);
    }

    void handleResourceFunction(ResourceMethodSymbol symbol) {
        FormData methodData = new FormData("Method");
        methodData.setTypeKind(FormData.FormDataTypeKind.IDENTIFIER);
        methodData.setValue(symbol.getName().orElse("Unknown Method"));
        modelBuilder.addFormData("Network", methodData);

        FormData pathData = new FormData("Path");
        pathData.setTypeKind(FormData.FormDataTypeKind.STRING);
        pathData.setValue(symbol.resourcePath().signature());
        modelBuilder.addFormData("Network", pathData);

        handleFunction(symbol);
    }

    void handleExpression(ExpressionNode stNode, String label, FormData.FormDataTypeKind typeKind) {
        FormData formData = new FormData(label);
        formData.setTypeKind(typeKind);
        formData.setValue(stNode.toSourceCode());
        semanticModel.symbol(stNode).ifPresent(symbol -> {
            if (symbol instanceof TypeSymbol typeSymbol) {
                formData.addAllowedTypes(typeSymbol.signature());
            }
        });
        modelBuilder.addFormData("Expressions", formData);
    }

    void handleFailOnError() {
        FormData formData = new FormData("FailOnError");
        formData.setTypeKind(FormData.FormDataTypeKind.BOOLEAN);
        formData.setValue("true");
        modelBuilder.addFormData("Error", formData);
    }

    void handleFunctionCall(FunctionCallExpressionNode stNode) {
        var symbol = semanticModel.symbol(stNode);
        if (symbol.isPresent() && symbol.get() instanceof FunctionSymbol functionSymbol) {
            extractFunctionSymbolFormData(functionSymbol);
        }
        handleFunctionArguments(stNode.arguments());
    }

    void handleMethodCall(MethodCallExpressionNode stNode) {
        var symbol = semanticModel.symbol(stNode);
        if (symbol.isPresent() && symbol.get() instanceof MethodSymbol functionSymbol) {
            extractFunctionSymbolFormData(functionSymbol);
        }
        handleFunctionArguments(stNode.arguments());
    }

    void handleRemoteCall(RemoteMethodCallActionNode stNode) {
        var symbol = semanticModel.symbol(stNode);
        if (symbol.isPresent() && symbol.get() instanceof MethodSymbol functionSymbol) {
            extractFunctionSymbolFormData(functionSymbol);
        }
        handleFunctionArguments(stNode.arguments());
    }

    void handleClientCall(ClientResourceAccessActionNode stNode) {
        var symbol = semanticModel.symbol(stNode);
        if (symbol.isPresent() && symbol.get() instanceof MethodSymbol functionSymbol) {
            extractFunctionSymbolFormData(functionSymbol);
        }
        stNode.arguments().ifPresent(args -> handleFunctionArguments(args.arguments()));
    }

    private void handleFunctionArguments(SeparatedNodeList<FunctionArgumentNode> args) {
        for (FunctionArgumentNode arg : args) {
            if (arg instanceof PositionalArgumentNode posArg) {
                handleExpression(posArg.expression(), "Arg", FormData.FormDataTypeKind.IDENTIFIER);
            } else if (arg instanceof NamedArgumentNode nameArg) {
                handleExpression(nameArg.expression(), "Named Arg", FormData.FormDataTypeKind.IDENTIFIER);
            } else if (arg instanceof RestArgumentNode restArg) {
                handleExpression(restArg.expression(), "Rest Arg", FormData.FormDataTypeKind.IDENTIFIER);
            }
        }
    }

    void extractServiceDeclarationFormData() {

        for (SymbolEntry entry : symbolStack) {
            if (entry.symbol() instanceof ServiceDeclarationSymbol serviceSymbol) {
                serviceSymbol.attachPoint().ifPresent(attachPoint -> {
                    FormData formData = new FormData("BasePath");
                    if (attachPoint instanceof AbsResourcePathAttachPoint attachPointSymbol) {
                        formData.setTypeKind(FormData.FormDataTypeKind.DEFAULT);
                        formData.setValue(attachPointSymbol.segments().stream().reduce("", (s1, s2) -> s1 + "/" + s2));
                        formData.setEditable(false);
                    } else if (attachPoint instanceof LiteralAttachPoint literalAttachPoint) {
                        formData.setTypeKind(FormData.FormDataTypeKind.STRING);
                        formData.setValue(literalAttachPoint.literal());
                        formData.setEditable(false);
                    }
                    modelBuilder.addFormData("Network", formData);
                });
            }
        }
        // TODO : Improve this further.
    }

    void handleVariableDeclarationFormData(VariableDeclarationNode stNode) {
        Optional<Symbol> symbol = semanticModel.symbol(stNode.typedBindingPattern().bindingPattern());
        if (symbol.isPresent()) {
            if (symbol.get().kind() == SymbolKind.VARIABLE) {
                VariableSymbol variableSymbol = (VariableSymbol) symbol.get();
                FormData formData = new FormData("New Variable");
                formData.setTypeKind(getFormDataType(variableSymbol.typeDescriptor()));
                formData.addAllowedTypes(variableSymbol.typeDescriptor().signature());
                formData.setValue(variableSymbol.getName().orElse("Unknown Variable"));
                formData.addFlags("Local");
                formData.addFlags("New");
                modelBuilder.addFormData("Variables", formData);
            }
        }
    }

    void handleAssignmentStatementFormData(AssignmentStatementNode stNode) {
        semanticModel.symbol(stNode.varRef()).ifPresent(symbol -> {
            if (symbol instanceof VariableSymbol variableSymbol) {
                FormData formData = new FormData("Updated Variable");
                formData.setTypeKind(getFormDataType(variableSymbol.typeDescriptor()));
                formData.addAllowedTypes(variableSymbol.typeDescriptor().signature());
                formData.setValue(variableSymbol.getName().orElse("Unknown Variable"));
                formData.addFlags("Update");
                modelBuilder.addFormData("Variables", formData);
            }
        });
    }

    void handleExpressionStatementFromData(ExpressionStatementNode stNode) {

    }

    void extractFunctionSymbolFormData(FunctionSymbol functionSymbol) {
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

    record SymbolEntry(io.ballerina.compiler.syntax.tree.Node node, Symbol symbol) {
    }
}
