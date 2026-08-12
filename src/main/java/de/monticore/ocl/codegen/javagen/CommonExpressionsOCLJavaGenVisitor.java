package de.monticore.ocl.codegen.javagen;


import de.monticore.codegen.javagen.SymTypeExpression2JavaConverter;
import de.monticore.expressions.commonexpressions._ast.ASTCallExpression;
import de.monticore.expressions.commonexpressions._ast.ASTFieldAccessExpression;
import de.monticore.expressions.commonexpressions.codegen.javagen.CommonExpressionsJavaGenVisitor;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.codegen.DomainTypeUtil;
import de.monticore.ocl.codegen.javagen.field_access.FieldAccessInfo;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.types3.OCLCollectionSymTypeRelations;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symboltable.ISymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mccollectiontypes.types3.MCCollectionSymTypeRelations;
import de.monticore.types3.SymTypeRelations;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.StringTransformations;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;

public class CommonExpressionsOCLJavaGenVisitor extends CommonExpressionsJavaGenVisitor {
  public CommonExpressionsOCLJavaGenVisitor(IndentPrinter printer) {
    super(printer);
  }

  @Override
  public void handle(ASTCallExpression node) {
    boolean isFieldAccess = OCLMill.typeDispatcher().isCommonExpressionsASTFieldAccessExpression(node.getExpression());
    boolean handled = false;

    if (isFieldAccess) {
      ASTFieldAccessExpression fa = OCLMill.typeDispatcher().asCommonExpressionsASTFieldAccessExpression(node.getExpression());
      handled = handleTranslatedListCall(fa, node);
    }

    if (!handled) {
      startParentheses();
      node.getExpression().accept(getTraverser());
      startParentheses();
      boolean first = true;
      for (ASTExpression arg : node.getArguments().getExpressionList()) {
        if (!first) {
          getPrinter().print(", ");
        }
        arg.accept(getTraverser());
        first = false;
      }
      endParentheses();
      endParentheses();
    }
  }

  private boolean handleTranslatedListCall(ASTFieldAccessExpression fa, ASTCallExpression node) {
    return switch (fa.getName()) {
      case "asSet" -> {
        String setName = nextListOperationVariable();
        String itemName = setName + "_item";
        printCollectionSupplierStart(node, setName, "java.util.HashSet");
        getPrinter().print("for (Object ");
        getPrinter().print(itemName);
        getPrinter().print(" : ");
        printReceiver(fa);
        getPrinter().println(") {");
        getPrinter().indent();
        getPrinter().print(setName);
        getPrinter().print(".add((");
        printCollectionElementType(node);
        getPrinter().print(") ");
        getPrinter().print(itemName);
        getPrinter().println(");");
        getPrinter().unindent();
        getPrinter().println("}");
        printListSupplierEnd(setName);
        yield true;
      }
      case "asList" -> {
        String listName = nextListOperationVariable();
        printCollectionSupplierStart(node, listName, "java.util.ArrayList");
        getPrinter().print(listName);
        getPrinter().println(".addAll(");
        printReceiver(fa);
        getPrinter().println(");");
        printListSupplierEnd(listName);
        yield true;
      }
      case "first" -> {
        printReceiverInParentheses(fa);
        getPrinter().print(".get(0)");
        yield true;
      }
      case "last" -> {
        printReceiverInParentheses(fa);
        getPrinter().print(".get(");
        printReceiverInParentheses(fa);
        getPrinter().print(".size() - 1)");
        yield true;
      }
      case "rest" -> {
        String listName = nextListOperationVariable();
        printListSupplierStart(node, listName);
        getPrinter().print(listName);
        getPrinter().println(".addAll(");
        printReceiver(fa);
        getPrinter().println(");");
        getPrinter().print("if (!");
        getPrinter().print(listName);
        getPrinter().println(".isEmpty()) {");
        getPrinter().indent();
        getPrinter().print(listName);
        getPrinter().println(".remove(0);");
        getPrinter().unindent();
        getPrinter().println("}");
        printListSupplierEnd(listName);
        yield true;
      }
      case "count" -> {
        if (node.getArguments().getExpressionList().size() != 1) {
          yield false;
        }
        String countName = nextListOperationVariable();
        String itemName = countName + "_item";
        getPrinter().println("((java.util.function.Supplier<Integer>)()->{");
        getPrinter().indent();
        getPrinter().print("int ");
        getPrinter().print(countName);
        getPrinter().println(" = 0;");
        getPrinter().print("for (Object ");
        getPrinter().print(itemName);
        getPrinter().print(" : ");
        printReceiver(fa);
        getPrinter().println(") {");
        getPrinter().indent();
        getPrinter().print("if (java.util.Objects.equals(");
        getPrinter().print(itemName);
        getPrinter().print(", ");
        printArgument(node, 0);
        getPrinter().println(")) {");
        getPrinter().indent();
        getPrinter().print(countName);
        getPrinter().println("++;");
        getPrinter().unindent();
        getPrinter().println("}");
        getPrinter().unindent();
        getPrinter().println("}");
        getPrinter().print("return ");
        getPrinter().print(countName);
        getPrinter().println(";");
        getPrinter().unindent();
        getPrinter().print("}).get()");
        yield true;
      }
      case "removeAtIndex" -> {
        if (node.getArguments().getExpressionList().size() != 1) {
          yield false;
        }
        String listName = nextListOperationVariable();
        printListSupplierStart(node, listName);
        getPrinter().print(listName);
        getPrinter().println(".addAll(");
        printReceiver(fa);
        getPrinter().println(");");
        getPrinter().print(listName);
        getPrinter().print(".remove((int) ");
        printArgument(node, 0);
        getPrinter().println(");");
        printListSupplierEnd(listName);
        yield true;
      }
      case "flatten" -> {
        String listName = nextListOperationVariable();
        String itemName = listName + "_item";
        printListSupplierStart(node, listName);
        getPrinter().print("for (java.util.Collection<?> ");
        getPrinter().print(itemName);
        getPrinter().print(" : ");
        printReceiver(fa);
        getPrinter().println(") {");
        getPrinter().indent();
        getPrinter().print(listName);
        getPrinter().print(".addAll(");
        getPrinter().print(itemName);
        getPrinter().println(");");
        getPrinter().unindent();
        getPrinter().println("}");
        printListSupplierEnd(listName);
        yield true;
      }
      default -> false;
    };
  }

