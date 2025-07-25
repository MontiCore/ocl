package de.monticore.expressions.commonexpressions;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsHandler;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsTraverser;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsVisitor2;
import de.monticore.refadaptation.AbstractAdaptationHandler;
import de.monticore.refadaptation.Binding;
import de.monticore.refadaptation.BindingConflictException;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symboltable.ISymbol;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CommonExpressionsBindingVariantsVisitor
        extends AbstractAdaptationHandler<CommonExpressionsAdaptationContext, CommonExpressionsAdaptationVariant>
        implements CommonExpressionsVisitor2, CommonExpressionsHandler {

  private static final String LOG_NAME = CommonExpressionsBindingVariantsVisitor.class.getName();

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
  public void handle(ASTNotEqualsExpression node) {
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
  public void handle(ASTLogicalNotExpression node) {
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

  @Override
  public void handle(ASTBracketExpression node) {
    getAdaptations4Ast().clearVariants(node);
    CommonExpressionsHandler.super.handle(node);
  }

  // TODO other handle methods

  @Override
  public void endVisit(ASTFieldAccessExpression refExpr) {
    /*
     * TODO Write the same logic for MethodSymbol/FunctionSymbol
     *  -> next: maybe we can refactor this to a common helper method for introducing
     *     variants for each incarnation of some symbol?
     */

    // 2. Get the source symbol for the field name
    Optional<ISymbol> sourceSymbolOpt = TypeCheck3.typeOf(refExpr).getSourceInfo().getSourceSymbol();
    if (sourceSymbolOpt.isPresent()) {
      ISymbol sourceSymbol = sourceSymbolOpt.get();
      System.out.println("FieldAccessExpression Variable Source symbol: " + sourceSymbol);
      System.out.println("symbol full name: " + sourceSymbol.getFullName());
      // 3. identify variants depending on the symbol kind
      if (sourceSymbol instanceof VariableSymbol) {
        addVariantsForEachVariableIncarnation(refExpr, (VariableSymbol) sourceSymbol);
      } else if (sourceSymbol instanceof FunctionSymbol) {
        /*
         * This is required as FieldAccessExpressions are also used to represent method calls as
         * part of a CallExpression.
         */
        addVariantsForEachFunctionIncarnation(refExpr, (FunctionSymbol) sourceSymbol);
      }
    } else {
      passChildVariantsUpwards(refExpr, refExpr.getExpression());
    }
  }

  protected void addVariantsForEachVariableIncarnation(ASTFieldAccessExpression refExpr, VariableSymbol refVariableSymbol) {
    // 1. get all variants of the parent expression
    List<CommonExpressionsAdaptationVariant> parentVariants = getAdaptations4Ast().getVariants(refExpr.getExpression());
    // 2. for each variant we can now check the available FieldSymbols incarnations
    for (CommonExpressionsAdaptationVariant parentVariant : parentVariants) {
      Set<VariableSymbol> incarnations = getAdaptationContext().getBasicSymbolsIncMapping().getIncarnations(refVariableSymbol);
      if (incarnations.isEmpty()) {
        // no field symbol, use the constraints from the parent expression
        getAdaptations4Ast().addVariant(refExpr, parentVariant);
        continue;
      }
      // we have the incarnations which are possible in this context
      for (VariableSymbol fieldIncarnation : incarnations) {
        CommonExpressionsAdaptationVariant newVariant = parentVariant.copy();
        try {
          newVariant.getBasicSymbolsBindings().addVariableBinding(Binding.createStrict(refVariableSymbol, fieldIncarnation));
        } catch (BindingConflictException e) {
          // This is unexpected as the current adaptation context should only return incarnations
          // that are valid in the current context, i.e., no conflicts with existing bindings.
          Log.warn("getIncarnations returned incarnation that conflicts with existing binding: "
                  + fieldIncarnation.getFullName() + " in " + refExpr.get_SourcePositionStart(), e);
          continue;
        }
        // TODO add implied bindings from original incarnation mapping
        getAdaptations4Ast().addVariant(refExpr, newVariant);
      }
    }
  }

  protected void addVariantsForEachFunctionIncarnation(ASTFieldAccessExpression refExpr, FunctionSymbol oclRefFunctionSymbol) {
    // TODO Decide / discuss where we need to do this translation from variable symbols in OCL scope to CD4C symbols
    Optional<FunctionSymbol> cd4cTranslatedSymbolOpt = CD4CodeMill.globalScope().resolveFunction(oclRefFunctionSymbol.getFullName());
    if (cd4cTranslatedSymbolOpt.isEmpty()) {
      Log.info("Could not resolve FunctionSymbol: " + oclRefFunctionSymbol.getFullName() + " in " + refExpr.get_SourcePositionStart(), LOG_NAME);
      // TODO Better have a "global" fallback in the handle in case no variant was published by ay visitor?
      passChildVariantsUpwards(refExpr, refExpr.getExpression());
      return;
    }
    // 1. get all variants of the parent expression
    List<CommonExpressionsAdaptationVariant> parentVariants = getAdaptations4Ast().getVariants(refExpr.getExpression());
    // 2. for each variant we can now check the available FieldSymbols incarnations
    for (CommonExpressionsAdaptationVariant parentVariant : parentVariants) {
      Set<FunctionSymbol> incarnations = getAdaptationContext().getBasicSymbolsIncMapping().getIncarnations(cd4cTranslatedSymbolOpt.get());
      if (incarnations.isEmpty()) {
        // no function symbol, use the constraints from the parent expression
        // TODO pass the parent variant upwards vs. error. vs. no variant?
        getAdaptations4Ast().addVariant(refExpr, parentVariant);
        continue;
      }
      // we have the incarnations which are possible in this context
      for (FunctionSymbol incarnation : incarnations) {
        CommonExpressionsAdaptationVariant newVariant = parentVariant.copy();
        try {
          newVariant.getBasicSymbolsBindings().addFunctionBinding(Binding.createStrict(oclRefFunctionSymbol, incarnation));
        } catch (BindingConflictException e) {
          // This is unexpected as the current adaptation context should only return incarnations
          // that are valid in the current context, i.e., no conflicts with existing bindings.
          Log.warn("getIncarnations returned incarnation that conflicts with existing binding: "
                  + incarnation.getFullName() + " in " + refExpr.get_SourcePositionStart(), e);
          continue;
        }
        // TODO add implied bindings from original incarnation mapping
        getAdaptations4Ast().addVariant(refExpr, newVariant);
      }
    }
  }

  @Override
  public void traverse(ASTEqualsExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTNotEqualsExpression node) {
    traverseForConsistentVariants(node, node.getLeft(), node.getRight());
  }

  @Override
  public void traverse(ASTLessThanExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTLessEqualExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTGreaterThanExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTGreaterEqualExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTBooleanAndOpExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTBooleanOrOpExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTPlusExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTMinusExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTMultExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTDivideExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTModuloExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTCallExpression callExpr) {
    traverseForConsistentVariants(callExpr, callExpr.getExpression(), callExpr.getArguments());
  }

  @Override
  public void endVisit(ASTBooleanNotExpression expr) {
    passChildVariantsUpwards(expr, expr.getExpression());
  }

  @Override
  public void endVisit(ASTLogicalNotExpression expr) {
    passChildVariantsUpwards(expr, expr.getExpression());
  }

  @Override
  public void endVisit(ASTBracketExpression expr) {
    passChildVariantsUpwards(expr, expr.getExpression());
  }
}
