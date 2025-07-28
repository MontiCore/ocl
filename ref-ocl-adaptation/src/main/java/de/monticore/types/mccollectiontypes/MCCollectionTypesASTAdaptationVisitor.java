package de.monticore.types.mccollectiontypes;

import de.monticore.refadaptation.AbstractAdaptationVisitor;
import de.monticore.refadaptation.IAdaptationVariant;
import de.monticore.refadaptation.RefAdaptationUtils;
import de.monticore.types.mcbasictypes._ast.ASTMCPrimitiveType;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType;
import de.monticore.types.mccollectiontypes._ast.*;
import de.monticore.types.mccollectiontypes._visitor.MCCollectionTypesVisitor2;

import java.util.List;
import java.util.Optional;

// NOTE: Could be generated
public class MCCollectionTypesASTAdaptationVisitor
        extends AbstractAdaptationVisitor<MCCollectionTypesAdaptationContext>
        implements MCCollectionTypesVisitor2 {

  @Override
  public void endVisit(ASTMCBasicTypeArgument node) {
    List<MCCollectionTypesAdaptationVariant> variants = getAdaptations4Ast().getVariants(node);
    for (MCCollectionTypesAdaptationVariant variant : variants) {
      ASTMCBasicTypeArgument adaptedNode = adapt(node, variant);
      variant.setAdaptedNode(node, adaptedNode);
    }
  }

  protected ASTMCBasicTypeArgument adapt(ASTMCBasicTypeArgument original, IAdaptationVariant variant) {
    ASTMCBasicTypeArgument adapted = MCCollectionTypesMill.mCBasicTypeArgumentBuilder().uncheckedBuild();
    Optional<ASTMCQualifiedType> adaptedType = variant.getAdaptedNode(original.getMCQualifiedType());
    adapted.setMCQualifiedType(adaptedType.orElseGet(original.getMCQualifiedType()::deepClone));
    RefAdaptationUtils.deepCloneComments(original, adapted);
    return adapted;
  }

  @Override
  public void endVisit(ASTMCPrimitiveTypeArgument node) {
    List<MCCollectionTypesAdaptationVariant> variants = getAdaptations4Ast().getVariants(node);
    for (MCCollectionTypesAdaptationVariant variant : variants) {
      ASTMCPrimitiveTypeArgument adaptedNode = adapt(node, variant);
      variant.setAdaptedNode(node, adaptedNode);
    }
  }

  protected ASTMCPrimitiveTypeArgument adapt(ASTMCPrimitiveTypeArgument original, IAdaptationVariant variant) {
    ASTMCPrimitiveTypeArgument adapted = MCCollectionTypesMill.mCPrimitiveTypeArgumentBuilder().uncheckedBuild();
    Optional<ASTMCPrimitiveType> adaptedType = variant.getAdaptedNode(original.getMCPrimitiveType());
    adapted.setMCPrimitiveType(adaptedType.orElseGet(original.getMCPrimitiveType()::deepClone));

    RefAdaptationUtils.deepCloneComments(original, adapted);
    return adapted;
  }

  @Override
  public void endVisit(ASTMCListType node) {
    List<MCCollectionTypesAdaptationVariant> variants = getAdaptations4Ast().getVariants(node);
    for (MCCollectionTypesAdaptationVariant variant : variants) {
      ASTMCListType adaptedNode = adapt(node, variant);
      variant.setAdaptedNode(node, adaptedNode);
    }
  }

  protected ASTMCListType adapt(ASTMCListType original, IAdaptationVariant variant) {
    ASTMCListType adapted = MCCollectionTypesMill.mCListTypeBuilder().uncheckedBuild();
    Optional<ASTMCTypeArgument> adaptedTypeArgument = variant.getAdaptedNode(original.getMCTypeArgument());
    adapted.setMCTypeArgument(adaptedTypeArgument.orElseGet(original.getMCTypeArgument()::deepClone));

    RefAdaptationUtils.deepCloneComments(original, adapted);
    return adapted;
  }

  @Override
  public void endVisit(ASTMCSetType node) {
    List<MCCollectionTypesAdaptationVariant> variants = getAdaptations4Ast().getVariants(node);
    for (MCCollectionTypesAdaptationVariant variant : variants) {
      ASTMCSetType adaptedNode = adapt(node, variant);
      variant.setAdaptedNode(node, adaptedNode);
    }
  }

  protected ASTMCSetType adapt(ASTMCSetType original, IAdaptationVariant variant) {
    ASTMCSetType adapted = MCCollectionTypesMill.mCSetTypeBuilder().uncheckedBuild();
    Optional<ASTMCTypeArgument> adaptedTypeArgument = variant.getAdaptedNode(original.getMCTypeArgument());
    adapted.setMCTypeArgument(adaptedTypeArgument.orElseGet(original.getMCTypeArgument()::deepClone));

    RefAdaptationUtils.deepCloneComments(original, adapted);
    return adapted;
  }

  @Override
  public void endVisit(ASTMCMapType node) {
    List<MCCollectionTypesAdaptationVariant> variants = getAdaptations4Ast().getVariants(node);
    for (MCCollectionTypesAdaptationVariant variant : variants) {
      ASTMCMapType adaptedNode = adapt(node, variant);
      variant.setAdaptedNode(node, adaptedNode);
    }
  }

  protected ASTMCMapType adapt(ASTMCMapType original, IAdaptationVariant variant) {
    ASTMCMapType adapted = MCCollectionTypesMill.mCMapTypeBuilder().uncheckedBuild();
    Optional<ASTMCTypeArgument> adaptedKey = variant.getAdaptedNode(original.getKey());
    adapted.setKey(adaptedKey.orElseGet(original.getKey()::deepClone));
    Optional<ASTMCTypeArgument> adaptedValue = variant.getAdaptedNode(original.getValue());
    adapted.setValue(adaptedValue.orElseGet(original.getValue()::deepClone));

    RefAdaptationUtils.deepCloneComments(original, adapted);
    return adapted;
  }

  @Override
  public void endVisit(ASTMCOptionalType node) {
    List<MCCollectionTypesAdaptationVariant> variants = getAdaptations4Ast().getVariants(node);
    for (MCCollectionTypesAdaptationVariant variant : variants) {
      ASTMCOptionalType adaptedNode = adapt(node, variant);
      variant.setAdaptedNode(node, adaptedNode);
    }
  }

  protected ASTMCOptionalType adapt(ASTMCOptionalType original, IAdaptationVariant variant) {
    ASTMCOptionalType adapted = MCCollectionTypesMill.mCOptionalTypeBuilder().uncheckedBuild();
    Optional<ASTMCTypeArgument> adaptedTypeArgument = variant.getAdaptedNode(original.getMCTypeArgument());
    adapted.setMCTypeArgument(adaptedTypeArgument.orElseGet(original.getMCTypeArgument()::deepClone));

    RefAdaptationUtils.deepCloneComments(original, adapted);
    return adapted;
  }
}