  private int listOperationVariableCounter = 0;

  private String nextListOperationVariable() {
    return "__oclListOp" + listOperationVariableCounter++;
  }

  private void printListSupplierStart(ASTCallExpression node, String variableName) {
    printCollectionSupplierStart(node, variableName, "java.util.ArrayList");
  }

  private void printCollectionSupplierStart(ASTCallExpression node, String variableName, String implementationType) {
    getPrinter().print("((java.util.function.Supplier<");
    getPrinter().print(SymTypeExpression2JavaConverter.printModelTypeAsJavaType(TypeCheck3.typeOf(node)));
    getPrinter().println(">)()->{");
    getPrinter().indent();
    getPrinter().print(SymTypeExpression2JavaConverter.printModelTypeAsJavaType(TypeCheck3.typeOf(node)));
    getPrinter().print(" ");
    getPrinter().print(variableName);
    getPrinter().print(" = new ");
    getPrinter().print(implementationType);
    getPrinter().println("<>();");
  }

  private void printListSupplierEnd(String listName) {
    getPrinter().print("return ");
    getPrinter().print(listName);
    getPrinter().println(";");
    getPrinter().unindent();
    getPrinter().print("}).get()");
  }

  private void printReceiver(ASTFieldAccessExpression fa) {
    fa.getExpression().accept(getTraverser());
  }

  private void printReceiverInParentheses(ASTFieldAccessExpression fa) {
    startParentheses();
    printReceiver(fa);
    endParentheses();
  }

  private void printArgument(ASTCallExpression node, int index) {
    node.getArguments().getExpression(index).accept(getTraverser());
  }

  private void printCollectionElementType(ASTCallExpression node) {
    SymTypeExpression collectionType = TypeCheck3.typeOf(node);
    if (collectionType.isGenericType() && !collectionType.asGenericType().getArgumentList().isEmpty()) {
      getPrinter().print(SymTypeExpression2JavaConverter.printModelTypeAsJavaType(
          collectionType.asGenericType().getArgument(0)));
    } else {
      getPrinter().print("Object");
    }
  }

