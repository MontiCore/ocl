package de.monticore.ocl.oclexpressions;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl.oclexpressions._visitor.OCLExpressionsVisitor2;
import de.monticore.refadapt.AbstractAdaptationVisitor;
import de.monticore.types.mcbasictypes._ast.ASTMCType;

import java.util.List;
import java.util.Optional;

import static de.monticore.refadapt.RefAdaptationUtils.deepCloneComments;

public class OCLExpressionsASTAdaptationVisitor
        extends AbstractAdaptationVisitor<IOCLExpressionsAdaptationContext>
        implements OCLExpressionsVisitor2 {

  @Override
  public void endVisit(ASTAnyExpression refAnyExpression) {
    List<IOCLExpressionsAdaptationVariant> variants = getVariants4Ast().getVariants(refAnyExpression);
    for (IOCLExpressionsAdaptationVariant variant : variants) {
      ASTAnyExpression adaptedNode = adapt(refAnyExpression, variant);
      variant.setAdaptedNode(refAnyExpression, adaptedNode);
    }
  }

  protected ASTAnyExpression adapt(ASTAnyExpression original, IOCLExpressionsAdaptationVariant variant) {
    ASTAnyExpression adapted = OCLExpressionsMill.anyExpressionBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
    adapted.setExpression(adaptedExpression.orElseGet(original.getExpression()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTInDeclaration refInDeclaration) {
    List<IOCLExpressionsAdaptationVariant> variants = getVariants4Ast().getVariants(refInDeclaration);
    for (IOCLExpressionsAdaptationVariant variant : variants) {
      ASTInDeclaration adaptedNode = adapt(refInDeclaration, variant);
      variant.setAdaptedNode(refInDeclaration, adaptedNode);
    }
  }

  protected ASTInDeclaration adapt(ASTInDeclaration original, IOCLExpressionsAdaptationVariant variant) {
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
    List<IOCLExpressionsAdaptationVariant> variants = getVariants4Ast().getVariants(refLetinExpression);
    for (IOCLExpressionsAdaptationVariant variant : variants) {
      ASTLetinExpression adaptedNode = adapt(refLetinExpression, variant);
      variant.setAdaptedNode(refLetinExpression, adaptedNode);
    }
  }

  protected ASTLetinExpression adapt(ASTLetinExpression original, IOCLExpressionsAdaptationVariant variant) {
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
    List<IOCLExpressionsAdaptationVariant> variants = getVariants4Ast().getVariants(refExistsExpression);
    for (IOCLExpressionsAdaptationVariant variant : variants) {
      ASTExistsExpression adaptedNode = adapt(refExistsExpression, variant);
      variant.setAdaptedNode(refExistsExpression, adaptedNode);
    }
  }

  protected ASTExistsExpression adapt(ASTExistsExpression original, IOCLExpressionsAdaptationVariant variant) {
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
    List<IOCLExpressionsAdaptationVariant> variants = getVariants4Ast().getVariants(refForallExpression);
    for (IOCLExpressionsAdaptationVariant variant : variants) {
      ASTForallExpression adaptedNode = adapt(refForallExpression, variant);
      variant.setAdaptedNode(refForallExpression, adaptedNode);
    }
  }

  protected ASTForallExpression adapt(ASTForallExpression original, IOCLExpressionsAdaptationVariant variant) {
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
    List<IOCLExpressionsAdaptationVariant> variants = getVariants4Ast().getVariants(refTypeIfExpression);
    for (IOCLExpressionsAdaptationVariant variant : variants) {
      ASTTypeIfExpression adaptedNode = adapt(refTypeIfExpression, variant);
      variant.setAdaptedNode(refTypeIfExpression, adaptedNode);
    }
  }

  protected ASTTypeIfExpression adapt(ASTTypeIfExpression original, IOCLExpressionsAdaptationVariant variant) {
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
    List<IOCLExpressionsAdaptationVariant> variants = getVariants4Ast().getVariants(refImpliesExpression);
    for (IOCLExpressionsAdaptationVariant variant : variants) {
      ASTImpliesExpression adaptedNode = adapt(refImpliesExpression, variant);
      variant.setAdaptedNode(refImpliesExpression, adaptedNode);
    }
  }

  protected ASTImpliesExpression adapt(ASTImpliesExpression original, IOCLExpressionsAdaptationVariant variant) {
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
    List<IOCLExpressionsAdaptationVariant> variants = getVariants4Ast().getVariants(refIterateExpression);
    for (IOCLExpressionsAdaptationVariant variant : variants) {
      ASTIterateExpression adaptedNode = adapt(refIterateExpression, variant);
      variant.setAdaptedNode(refIterateExpression, adaptedNode);
    }
  }

  protected ASTIterateExpression adapt(ASTIterateExpression original, IOCLExpressionsAdaptationVariant variant) {
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
    List<IOCLExpressionsAdaptationVariant> variants = getVariants4Ast().getVariants(refEquivalentExpression);
    for (IOCLExpressionsAdaptationVariant variant : variants) {
      ASTEquivalentExpression adaptedNode = adapt(refEquivalentExpression, variant);
      variant.setAdaptedNode(refEquivalentExpression, adaptedNode);
    }
  }

  protected ASTEquivalentExpression adapt(ASTEquivalentExpression original, IOCLExpressionsAdaptationVariant variant) {
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
    List<IOCLExpressionsAdaptationVariant> variants = getVariants4Ast().getVariants(refIfThenElseExpression);
    for (IOCLExpressionsAdaptationVariant variant : variants) {
      ASTIfThenElseExpression adaptedNode = adapt(refIfThenElseExpression, variant);
      variant.setAdaptedNode(refIfThenElseExpression, adaptedNode);
    }
  }

  protected ASTIfThenElseExpression adapt(ASTIfThenElseExpression original, IOCLExpressionsAdaptationVariant variant) {
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
    List<IOCLExpressionsAdaptationVariant> variants = getVariants4Ast().getVariants(refTypeIfThenExpression);
    for (IOCLExpressionsAdaptationVariant variant : variants) {
      ASTTypeIfThenExpression adaptedNode = adapt(refTypeIfThenExpression, variant);
      variant.setAdaptedNode(refTypeIfThenExpression, adaptedNode);
    }
  }

  protected ASTTypeIfThenExpression adapt(ASTTypeIfThenExpression original, IOCLExpressionsAdaptationVariant variant) {
    ASTTypeIfThenExpression adapted = OCLExpressionsMill.typeIfThenExpressionBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
    adapted.setExpression(adaptedExpression.orElseGet(original.getExpression()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTInDeclarationVariable refInDeclarationVariable) {
    List<IOCLExpressionsAdaptationVariant> variants = getVariants4Ast().getVariants(refInDeclarationVariable);
    for (IOCLExpressionsAdaptationVariant variant : variants) {
      ASTInDeclarationVariable adaptedNode = adapt(refInDeclarationVariable, variant);
      variant.setAdaptedNode(refInDeclarationVariable, adaptedNode);
    }
  }

  protected ASTInDeclarationVariable adapt(ASTInDeclarationVariable original, IOCLExpressionsAdaptationVariant variant) {
    ASTInDeclarationVariable adapted = OCLExpressionsMill.inDeclarationVariableBuilder().uncheckedBuild();
    
    adapted.setName(original.getName());
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTOCLAtPreQualification refOCLAtPreQualification) {
    List<IOCLExpressionsAdaptationVariant> variants = getVariants4Ast().getVariants(refOCLAtPreQualification);
    for (IOCLExpressionsAdaptationVariant variant : variants) {
      ASTOCLAtPreQualification adaptedNode = adapt(refOCLAtPreQualification, variant);
      variant.setAdaptedNode(refOCLAtPreQualification, adaptedNode);
    }
  }

  protected ASTOCLAtPreQualification adapt(ASTOCLAtPreQualification original, IOCLExpressionsAdaptationVariant variant) {
    ASTOCLAtPreQualification adapted = OCLExpressionsMill.oCLAtPreQualificationBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
    adapted.setExpression(adaptedExpression.orElseGet(original.getExpression()::deepClone));

    adapted.setAtpre(original.isAtpre());
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTOCLVariableDeclaration refOCLVariableDeclaration) {
    List<IOCLExpressionsAdaptationVariant> variants = getVariants4Ast().getVariants(refOCLVariableDeclaration);
    for (IOCLExpressionsAdaptationVariant variant : variants) {
      ASTOCLVariableDeclaration adaptedNode = adapt(refOCLVariableDeclaration, variant);
      variant.setAdaptedNode(refOCLVariableDeclaration, adaptedNode);
    }
  }

  protected ASTOCLVariableDeclaration adapt(ASTOCLVariableDeclaration original, IOCLExpressionsAdaptationVariant variant) {
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
    List<IOCLExpressionsAdaptationVariant> variants = getVariants4Ast().getVariants(refOCLTransitiveQualification);
    for (IOCLExpressionsAdaptationVariant variant : variants) {
      ASTOCLTransitiveQualification adaptedNode = adapt(refOCLTransitiveQualification, variant);
      variant.setAdaptedNode(refOCLTransitiveQualification, adaptedNode);
    }
  }

  protected ASTOCLTransitiveQualification adapt(ASTOCLTransitiveQualification original, IOCLExpressionsAdaptationVariant variant) {
    ASTOCLTransitiveQualification adapted = OCLExpressionsMill.oCLTransitiveQualificationBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
    adapted.setExpression(adaptedExpression.orElseGet(original.getExpression()::deepClone));

    adapted.setTransitive(original.isTransitive());
    
    deepCloneComments(adapted, original);
    return adapted;
  }

}
