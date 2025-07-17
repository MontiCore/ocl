package de.monticore.expressions.commonexpressions;

import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsHandler;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsInheritanceHandler;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsTraverser;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsVisitor2;
import de.monticore.refadaptation.AbstractAdaptationHandler;
import de.monticore.refadaptation.Binding;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.TypeCheck3;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CommonExpressionsBindingVariantsVisitor
        extends AbstractAdaptationHandler<CommonExpressionsAdaptationContext, CommonExpressionsAdaptationVariant>
        implements CommonExpressionsVisitor2, CommonExpressionsHandler {

  private CommonExpressionsTraverser traverser;

  @Override
  public CommonExpressionsTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(CommonExpressionsTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void handle(ASTEqualsExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTLessThanExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTLessEqualExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTGreaterThanExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTGreaterEqualExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTBooleanAndOpExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTBooleanOrOpExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTBooleanNotExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTPlusExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMinusExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMultExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTDivideExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTModuloExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTFieldAccessExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  @Override
  public void handle(ASTCallExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  // TODO other handle methods

  @Override
  public void endVisit(ASTFieldAccessExpression refExpr) {
    SymTypeExpression expressionType = TypeCheck3.typeOf(refExpr);
    Optional<VariableSymbol> sourceSymbolOpt = expressionType.getSourceInfo().getSourceSymbol()
            .filter(s -> s instanceof VariableSymbol)
            .map(s -> (VariableSymbol) s);
    if (sourceSymbolOpt.isPresent()) {
      VariableSymbol sourceSymbol = sourceSymbolOpt.get();
      System.out.println("FieldAccessExpression Variable Source symbol: " + sourceSymbol);
      System.out.println("symbol full name: " + sourceSymbol.getFullName());
    }

    /*
     * TODO Write the same logic for MethodSymbol/FunctionSymbol
     *  -> next: maybe we can refactor this to a common helper method for introducing
     *     variants for each incarnation of some symbol?
     */

    /*
     * 2. get all variants of the parent expression
     */
    List<CommonExpressionsAdaptationVariant> parentVariants = getAdaptations4Ast().getVariants(refExpr.getExpression());

    // 2. for each variant we can now check the available FieldSymbols incarnations
    for (CommonExpressionsAdaptationVariant parentVariant : parentVariants) {
      // 2. if we have a field symbol, get all incarnations and create variants for it
      if (sourceSymbolOpt.isPresent()) {
        VariableSymbol refFieldSymbol = sourceSymbolOpt.get();
        Set<VariableSymbol> incarnations = getAdaptationContext().getBasicSymbolsIncMapping().getIncarnations(refFieldSymbol);
        if (incarnations.isEmpty()) {
          // no field symbol, use the constraints from the parent expression
          getAdaptations4Ast().addVariant(refExpr, parentVariant);
          continue;
        }
        // we have the incarnations which are possible in this context
        for (VariableSymbol fieldIncarnation : incarnations) {
          CommonExpressionsAdaptationVariant newVariant = parentVariant.copy();
          newVariant.getBasicSymbolsBindings().addVariableBinding(Binding.createStrict(refFieldSymbol, fieldIncarnation));
          getAdaptations4Ast().addVariant(refExpr, newVariant);
        }
      } else {
        // no field symbol, just pass the variants upwards
        getAdaptations4Ast().addVariant(refExpr, parentVariant);
      }
    }
  }

  @Override
  public void traverse(ASTEqualsExpression expr) {
    // TODO maybe introduce helper method to make this even shorter and more readable
    getAdaptations4Ast().addVariants(expr, traverseAndPropagateConstraints(expr.getLeft(), expr.getRight()));
  }

  @Override
  public void traverse(ASTLessThanExpression expr) {
    getAdaptations4Ast().addVariants(expr, traverseAndPropagateConstraints(expr.getLeft(), expr.getRight()));
  }

  @Override
  public void traverse(ASTLessEqualExpression expr) {
    getAdaptations4Ast().addVariants(expr, traverseAndPropagateConstraints(expr.getLeft(), expr.getRight()));
  }

  @Override
  public void traverse(ASTGreaterThanExpression expr) {
    getAdaptations4Ast().addVariants(expr, traverseAndPropagateConstraints(expr.getLeft(), expr.getRight()));
  }

  @Override
  public void traverse(ASTGreaterEqualExpression expr) {
    getAdaptations4Ast().addVariants(expr, traverseAndPropagateConstraints(expr.getLeft(), expr.getRight()));
  }

  @Override
  public void traverse(ASTBooleanAndOpExpression expr) {
    getAdaptations4Ast().addVariants(expr, traverseAndPropagateConstraints(expr.getLeft(), expr.getRight()));
  }

  @Override
  public void traverse(ASTBooleanOrOpExpression expr) {
    getAdaptations4Ast().addVariants(expr, traverseAndPropagateConstraints(expr.getLeft(), expr.getRight()));
  }

  @Override
  public void traverse(ASTPlusExpression expr) {
    getAdaptations4Ast().addVariants(expr, traverseAndPropagateConstraints(expr.getLeft(), expr.getRight()));
  }

  @Override
  public void traverse(ASTMinusExpression expr) {
    getAdaptations4Ast().addVariants(expr, traverseAndPropagateConstraints(expr.getLeft(), expr.getRight()));
  }

  @Override
  public void traverse(ASTMultExpression expr) {
    getAdaptations4Ast().addVariants(expr, traverseAndPropagateConstraints(expr.getLeft(), expr.getRight()));
  }

  @Override
  public void traverse(ASTDivideExpression expr) {
    getAdaptations4Ast().addVariants(expr, traverseAndPropagateConstraints(expr.getLeft(), expr.getRight()));
  }

  @Override
  public void traverse(ASTModuloExpression expr) {
    getAdaptations4Ast().addVariants(expr, traverseAndPropagateConstraints(expr.getLeft(), expr.getRight()));
  }

  @Override
  public void endVisit(ASTBooleanNotExpression expr) {
    passChildVariantsUpwards(expr, expr.getExpression());
  }

  @Override
  public void traverse(ASTCallExpression callExpr) {
    getAdaptations4Ast().addVariants(callExpr,
            traverseAndPropagateConstraints(callExpr.getExpression(), callExpr.getArguments()));
  }
}