  @Override
  public void handle(ASTFieldAccessExpression node) {
    // functions should not be replaced by getter name conversion
    if (TypeCheck3.typeOf(node).isFunctionType()) {
      node.getExpression().accept(getTraverser());
      printer.print(".");
      printer.print(node.getName());
      return;
    }

    // UMLP specific field access
    node.getExpression().accept(getTraverser());

    FieldAccessInfo info = FieldAccessInfo.from(node);

    SymTypeExpression lhsType = TypeCheck3.typeOf(node.getExpression());
    SymTypeExpression fullType = TypeCheck3.typeOf(node);
    if (OCLCollectionSymTypeRelations.isOCLCollection(lhsType) && OCLCollectionSymTypeRelations.isOCLCollection(fullType)) {
      // flattening

      // check if source type
      boolean isCollectionType = false;
      boolean isDomainType = false;
      boolean isBoolean = false;
      SymTypeExpression varType = null;
      Optional<ISymbol> sourceSymbolOpt = fullType.getSourceInfo().getSourceSymbol();
      if (sourceSymbolOpt.isEmpty()) {
        Log.warn("Can not convert field access since source symbol is not available", node.get_SourcePositionStart(), node.get_SourcePositionEnd());
        return;
      }
      ISymbol sourceSymbol = sourceSymbolOpt.get();
      if (BasicSymbolsMill.typeDispatcher().isBasicSymbolsVariable(sourceSymbol)) {
        VariableSymbol varSymbol = BasicSymbolsMill.typeDispatcher().asBasicSymbolsVariable(sourceSymbol);
        varType = varSymbol.getType();
        isCollectionType = MCCollectionSymTypeRelations.isMCCollection(varType) && !MCCollectionSymTypeRelations.isOptional(varType);
        isDomainType = DomainTypeUtil.getInstance().isDomainType(varType);
        isBoolean = SymTypeRelations.isBoolean(varType);
      }

      // - variable with simple type
      if (!isCollectionType && !isDomainType) {
        printFlatteningMap(node.getName(), isBoolean ? "is" : "get", lhsType);
      }

      // - variable with collection simple type
      if (isCollectionType && !isDomainType) {
        Log.warn("Not translating flattening for attribute list of simple type");
        return;
      }

      // - association with opt or one cardinality
      if (!isCollectionType && isDomainType) {
        // TODO: opt needs extra check
        printFlatteningMap(node.getName(), "get", lhsType);
      }

      // - association with opt mult cardinality
      if (isCollectionType && isDomainType) {
        printer.print(".stream().flatMap(it -> it.");
        printer.print("get");
        printer.print(StringTransformations.capitalize(node.getName()));
        printer.print("List().stream()).to");
        printCollectorFor(lhsType);
      }
    } else {
      // normal access
      printFieldAccess(info, node.getName());
    }
  }

  private void printFlatteningMap(String fieldName, String accessorPrefix, SymTypeExpression lhsType) {
    printer.print(".stream().map(it -> it.");
    printer.print(accessorPrefix);
    printer.print(StringTransformations.capitalize(fieldName));
    printer.print("()).to");
    printCollectorFor(lhsType);
  }

  private void printCollectorFor(SymTypeExpression lhsType) {
    if (OCLCollectionSymTypeRelations.isList(lhsType)) {
      printer.print("List()");
    } else if (OCLCollectionSymTypeRelations.isSet(lhsType)) {
      printer.print("Set()");
    } else {
      printer.print("Collection()");
    }
  }

  private void printFieldAccess(FieldAccessInfo info, String nodeName) {
    switch (info.getKind()) {
      case TO_ONE_DOMAIN ->
          printer.print(".get" + StringTransformations.capitalize(nodeName) + "()");
      case TO_MANY_DOMAIN, COLLECTION_OF_SIMPLE_VALUE ->
          printer.print(".get" + StringTransformations.capitalize(nodeName) + "List()");
      case SIMPLE_VALUE -> {
        if (SymTypeRelations.isBoolean(info.getFieldType())) {
          printer.print(".is" + StringTransformations.capitalize(nodeName) + "()");
        } else {
          printer.print(".get" + StringTransformations.capitalize(nodeName) + "()");
        }
      }
    }
  }
}
