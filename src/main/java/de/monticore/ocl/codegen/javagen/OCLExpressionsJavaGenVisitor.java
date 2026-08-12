// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.codegen.javagen;

import com.google.common.collect.Lists;
import de.monticore.codegen.javagen.AbstractJavaGenVisitor;
import de.monticore.codegen.javagen.SymTypeExpression2JavaConverter;
import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl.oclexpressions._symboltable.ICommonOCLExpressionsSymbol;
import de.monticore.ocl.oclexpressions._symboltable.IOCLExpressionsArtifactScope;
import de.monticore.ocl.oclexpressions._symboltable.IOCLExpressionsGlobalScope;
import de.monticore.ocl.oclexpressions._symboltable.IOCLExpressionsScope;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsHandler;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsTraverser;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.SymTypeRelations;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

import java.util.Map;
import java.util.Stack;

public class OCLExpressionsJavaGenVisitor extends AbstractJavaGenVisitor
    implements OCLExpressionsHandler {

  // Traverser
  protected OCLExpressionsTraverser traverser;
  protected Map<String, Stack<SymTypeExpression>> knownTypeOfVar;

  public OCLExpressionsJavaGenVisitor(IndentPrinter printer, Map<String, Stack<SymTypeExpression>> knownTypeOfVar) {
    super(printer);
    this.knownTypeOfVar = knownTypeOfVar;
  }

  @Override
  public OCLExpressionsTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(OCLExpressionsTraverser traverser) {
    this.traverser = traverser;
  }

  // CodeGen

  @Override
  public void handle(ASTImpliesExpression node) {
    getPrinter().print("!(");
    node.getLeft().accept(getTraverser());
    getPrinter().print(") || (");
    node.getRight().accept(getTraverser());
    getPrinter().print(")");
  }

  @Override
  public void handle(ASTLetinExpression node) {
    // TODO: dimensions dim of OCLVariableDeclaration!

    //  scope (non_exporting) LetinExpression implements Expression <100> =
    //    "let" (OCLVariableDeclaration || ";")+
    //    "in" Expression
    // OCLVariableDeclaration implements Variable = MCType? Name (dim:"[" "]")* ("=" Expression)?;

    // Will be transformed to something like
    // let a = 20 in a == 10
    // Is generated to
    // Boolean res = ((Function<Integer, Boolean>) a -> a == 10).apply(20);

    // Nesting could be handled like this.
    // By not using Function, Function2, etc., the limit on the number of variables is avoided.
    // let a = 20; b = 30 in a == 10 && b == 20
    // Is generated to
    // res = ((Function<Integer, Boolean>) a ->
    //     ((Function<Integer, Boolean>) b -> a == 10 && b == 2).apply(30)
    // ).apply(20);

    // Open lambdas
    for (ASTOCLVariableDeclaration decl : node.getOCLVariableDeclarationList()) {
      getPrinter().print("(");
      getPrinter().indent();
      getPrinter().print("(Function<");

      // variable type
      SymTypeExpression modelType;
      if (decl.isPresentMCType()) {
        modelType = TypeCheck3.symTypeFromAST(decl.getMCType());
      } else {
        modelType = TypeCheck3.typeOf(decl.getExpression());
      }
      getPrinter().print(SymTypeExpression2JavaConverter.printModelTypeAsJavaType(SymTypeRelations.box(modelType)));

      getPrinter().print(", Boolean>) ");
      getPrinter().print(decl.getName());
      getPrinter().println(" -> ");
      getPrinter().indent();
    }

    // Expression
    getPrinter().indent();
    node.getExpression().accept(getTraverser());
    getPrinter().println();
    getPrinter().unindent();

    // Close lambdas and apply parameters, last to first
    for (ASTOCLVariableDeclaration decl : Lists.reverse(node.getOCLVariableDeclarationList())) {
      getPrinter().unindent();
      getPrinter().print(").apply(");
      decl.getExpression().accept(getTraverser());
      getPrinter().print(")");
    }
  }

  @Override
  public void handle(ASTOCLVariableDeclaration node) {
    // throw new IllegalStateException("Can not directly handle variable declaration, since it is always dependent on context!");
  }

  @Override
  public void handle(ASTTypeIfExpression node) {
    // TypeIfExpression implements Expression <100> =
    //     "typeif" Name@Variable "instanceof" MCType
    //     "then"   thenExpression:TypeIfThenExpression
    //     "else"   elseExpression:Expression
    // ;

    getPrinter().print("(");
    getPrinter().print(node.getName());
    getPrinter().print(" instanceof ");

    SymTypeExpression modelType = TypeCheck3.symTypeFromAST(node.getMCType());
    getPrinter().print(SymTypeExpression2JavaConverter.printModelTypeAsJavaType(modelType));

    getPrinter().print(" ? ");

    knownTypeOfVar.computeIfAbsent(node.getName(), _ignored -> new Stack<>()).push(modelType);
    node.getThenExpression().accept(getTraverser());
    knownTypeOfVar.get(node.getName()).pop();

    getPrinter().print(" : ");
    node.getElseExpression().accept(getTraverser());
    getPrinter().print(")");
  }

  @Override
  public void handle(ASTEquivalentExpression node) {
    getPrinter().print("Objects.equals(");
    node.getLeft().accept(getTraverser());
    getPrinter().print(",");
    node.getRight().accept(getTraverser());
    getPrinter().print(")");
  }

  @Override
  public void handle(ASTAnyExpression node) {
    getPrinter().print("(");
    node.getExpression().accept(getTraverser());
    getPrinter().print(").get(0)");
    // TODO: choose element at random?
    //  Implementation is underspecified, so first element is valid
  }

  @Override
  public void handle(ASTIterateExpression node) {
    // scope (non_exporting) IterateExpression implements Expression <100> =
    //    "iterate" "{"
    //    iteration:InDeclaration ";"
    //    init:OCLVariableDeclaration ":"
    //    Name@Variable "=" value:Expression
    //    "}";

    //  Translated using
    //  Stream#reduce(U identity, BiFunction<U,? super T,U> accumulator, BinaryOperator<U> combiner)

    ASTInDeclaration inDecl = node.getIteration();
    ASTOCLVariableDeclaration init = node.getInit();

    SymTypeExpression accType;
    if (init.isPresentMCType()) {
      accType = TypeCheck3.symTypeFromAST(init.getMCType());
    } else {
      accType = TypeCheck3.typeOf(init.getExpression());
    }
    String javaAccType = SymTypeExpression2JavaConverter.printModelTypeAsJavaType(SymTypeRelations.box(accType));

    getPrinter().print("(");
    if (inDecl.isPresentExpression()) {
      inDecl.getExpression().accept(getTraverser());
    } else {
      Log.error("'iterate' without 'in' source expression is not supported.");
    }
    getPrinter().print(").stream().<" + javaAccType + ">reduce(");

    if (init.isPresentExpression()) {
      init.getExpression().accept(getTraverser());
    }
    getPrinter().print(", ");

    getPrinter().print("(" + init.getName() + ", ");
    getPrinter().print(inDecl.getInDeclarationVariableList().get(0).getName());
    getPrinter().print(") -> ");
    node.getValue().accept(getTraverser());
    getPrinter().print(", ");

    getPrinter().print("(" + init.getName() + "1, " + init.getName() + "2) -> { ");
    getPrinter().print("throw new UnsupportedOperationException(\"Parallel OCL iterate is not supported\"); }");

    getPrinter().print(")");
  }

  @Override
  public void handle(ASTInDeclaration node) {
    //  InDeclaration =
    //      MCType  (InDeclarationVariable || ",")+ // all elements of MCType
    //    | MCType? (InDeclarationVariable || ",")+ ("in" Expression)
    //    ;

    // Similar to OCLVariableDeclaration, this always depends on context
  }

  @Override
  public void handle(ASTForallExpression node) {
    //  scope (non_exporting) ForallExpression implements Expression <90> =
    //    "forall"
    //    (InDeclaration || ",")+
    //    ":"
    //    Expression
    //    ;

    // forall a in aList : a == 3
    // can be transformed to
    // List<Integer> aList = List.of(3,5);
    // aList.stream().allMatch(a -> a == 3);

    // Nested similar to LetInExpression
    // forall a in aList, b in bList : a == b
    // is generated to:
    // (aList).stream().allMatch(a ->
    //    (bList).stream().allMatch(b -> a == b)
    // )

    int openStreams = 0;

    for (ASTInDeclaration inDecl : node.getInDeclarationList()) {
      for (ASTInDeclarationVariable var : inDecl.getInDeclarationVariableList()) {
        getPrinter().print("(");

        if (inDecl.isPresentExpression()) {
          inDecl.getExpression().accept(getTraverser());
        } else {
          // TODO
          Log.error("'forall' without 'in' source expression is not supported.");
        }

        getPrinter().print(").stream().allMatch(");
        getPrinter().print(var.getName());
        getPrinter().println(" -> ");
        getPrinter().indent();

        openStreams++;
      }
    }

    node.getExpression().accept(getTraverser());

    for (int i = 0; i < openStreams; i++) {
      getPrinter().unindent();
      getPrinter().println();
      getPrinter().print(")");
    }
  }

  @Override
  public void handle(ASTExistsExpression node) {
    //  scope (non_exporting) ExistsExpression implements Expression <90> =
    //    "exists"
    //    (InDeclaration || ",")+
    //    ":"
    //    Expression
    //    ;

    // exists a in aList : a == 3
    // is generated to:
    // (aList).stream().anyMatch(a -> a == 3)

    // Nested: exists a in aList, b in bList : a == b
    // is generated to:
    // (aList).stream().anyMatch(a ->
    //    (bList).stream().anyMatch(b -> a == b)
    // )

    int openStreams = 0;

    for (ASTInDeclaration inDecl : node.getInDeclarationList()) {
      for (ASTInDeclarationVariable var : inDecl.getInDeclarationVariableList()) {
        getPrinter().print("(");

        if (inDecl.isPresentExpression()) {
          inDecl.getExpression().accept(getTraverser());
        } else {
          // TODO
          Log.error("'exists' without 'in' source expression is not supported.");
        }

        getPrinter().print(").stream().anyMatch(");
        getPrinter().print(var.getName());
        getPrinter().println(" -> ");
        getPrinter().indent();

        openStreams++;
      }
    }

    node.getExpression().accept(getTraverser());

    for (int i = 0; i < openStreams; i++) {
      getPrinter().unindent();
      getPrinter().println();
      getPrinter().print(")");
    }
  }

  @Override
  public void handle(ASTInDeclarationVariable node) {
    // Should not be called directly, as translation depends on context
  }

  @Override
  public void handle(ASTOCLAtPreQualification node) {
    Log.error("@pre is not yet supported", node.get_SourcePositionStart(), node.get_SourcePositionEnd());
  }

  @Override
  public void handle(ASTOCLTransitiveQualification node) {
    Log.error("The '**' operator is not yet supported", node.get_SourcePositionStart(), node.get_SourcePositionEnd());
  }

  @Override
  public void handle(ASTIfThenElseExpression node) {
    getPrinter().print("(");
    node.getCondition().accept(getTraverser());
    getPrinter().print(" ? ");
    node.getThenExpression().accept(getTraverser());
    getPrinter().print(" : ");
    node.getElseExpression().accept(getTraverser());
    getPrinter().print(")");
  }

  // Aliases and interfaces.
  // Added such that 'override method' can be used to check completeness of implementation

  @Override
  public void handle(ASTTypeIfThenExpression node) {
    // Alias for Expression
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTOCLExpressionsNode node) {
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ICommonOCLExpressionsSymbol node) {
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(IOCLExpressionsScope node) {
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(IOCLExpressionsArtifactScope node) {
    OCLExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(IOCLExpressionsGlobalScope node) {
    OCLExpressionsHandler.super.handle(node);
  }
}
