package de.monticore.ocl.codegen.javagen;

import de.monticore.codegen.AbstractCodeGenVisitor;
import de.monticore.codegen.javagen.JavaGenSymTypeExpressionConverter;
import de.monticore.codegen.javagen.SymTypeExpression2JavaConverter;
import de.monticore.expressions.bitexpressions.codegen.javagen.BitExpressionsJavaGenVisitor;
import de.monticore.expressions.commonexpressions.codegen.javagen.CommonExpressionsJavaGenVisitor;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.expressions.expressionsbasis.codegen.javagen.ExpressionsBasisJavaGenVisitor;
import de.monticore.literals.mccommonliterals.codegen.javagen.MCCommonLiteralsJavaGenVisitor;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.symbols.basicsymbols._symboltable.IBasicSymbolsScope;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols.OOSymbolsMill;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.visitor.ITraverser;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

public class OCL2JavaCodeGenerator extends AbstractCodeGenVisitor {
  OCLTraverser traverser;
  protected String fullPackage;

  public OCL2JavaCodeGenerator(IndentPrinter printer) {
    super(printer);
    init();
  }

  public OCL2JavaCodeGenerator(IndentPrinter printer, String fullPackage) {
    super(printer);
    this.fullPackage = fullPackage;
    init();
  }

  public void init() {
    traverser = OCLMill.traverser();
    Map<String, Stack<SymTypeExpression>> knowTypeOfVar = new HashMap<>();

    JavaGenSymTypeExpressionConverter.init();
    OCLSymTypeExpression2JavaConverter.init();
    OCL2JavaOperationPrinter.init();

    // Literals

    MCCommonLiteralsJavaGenVisitor visMCCommonLiterals = new MCCommonLiteralsJavaGenVisitor(getPrinter());
    traverser.setMCCommonLiteralsHandler(visMCCommonLiterals);

    // Expressions

    BitExpressionsJavaGenVisitor visBitExpressions = new BitExpressionsJavaGenVisitor(getPrinter());
    traverser.setBitExpressionsHandler(visBitExpressions);

    CommonExpressionsJavaGenVisitor visCommonExpressions = new CommonExpressionsOCLJavaGenVisitor(getPrinter());
    traverser.setCommonExpressionsHandler(visCommonExpressions);

    ExpressionsBasisJavaGenVisitor visExpressionBasis = new ExpressionsBasisJavaGenVisitor(getPrinter()) {
      @Override
      public void handle(ASTNameExpression node) {
        Optional<VariableSymbol> symbol = ((IBasicSymbolsScope) node.getEnclosingScope()).resolveVariableMany(node.getName()).stream().findAny();
        Optional<TypeSymbol> type = ((IBasicSymbolsScope) node.getEnclosingScope()).resolveTypeMany(node.getName()).stream().findAny();
        if (symbol.isPresent() && OOSymbolsMill.typeDispatcher().isOOSymbolsField(symbol.get()) && ((FieldSymbol) symbol.get()).isIsStatic()) {
          getPrinter().print(symbol.get().getFullName());
        } else if (type.isPresent() && OOSymbolsMill.typeDispatcher().isOOSymbolsOOType(type.get())) {
          getPrinter().print(type.get().getFullName());
        } else {
          // Handle scoped casts, e.g. from type if expression
          if (!knowTypeOfVar.getOrDefault(node.getName(), new Stack<>()).isEmpty()) {
            getPrinter().print("((");
            getPrinter().print(SymTypeExpression2JavaConverter.printModelTypeAsJavaType(knowTypeOfVar.get(node.getName()).peek()));
            getPrinter().print(")");
            getPrinter().print(node.getName());
            getPrinter().print(")");
          } else {
            if ("this".equals(node.getName())) {
              getPrinter().print("_this");
            } else {
              getPrinter().print(node.getName());
            }
          }
        }
      }
    };
    traverser.setExpressionsBasisHandler(visExpressionBasis);

    OCLExpressionsJavaGenVisitor visOCLExpressions = new OCLExpressionsJavaGenVisitor(getPrinter(), knowTypeOfVar);
    traverser.setOCLExpressionsHandler(visOCLExpressions);

    OptionalOperatorsJavaGenVisitor visOptionalOperators = new OptionalOperatorsJavaGenVisitor(getPrinter());
    traverser.setOptionalOperatorsHandler(visOptionalOperators);

    SetExpressionsJavaGenVisitor visSetExpressions = new SetExpressionsJavaGenVisitor(getPrinter());
    traverser.setSetExpressionsHandler(visSetExpressions);

    // OCL
    OCLJavaGenVisitor oclJavaGenVisitor = new OCLJavaGenVisitor(getPrinter(), fullPackage);
    traverser.setOCLHandler(oclJavaGenVisitor);
  }

  @Override
  public ITraverser getTraverser() {
    return traverser;
  }
}
