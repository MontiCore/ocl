package de.monticore.ocl2smt.helpers;

import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd4analysis.CD4AnalysisMill;
import de.monticore.cdassociation._ast.ASTCDAssocLeftSide;
import de.monticore.cdassociation._ast.ASTCDAssocRightSide;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.cdbasis._visitor.CDBasisTraverser;
import de.monticore.ocl2smt.trafo.BuildPreCDTrafo;
import de.monticore.ocl2smt.util.OCLType;
import de.se_rwth.commons.logging.Log;

public class OCLCDHelper {
  public static ASTCDAssociation getPreAssociation(
      ASTCDAssociation association, ASTCDDefinition cd) {

    ASTCDAssocRightSide right = association.getRight();
    ASTCDAssocLeftSide left = association.getLeft();
    for (ASTCDAssociation preAssoc : cd.getCDAssociationsList()) {
      ASTCDAssocRightSide preRight = preAssoc.getRight();
      ASTCDAssocLeftSide preLeft = preAssoc.getLeft();
      if (right.getMCQualifiedType().equals(preRight.getMCQualifiedType())
          && right.getMCQualifiedType().equals(preRight.getMCQualifiedType())
          && OCLHelper.mkPre(left.getCDRole().getName()).equals(preLeft.getCDRole().getName())
          && OCLHelper.mkPre(right.getCDRole().getName()).equals(preRight.getCDRole().getName())) {
        return preAssoc;
      }
    }
    Log.info(
        "pre-association "
            + association.getLeftQualifiedName().getQName()
            + " -- "
            + association.getRightQualifiedName().getQName()
            + " not found ",
        "Pre Assoc Not Found");
    return null;
  }

  public static void buildPreCD(ASTCDCompilationUnit ast) {
    final BuildPreCDTrafo preAttributeTrafo = new BuildPreCDTrafo();

    final CDBasisTraverser traverser = CD4AnalysisMill.traverser();
    traverser.add4CDBasis(preAttributeTrafo);
    traverser.setCDBasisHandler(preAttributeTrafo);
    ast.accept(traverser);
  }

  public static ASTCDAssociation getAssociation(
      OCLType type, String otherRole, ASTCDDefinition cd) {
    return CDHelper.getAssociation(CDHelper.getASTCDType(type.getName(), cd), otherRole, cd);
  }

  public static OCLType getOtherType(ASTCDAssociation association, OCLType type) {
    OCLType type1 = OCLType.buildOCLType(association.getLeftQualifiedName().getQName());
    OCLType type2 = OCLType.buildOCLType(association.getRightQualifiedName().getQName());
    if (type.equals(type1)) {
      return type2;
    } else {
      return type1;
    }
  }
}
