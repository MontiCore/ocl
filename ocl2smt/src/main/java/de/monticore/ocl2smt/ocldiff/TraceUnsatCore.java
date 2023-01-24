/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocldiff;
;
import com.microsoft.z3.Solver;
import de.monticore.cd.facade.MCQualifiedNameFacade;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.Helper.ODHelper;
import de.monticore.od4report.OD4ReportMill;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odbasis._ast.ASTODAttribute;
import de.monticore.odbasis._ast.ASTODNamedObject;
import de.monticore.odlink._ast.ASTODLink;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TraceUnsatCore {

  public static ASTODArtifact buildUnsatOD(
      List<IdentifiableBoolExpr> posConstraints,
      List<IdentifiableBoolExpr> negConstraints,
      List<ASTODLink> unsatCore) {
    // add All positive invariant objects
    ASTODArtifact unsatOd =
        ODHelper.buildOD(
            "UNSAT_CORE_OD",
            posConstraints.stream()
                .map(TraceUnsatCore::buildInvObject)
                .collect(Collectors.toList()));
    // add All negative invariant objects
    negConstraints.forEach(i -> unsatOd.getObjectDiagram().addODElement(buildInvObject(i)));
    // add All links
    unsatOd.getObjectDiagram().addAllODElements(unsatCore);

    return unsatOd;
  }

  public static List<ASTODLink> traceUnsatCore(Solver solver) {
    List<ASTODLink> elementList = new ArrayList<>();
    List<IdentifiableBoolExpr> posConstraints = new ArrayList<>();
    List<IdentifiableBoolExpr> negConstraints = new ArrayList<>();

    // get the constraints from the id
    Arrays.stream(solver.getUnsatCore())
        .forEach(
            b -> {
              int i = Integer.parseInt(b.getSExpr().replace("|", ""));
              IdentifiableBoolExpr constraint = IdentifiableBoolExpr.getBoolExprIdentifiable(i);
              // add the constraints to the corresponding constraints list
              if (constraint.wasNegated()) {
                negConstraints.add(constraint);
              } else {
                posConstraints.add(constraint);
              }
            });
    if (posConstraints.size() == 0) {
      ASTODLink link =
          ODHelper.buildLink(
              getInvObjName(negConstraints.get(0)), getInvObjName(negConstraints.get(0)), "trace");
      link.setODLinkDirection(OD4ReportMill.oDLeftToRightDirBuilder().build());
      elementList.add(link);
    } else {
      // add links
      for (IdentifiableBoolExpr left : posConstraints) {
        for (IdentifiableBoolExpr right : negConstraints) {
          ASTODLink link = ODHelper.buildLink(getInvObjName(left), getInvObjName(right), "trace");
          link.setODLinkDirection(OD4ReportMill.oDLeftToRightDirBuilder().build());
          elementList.add(link);
        }
      }
    }
    return elementList;
  }

  protected static List<ASTODAttribute> buildInvODAttributeList(IdentifiableBoolExpr identifiable) {
    List<ASTODAttribute> attributeList = new ArrayList<>();
    attributeList.add(
        ODHelper.buildAttribute(
            "line",
            OD4ReportMill.mCPrimitiveTypeBuilder().setPrimitive(6).build(),
            "" + identifiable.getSourcePosition().getLine()));
    attributeList.add(
        ODHelper.buildAttribute(
            "file",
            OD4ReportMill.mCQualifiedTypeBuilder()
                .setMCQualifiedName(MCQualifiedNameFacade.createQualifiedName("Path"))
                .build(),
            '"' + identifiable.getFile().toString() + '"'));

    if (identifiable.getInvariantName().isPresent()) {
      attributeList.add(
          ODHelper.buildAttribute(
              "name",
              OD4ReportMill.mCQualifiedTypeBuilder()
                  .setMCQualifiedName(MCQualifiedNameFacade.createQualifiedName("String"))
                  .build(),
              '"' + identifiable.getInvariantName().get() + '"'));
    }
    return attributeList;
  }

  protected static ASTODNamedObject buildInvObject(IdentifiableBoolExpr oclConstraint) {
    return ODHelper.buildObject(
        getInvObjName(oclConstraint), "OCLInv", buildInvODAttributeList(oclConstraint));
  }

  protected static String getInvObjName(IdentifiableBoolExpr identifiable) {
    return "obj_" + identifiable.getInvariantName().orElse("") + "_" + identifiable.getId();
  }
}
