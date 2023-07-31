package de.monticore.ocl2smt.ocldiff;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.monticore.ocl2smt.helpers.IOHelper;
import de.monticore.ocl2smt.ocldiff.invariantDiff.FiniteOCLInvariantDiff;
import de.monticore.ocl2smt.ocldiff.invariantDiff.OCLInvDiffResult;
import de.monticore.ocl2smt.ocldiff.invariantDiff.OCLInvariantDiff;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPDiffResult;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPWitness;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOperationDiff;
import de.monticore.ocl2smt.util.OCL_Loader;
import de.monticore.od2smt.OD2SMTGenerator;
import de.monticore.od2smt.OD2SMTUtils;
import de.monticore.odbasis._ast.ASTODArtifact;

import java.io.File;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class OCLDiffGenerator {

  /**
   * Converts CD + OCL Model in SMT and produces a witness Object Diagram as proof for the
   * consistency.
   *
   * @param cdFile the class diagram.
   * @param oclFiles the Set of OCl constraints.
   * @param posODFiles as set of positive example as object diagram (optional)
   * @param negODFiles as set of negative example as object diagram (optional)
   * @param partial if partial == true, the Object diagram will be partial regarding the attribute.
   */
  public static void oclWitness(
      File cdFile,
      Set<File> oclFiles,
      Set<File> posODFiles,
      Set<File> negODFiles,
      boolean partial,
      Path output) {

    ASTCDCompilationUnit cd = IOHelper.parseCD(cdFile);
    Set<ASTOCLCompilationUnit> ocl = IOHelper.parseOCl(cdFile, oclFiles);
    OCLInvariantDiff operator = new OCLInvariantDiff();
    Context ctx = buildContext();
    Set<IdentifiableBoolExpr> additionalConstraints = od2Constraints(posODFiles, cd, ctx);
    additionalConstraints.addAll(negativeOd2smt(negODFiles, cd, ctx));

    ASTODArtifact witness = operator.oclWitness(cd, ocl, additionalConstraints, ctx, partial);
    IOHelper.printOD(witness, output);
  }

  /**
   * Compute the semantic difference (of OCL-invariant) between a new ocl-Model and an old one. The
   * result is a set of witness object diagram and specifications tracing.
   *
   * @param cdFile the class diagram.
   * @param oldOCLFiles the old OCL-Model.
   * @param newOCLFiles the new OCl-Model.
   * @param posODFiles as set of positive example as object diagram (optional)
   * @param negODFiles as set of negative example as object diagram (optional)
   * @param partial if partial == true, the Object diagram will be partial regarding the attributes.
   */
  public static void oclDiff(
      File cdFile,
      Set<File> oldOCLFiles,
      Set<File> newOCLFiles,
      Set<File> posODFiles,
      Set<File> negODFiles,
      boolean partial,
      Path output) {

    Set<ASTOCLCompilationUnit> oldOCL = IOHelper.parseOCl(cdFile, oldOCLFiles);
    Set<ASTOCLCompilationUnit> newOCL = IOHelper.parseOCl(cdFile, newOCLFiles);
    ASTCDCompilationUnit cd = IOHelper.parseCD(cdFile);
    OCLInvariantDiff operator = new OCLInvariantDiff();
    Context ctx = buildContext();
    Set<IdentifiableBoolExpr> additionalConstraints = od2Constraints(posODFiles, cd, ctx);
    additionalConstraints.addAll(negativeOd2smt(negODFiles, cd, ctx));

    OCLInvDiffResult res =
        operator.oclDiff(cd, oldOCL, newOCL, additionalConstraints, ctx, partial);

    IOHelper.printInvDiffResult(res, output);
  }

  /**
   * Computes the semantic difference (of OCL-invariant) between a new CD/OCL-Model and an old one.
   * The result is a set of witness Object Diagrams and specifications tracing.
   *
   * @param oldCdFile the old class diagram.
   * @param newCdFile the new class diagram.
   * @param newOCLFiles the new OCl-Model.
   * @param oldOCLFiles the old OCL-Model.
   * @param posODFiles as set of positive example as object diagram (optional)
   * @param negODFiles as set of negative example as object diagram (optional)
   * @param partial if partial == true, the Object Diagram will be partial regarding the attributes.
   */
  public static void oclDiff(
      File oldCdFile,
      File newCdFile,
      Set<File> oldOCLFiles,
      Set<File> newOCLFiles,
      Set<File> posODFiles,
      Set<File> negODFiles,
      boolean partial,
      Path output) {

    Set<ASTOCLCompilationUnit> oldOCL = IOHelper.parseOCl(newCdFile, oldOCLFiles);
    Set<ASTOCLCompilationUnit> newOCL = IOHelper.parseOCl(newCdFile, newOCLFiles);
    ASTCDCompilationUnit oldCD = IOHelper.parseCD(oldCdFile);
    ASTCDCompilationUnit newCD = IOHelper.parseCD(newCdFile);
    Context ctx = buildContext();
    Set<IdentifiableBoolExpr> additionalConstraints = od2Constraints(posODFiles, newCD, ctx);
    additionalConstraints.addAll(negativeOd2smt(negODFiles, newCD, ctx));

    OCLInvariantDiff operator = new OCLInvariantDiff();
    OCLInvDiffResult res =
        operator.CDOCLDiff(oldCD, newCD, oldOCL, newOCL, additionalConstraints, ctx, partial);

    IOHelper.printInvDiffResult(res, output);
  }

  /**
   * Converts CD + OCL-Model with Operation constraints in SMT and produces a set of witness Object
   * Diagrams as witness for the operation.
   *
   * @param cdFile the class diagram.
   * @param oclFiles Set of OCl constraints.
   * @param methodName method whose constraints must be analyzed.
   * @param partial if partial == true, the Object Diagrams will be partial regarding the
   *     attributes.
   */
  public static void oclOPWitness(
      File cdFile, Set<File> oclFiles, String methodName, boolean partial, Path output) {

    ASTCDCompilationUnit cd = IOHelper.parseCD(cdFile);
    Set<ASTOCLCompilationUnit> ocl = IOHelper.parseOCl(cdFile, oclFiles);
    ASTOCLMethodSignature method = IOHelper.getMethodSignature(ocl, methodName);
    OCLOperationDiff operator = new OCLOperationDiff();
    Set<OCLOPWitness> res = operator.oclWitness(cd, ocl, method, partial);
    IOHelper.printOPWitness(res, output);
  }

  /**
   * Converts CD + OCL-Model with Operation constraints in SMT and produces a set of witness Object
   * Diagrams as witness for the operations.
   *
   * @param cdFile the class diagram.
   * @param oclFiles Set of OCl constraints.
   * @param partial if partial == true, the Object diagram will be partial regarding the attributes.
   */
  public static void oclOPWitness(File cdFile, Set<File> oclFiles, boolean partial, Path output) {

    OCLOperationDiff operator = new OCLOperationDiff();
    ASTCDCompilationUnit cd = IOHelper.parseCD(cdFile);
    Set<ASTOCLCompilationUnit> ocl = IOHelper.parseOCl(cdFile, oclFiles);
    Set<OCLOPWitness> res = operator.oclWitness(cd, ocl, partial);
    IOHelper.printOPWitness(res, output);
  }

  /**
   * Computes the semantic difference(of an operation constraint) between a new OCL-Model and an old
   * one. * All the preconditions of operation constraints of the new model must hold The result is
   * a set of witness object diagrams and specifications tracing.
   *
   * @param cdFile the new class diagram.
   * @param newOclFiles the new OCl-Model.
   * @param oldOclFiles the old OCL-Model.
   * @param methodName the method for which the diff of the constraints has to be calculated.
   * @param partial if partial == true, the Object Diagrams will be partial regarding the
   *     attributes.
   */
  public static void oclOPDiffV1(
      File cdFile,
      Set<File> oldOclFiles,
      Set<File> newOclFiles,
      String methodName,
      boolean partial,
      Path output) {

    ASTCDCompilationUnit cd = IOHelper.parseCD(cdFile);
    Set<ASTOCLCompilationUnit> oldOcl = IOHelper.parseOCl(cdFile, oldOclFiles);
    Set<ASTOCLCompilationUnit> newOcl = IOHelper.parseOCl(cdFile, newOclFiles);
    ASTOCLMethodSignature method = IOHelper.getMethodSignature(newOcl, methodName);
    OCLOperationDiff operator = new OCLOperationDiff();
    OCLOPDiffResult res = operator.oclDiffV1(cd, oldOcl, newOcl, method, partial);
    IOHelper.printOPDiff(res, output);
  }

  /**
   * Computes the semantic difference(of an operation constraint) between a new OCL-Model and an old
   * one. All the preconditions of operation constraints of the new model don't hold. The result is
   * a set of witness object diagrams and specifications tracing.
   *
   * @param cdFile the new class diagram.
   * @param newOclFiles the new OCl-Model.
   * @param oldOclFiles the old OCL-Model.
   * @param methodName the method for which the diff of the constraints has to be calculated.
   * @param partial if partial == true, the Object Diagrams will be partial regarding the
   *     attributes.
   */
  public static void oclOPDiffV2(
      File cdFile,
      Set<File> oldOclFiles,
      Set<File> newOclFiles,
      String methodName,
      boolean partial,
      Path output) {

    ASTCDCompilationUnit cd = IOHelper.parseCD(cdFile);
    Set<ASTOCLCompilationUnit> oldOcl = IOHelper.parseOCl(cdFile, oldOclFiles);
    Set<ASTOCLCompilationUnit> newOcl = IOHelper.parseOCl(cdFile, newOclFiles);
    ASTOCLMethodSignature method = IOHelper.getMethodSignature(newOcl, methodName);
    OCLOperationDiff operator = new OCLOperationDiff();
    OCLOPDiffResult res = operator.oclDiffV2(cd, oldOcl, newOcl, method, partial);
    IOHelper.printOPDiff(res, output);
  }

  /**
   * Converts CD + OCL Model in SMT and produces a witness Object Diagram as proof for the
   * consistency.
   *
   * @param cd the class diagram.
   * @param ocl the Set of OCl constraints.
   * @param partial if partial == true, the Object diagram will be partial regarding the attribute.
   * @return the witness Object Diagram.
   */
  public static ASTODArtifact oclWitness(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> ocl,
      Set<File> posODFiles,
      Set<File> negODFiles,
      boolean partial) {
    OCLInvariantDiff operator = new OCLInvariantDiff();
    Context ctx = buildContext();
    Set<IdentifiableBoolExpr> additionalConstraints = od2Constraints(posODFiles, cd, ctx);
    additionalConstraints.addAll(negativeOd2smt(negODFiles, cd, ctx));

    return operator.oclWitness(cd, ocl, additionalConstraints, ctx, partial);
  }

  /**
   * Compute the semantic difference (of OCL-invariant) between a new ocl-Model and an old one. The
   * result is a set of witness object diagram and specifications tracing.
   *
   * @param cd the class diagram.
   * @param oldOcl the old OCL-Model.
   * @param newOCL the new OCl-Model.
   * @param posOD as set of positive example as object diagram (optional)
   * @param negOD as set of negative example as object diagram (optional)
   * @param partial if partial == true, the Object diagram will be partial regarding the attributes.
   * @return the diff witness
   */
  public static OCLInvDiffResult oclDiff(
          ASTCDCompilationUnit cd,
          Set<ASTOCLCompilationUnit> oldOcl,
          Set<ASTOCLCompilationUnit> newOCL,
          Set<ASTODArtifact> posOD,
          Set<ASTODArtifact> negOD,
          boolean partial) {

      OCLInvariantDiff operator = new OCLInvariantDiff();
      Context ctx = buildContext();
      Set<IdentifiableBoolExpr> additionalConstraints = od2constraints(posOD, cd, ctx);
      additionalConstraints.addAll(negativOd2smt(negOD, cd, ctx));

      return operator.oclDiff(cd, oldOcl, newOCL, additionalConstraints, ctx, partial);
  }

    /**
     * Compute the semantic difference (of OCL-invariant) between a new ocl-Model and an old one. The
     * result is a set of witness object diagram and specifications tracing.
     *
     * @param cd      the class diagram.
     * @param oldOcl  the old OCL-Model.
     * @param newOCL  the new OCl-Model.
     * @param posOD   as set of positive example as object diagram (optional)
     * @param negOD   as set of negative example as object diagram (optional)
     * @param partial if partial == true, the Object diagram will be partial regarding the attributes.
     * @return the diff witness
     */
    public static OCLInvDiffResult oclDiffFinite(
            ASTCDCompilationUnit cd,
            Set<ASTOCLCompilationUnit> oldOcl,
            Set<ASTOCLCompilationUnit> newOCL,
            Set<ASTODArtifact> posOD,
            Set<ASTODArtifact> negOD,
            long max,
            boolean partial) {

        FiniteOCLInvariantDiff operator = new FiniteOCLInvariantDiff();
        Context ctx = buildContext();
        Set<IdentifiableBoolExpr> additionalConstraints = od2constraints(posOD, cd, ctx);
        additionalConstraints.addAll(negativOd2smt(negOD, cd, ctx));

        return operator.oclDiffFinite(cd, oldOcl, newOCL, additionalConstraints, ctx, max, partial);
    }

  /**
   * Computes the semantic difference (of OCL-invariant) between a new CD/OCL-Model and an old one.
   * The result is a set of witness Object Diagrams and specifications tracing.
   *
   * @param oldCD the old class diagram.
   * @param newCD the new class diagram.
   * @param newOCL the new OCl-Model.
   * @param oldOCL the old OCL-Model.
   * @param posOD as set of positive example as object diagram (optional)
   * @param negOD as set of negative example as object diagram (optional)
   * @param partial if partial == true, the Object Diagram will be partial regarding the attributes.
   * @return the diff witness
   */
  public static OCLInvDiffResult oclDiff(
      ASTCDCompilationUnit oldCD,
      ASTCDCompilationUnit newCD,
      Set<ASTOCLCompilationUnit> oldOCL,
      Set<ASTOCLCompilationUnit> newOCL,
      Set<ASTODArtifact> posOD,
      Set<ASTODArtifact> negOD,
      boolean partial) {
    OCLInvariantDiff operator = new OCLInvariantDiff();
    Context ctx = buildContext();
    Set<IdentifiableBoolExpr> additionalConstraints = od2constraints(posOD, newCD, ctx);
    additionalConstraints.addAll(negativOd2smt(negOD, newCD, ctx));

    return operator.CDOCLDiff(oldCD, newCD, oldOCL, newOCL, additionalConstraints, ctx, partial);
  }

  /**
   * Converts CD + OCL-Model with Operation constraints in SMT and produces a set of witness Object
   * Diagrams as witness for the operation.
   *
   * @param cd the class diagram.
   * @param ocl Set of OCl constraints.
   * @param method method whose constraints must be analyzed.
   * @param partial if partial == true, the Object Diagrams will be partial regarding the
   *     attributes.
   * @return the witness Object Diagram.
   */
  public static Set<OCLOPWitness> oclOPWitness(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> ocl,
      ASTOCLMethodSignature method,
      boolean partial) {
    OCLOperationDiff operator = new OCLOperationDiff();
    return operator.oclWitness(cd, ocl, method, partial);
  }

  /**
   * Converts CD + OCL-Model with Operation constraints in SMT and produces a set of witness Object
   * Diagrams as witness for the operations.
   *
   * @param cd the class diagram.
   * @param ocl Set of OCl constraints.
   * @param partial if partial == true, the Object diagram will be partial regarding the attributes.
   * @return the witness Object Diagram.
   */
  public static Set<OCLOPWitness> oclOPWitness(
      ASTCDCompilationUnit cd, Set<ASTOCLCompilationUnit> ocl, boolean partial) {
    OCLOperationDiff operator = new OCLOperationDiff();
    return operator.oclWitness(cd, ocl, partial);
  }

  /***
   * Computes the semantic difference(of an operation constraint) between a new OCL-Model and an old one.
   * All the preconditions of operation constraints of the new model don't hold.
   * The result is a set of witness object diagrams and specifications tracing.
   *
   * @param cd the new class diagram.
   * @param newOcl the new OCl-Model.
   * @param oldOcl the old OCL-Model.
   * @param method the method for which the diff of the constraints has to be calculated.
   * @param partial if partial == true, the Object Diagrams will be partial regarding the
   *     attributes.
   * @return the diff witness
   */
  public static OCLOPDiffResult oclOPDiffV2(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> oldOcl,
      Set<ASTOCLCompilationUnit> newOcl,
      ASTOCLMethodSignature method,
      boolean partial) {

    OCLOperationDiff operator = new OCLOperationDiff();
    return operator.oclDiffV2(cd, oldOcl, newOcl, method, partial);
  }

  /***
   * Computes the semantic difference(of an operation constraint) between a new OCL-Model and an old one.
   * All the preconditions of operation constraints of the new model must hold.
   * The result is a set of witness object diagrams and specifications tracing.
   *
   * @param cd the new class diagram.
   * @param newOcl the new OCl-Model.
   * @param oldOcl the old OCL-Model.
   * @param method the method for which the diff of the constraints has to be calculated.
   * @param partial if partial == true, the Object Diagrams will be partial regarding the
   *     attributes.
   * @return the diff witness
   */
  public static OCLOPDiffResult oclOPDiffV1(
      ASTCDCompilationUnit cd,
      Set<ASTOCLCompilationUnit> oldOcl,
      Set<ASTOCLCompilationUnit> newOcl,
      ASTOCLMethodSignature method,
      boolean partial) {

    OCLOperationDiff operator = new OCLOperationDiff();
    return operator.oclDiffV1(cd, oldOcl, newOcl, method, partial);
  }

  private static Set<IdentifiableBoolExpr> od2constraints(
      Set<ASTODArtifact> ods, ASTCDCompilationUnit cd, Context ctx) {
    Set<IdentifiableBoolExpr> constraint = new HashSet<>();
    for (ASTODArtifact od : ods) {
      OD2SMTGenerator od2SMTGenerator = new OD2SMTGenerator();
      od2SMTGenerator.od2smt(od, cd, ctx);

      constraint.addAll(od2SMTGenerator.getAllODConstraints());
    }
    return constraint;
  }

  private static Set<IdentifiableBoolExpr> negativeOd2smt(
      Set<File> ods, ASTCDCompilationUnit cd, Context ctx) {
    return negativOd2smt(
        ods.stream().map(OCL_Loader::loadAndCheckOD).collect(Collectors.toSet()), cd, ctx);
  }

  private static Set<IdentifiableBoolExpr> negativOd2smt(
      Set<ASTODArtifact> ods, ASTCDCompilationUnit cd, Context ctx) {
    Set<IdentifiableBoolExpr> res = new HashSet<>();
    for (ASTODArtifact od : ods) {
      OD2SMTGenerator od2SMTGenerator = new OD2SMTGenerator();
      od2SMTGenerator.od2smt(od, cd, ctx);

      // get all constraints
      BoolExpr[] constraints =
          od2SMTGenerator.getAllODConstraints().stream()
              .map(IdentifiableBoolExpr::getValue)
              .toArray(BoolExpr[]::new);

      // get all objects
      Expr<?>[] vars =
          OD2SMTUtils.getObjectList(od).stream()
              .map(od2SMTGenerator::getObject)
              .toArray(Expr[]::new);

      // build the negative constraint
      BoolExpr constr =
          ctx.mkNot(ctx.mkExists(vars, ctx.mkAnd(constraints), 0, null, null, null, null));

      res.add(
          IdentifiableBoolExpr.buildIdentifiable(
              constr, od.get_SourcePositionStart(), Optional.of(od.getObjectDiagram().getName())));
    }

    return res;
  }

  private static Set<IdentifiableBoolExpr> od2Constraints(
      Set<File> ods, ASTCDCompilationUnit cd, Context ctx) {
    return od2constraints(
        ods.stream().map(OCL_Loader::loadAndCheckOD).collect(Collectors.toSet()), cd, ctx);
  }

  private static Context buildContext() {
    Map<String, String> cfg = new HashMap<>();
    cfg.put("model", "true");
    return new Context(cfg);
  }
}
