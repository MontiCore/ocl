package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import de.monticore.ocl.ocl._ast.ASTOCLOperationSignature;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.ocl2smt.expr2smt.Z3ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.Z3ExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.Z3TypeFactory;
import de.monticore.ocl2smt.ocl2smt.oclExpr2smt.FullOCLExprConverter;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPWitness;
import de.monticore.ocl2smt.ocldiff.operationDiff.OPConstraint;
import de.monticore.odbasis._ast.ASTODArtifact;
import java.util.List;
import java.util.Optional;

public class FullOCL2SMTGenerator extends OCL2SMTGenerator {
  private FullOCLExprConverter<Z3ExprAdapter, Sort> fullConv;

  public FullOCL2SMTGenerator(ASTCDCompilationUnit ast, Context ctx) {
    super(ast, ctx);
    CD2SMTGenerator cd2SMTGenerator = CD2SMTMill.cd2SMTGenerator();
    cd2SMTGenerator.cd2smt(ast, ctx);

    eFactory = new Z3ExprFactory(cd2SMTGenerator);
    tFactory = new Z3TypeFactory(cd2SMTGenerator);
    exprConv = new FullOCLExprConverter<>(eFactory, (Z3ExprFactory) eFactory, tFactory);
    fullConv = (FullOCLExprConverter<Z3ExprAdapter, Sort>) exprConv;
  }

  // TODO:: fix   OCLOperationSignature = OCLMethodSignature | OCLConstructorSignature
  private Z3ExprAdapter openOpScope(ASTOCLOperationSignature node) {
    ASTOCLMethodSignature method = (ASTOCLMethodSignature) node;

    // set the type of the method result
    ((FullOCLExprConverter<Z3ExprAdapter, Sort>) exprConv).setResultType(method.getMCReturnType());

    //  Z3ExprAdapter type = tFactory.adapt(method.getMethodName().getParts(0));
    // declare the object to which the method will be applied
    // return eFactory.mkConst(type + "__This", (Z3TypeAdapter)type);
    return null;
  }

  private Z3ExprAdapter convertPreCond(ASTOCLOperationConstraint node) {
    fullConv.enterPreCond();

    // TODO:fix if many pre conditions
    Z3ExprAdapter pre = exprConv.convertExpr(node.getPreCondition(0));
    for (Z3ExprAdapter constr : exprConv.getGenConstraints()) {
      pre = eFactory.mkAnd(pre, constr);
    }

    fullConv.exitPreCond();
    return pre;
  }

  private Z3ExprAdapter convertPostCond(ASTOCLOperationConstraint node) {
    // TODO : fix if many Post conditions
    Z3ExprAdapter post = exprConv.convertExpr(node.getPostCondition(0));
    for (Z3ExprAdapter constr : exprConv.getGenConstraints()) {
      post = eFactory.mkAnd(post, constr);
    }

    return post;
  }

  public OPConstraint convertOpConst(ASTOCLOperationConstraint node) {

    exprConv.reset();

    Z3ExprAdapter thisObj = openOpScope(node.getOCLOperationSignature());
    fullConv.setThisObj(thisObj);

    // convert pre and post conditions
    Z3ExprAdapter pre = convertPreCond(node);
    Z3ExprAdapter post = convertPostCond(node);

    IdentifiableBoolExpr preConstr =
        IdentifiableBoolExpr.buildIdentifiable(
            (BoolExpr) pre.getExpr(),
            node.getPreCondition(0).get_SourcePositionStart(),
            Optional.of("pre"));

    IdentifiableBoolExpr postConstr =
        IdentifiableBoolExpr.buildIdentifiable(
            (BoolExpr) post.getExpr(),
            node.getPostCondition(0).get_SourcePositionStart(),
            Optional.of("post"));

    Z3ExprAdapter op = eFactory.mkImplies(pre, post);
   IdentifiableBoolExpr opConstraint =
            IdentifiableBoolExpr.buildIdentifiable((BoolExpr) op.getExpr(), node.get_SourcePositionStart(), Optional.of("pre ==> Post"));

    return new OPConstraint(preConstr, postConstr,opConstraint ,fullConv.getResult(), thisObj);
  }

  public IdentifiableBoolExpr convertPreInv(ASTOCLInvariant invariant) {
    exprConv.reset();
    fullConv.enterPreCond();
    IdentifiableBoolExpr res = super.convertInv(invariant);
    fullConv.exitPreCond();
    return res;
  }

  public Optional<ASTODArtifact> buildOd(Model model, String ODName, boolean partial) {
    return cd2SMTGenerator.smt2od(model, partial, ODName);
  }

  public OCLOPWitness buildOPOd(
      Model model,
      String odName,
      ASTOCLMethodSignature method,
      OPConstraint opConstraint,
      boolean partial) {
    Optional<ASTODArtifact> od = buildOd(model, odName, partial);
    assert od.isPresent();
  return null ;   // return OCLHelper.splitPreOD(method, od.get(), model, opConstraint); todo fixme
  }

  public Solver makeSolver(List<IdentifiableBoolExpr> constraints) {
   // return exprConv.getCd2smtGenerator().makeSolver(constraints);
    return  null ; //todo fixme
  }
}
