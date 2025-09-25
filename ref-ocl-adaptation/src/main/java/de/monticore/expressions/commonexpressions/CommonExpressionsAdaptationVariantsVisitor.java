package de.monticore.expressions.commonexpressions;

import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.refmodel.BindingConflictException;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symboltable.ISymbol;
import de.monticore.types3.TypeCheck3;

import java.util.*;

/**
 * Adaptation variant visitor for the CommonExpressions language.
 * <h5>Adaptations</h5>
 * <code>FieldAccessExpression</code>:
 * <ul>
 *   <li>One variant for each incarnation of the related VariableSymbol</li>
 *   <li>One variant for each incarnation of the related FunctionSymbol</li>
 * </ul>
 */
public class CommonExpressionsAdaptationVariantsVisitor
        extends CommonExpressionsAdaptationVariantsVisitorBase {

  @Override
  public void endVisit(ASTFieldAccessExpression refExpr) {
    // 1. Get the source symbol for the field name
    Optional<ISymbol> sourceSymbolOpt = TypeCheck3.typeOf(refExpr).getSourceInfo().getSourceSymbol();
    if (sourceSymbolOpt.isPresent()) {
      ISymbol sourceSymbol = sourceSymbolOpt.get();
      // 2. identify variants depending on the symbol kind
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

  /**
   * Introduces one variant for each incarnation of the given variable symbol (including field
   * symbols).
   *
   * @param refExpr the ASTFieldAccessExpression that references the variable
   * @param refVariableSymbol the VariableSymbol form the reference model
   */
  protected void addVariantsForEachVariableIncarnation(
          ASTFieldAccessExpression refExpr,
          VariableSymbol refVariableSymbol) {
    expandChildVariants(refExpr, refExpr.getExpression(), (childVariant) -> {
      Set<VariableSymbol> incarnations = getAdaptationContext().getBasicSymbolsIncMapping()
              .getIncarnations(refVariableSymbol);
      if (incarnations.isEmpty()) {
        // no field symbol, use the constraints from the child expression
        // TODO only pass variant upwards if the refSymbol is not defined in the inc mapping
        //  if it is defined, no incarnation is a sign that we should drop this variant
        return List.of(getAdaptationContext().createVariant());
      }
      return tryCreateVariantsForIncarnations(incarnations, (incarnation)
              -> createVariantForVariableIncarnation(refExpr, refVariableSymbol, incarnation));
    });
  }

  /**
   * Creates a new variant which adapts the FieldAccessExpression according to the given
   * incarnation of the variable symbol.
   *
   * @param refExpr the ASTFieldAccessExpression to adapt
   * @param refVariableSymbol the reference VariableSymbol referenced in the expression
   * @param incarnation the incarnation of the variable symbol to adapt to
   * @return a new CommonExpressionsAdaptationVariant that adapts the expression
   *
   * @throws BindingConflictException if the binding conflicts with existing bindings in the context
   */
  protected ICommonExpressionsAdaptationVariant createVariantForVariableIncarnation(
          ASTFieldAccessExpression refExpr,
          VariableSymbol refVariableSymbol,
          VariableSymbol incarnation) throws BindingConflictException {
    // 1. Create a variant for the incarnation
    ICommonExpressionsAdaptationVariant newVariant = getAdaptationContext()
            .createVariantForIncarnation(refVariableSymbol, incarnation,
                    refExpr.get_SourcePositionStart());
    // 2. Specify the AST Adaptation / transformation
    newVariant.addASTAdaptation(refExpr, adaptedNode -> {
      adaptedNode.setName(incarnation.getName());
      return adaptedNode;
    });
    return newVariant;
  }

  /**
   * Introduces one variant for each incarnation of the given function symbol (including method
   * symbols).
   *
   * @param refExpr the ASTFieldAccessExpression that references the function
   * @param refFunctionSymbol the FunctionSymbol from the reference model
   */
  protected void addVariantsForEachFunctionIncarnation(ASTFieldAccessExpression refExpr, FunctionSymbol refFunctionSymbol) {
    expandChildVariants(refExpr, refExpr.getExpression(), (childVariant) -> {
      Set<FunctionSymbol> incarnations = getAdaptationContext().getBasicSymbolsIncMapping().getIncarnations(refFunctionSymbol);
      if (incarnations.isEmpty()) {
        // no field symbol, use the constraints from the child expression
        // TODO only pass variant upwards if the refSymbol is not defined in the inc mapping
        //  if it is defined, no incarnation is a sign that we should drop this variant
        return List.of(getAdaptationContext().createVariant());
      }
      return tryCreateVariantsForIncarnations(incarnations, (incarnation)
              -> createVariantForFunctionIncarnation(refExpr, refFunctionSymbol, incarnation));
    });
  }

  protected ICommonExpressionsAdaptationVariant createVariantForFunctionIncarnation(
          ASTFieldAccessExpression refExpr,
          FunctionSymbol refFunctionSymbol,
          FunctionSymbol incarnation) throws BindingConflictException {
    // 1. Create a variant for the incarnation
    ICommonExpressionsAdaptationVariant newVariant = getAdaptationContext()
            .createVariantForIncarnation(refFunctionSymbol, incarnation,
                    refExpr.get_SourcePositionStart());
    // 2. Specify the AST Adaptation / transformation
    newVariant.addASTAdaptation(refExpr, adaptedNode -> {
      adaptedNode.setName(incarnation.getName());
      return adaptedNode;
    });
    return newVariant;
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
