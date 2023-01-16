package de.monticore.ocl2smt.helpers;

import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.ocl2smt.util.OCLType;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odbasis._ast.ASTODNamedObject;
import de.monticore.odlink._ast.ASTODLink;
import java.util.List;
import java.util.stream.Collectors;

public class OCLHelper {

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

  public static List<ASTODNamedObject> getObjectList(ASTODArtifact od) {
    return od.getObjectDiagram().getODElementList().stream()
        .filter(x -> x instanceof ASTODNamedObject)
        .map(x -> (ASTODNamedObject) x)
        .collect(Collectors.toList());
  }

  public static List<ASTODLink> getLinkList(ASTODArtifact od) {

    return od.getObjectDiagram().getODElementList().stream()
        .filter(x -> x instanceof ASTODLink)
        .map(x -> (ASTODLink) x)
        .collect(Collectors.toList());
  }
}
