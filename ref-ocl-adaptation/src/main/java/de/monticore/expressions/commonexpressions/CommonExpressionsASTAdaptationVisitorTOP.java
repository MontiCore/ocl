package de.monticore.expressions.commonexpressions;

import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsVisitor2;
import de.monticore.expressions.expressionsbasis._ast.ASTArguments;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.refadapt.AbstractAdaptationVisitor;
import de.monticore.refadapt.IASTAdaptation;
import de.monticore.refadapt.RefAdaptationUtils;

import java.util.List;
import java.util.Optional;

public class CommonExpressionsASTAdaptationVisitorTOP
        extends AbstractAdaptationVisitor<ICommonExpressionsAdaptationContext>
        implements CommonExpressionsVisitor2 {

  @Override
  public void endVisit(ASTEqualsExpression expr) {
    List<ICommonExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(expr);
    for (ICommonExpressionsAdaptationVariant variant : variants) {
      // 1. Default adaptation (links to adapted child nodes)
      ASTEqualsExpression adaptedNode = adapt(expr, variant);
      // 2. Apply AST adaptations registered specifically for this variant
      for (IASTAdaptation<ASTEqualsExpression> adaptationFun : variant.getASTAdaptations(expr)) {
        adaptedNode = adaptationFun.adapt(adaptedNode);
      }
      variant.setAdaptedNode(expr, adaptedNode);
    }
  }

  protected ASTEqualsExpression adapt(ASTEqualsExpression original, ICommonExpressionsAdaptationVariant variant) {
    ASTEqualsExpression adapted = CommonExpressionsMill.equalsExpressionBuilder().uncheckedBuild();
    Optional<ASTExpression> adaptedLeft = variant.getAdaptedNode(original.getLeft());
    adapted.setLeft(adaptedLeft.orElseGet(() -> original.getLeft().deepClone()));
    Optional<ASTExpression> adaptedRight = variant.getAdaptedNode(original.getRight());
    adapted.setRight(adaptedRight.orElseGet(() -> original.getRight().deepClone()));

    RefAdaptationUtils.deepCloneComments(original, adapted);
    return adapted;
  }

  @Override
  public void endVisit(ASTBooleanNotExpression expr) {
    List<ICommonExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(expr);
    for (ICommonExpressionsAdaptationVariant variant : variants) {
      // 1. Default adaptation (links to adapted child nodes)
      ASTBooleanNotExpression adaptedNode = adapt(expr, variant);
      // 2. Apply AST adaptations registered specifically for this variant
      for (IASTAdaptation<ASTBooleanNotExpression> adaptationFun : variant.getASTAdaptations(expr)) {
        adaptedNode = adaptationFun.adapt(adaptedNode);
      }
      variant.setAdaptedNode(expr, adaptedNode);
    }
  }

  protected ASTBooleanNotExpression adapt(ASTBooleanNotExpression original, ICommonExpressionsAdaptationVariant variant) {
    ASTBooleanNotExpression adapted = CommonExpressionsMill.booleanNotExpressionBuilder().uncheckedBuild();
    Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
    adapted.setExpression(adaptedExpression.orElseGet(() -> original.getExpression().deepClone()));
    RefAdaptationUtils.deepCloneComments(original, adapted);
    return adapted;
  }

  @Override
  public void endVisit(ASTLogicalNotExpression expr) {
    List<ICommonExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(expr);
    for (ICommonExpressionsAdaptationVariant variant : variants) {
      // 1. Default adaptation (links to adapted child nodes)
      ASTLogicalNotExpression adaptedNode = adapt(expr, variant);
      // 2. Apply AST adaptations registered specifically for this variant
      for (IASTAdaptation<ASTLogicalNotExpression> adaptationFun : variant.getASTAdaptations(expr)) {
        adaptedNode = adaptationFun.adapt(adaptedNode);
      }
      variant.setAdaptedNode(expr, adaptedNode);
    }
  }

  protected ASTLogicalNotExpression adapt(ASTLogicalNotExpression original, ICommonExpressionsAdaptationVariant variant) {
    ASTLogicalNotExpression adapted = CommonExpressionsMill.logicalNotExpressionBuilder().uncheckedBuild();
    Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
    adapted.setExpression(adaptedExpression.orElseGet(() -> original.getExpression().deepClone()));
    RefAdaptationUtils.deepCloneComments(original, adapted);
    return adapted;
  }

  @Override
  public void endVisit(ASTBracketExpression expr) {
    List<ICommonExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(expr);
    for (ICommonExpressionsAdaptationVariant variant : variants) {
      // 1. Default adaptation (links to adapted child nodes)
      ASTBracketExpression adaptedNode = adapt(expr, variant);
      // 2. Apply AST adaptations registered specifically for this variant
      for (IASTAdaptation<ASTBracketExpression> adaptationFun : variant.getASTAdaptations(expr)) {
        adaptedNode = adaptationFun.adapt(adaptedNode);
      }
      variant.setAdaptedNode(expr, adaptedNode);
    }
  }

  protected ASTBracketExpression adapt(ASTBracketExpression original, ICommonExpressionsAdaptationVariant variant) {
    ASTBracketExpression adapted = CommonExpressionsMill.bracketExpressionBuilder().uncheckedBuild();
    Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
    adapted.setExpression(adaptedExpression.orElseGet(() -> original.getExpression().deepClone()));

    RefAdaptationUtils.deepCloneComments(original, adapted);
    return adapted;
  }

  @Override
  public void endVisit(ASTFieldAccessExpression expr) {
    /*
     * Get all result variants that were found during traversal of the expression.
     * Each entry "AdaptationVariant" holds a consistent combination of all adapted
     * sub-nodes/expressions and the bindings that were used to adapt them.
     */
    List<ICommonExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(expr);
    for (ICommonExpressionsAdaptationVariant variant : variants) {
      // 1. Default adaptation (links to adapted child nodes)
      ASTFieldAccessExpression adaptedNode = adapt(expr, variant);
      // 2. Apply AST adaptations registered specifically for this variant
      for (IASTAdaptation<ASTFieldAccessExpression> adaptationFun : variant.getASTAdaptations(expr)) {
        adaptedNode = adaptationFun.adapt(adaptedNode);
      }
      variant.setAdaptedNode(expr, adaptedNode);
    }
  }

  protected ASTFieldAccessExpression adapt(ASTFieldAccessExpression original, ICommonExpressionsAdaptationVariant variant) {
    ASTFieldAccessExpression adapted = CommonExpressionsMill.fieldAccessExpressionBuilder().uncheckedBuild();
    Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
    adapted.setExpression(adaptedExpression.orElseGet(() -> original.getExpression().deepClone()));

    adapted.setName(original.getName());

    RefAdaptationUtils.deepCloneComments(original, adapted);
    return adapted;
  }

  @Override
  public void endVisit(ASTCallExpression expr) {
    List<ICommonExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(expr);
    for (ICommonExpressionsAdaptationVariant variant : variants) {
      // 1. Default adaptation (links to adapted child nodes)
      ASTCallExpression adaptedNode = adapt(expr, variant);
      // 2. Apply AST adaptations registered specifically for this variant
      for (IASTAdaptation<ASTCallExpression> adaptationFun : variant.getASTAdaptations(expr)) {
        adaptedNode = adaptationFun.adapt(adaptedNode);
      }
      variant.setAdaptedNode(expr, adaptedNode);
    }
  }

  protected ASTCallExpression adapt(ASTCallExpression original, ICommonExpressionsAdaptationVariant variant) {
    ASTCallExpression adapted = CommonExpressionsMill.callExpressionBuilder().uncheckedBuild();
    Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
    adapted.setExpression(adaptedExpression.orElseGet(() -> original.getExpression().deepClone()));
    Optional<ASTArguments> adaptedArguments = variant.getAdaptedNode(original.getArguments());
    adapted.setArguments(adaptedArguments.orElseGet(() -> original.getArguments().deepClone()));

    RefAdaptationUtils.deepCloneComments(original, adapted);
    return adapted;
  }

  // TODO Add endVisit/adapt for missing AST nodes
}
