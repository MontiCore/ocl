package de.monticore.ocl.setexpressions;

import de.monticore.ocl.setexpressions._visitor.SetExpressionsVisitor2;
import de.monticore.refadaptation.AbstractAdaptationVisitor;
import de.monticore.ocl.setexpressions._ast.*;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.util.List;
import java.util.Optional;

import static de.monticore.refadaptation.RefAdaptationUtils.deepCloneComments;

public class SetExpressionsASTAdaptationVisitor extends AbstractAdaptationVisitor<SetExpressionsAdaptationContext>
        implements SetExpressionsVisitor2 {

  @Override
  public void endVisit(ASTSetInExpression refSetInExpression) {
    List<SetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetInExpression);
    for (SetExpressionsAdaptationVariant variant : variants) {
      ASTSetInExpression adaptedNode = adapt(refSetInExpression, variant);
      variant.setAdaptedNode(refSetInExpression, adaptedNode);
    }
  }

  protected ASTSetInExpression adapt(ASTSetInExpression original, SetExpressionsAdaptationVariant variant) {
    ASTSetInExpression adapted = SetExpressionsMill.setInExpressionBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedElem = variant.getAdaptedNode(original.getElem());
    adapted.setElem(adaptedElem.orElseGet(original.getElem()::deepClone));
    
    Optional<ASTExpression> adaptedSet = variant.getAdaptedNode(original.getSet());
    adapted.setSet(adaptedSet.orElseGet(original.getSet()::deepClone));

    adapted.setOperator(original.getOperator());
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTSetNotInExpression refSetNotInExpression) {
    List<SetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetNotInExpression);
    for (SetExpressionsAdaptationVariant variant : variants) {
      ASTSetNotInExpression adaptedNode = adapt(refSetNotInExpression, variant);
      variant.setAdaptedNode(refSetNotInExpression, adaptedNode);
    }
  }

  protected ASTSetNotInExpression adapt(ASTSetNotInExpression original, SetExpressionsAdaptationVariant variant) {
    ASTSetNotInExpression adapted = SetExpressionsMill.setNotInExpressionBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedElem = variant.getAdaptedNode(original.getElem());
    adapted.setElem(adaptedElem.orElseGet(original.getElem()::deepClone));
    
    Optional<ASTExpression> adaptedSet = variant.getAdaptedNode(original.getSet());
    adapted.setSet(adaptedSet.orElseGet(original.getSet()::deepClone));

    adapted.setOperator(original.getOperator());
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTUnionExpression refUnionExpression) {
    List<SetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refUnionExpression);
    for (SetExpressionsAdaptationVariant variant : variants) {
      ASTUnionExpression adaptedNode = adapt(refUnionExpression, variant);
      variant.setAdaptedNode(refUnionExpression, adaptedNode);
    }
  }

  protected ASTUnionExpression adapt(ASTUnionExpression original, SetExpressionsAdaptationVariant variant) {
    ASTUnionExpression adapted = SetExpressionsMill.unionExpressionBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedLeft = variant.getAdaptedNode(original.getLeft());
    adapted.setLeft(adaptedLeft.orElseGet(original.getLeft()::deepClone));
    
    Optional<ASTExpression> adaptedRight = variant.getAdaptedNode(original.getRight());
    adapted.setRight(adaptedRight.orElseGet(original.getRight()::deepClone));

    adapted.setOperator(original.getOperator());
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTIntersectionExpression refIntersectionExpression) {
    List<SetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refIntersectionExpression);
    for (SetExpressionsAdaptationVariant variant : variants) {
      ASTIntersectionExpression adaptedNode = adapt(refIntersectionExpression, variant);
      variant.setAdaptedNode(refIntersectionExpression, adaptedNode);
    }
  }

  protected ASTIntersectionExpression adapt(ASTIntersectionExpression original, SetExpressionsAdaptationVariant variant) {
    ASTIntersectionExpression adapted = SetExpressionsMill.intersectionExpressionBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedLeft = variant.getAdaptedNode(original.getLeft());
    adapted.setLeft(adaptedLeft.orElseGet(original.getLeft()::deepClone));
    
    Optional<ASTExpression> adaptedRight = variant.getAdaptedNode(original.getRight());
    adapted.setRight(adaptedRight.orElseGet(original.getRight()::deepClone));

    adapted.setOperator(original.getOperator());
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTSetMinusExpression refSetMinusExpression) {
    List<SetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetMinusExpression);
    for (SetExpressionsAdaptationVariant variant : variants) {
      ASTSetMinusExpression adaptedNode = adapt(refSetMinusExpression, variant);
      variant.setAdaptedNode(refSetMinusExpression, adaptedNode);
    }
  }

  protected ASTSetMinusExpression adapt(ASTSetMinusExpression original, SetExpressionsAdaptationVariant variant) {
    ASTSetMinusExpression adapted = SetExpressionsMill.setMinusExpressionBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedLeft = variant.getAdaptedNode(original.getLeft());
    adapted.setLeft(adaptedLeft.orElseGet(original.getLeft()::deepClone));
    
    Optional<ASTExpression> adaptedRight = variant.getAdaptedNode(original.getRight());
    adapted.setRight(adaptedRight.orElseGet(original.getRight()::deepClone));

    adapted.setOperator(original.getOperator());
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTSetUnionExpression refSetUnionExpression) {
    List<SetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetUnionExpression);
    for (SetExpressionsAdaptationVariant variant : variants) {
      ASTSetUnionExpression adaptedNode = adapt(refSetUnionExpression, variant);
      variant.setAdaptedNode(refSetUnionExpression, adaptedNode);
    }
  }

  protected ASTSetUnionExpression adapt(ASTSetUnionExpression original, SetExpressionsAdaptationVariant variant) {
    ASTSetUnionExpression adapted = SetExpressionsMill.setUnionExpressionBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedSet = variant.getAdaptedNode(original.getSet());
    adapted.setSet(adaptedSet.orElseGet(original.getSet()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTSetIntersectionExpression refSetIntersectionExpression) {
    List<SetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetIntersectionExpression);
    for (SetExpressionsAdaptationVariant variant : variants) {
      ASTSetIntersectionExpression adaptedNode = adapt(refSetIntersectionExpression, variant);
      variant.setAdaptedNode(refSetIntersectionExpression, adaptedNode);
    }
  }

  protected ASTSetIntersectionExpression adapt(ASTSetIntersectionExpression original, SetExpressionsAdaptationVariant variant) {
    ASTSetIntersectionExpression adapted = SetExpressionsMill.setIntersectionExpressionBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedSet = variant.getAdaptedNode(original.getSet());
    adapted.setSet(adaptedSet.orElseGet(original.getSet()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTSetAndExpression refSetAndExpression) {
    List<SetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetAndExpression);
    for (SetExpressionsAdaptationVariant variant : variants) {
      ASTSetAndExpression adaptedNode = adapt(refSetAndExpression, variant);
      variant.setAdaptedNode(refSetAndExpression, adaptedNode);
    }
  }

  protected ASTSetAndExpression adapt(ASTSetAndExpression original, SetExpressionsAdaptationVariant variant) {
    ASTSetAndExpression adapted = SetExpressionsMill.setAndExpressionBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedSet = variant.getAdaptedNode(original.getSet());
    adapted.setSet(adaptedSet.orElseGet(original.getSet()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTSetOrExpression refSetOrExpression) {
    List<SetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetOrExpression);
    for (SetExpressionsAdaptationVariant variant : variants) {
      ASTSetOrExpression adaptedNode = adapt(refSetOrExpression, variant);
      variant.setAdaptedNode(refSetOrExpression, adaptedNode);
    }
  }

  protected ASTSetOrExpression adapt(ASTSetOrExpression original, SetExpressionsAdaptationVariant variant) {
    ASTSetOrExpression adapted = SetExpressionsMill.setOrExpressionBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedSet = variant.getAdaptedNode(original.getSet());
    adapted.setSet(adaptedSet.orElseGet(original.getSet()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTSetVariableDeclaration refSetVariableDeclaration) {
    List<SetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetVariableDeclaration);
    for (SetExpressionsAdaptationVariant variant : variants) {
      ASTSetVariableDeclaration adaptedNode = adapt(refSetVariableDeclaration, variant);
      variant.setAdaptedNode(refSetVariableDeclaration, adaptedNode);
    }
  }

  protected ASTSetVariableDeclaration adapt(ASTSetVariableDeclaration original, SetExpressionsAdaptationVariant variant) {
    ASTSetVariableDeclaration adapted = SetExpressionsMill.setVariableDeclarationBuilder().uncheckedBuild();
    
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
  public void endVisit(ASTSetComprehension refSetComprehension) {
    List<SetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetComprehension);
    for (SetExpressionsAdaptationVariant variant : variants) {
      ASTSetComprehension adaptedNode = adapt(refSetComprehension, variant);
      variant.setAdaptedNode(refSetComprehension, adaptedNode);
    }
  }

  protected ASTSetComprehension adapt(ASTSetComprehension original, SetExpressionsAdaptationVariant variant) {
    ASTSetComprehension adapted = SetExpressionsMill.setComprehensionBuilder().uncheckedBuild();
    
    if (original.isPresentLeft()) {
      Optional<ASTSetComprehensionItem> adaptedLeft = variant.getAdaptedNode(original.getLeft());
      adapted.setLeft(adaptedLeft.orElseGet(original.getLeft()::deepClone));
    } else {
      adapted.setLeftAbsent();
    }
    
    for (ASTSetComprehensionItem item : original.getSetComprehensionItemList()) {
      Optional<ASTSetComprehensionItem> adaptedItem = variant.getAdaptedNode(item);
      adapted.addSetComprehensionItem(adaptedItem.orElseGet(item::deepClone));
    }

    if (original.isPresentSet()) {
      adapted.setSet(original.getSet());
    } else {
      adapted.setSetAbsent();
    }
    if (original.isPresentOpeningBracket()) {
      adapted.setOpeningBracket(original.getOpeningBracket());
    } else {
      adapted.setOpeningBracketAbsent();
    }
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTSetComprehensionItem refSetComprehensionItem) {
    List<SetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetComprehensionItem);
    for (SetExpressionsAdaptationVariant variant : variants) {
      ASTSetComprehensionItem adaptedNode = adapt(refSetComprehensionItem, variant);
      variant.setAdaptedNode(refSetComprehensionItem, adaptedNode);
    }
  }

  protected ASTSetComprehensionItem adapt(ASTSetComprehensionItem original, SetExpressionsAdaptationVariant variant) {
    ASTSetComprehensionItem adapted = SetExpressionsMill.setComprehensionItemBuilder().uncheckedBuild();
    
    if (original.isPresentExpression()) {
      Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
      adapted.setExpression(adaptedExpression.orElseGet(original.getExpression()::deepClone));
    } else {
      adapted.setExpressionAbsent();
    }
    
    if (original.isPresentSetVariableDeclaration()) {
      Optional<ASTSetVariableDeclaration> adaptedVarDecl = variant.getAdaptedNode(original.getSetVariableDeclaration());
      adapted.setSetVariableDeclaration(adaptedVarDecl.orElseGet(original.getSetVariableDeclaration()::deepClone));
    } else {
      adapted.setSetVariableDeclarationAbsent();
    }
    
    if (original.isPresentGeneratorDeclaration()) {
      Optional<ASTGeneratorDeclaration> adaptedGenDecl = variant.getAdaptedNode(original.getGeneratorDeclaration());
      adapted.setGeneratorDeclaration(adaptedGenDecl.orElseGet(original.getGeneratorDeclaration()::deepClone));
    } else {
      adapted.setGeneratorDeclarationAbsent();
    }
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTGeneratorDeclaration refGeneratorDeclaration) {
    List<SetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refGeneratorDeclaration);
    for (SetExpressionsAdaptationVariant variant : variants) {
      ASTGeneratorDeclaration adaptedNode = adapt(refGeneratorDeclaration, variant);
      variant.setAdaptedNode(refGeneratorDeclaration, adaptedNode);
    }
  }

  protected ASTGeneratorDeclaration adapt(ASTGeneratorDeclaration original, SetExpressionsAdaptationVariant variant) {
    ASTGeneratorDeclaration adapted = SetExpressionsMill.generatorDeclarationBuilder().uncheckedBuild();
    
    if (original.isPresentMCType()) {
      Optional<ASTMCType> adaptedType = variant.getAdaptedNode(original.getMCType());
      adapted.setMCType(adaptedType.orElseGet(original.getMCType()::deepClone));
    } else {
      adapted.setMCTypeAbsent();
    }
    
    adapted.setName(original.getName());
    
    Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
    adapted.setExpression(adaptedExpression.orElseGet(original.getExpression()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTSetEnumeration refSetEnumeration) {
    List<SetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetEnumeration);
    for (SetExpressionsAdaptationVariant variant : variants) {
      ASTSetEnumeration adaptedNode = adapt(refSetEnumeration, variant);
      variant.setAdaptedNode(refSetEnumeration, adaptedNode);
    }
  }

  protected ASTSetEnumeration adapt(ASTSetEnumeration original, SetExpressionsAdaptationVariant variant) {
    ASTSetEnumeration adapted = SetExpressionsMill.setEnumerationBuilder().uncheckedBuild();
    
    for (ASTSetCollectionItem item : original.getSetCollectionItemList()) {
      Optional<ASTSetCollectionItem> adaptedItem = variant.getAdaptedNode(item);
      adapted.addSetCollectionItem(adaptedItem.orElseGet(item::deepClone));
    }

    if (original.isPresentSet()) {
      adapted.setSet(original.getSet());
    } else {
      adapted.setSetAbsent();
    }
    if (original.isPresentOpeningBracket()) {
      adapted.setOpeningBracket(original.getOpeningBracket());
    } else {
      adapted.setOpeningBracketAbsent();
    }
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTSetValueItem refSetValueItem) {
    List<SetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetValueItem);
    for (SetExpressionsAdaptationVariant variant : variants) {
      ASTSetValueItem adaptedNode = adapt(refSetValueItem, variant);
      variant.setAdaptedNode(refSetValueItem, adaptedNode);
    }
  }

  protected ASTSetValueItem adapt(ASTSetValueItem original, SetExpressionsAdaptationVariant variant) {
    ASTSetValueItem adapted = SetExpressionsMill.setValueItemBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
    adapted.setExpression(adaptedExpression.orElseGet(original.getExpression()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTSetValueRange refSetValueRange) {
    List<SetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetValueRange);
    for (SetExpressionsAdaptationVariant variant : variants) {
      ASTSetValueRange adaptedNode = adapt(refSetValueRange, variant);
      variant.setAdaptedNode(refSetValueRange, adaptedNode);
    }
  }

  protected ASTSetValueRange adapt(ASTSetValueRange original, SetExpressionsAdaptationVariant variant) {
    ASTSetValueRange adapted = SetExpressionsMill.setValueRangeBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedLowerBound = variant.getAdaptedNode(original.getLowerBound());
    adapted.setLowerBound(adaptedLowerBound.orElseGet(original.getLowerBound()::deepClone));
    
    Optional<ASTExpression> adaptedUpperBound = variant.getAdaptedNode(original.getUpperBound());
    adapted.setUpperBound(adaptedUpperBound.orElseGet(original.getUpperBound()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

}
