package de.monticore.od2smt;

import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.odbasis._ast.*;
import de.monticore.odlink._ast.ASTODLink;
import java.util.List;
import java.util.stream.Collectors;

public class OD2SMTUtils {
  public static List<ASTODNamedObject> getObjectList(ASTODArtifact od) {
    return od.getObjectDiagram().getODElementList().stream()
        .filter(obj -> obj instanceof ASTODNamedObject)
        .map(obj -> ((ASTODNamedObject) obj))
        .collect(Collectors.toList());
  }

  public static List<ASTODLink> getLinkList(ASTODArtifact od) {
    return od.getObjectDiagram().getODElementList().stream()
        .filter(obj -> obj instanceof ASTODLink)
        .map(obj -> ((ASTODLink) obj))
        .collect(Collectors.toList());
  }

  public static ASTODNamedObject getObject(String name, ASTODArtifact od) {
    return getObjectList(od).stream()
        .filter(node -> node.getName().equals(name))
        .findAny()
        .orElse(null);
  }

  public static ASTCDType getObjectType(ASTODObject node, ASTCDDefinition cd) {
    return CDHelper.getASTCDType(node.getMCObjectType().printType(), cd);
  }

  public static ASTCDType getObjectType(String objectName, ASTODArtifact od, ASTCDDefinition cd) {
    return getObjectType(getObject(objectName, od), cd);
  }

  public static String getAttributeValue(ASTODNamedObject obj, String attributeName) {
    assert obj != null;
    for (ASTODAttribute attribute : obj.getODAttributeList()) {
      if (attribute.getName().equals(attributeName)) {
        ASTODName value = (ASTODName) attribute.getODValue();
        return value.getName();
      }
    }
    return null;
  }
}
