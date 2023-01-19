package de.monticore.ocl2smt.helpers;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.ocl2smt.ocl2smt.ConstConverter;
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


    public static BoolExpr evaluateLink(
            ASTCDAssociation association,
            Expr<? extends Sort > obj1,
            Expr<? extends Sort> obj2,
            CD2SMTGenerator cd2SMTGenerator,
            ConstConverter cc) {

      ASTCDDefinition cd = cd2SMTGenerator.getClassDiagram().getCDDefinition();

      ASTCDType left = CDHelper.getLeftType(association, cd);
      ASTCDType right = CDHelper.getRightType(association, cd);

      OCLType type1 = cc.getType(obj1);
      BoolExpr res;
      if (left.getName().equals(type1.getName())) {
        res = cd2SMTGenerator.evaluateLink(association, left, right, obj1, obj2);
      } else {
        res = cd2SMTGenerator.evaluateLink(association, right, left, obj1, obj2);
      }

      return res;
    }

  public static Expr<? extends Sort> getAttribute(
          Expr<? extends Sort> obj,
          OCLType type,
          String attributeName,
          CD2SMTGenerator cd2SMTGenerator) {

    return cd2SMTGenerator.getAttribute(
            CDHelper.getASTCDType(
                    type.getName(), cd2SMTGenerator.getClassDiagram().getCDDefinition()),
            attributeName,
            obj);
  }
  }

