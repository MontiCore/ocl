package de.monticore.ocl.oclexpressions;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsVisitor2;
import de.monticore.refadaptation.AbstractAdaptationVisitor;
import de.monticore.types.mcbasictypes._ast.ASTMCType;

import java.util.List;
import java.util.Optional;

import static de.monticore.refadaptation.RefAdaptationUtils.deepCloneComments;

public class OCLExpressionsASTAdaptationVisitor
        extends AbstractAdaptationVisitor<OCLExpressionsAdaptationContext>
        implements OCLExpressionsVisitor2 {

  @Override
  public void endVisit(ASTAnyExpression refAnyExpression) {
    List<OCLExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refAnyExpression);
    for (OCLExpressionsAdaptationVariant variant : variants) {
      ASTAnyExpression adaptedNode = adapt(refAnyExpression, variant);
      variant.setAdaptedNode(refAnyExpression, adaptedNode);
    }
  }

  protected ASTAnyExpression adapt(ASTAnyExpression original, OCLExpressionsAdaptationVariant variant) {
    ASTAnyExpression adapted = OCLExpressionsMill.anyExpressionBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
    adapted.setExpression(adaptedExpression.orElseGet(original.getExpression()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTInDeclaration refInDeclaration) {
    List<OCLExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refInDeclaration);
    for (OCLExpressionsAdaptationVariant variant : variants) {
      ASTInDeclaration adaptedNode = adapt(refInDeclaration, variant);
      variant.setAdaptedNode(refInDeclaration, adaptedNode);
    }
  }

  protected ASTInDeclaration adapt(ASTInDeclaration original, OCLExpressionsAdaptationVariant variant) {
    ASTInDeclaration adapted = OCLExpressionsMill.inDeclarationBuilder().uncheckedBuild();
    
    if (original.isPresentMCType()) {
      Optional<ASTMCType> adaptedType = variant.getAdaptedNode(original.getMCType());
      adapted.setMCType(adaptedType.orElseGet(original.getMCType()::deepClone));
    } else {
      adapted.setMCTypeAbsent();
    }
    
    for (ASTInDeclarationVariable variable : original.getInDeclarationVariableList()) {
      Optional<ASTInDeclarationVariable> adaptedVariable = variant.getAdaptedNode(variable);
      adapted.addInDeclarationVariable(adaptedVariable.orElseGet(variable::deepClone));
    }
    
    if (original.isPresentExpression()) {
      Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
      adapted.setExpression(adaptedExpression.orElseGet(original.getExpression()::deepClone));
    } else {
      adapted.setExpressionAbsent();
    }
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTLetinExpression refLetinExpression) {
    List<OCLExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refLetinExpression);
    for (OCLExpressionsAdaptationVariant variant : variants) {
      ASTLetinExpression adaptedNode = adapt(refLetinExpression, variant);
      variant.setAdaptedNode(refLetinExpression, adaptedNode);
    }
  }

  protected ASTLetinExpression adapt(ASTLetinExpression original, OCLExpressionsAdaptationVariant variant) {
    ASTLetinExpression adapted = OCLExpressionsMill.letinExpressionBuilder().uncheckedBuild();
    
    for (ASTOCLVariableDeclaration variableDecl : original.getOCLVariableDeclarationList()) {
      Optional<ASTOCLVariableDeclaration> adaptedVariableDecl = variant.getAdaptedNode(variableDecl);
      adapted.addOCLVariableDeclaration(adaptedVariableDecl.orElseGet(variableDecl::deepClone));
    }
    
    Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
    adapted.setExpression(adaptedExpression.orElseGet(original.getExpression()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTExistsExpression refExistsExpression) {
    List<OCLExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refExistsExpression);
    for (OCLExpressionsAdaptationVariant variant : variants) {
      ASTExistsExpression adaptedNode = adapt(refExistsExpression, variant);
      variant.setAdaptedNode(refExistsExpression, adaptedNode);
    }
  }

  protected ASTExistsExpression adapt(ASTExistsExpression original, OCLExpressionsAdaptationVariant variant) {
    ASTExistsExpression adapted = OCLExpressionsMill.existsExpressionBuilder().uncheckedBuild();
    
    for (ASTInDeclaration inDeclaration : original.getInDeclarationList()) {
      Optional<ASTInDeclaration> adaptedInDeclaration = variant.getAdaptedNode(inDeclaration);
      adapted.addInDeclaration(adaptedInDeclaration.orElseGet(inDeclaration::deepClone));
    }
    
    Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
    adapted.setExpression(adaptedExpression.orElseGet(original.getExpression()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTForallExpression refForallExpression) {
    List<OCLExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refForallExpression);
    for (OCLExpressionsAdaptationVariant variant : variants) {
      ASTForallExpression adaptedNode = adapt(refForallExpression, variant);
      variant.setAdaptedNode(refForallExpression, adaptedNode);
    }
  }

  protected ASTForallExpression adapt(ASTForallExpression original, OCLExpressionsAdaptationVariant variant) {
    ASTForallExpression adapted = OCLExpressionsMill.forallExpressionBuilder().uncheckedBuild();
    
    for (ASTInDeclaration inDeclaration : original.getInDeclarationList()) {
      Optional<ASTInDeclaration> adaptedInDeclaration = variant.getAdaptedNode(inDeclaration);
      adapted.addInDeclaration(adaptedInDeclaration.orElseGet(inDeclaration::deepClone));
    }
    
    Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
    adapted.setExpression(adaptedExpression.orElseGet(original.getExpression()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTTypeIfExpression refTypeIfExpression) {
    List<OCLExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refTypeIfExpression);
    for (OCLExpressionsAdaptationVariant variant : variants) {
      ASTTypeIfExpression adaptedNode = adapt(refTypeIfExpression, variant);
      variant.setAdaptedNode(refTypeIfExpression, adaptedNode);
    }
  }

  protected ASTTypeIfExpression adapt(ASTTypeIfExpression original, OCLExpressionsAdaptationVariant variant) {
    ASTTypeIfExpression adapted = OCLExpressionsMill.typeIfExpressionBuilder().uncheckedBuild();
    
    Optional<ASTMCType> adaptedType = variant.getAdaptedNode(original.getMCType());
    adapted.setMCType(adaptedType.orElseGet(original.getMCType()::deepClone));
    
    Optional<ASTTypeIfThenExpression> adaptedThenExpression = variant.getAdaptedNode(original.getThenExpression());
    adapted.setThenExpression(adaptedThenExpression.orElseGet(original.getThenExpression()::deepClone));
    
    Optional<ASTExpression> adaptedElseExpression = variant.getAdaptedNode(original.getElseExpression());
    adapted.setElseExpression(adaptedElseExpression.orElseGet(original.getElseExpression()::deepClone));
    
    adapted.setName(original.getName());
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTImpliesExpression refImpliesExpression) {
    List<OCLExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refImpliesExpression);
    for (OCLExpressionsAdaptationVariant variant : variants) {
      ASTImpliesExpression adaptedNode = adapt(refImpliesExpression, variant);
      variant.setAdaptedNode(refImpliesExpression, adaptedNode);
    }
  }

  protected ASTImpliesExpression adapt(ASTImpliesExpression original, OCLExpressionsAdaptationVariant variant) {
    ASTImpliesExpression adapted = OCLExpressionsMill.impliesExpressionBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedLeft = variant.getAdaptedNode(original.getLeft());
    adapted.setLeft(adaptedLeft.orElseGet(original.getLeft()::deepClone));
    
    Optional<ASTExpression> adaptedRight = variant.getAdaptedNode(original.getRight());
    adapted.setRight(adaptedRight.orElseGet(original.getRight()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTIterateExpression refIterateExpression) {
    List<OCLExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refIterateExpression);
    for (OCLExpressionsAdaptationVariant variant : variants) {
      ASTIterateExpression adaptedNode = adapt(refIterateExpression, variant);
      variant.setAdaptedNode(refIterateExpression, adaptedNode);
    }
  }

  protected ASTIterateExpression adapt(ASTIterateExpression original, OCLExpressionsAdaptationVariant variant) {
    ASTIterateExpression adapted = OCLExpressionsMill.iterateExpressionBuilder().uncheckedBuild();
    
    Optional<ASTInDeclaration> adaptedIteration = variant.getAdaptedNode(original.getIteration());
    adapted.setIteration(adaptedIteration.orElseGet(original.getIteration()::deepClone));
    
    Optional<ASTOCLVariableDeclaration> adaptedInit = variant.getAdaptedNode(original.getInit());
    adapted.setInit(adaptedInit.orElseGet(original.getInit()::deepClone));
    
    Optional<ASTExpression> adaptedValue = variant.getAdaptedNode(original.getValue());
    adapted.setValue(adaptedValue.orElseGet(original.getValue()::deepClone));
    
    adapted.setName(original.getName());
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTEquivalentExpression refEquivalentExpression) {
    List<OCLExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refEquivalentExpression);
    for (OCLExpressionsAdaptationVariant variant : variants) {
      ASTEquivalentExpression adaptedNode = adapt(refEquivalentExpression, variant);
      variant.setAdaptedNode(refEquivalentExpression, adaptedNode);
    }
  }

  protected ASTEquivalentExpression adapt(ASTEquivalentExpression original, OCLExpressionsAdaptationVariant variant) {
    ASTEquivalentExpression adapted = OCLExpressionsMill.equivalentExpressionBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedLeft = variant.getAdaptedNode(original.getLeft());
    adapted.setLeft(adaptedLeft.orElseGet(original.getLeft()::deepClone));
    
    Optional<ASTExpression> adaptedRight = variant.getAdaptedNode(original.getRight());
    adapted.setRight(adaptedRight.orElseGet(original.getRight()::deepClone));

    adapted.setOperator(original.getOperator());
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTIfThenElseExpression refIfThenElseExpression) {
    List<OCLExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refIfThenElseExpression);
    for (OCLExpressionsAdaptationVariant variant : variants) {
      ASTIfThenElseExpression adaptedNode = adapt(refIfThenElseExpression, variant);
      variant.setAdaptedNode(refIfThenElseExpression, adaptedNode);
    }
  }

  protected ASTIfThenElseExpression adapt(ASTIfThenElseExpression original, OCLExpressionsAdaptationVariant variant) {
    ASTIfThenElseExpression adapted = OCLExpressionsMill.ifThenElseExpressionBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedCondition = variant.getAdaptedNode(original.getCondition());
    adapted.setCondition(adaptedCondition.orElseGet(original.getCondition()::deepClone));
    
    Optional<ASTExpression> adaptedThenExpression = variant.getAdaptedNode(original.getThenExpression());
    adapted.setThenExpression(adaptedThenExpression.orElseGet(original.getThenExpression()::deepClone));
    
    Optional<ASTExpression> adaptedElseExpression = variant.getAdaptedNode(original.getElseExpression());
    adapted.setElseExpression(adaptedElseExpression.orElseGet(original.getElseExpression()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTTypeIfThenExpression refTypeIfThenExpression) {
    List<OCLExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refTypeIfThenExpression);
    for (OCLExpressionsAdaptationVariant variant : variants) {
      ASTTypeIfThenExpression adaptedNode = adapt(refTypeIfThenExpression, variant);
      variant.setAdaptedNode(refTypeIfThenExpression, adaptedNode);
    }
  }

  protected ASTTypeIfThenExpression adapt(ASTTypeIfThenExpression original, OCLExpressionsAdaptationVariant variant) {
    ASTTypeIfThenExpression adapted = OCLExpressionsMill.typeIfThenExpressionBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
    adapted.setExpression(adaptedExpression.orElseGet(original.getExpression()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTInDeclarationVariable refInDeclarationVariable) {
    List<OCLExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refInDeclarationVariable);
    for (OCLExpressionsAdaptationVariant variant : variants) {
      ASTInDeclarationVariable adaptedNode = adapt(refInDeclarationVariable, variant);
      variant.setAdaptedNode(refInDeclarationVariable, adaptedNode);
    }
  }

  protected ASTInDeclarationVariable adapt(ASTInDeclarationVariable original, OCLExpressionsAdaptationVariant variant) {
    ASTInDeclarationVariable adapted = OCLExpressionsMill.inDeclarationVariableBuilder().uncheckedBuild();
    
    adapted.setName(original.getName());
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTOCLAtPreQualification refOCLAtPreQualification) {
    List<OCLExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refOCLAtPreQualification);
    for (OCLExpressionsAdaptationVariant variant : variants) {
      ASTOCLAtPreQualification adaptedNode = adapt(refOCLAtPreQualification, variant);
      variant.setAdaptedNode(refOCLAtPreQualification, adaptedNode);
    }
  }

  protected ASTOCLAtPreQualification adapt(ASTOCLAtPreQualification original, OCLExpressionsAdaptationVariant variant) {
    ASTOCLAtPreQualification adapted = OCLExpressionsMill.oCLAtPreQualificationBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
    adapted.setExpression(adaptedExpression.orElseGet(original.getExpression()::deepClone));

    adapted.setAtpre(original.isAtpre());
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTOCLVariableDeclaration refOCLVariableDeclaration) {
    List<OCLExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refOCLVariableDeclaration);
    for (OCLExpressionsAdaptationVariant variant : variants) {
      ASTOCLVariableDeclaration adaptedNode = adapt(refOCLVariableDeclaration, variant);
      variant.setAdaptedNode(refOCLVariableDeclaration, adaptedNode);
    }
  }

  protected ASTOCLVariableDeclaration adapt(ASTOCLVariableDeclaration original, OCLExpressionsAdaptationVariant variant) {
    ASTOCLVariableDeclaration adapted = OCLExpressionsMill.oCLVariableDeclarationBuilder().uncheckedBuild();
    
    if (original.isPresentMCType()) {
      Optional<ASTMCType> adaptedType = variant.getAdaptedNode(original.getMCType());
      adapted.setMCType(adaptedType.orElseGet(original.getMCType()::deepClone));
    } else {
      adapted.setMCTypeAbsent();
    }
    
    adapted.setName(original.getName());

    for (String dim : original.getDimList()) {
      adapted.addDim(dim);
    }
    
    if (original.isPresentExpression()) {
      Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
      adapted.setExpression(adaptedExpression.orElseGet(original.getExpression()::deepClone));
    } else {
      adapted.setExpressionAbsent();
    }
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTOCLTransitiveQualification refOCLTransitiveQualification) {
    List<OCLExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refOCLTransitiveQualification);
    for (OCLExpressionsAdaptationVariant variant : variants) {
      ASTOCLTransitiveQualification adaptedNode = adapt(refOCLTransitiveQualification, variant);
      variant.setAdaptedNode(refOCLTransitiveQualification, adaptedNode);
    }
  }

  protected ASTOCLTransitiveQualification adapt(ASTOCLTransitiveQualification original, OCLExpressionsAdaptationVariant variant) {
    ASTOCLTransitiveQualification adapted = OCLExpressionsMill.oCLTransitiveQualificationBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
    adapted.setExpression(adaptedExpression.orElseGet(original.getExpression()::deepClone));

    adapted.setTransitive(original.isTransitive());
    
    deepCloneComments(adapted, original);
    return adapted;
  }

}
