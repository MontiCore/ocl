package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import de.monticore.ocl.ocl._ast.ASTOCLOperationSignature;
import de.monticore.ocl.ocl._ast.ASTOCLParamDeclaration;
import de.monticore.ocl2smt.ocl2smt.expressionconverter.FullOCLExpressionConverter;
import de.monticore.ocl2smt.ocl2smt.expressionconverter.OCLExpressionConverter;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPWitness;
import de.monticore.ocl2smt.ocldiff.operationDiff.OPConstraint;
import de.monticore.ocl2smt.util.OCLType;
import de.monticore.ocl2smt.util.TypeConverter;
import de.monticore.odbasis._ast.ASTODArtifact;
import java.util.List;
import java.util.Optional;

public class FullOCL2SMTGenerator {
  // TODO::  fix name

  protected FullOCLExpressionConverter fullExprConv;
  private final Context ctx;

  public FullOCL2SMTGenerator(ASTCDCompilationUnit ast, Context ctx) {
    fullExprConv = new FullOCLExpressionConverter(ast, ctx);
    this.ctx = ctx;
  }

  public Context getCtx() {
    return ctx;
  }

  public CD2SMTGenerator getCD2SMTGenerator() {
    return fullExprConv.getCd2smtGenerator();
  }

  // TODO:: fix context Decalration (OCLContextDefinition = MCType | GeneratorDeclaration
  // |OCLParamDeclaration)
  protected Expr<? extends Sort> convertCtxParDec(ASTOCLParamDeclaration node) {
    OCLType oclType = TypeConverter.buildOCLType(node.getMCType());
    return fullExprConv.declVariable(oclType, node.getName());
  }

  // TODO:: fix   OCLOperationSignature = OCLMethodSignature | OCLConstructorSignature
  private Expr<? extends Sort> openOpScope(
      ASTOCLOperationSignature node, OCLExpressionConverter opConverter) {
    ASTOCLMethodSignature method = (ASTOCLMethodSignature) node;

    OCLType type = OCLType.buildOCLType(method.getMethodName().getParts(0));
    // declare the object to which the method will be applied
    return opConverter.declVariable(type, type.getName() + "__This");
  }

  private BoolExpr convertPreCond(
      ASTOCLOperationConstraint node, FullOCLExpressionConverter opConverter) {
    opConverter.enterPreCond();

    // TODO:fix if many pre conditions
    BoolExpr pre = opConverter.convertBoolExpr(node.getPreCondition(0));
    for (BoolExpr constr : opConverter.genConstraints) {
      pre = ctx.mkAnd(pre, constr);
    }

    opConverter.exitPreCond();
    return pre;
  }

  private BoolExpr convertPostCond(
      ASTOCLOperationConstraint node, FullOCLExpressionConverter opConverter) {
    // TODO : fix if many Post conditions
    BoolExpr post = opConverter.convertBoolExpr(node.getPostCondition(0));
    for (BoolExpr constr : opConverter.genConstraints) {
      post = ctx.mkAnd(post, constr);
    }

    return post;
  }

  public OPConstraint convertOpConst(ASTOCLOperationConstraint node) {

    fullExprConv.init();

    Expr<? extends Sort> thisObj = openOpScope(node.getOCLOperationSignature(), fullExprConv);

    // convert pre and post conditions
    BoolExpr pre = convertPreCond(node, fullExprConv);
    BoolExpr post = convertPostCond(node, fullExprConv);

    IdentifiableBoolExpr preConstr =
        IdentifiableBoolExpr.buildIdentifiable(
            pre, node.getPreCondition(0).get_SourcePositionStart(), Optional.of("pre"));

    IdentifiableBoolExpr postConstr =
        IdentifiableBoolExpr.buildIdentifiable(
            post, node.getPostCondition(0).get_SourcePositionStart(), Optional.of("post"));
    Expr<? extends Sort> res = null;
    if (fullExprConv.varNames.containsKey("result")) {
      res = fullExprConv.varNames.get("result");
    }
    return new OPConstraint(
        preConstr,
        postConstr,
        res,
        thisObj,
        fullExprConv.constConverter.getType(res),
        fullExprConv.constConverter.getType(thisObj),
        ctx);
  }

  public Optional<ASTODArtifact> buildOd(Model model, String ODName, boolean partial) {
    return fullExprConv.getCd2smtGenerator().smt2od(model, partial, ODName);
  }

  public OCLOPWitness buildOPOd(
      Model model, String odName, ASTOCLMethodSignature method, boolean partial) {
    Optional<ASTODArtifact> od = buildOd(model, odName, partial);
    assert od.isPresent();
    return OCL2SMTStrategy.splitPreOD(method, od.get(), model, null); // FIXME
  }

  public Solver makeSolver(List<IdentifiableBoolExpr> constraints) {
    return fullExprConv.getCd2smtGenerator().makeSolver(constraints);
  }
}
