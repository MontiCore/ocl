package de.monticore.ocl2smt.helpers;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdassociation._ast.*;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.ocl2smt.ocl2smt.ExpressionsConverter;
import de.monticore.ocl2smt.util.OCLType;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class OCLHelper {

  public static BoolExpr evaluateLink(
      ASTCDAssociation association,
      Expr<? extends Sort> obj1,
      Expr<? extends Sort> obj2,
      CD2SMTGenerator cd2SMTGenerator,
      ExpressionsConverter ec) {

    ASTCDDefinition cd = cd2SMTGenerator.getClassDiagram().getCDDefinition();

    ASTCDType left = CDHelper.getLeftType(association, cd);
    ASTCDType right = CDHelper.getRightType(association, cd);
    OCLType type1 = ec.getType(obj1);
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
      boolean pre,
      ExpressionsConverter ec) {
    if (pre) {
      association =
          OCLCDHelper.getPreAssociation(
              association, cd2SMTGenerator.getClassDiagram().getCDDefinition());
    }

    return evaluateLink(association, obj1, obj2, cd2SMTGenerator, ec);
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

  public static String mkPre(String s) {
    return s + "__pre";
  }

  public static boolean isPre(String s) {
    return s.endsWith("__pre");
  }

  public static String removePre(String s) {
    if (isPre(s)) {
      return s.substring(0, s.length() - 5);
    }
    return s;
  }
}
