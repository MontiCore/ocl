package de.monticore.ocl.setexpressions;

import de.monticore.ocl.setexpressions._visitor.SetExpressionsVisitor2;
import de.monticore.refadapt.AbstractAdaptationVisitor;
import de.monticore.ocl.setexpressions._ast.*;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import java.util.List;
import java.util.Optional;

import static de.monticore.refadapt.RefAdaptationUtils.deepCloneComments;

public class SetExpressionsASTAdaptationVisitor extends AbstractAdaptationVisitor<ISetExpressionsAdaptationContext>
        implements SetExpressionsVisitor2 {

  @Override
  public void endVisit(ASTSetInExpression refSetInExpression) {
    List<ISetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetInExpression);
    for (ISetExpressionsAdaptationVariant variant : variants) {
      ASTSetInExpression adaptedNode = adapt(refSetInExpression, variant);
      variant.setAdaptedNode(refSetInExpression, adaptedNode);
    }
  }

  protected ASTSetInExpression adapt(ASTSetInExpression original, ISetExpressionsAdaptationVariant variant) {
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
    List<ISetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetNotInExpression);
    for (ISetExpressionsAdaptationVariant variant : variants) {
      ASTSetNotInExpression adaptedNode = adapt(refSetNotInExpression, variant);
      variant.setAdaptedNode(refSetNotInExpression, adaptedNode);
    }
  }

  protected ASTSetNotInExpression adapt(ASTSetNotInExpression original, ISetExpressionsAdaptationVariant variant) {
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
    List<ISetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refUnionExpression);
    for (ISetExpressionsAdaptationVariant variant : variants) {
      ASTUnionExpression adaptedNode = adapt(refUnionExpression, variant);
      variant.setAdaptedNode(refUnionExpression, adaptedNode);
    }
  }

  protected ASTUnionExpression adapt(ASTUnionExpression original, ISetExpressionsAdaptationVariant variant) {
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
    List<ISetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refIntersectionExpression);
    for (ISetExpressionsAdaptationVariant variant : variants) {
      ASTIntersectionExpression adaptedNode = adapt(refIntersectionExpression, variant);
      variant.setAdaptedNode(refIntersectionExpression, adaptedNode);
    }
  }

  protected ASTIntersectionExpression adapt(ASTIntersectionExpression original, ISetExpressionsAdaptationVariant variant) {
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
    List<ISetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetMinusExpression);
    for (ISetExpressionsAdaptationVariant variant : variants) {
      ASTSetMinusExpression adaptedNode = adapt(refSetMinusExpression, variant);
      variant.setAdaptedNode(refSetMinusExpression, adaptedNode);
    }
  }

  protected ASTSetMinusExpression adapt(ASTSetMinusExpression original, ISetExpressionsAdaptationVariant variant) {
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
    List<ISetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetUnionExpression);
    for (ISetExpressionsAdaptationVariant variant : variants) {
      ASTSetUnionExpression adaptedNode = adapt(refSetUnionExpression, variant);
      variant.setAdaptedNode(refSetUnionExpression, adaptedNode);
    }
  }

  protected ASTSetUnionExpression adapt(ASTSetUnionExpression original, ISetExpressionsAdaptationVariant variant) {
    ASTSetUnionExpression adapted = SetExpressionsMill.setUnionExpressionBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedSet = variant.getAdaptedNode(original.getSet());
    adapted.setSet(adaptedSet.orElseGet(original.getSet()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTSetIntersectionExpression refSetIntersectionExpression) {
    List<ISetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetIntersectionExpression);
    for (ISetExpressionsAdaptationVariant variant : variants) {
      ASTSetIntersectionExpression adaptedNode = adapt(refSetIntersectionExpression, variant);
      variant.setAdaptedNode(refSetIntersectionExpression, adaptedNode);
    }
  }

  protected ASTSetIntersectionExpression adapt(ASTSetIntersectionExpression original, ISetExpressionsAdaptationVariant variant) {
    ASTSetIntersectionExpression adapted = SetExpressionsMill.setIntersectionExpressionBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedSet = variant.getAdaptedNode(original.getSet());
    adapted.setSet(adaptedSet.orElseGet(original.getSet()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTSetAndExpression refSetAndExpression) {
    List<ISetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetAndExpression);
    for (ISetExpressionsAdaptationVariant variant : variants) {
      ASTSetAndExpression adaptedNode = adapt(refSetAndExpression, variant);
      variant.setAdaptedNode(refSetAndExpression, adaptedNode);
    }
  }

  protected ASTSetAndExpression adapt(ASTSetAndExpression original, ISetExpressionsAdaptationVariant variant) {
    ASTSetAndExpression adapted = SetExpressionsMill.setAndExpressionBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedSet = variant.getAdaptedNode(original.getSet());
    adapted.setSet(adaptedSet.orElseGet(original.getSet()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTSetOrExpression refSetOrExpression) {
    List<ISetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetOrExpression);
    for (ISetExpressionsAdaptationVariant variant : variants) {
      ASTSetOrExpression adaptedNode = adapt(refSetOrExpression, variant);
      variant.setAdaptedNode(refSetOrExpression, adaptedNode);
    }
  }

  protected ASTSetOrExpression adapt(ASTSetOrExpression original, ISetExpressionsAdaptationVariant variant) {
    ASTSetOrExpression adapted = SetExpressionsMill.setOrExpressionBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedSet = variant.getAdaptedNode(original.getSet());
    adapted.setSet(adaptedSet.orElseGet(original.getSet()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTSetVariableDeclaration refSetVariableDeclaration) {
    List<ISetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetVariableDeclaration);
    for (ISetExpressionsAdaptationVariant variant : variants) {
      ASTSetVariableDeclaration adaptedNode = adapt(refSetVariableDeclaration, variant);
      variant.setAdaptedNode(refSetVariableDeclaration, adaptedNode);
    }
  }

  protected ASTSetVariableDeclaration adapt(ASTSetVariableDeclaration original, ISetExpressionsAdaptationVariant variant) {
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
    List<ISetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetComprehension);
    for (ISetExpressionsAdaptationVariant variant : variants) {
      ASTSetComprehension adaptedNode = adapt(refSetComprehension, variant);
      variant.setAdaptedNode(refSetComprehension, adaptedNode);
    }
  }

  protected ASTSetComprehension adapt(ASTSetComprehension original, ISetExpressionsAdaptationVariant variant) {
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
    List<ISetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetComprehensionItem);
    for (ISetExpressionsAdaptationVariant variant : variants) {
      ASTSetComprehensionItem adaptedNode = adapt(refSetComprehensionItem, variant);
      variant.setAdaptedNode(refSetComprehensionItem, adaptedNode);
    }
  }

  protected ASTSetComprehensionItem adapt(ASTSetComprehensionItem original, ISetExpressionsAdaptationVariant variant) {
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
    List<ISetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refGeneratorDeclaration);
    for (ISetExpressionsAdaptationVariant variant : variants) {
      ASTGeneratorDeclaration adaptedNode = adapt(refGeneratorDeclaration, variant);
      variant.setAdaptedNode(refGeneratorDeclaration, adaptedNode);
    }
  }

  protected ASTGeneratorDeclaration adapt(ASTGeneratorDeclaration original, ISetExpressionsAdaptationVariant variant) {
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
    List<ISetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetEnumeration);
    for (ISetExpressionsAdaptationVariant variant : variants) {
      ASTSetEnumeration adaptedNode = adapt(refSetEnumeration, variant);
      variant.setAdaptedNode(refSetEnumeration, adaptedNode);
    }
  }

  protected ASTSetEnumeration adapt(ASTSetEnumeration original, ISetExpressionsAdaptationVariant variant) {
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
    List<ISetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetValueItem);
    for (ISetExpressionsAdaptationVariant variant : variants) {
      ASTSetValueItem adaptedNode = adapt(refSetValueItem, variant);
      variant.setAdaptedNode(refSetValueItem, adaptedNode);
    }
  }

  protected ASTSetValueItem adapt(ASTSetValueItem original, ISetExpressionsAdaptationVariant variant) {
    ASTSetValueItem adapted = SetExpressionsMill.setValueItemBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
    adapted.setExpression(adaptedExpression.orElseGet(original.getExpression()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTSetValueRange refSetValueRange) {
    List<ISetExpressionsAdaptationVariant> variants = getAdaptations4Ast().getVariants(refSetValueRange);
    for (ISetExpressionsAdaptationVariant variant : variants) {
      ASTSetValueRange adaptedNode = adapt(refSetValueRange, variant);
      variant.setAdaptedNode(refSetValueRange, adaptedNode);
    }
  }

  protected ASTSetValueRange adapt(ASTSetValueRange original, ISetExpressionsAdaptationVariant variant) {
    ASTSetValueRange adapted = SetExpressionsMill.setValueRangeBuilder().uncheckedBuild();
    
    Optional<ASTExpression> adaptedLowerBound = variant.getAdaptedNode(original.getLowerBound());
    adapted.setLowerBound(adaptedLowerBound.orElseGet(original.getLowerBound()::deepClone));
    
    Optional<ASTExpression> adaptedUpperBound = variant.getAdaptedNode(original.getUpperBound());
    adapted.setUpperBound(adaptedUpperBound.orElseGet(original.getUpperBound()::deepClone));
    
    deepCloneComments(adapted, original);
    return adapted;
  }

}
