package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import de.monticore.ocl.ocl._ast.ASTOCLOperationSignature;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.ocl2smt.expr.ExprBuilder;
import de.monticore.ocl2smt.ocl2smt.expr.ExprMill;
import de.monticore.ocl2smt.ocl2smt.expressionconverter.FullOCLExpressionConverter;
import de.monticore.ocl2smt.ocl2smt.expressionconverter.OCLExpressionConverter;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPWitness;
import de.monticore.ocl2smt.ocldiff.operationDiff.OPConstraint;
import de.monticore.ocl2smt.util.OCLType;
import de.monticore.odbasis._ast.ASTODArtifact;
import java.util.List;
import java.util.Optional;

public class FullOCL2SMTGenerator extends OCL2SMTGenerator {

  public FullOCL2SMTGenerator(ASTCDCompilationUnit ast, Context ctx) {
    super(ast, ctx);
    exprConv = new FullOCLExpressionConverter(ast, ctx);
  }

  // TODO:: fix   OCLOperationSignature = OCLMethodSignature | OCLConstructorSignature
  private ExprBuilder openOpScope(
      ASTOCLOperationSignature node, OCLExpressionConverter opConverter) {
    ASTOCLMethodSignature method = (ASTOCLMethodSignature) node;

    // set the type of the method result
    ((FullOCLExpressionConverter) exprConv).setResultType(method.getMCReturnType());

    OCLType type = OCLType.buildOCLType(method.getMethodName().getParts(0));
    // declare the object to which the method will be applied
    return opConverter.declVariable(type, type.getName() + "__This");
  }

  private ExprBuilder convertPreCond(ASTOCLOperationConstraint node) {
    ((FullOCLExpressionConverter) exprConv).enterPreCond();

    // TODO:fix if many pre conditions
    ExprBuilder pre = exprConv.convertExpr(node.getPreCondition(0));
    for (ExprBuilder constr : exprConv.getGenConstraints()) {
      pre = ExprMill.exprBuilder(ctx).mkAnd(pre, constr);
    }

    ((FullOCLExpressionConverter) exprConv).exitPreCond();
    return pre;
  }

  private ExprBuilder convertPostCond(ASTOCLOperationConstraint node) {
    // TODO : fix if many Post conditions
    ExprBuilder post = exprConv.convertExpr(node.getPostCondition(0));
    for (ExprBuilder constr : exprConv.getGenConstraints()) {
      post = ExprMill.exprBuilder(ctx).mkAnd(post, constr);
    }

    return post;
  }

  public OPConstraint convertOpConst(ASTOCLOperationConstraint node) {

    exprConv.reset();

    ExprBuilder thisObj = openOpScope(node.getOCLOperationSignature(), exprConv);
    ((FullOCLExpressionConverter) exprConv).setThisObj(thisObj);

    // convert pre and post conditions
    ExprBuilder pre = convertPreCond(node);
    ExprBuilder post = convertPostCond(node);

    IdentifiableBoolExpr preConstr =
        IdentifiableBoolExpr.buildIdentifiable(
            pre.expr(), node.getPreCondition(0).get_SourcePositionStart(), Optional.of("pre"));

    IdentifiableBoolExpr postConstr =
        IdentifiableBoolExpr.buildIdentifiable(
            post.expr(), node.getPostCondition(0).get_SourcePositionStart(), Optional.of("post"));

    return new OPConstraint(
        preConstr,
        postConstr,
        ((FullOCLExpressionConverter) exprConv).getResult(),
        thisObj.expr(),
        exprConv.getType(thisObj),
        ctx);
  }

  public IdentifiableBoolExpr convertPreInv(ASTOCLInvariant invariant) {
    exprConv.reset();
    ((FullOCLExpressionConverter) exprConv).enterPreCond();
    IdentifiableBoolExpr res = super.convertInv(invariant);
    ((FullOCLExpressionConverter) exprConv).exitPreCond();
    return res;
  }

  public Optional<ASTODArtifact> buildOd(Model model, String ODName, boolean partial) {
    return exprConv.getCd2smtGenerator().smt2od(model, partial, ODName);
  }

  public OCLOPWitness buildOPOd(
      Model model,
      String odName,
      ASTOCLMethodSignature method,
      OPConstraint opConstraint,
      boolean partial) {
    Optional<ASTODArtifact> od = buildOd(model, odName, partial);
    assert od.isPresent();
    return OCLHelper.splitPreOD(method, od.get(), model, opConstraint);
  }

  public Solver makeSolver(List<IdentifiableBoolExpr> constraints) {
    return exprConv.getCd2smtGenerator().makeSolver(constraints);
  }
}
