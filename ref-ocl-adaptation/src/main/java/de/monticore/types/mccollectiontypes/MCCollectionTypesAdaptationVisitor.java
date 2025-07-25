package de.monticore.types.mccollectiontypes;

import de.monticore.refadaptation.AbstractAdaptationVisitor;
import de.monticore.refadaptation.IAdaptationVariant;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType;
import de.monticore.types.mccollectiontypes._ast.ASTMCBasicTypeArgument;
import de.monticore.types.mccollectiontypes._ast.ASTMCListType;
import de.monticore.types.mccollectiontypes._ast.ASTMCTypeArgument;
import de.monticore.types.mccollectiontypes._visitor.MCCollectionTypesVisitor2;

import java.util.List;
import java.util.Optional;

public class MCCollectionTypesAdaptationVisitor
        extends AbstractAdaptationVisitor<MCCollectionTypesAdaptationContext>
        implements MCCollectionTypesVisitor2 {

  @Override
  public void endVisit(ASTMCBasicTypeArgument node) {
    List<MCCollectionTypesAdaptationVariant> variants = getAdaptations4Ast().getVariants(node.getMCQualifiedType());
    for (MCCollectionTypesAdaptationVariant variant : variants) {
      ASTMCBasicTypeArgument adaptedNode = adapt(node, variant);
      variant.setAdaptedNode(node, adaptedNode);
    }
  }

  @Override
  public void endVisit(ASTMCListType node) {
    List<MCCollectionTypesAdaptationVariant> variants = getAdaptations4Ast().getVariants(node.getMCTypeArgument());
    for (MCCollectionTypesAdaptationVariant variant : variants) {
      ASTMCListType adaptedNode = adapt(node, variant);
      variant.setAdaptedNode(node, adaptedNode);
    }
  }

  // TODO This can be generated for each node to compose it from the adapted child nodes and copy the rest like deepClone
  protected ASTMCBasicTypeArgument adapt(ASTMCBasicTypeArgument original, IAdaptationVariant variant) {
    ASTMCBasicTypeArgument adapted = MCCollectionTypesMill.mCBasicTypeArgumentBuilder().uncheckedBuild();
    Optional<ASTMCQualifiedType> adaptedType = variant.getAdaptedNode(original.getMCQualifiedType());
    adapted.setMCQualifiedType(adaptedType.orElseGet(() -> original.getMCQualifiedType().deepClone()));
    for (de.monticore.ast.Comment x : original.get_PreCommentList()) {
      adapted.get_PreCommentList().add(new de.monticore.ast.Comment(x.getText()));
    }
    for (de.monticore.ast.Comment x : original.get_PostCommentList()) {
      adapted.get_PostCommentList().add(new de.monticore.ast.Comment(x.getText()));
    }
    return adapted;
  }

  // TODO This can be generated
  protected ASTMCListType adapt(ASTMCListType original, IAdaptationVariant variant) {
    ASTMCListType adapted = MCCollectionTypesMill.mCListTypeBuilder().uncheckedBuild();
    Optional<ASTMCTypeArgument> adaptedTypeArgument = variant.getAdaptedNode(original.getMCTypeArgument());
    adapted.setMCTypeArgument(adaptedTypeArgument.orElseGet(() -> original.getMCTypeArgument().deepClone()));
    for (de.monticore.ast.Comment x : original.get_PreCommentList()) {
      adapted.get_PreCommentList().add(new de.monticore.ast.Comment(x.getText()));
    }
    for (de.monticore.ast.Comment x : original.get_PostCommentList()) {
      adapted.get_PostCommentList().add(new de.monticore.ast.Comment(x.getText()));
    }
    return adapted;
  }
}
