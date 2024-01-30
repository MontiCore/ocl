package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import de.monticore.ocl.ocl._ast.ASTOCLOperationSignature;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expressionconverter.FullOCLExpressionConverter;
import de.monticore.ocl2smt.ocl2smt.expressionconverter.OCLExprConverter;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPWitness;
import de.monticore.ocl2smt.ocldiff.operationDiff.OPConstraint;
import de.monticore.ocl2smt.util.OCLType;
import de.monticore.odbasis._ast.ASTODArtifact;
import java.util.List;
import java.util.Optional;

public class FullOCL2SMTGenerator<T extends ExprAdapter<?>> extends OCL2SMTGenerator<T> {

  public FullOCL2SMTGenerator(ASTCDCompilationUnit ast, Context ctx) {
    super(ast, ctx);
    exprConv = new FullOCLExpressionConverter<>(ast, ctx);
  }

  // TODO:: fix   OCLOperationSignature = OCLMethodSignature | OCLConstructorSignature
  private T openOpScope(ASTOCLOperationSignature node, OCLExprConverter<T> opConverter) {
    ASTOCLMethodSignature method = (ASTOCLMethodSignature) node;

    // set the type of the method result
    ((FullOCLExpressionConverter<T>) exprConv).setResultType(method.getMCReturnType());

    OCLType type = OCLType.buildOCLType(method.getMethodName().getParts(0));
    // declare the object to which the method will be applied
    return opConverter.mkConst(type.getName() + "__This", type);
  }

  private T convertPreCond(ASTOCLOperationConstraint node) {
    ((FullOCLExpressionConverter<T>) exprConv).enterPreCond();

    // TODO:fix if many pre conditions
    T pre = exprConv.convertExpr(node.getPreCondition(0));
    for (T constr : exprConv.getGenConstraints()) {
      pre = exprConv.factory.mkAnd(pre, constr);
    }

    ((FullOCLExpressionConverter<T>) exprConv).exitPreCond();
    return pre;
  }

  private T convertPostCond(ASTOCLOperationConstraint node) {
    // TODO : fix if many Post conditions
    T post = exprConv.convertExpr(node.getPostCondition(0));
    for (T constr : exprConv.getGenConstraints()) {
      post = exprConv.factory.mkAnd(post, constr);
    }

    return post;
  }

  public OPConstraint convertOpConst(ASTOCLOperationConstraint node) {

    exprConv.reset();

    T thisObj = openOpScope(node.getOCLOperationSignature(), exprConv);
    ((FullOCLExpressionConverter<T>) exprConv).setThisObj(thisObj);

    // convert pre and post conditions
    T pre = convertPreCond(node);
    T post = convertPostCond(node);

    IdentifiableBoolExpr preConstr =
        IdentifiableBoolExpr.buildIdentifiable(
            (BoolExpr) pre.getExpr(),
            node.getPreCondition(0).get_SourcePositionStart(),
            Optional.of("pre"));

    IdentifiableBoolExpr postConstr =
        IdentifiableBoolExpr.buildIdentifiable(
            (BoolExpr) post,
            node.getPostCondition(0).get_SourcePositionStart(),
            Optional.of("post"));

    return new OPConstraint(
        preConstr,
        postConstr,
        ((FullOCLExpressionConverter<T>) exprConv).getResult(),
        (BoolExpr) thisObj,
        exprConv.getType(thisObj),
        ctx);
  }

  public IdentifiableBoolExpr convertPreInv(ASTOCLInvariant invariant) {
    exprConv.reset();
    ((FullOCLExpressionConverter<T>) exprConv).enterPreCond();
    IdentifiableBoolExpr res = super.convertInv(invariant);
    ((FullOCLExpressionConverter<T>) exprConv).exitPreCond();
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
