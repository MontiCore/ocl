/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.trafo;

import de.monticore.cd._parser.CDAfterParseHelper;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdassociation._visitor.CDAssociationHandler;
import de.monticore.cdassociation._visitor.CDAssociationTraverser;
import de.monticore.cdassociation._visitor.CDAssociationVisitor2;
import java.util.ArrayList;
import java.util.List;

public class BuildPreAssociationTrafo extends CDAfterParseHelper
    implements CDAssociationVisitor2, CDAssociationHandler {
  protected CDAssociationTraverser traverser;
  List<ASTCDAssociation> preAssociations = new ArrayList<>();

  @Override
  public CDAssociationTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void handle(ASTCDAssociation node) {}

  @Override
  public void setTraverser(CDAssociationTraverser traverser) {
    this.traverser = traverser;
  }
}
