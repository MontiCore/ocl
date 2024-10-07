package de.monticore.ocl2smt.trafo;

import de.monticore.cd.facade.CDAttributeFacade;
import de.monticore.cdassociation.CDAssociationMill;
import de.monticore.cdassociation._ast.*;
import de.monticore.cdbasis._ast.*;
import de.monticore.cdbasis._visitor.CDBasisHandler;
import de.monticore.cdbasis._visitor.CDBasisTraverser;
import de.monticore.cdbasis._visitor.CDBasisVisitor2;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.umlmodifier._ast.ASTModifier;

public class BuildPreCDTrafo implements CDBasisHandler, CDBasisVisitor2 {

  protected CDBasisTraverser traverser;

  @Override
  public CDBasisTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(CDBasisTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void handle(ASTCDDefinition node) {
    node.getCDAssociationsList().forEach(assoc -> node.addCDElement(buildPreAssociation(assoc)));
    node.getCDClassesList().forEach(Class -> Class.accept(traverser));
  }

  @Override
  public void handle(ASTCDClass node) {
    node.getCDAttributeList().forEach(attr -> node.addCDMember(createPreAttribute(attr)));
  }

  private ASTCDAttribute createPreAttribute(ASTCDAttribute node) {
    ASTMCType type = node.getMCType();
    CDAttributeFacade facade = CDAttributeFacade.getInstance();
    ASTModifier mod = node.getModifier();
    return facade.createAttribute(mod, type, OCLHelper.mkPre(node.getName()));
  }

  public ASTCDAssociation buildPreAssociation(ASTCDAssociation association) {
    ASTCDAssocLeftSide left = copyAssocLeftSide(association.getLeft());
    left.getCDRole().setName(OCLHelper.mkPre(left.getCDRole().getName()));

    ASTCDAssocRightSide right = copyAssocRightSide(association.getRight());
    right.getCDRole().setName((OCLHelper.mkPre(right.getCDRole().getName())));

    return CDAssociationMill.cDAssociationBuilder()
        .setModifier(association.getModifier())
        .setLeft(left)
        .setRight(right)
        .setCDAssocType(association.getCDAssocType())
        .setCDAssocDir(association.getCDAssocDir())
        .set_SourcePositionStart(association.get_SourcePositionStart())
        .set_SourcePositionEnd(association.get_SourcePositionEnd())
        .build();
  }

  protected ASTCDAssocLeftSide copyAssocLeftSide(ASTCDAssocLeftSide leftSide) {
    ASTCDRole leftRole =
        CDAssociationMill.cDRoleBuilder().setName(leftSide.getCDRole().getName()).build();
    ASTCDAssocLeftSideBuilder left =
        CDAssociationMill.cDAssocLeftSideBuilder()
            .setModifier(leftSide.getModifier())
            .setMCQualifiedType(leftSide.getMCQualifiedType());

    if (leftSide.isPresentCDRole()) {
      left.setCDRole(leftRole);
    }
    if (leftSide.isPresentCDCardinality()) {
      left.setCDCardinality(leftSide.getCDCardinality());
    }
    if (leftSide.isPresentCDQualifier()) {
      left.setCDQualifier(leftSide.getCDQualifier()).build();
    }

    return left.build();
  }

  protected ASTCDAssocRightSide copyAssocRightSide(ASTCDAssocRightSide rightSide) {
    ASTCDRole rightRole =
        CDAssociationMill.cDRoleBuilder().setName(rightSide.getCDRole().getName()).build();
    ASTCDAssocRightSideBuilder right =
        CDAssociationMill.cDAssocRightSideBuilder()
            .setModifier(rightSide.getModifier())
            .setMCQualifiedType(rightSide.getMCQualifiedType());

    if (rightSide.isPresentCDRole()) {
      right.setCDRole(rightRole);
    }
    if (rightSide.isPresentCDCardinality()) {
      right.setCDCardinality(rightSide.getCDCardinality());
    }
    if (rightSide.isPresentCDQualifier()) {
      right.setCDQualifier(rightSide.getCDQualifier()).build();
    }

    return right.build();
  }
}
