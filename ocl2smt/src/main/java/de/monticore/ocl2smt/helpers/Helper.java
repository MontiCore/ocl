package de.monticore.ocl2smt.helpers;

import de.monticore.cd4analysis.CD4AnalysisMill;
import de.monticore.cdassociation.CDAssociationMill;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._visitor.CDBasisTraverser;
import de.monticore.ocl2smt.trafo.BuildPreCDTrafo;

public class Helper {

  public static void buildPreCD(ASTCDCompilationUnit ast) {
    final BuildPreCDTrafo preAttributeTrafo = new BuildPreCDTrafo();

    final CDBasisTraverser traverser = CD4AnalysisMill.traverser();
    traverser.add4CDBasis(preAttributeTrafo);
    traverser.setCDBasisHandler(preAttributeTrafo);
    ast.accept(traverser);

  }

  public static ASTCDAssociation buildPreAssociation(ASTCDAssociation association){
   return CDAssociationMill.cDAssociationBuilder()
            .setModifier(association.getModifier())
            .setLeft(association.getLeft())
            .setRight(association.getRight())
            .setCDAssocType(association.getCDAssocType())
            .setName("pre")
            .setCDAssocDir(association.getCDAssocDir()).build(); //TODO: fix the assoc name
  }
}
