package de.monticore.ocl2smt.helpers;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cd4analysis.CD4AnalysisMill;
import de.monticore.cdassociation._ast.*;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdbasis._visitor.CDBasisTraverser;
import de.monticore.ocl2smt.ocl2smt.ExpressionsConverter;
import de.monticore.ocl2smt.trafo.BuildPreCDTrafo;
import de.monticore.ocl2smt.util.OCLType;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class Helper {

  public static void buildPreCD(ASTCDCompilationUnit ast) {
    final BuildPreCDTrafo preAttributeTrafo = new BuildPreCDTrafo();

    final CDBasisTraverser traverser = CD4AnalysisMill.traverser();
    traverser.add4CDBasis(preAttributeTrafo);
    traverser.setCDBasisHandler(preAttributeTrafo);
    ast.accept(traverser);
  }

  public static BoolExpr evaluateLink(
      ASTCDAssociation association,
      Expr<? extends Sort> obj1,
      Expr<? extends Sort> obj2,
      CD2SMTGenerator cd2SMTGenerator) {

    ASTCDDefinition cd = cd2SMTGenerator.getClassDiagram().getCDDefinition();

    ASTCDType left = CDHelper.getLeftType(association, cd);
    ASTCDType right = CDHelper.getRightType(association, cd);
    OCLType type1 = ExpressionsConverter.getType(obj1);
    if (left.getName().equals(type1.getName())) {
      return evaluateLink(
          association,
          new ImmutablePair<>(left, obj1),
          new ImmutablePair<>(right, obj2),
          cd2SMTGenerator);
    } else {
      return evaluateLink(
          association,
          new ImmutablePair<>(right, obj1),
          new ImmutablePair<>(left, obj2),
          cd2SMTGenerator);
    }
  }

  public static BoolExpr evaluateLink(
      ASTCDAssociation association,
      Expr<? extends Sort> obj1,
      Expr<? extends Sort> obj2,
      CD2SMTGenerator cd2SMTGenerator,
      boolean pre) {
    if (pre) {
      association = getPreAssoc(association, cd2SMTGenerator.getClassDiagram().getCDDefinition());
    }

    return evaluateLink(association, obj1, obj2, cd2SMTGenerator);
  }

  private static BoolExpr evaluateLink(
      ASTCDAssociation association,
      Pair<ASTCDType, Expr<? extends Sort>> obj1,
      Pair<ASTCDType, Expr<? extends Sort>> obj2,
      CD2SMTGenerator cd2SMTGenerator) {
    return cd2SMTGenerator.evaluateLink(
        association, obj1.getLeft(), obj2.getLeft(), obj1.getRight(), obj2.getRight());
  }

  public static Expr<? extends Sort> getAttribute(
      Expr<? extends Sort> obj,
      OCLType type,
      String attributeName,
      CD2SMTGenerator cd2SMTGenerator,
      boolean pre) {
    if (pre && !isPre(attributeName)) {
      attributeName = mkPre(attributeName);
    }
    return cd2SMTGenerator.getAttribute(
        CDHelper.getASTCDType(type.getName(), cd2SMTGenerator.getClassDiagram().getCDDefinition()),
        attributeName,
        obj);
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

  public static String mkPre(String s) {
    return s + "__pre";
  }

  public static boolean isPre(String s) {
    return s.endsWith("__pre");
  }

  public static ASTCDAssociation getPreAssoc(ASTCDAssociation association, ASTCDDefinition cd) {

    ASTCDAssocRightSide right = association.getRight();
    ASTCDAssocLeftSide left = association.getLeft();
    for (ASTCDAssociation preAssoc : cd.getCDAssociationsList()) {
      ASTCDAssocRightSide preRight = preAssoc.getRight();
      ASTCDAssocLeftSide preLeft = preAssoc.getLeft();
      if (right.getMCQualifiedType().equals(preRight.getMCQualifiedType())
          && right.getMCQualifiedType().equals(preRight.getMCQualifiedType())
          && mkPre(left.getCDRole().getName()).equals(preLeft.getCDRole().getName())
          && mkPre(right.getCDRole().getName()).equals(preRight.getCDRole().getName())) {
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
}
