package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.Z3ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.Z3ExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.Z3TypeAdapter;
import de.monticore.ocl2smt.ocl2smt.oclExpr2smt.FullOCLExprConverter;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPWitness;
import de.monticore.ocl2smt.ocldiff.operationDiff.OPConstraint;
import de.monticore.odbasis._ast.ASTODArtifact;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class FullOCL2SMTGenerator extends OCL2SMTGenerator {
  private final FullOCLExprConverter<Z3ExprAdapter> fullConv;

  public FullOCL2SMTGenerator(ASTCDCompilationUnit ast, Context ctx) {
    super(ast, ctx);
    CD2SMTGenerator cd2SMTGenerator = CD2SMTMill.cd2SMTGenerator();
    cd2SMTGenerator.cd2smt(ast, ctx);

    eFactory = new Z3ExprFactory(tFactory, cd2SMTGenerator);
    exprConv = new FullOCLExprConverter<>(eFactory, tFactory);
    fullConv = (FullOCLExprConverter<Z3ExprAdapter>) exprConv;
  }

  private Z3ExprAdapter convertPreCond(ASTOCLOperationConstraint node) {
    fullConv.enterPreCond();

    // TODO:fix if many pre conditions
    Z3ExprAdapter pre = exprConv.convertExpr(node.getPreCondition(0));

    fullConv.exitPreCond();
    return pre;
  }

  private Z3ExprAdapter convertPostCond(ASTOCLOperationConstraint node) {
    // TODO : fix if many Post conditions
    Z3ExprAdapter post = exprConv.convertExpr(node.getPostCondition(0));

    return post;
  }

  public OPConstraint convertOpConst(ASTOCLOperationConstraint node) {
    exprConv.reset();
    ASTOCLMethodSignature method = (ASTOCLMethodSignature) node.getOCLOperationSignature();

    Pair<Z3ExprAdapter, List<Z3ExprAdapter>> vars = openScope(node);
    List<String> methodParams = getMethodParams(method);

    fullConv.setThisObj(vars.getLeft());
    fullConv.setParams(methodParams, vars.getRight());

    // convert pre and post conditions
    Z3ExprAdapter pre = convertPreCond(node);
    Z3ExprAdapter post = convertPostCond(node);

    IdentifiableBoolExpr preConstr =
        IdentifiableBoolExpr.buildIdentifiable(
            (BoolExpr) pre.getExpr().simplify(),
            node.getPreCondition(0).get_SourcePositionStart(),
            Optional.of("pre"));

    IdentifiableBoolExpr postConstr =
        IdentifiableBoolExpr.buildIdentifiable(
            (BoolExpr) post.getExpr().simplify(),
            node.getPostCondition(0).get_SourcePositionStart(),
            Optional.of("post"));

    Z3ExprAdapter op = eFactory.mkImplies(pre, post);
    IdentifiableBoolExpr opConstraint =
        IdentifiableBoolExpr.buildIdentifiable(
            (BoolExpr) op.getExpr(), node.get_SourcePositionStart(), Optional.of("pre ==> Post"));

    return new OPConstraint(
        preConstr, postConstr, opConstraint, fullConv.getResult(), vars.getLeft());
  }

  private Pair<Z3ExprAdapter, List<Z3ExprAdapter>> openScope(ASTOCLOperationConstraint node) {
    ASTCDDefinition cd = cd2SMTGenerator.getClassDiagram().getCDDefinition();
    ASTOCLMethodSignature method = ((ASTOCLMethodSignature) node.getOCLOperationSignature());

    String typeName = method.getMethodName().getParts(0);
    ASTCDType type = CDHelper.getASTCDType(typeName, cd);
    List<Z3ExprAdapter> params = new ArrayList<>();

    for (ASTOCLParamDeclaration param : method.getOCLParamDeclarationList()) {
      Z3TypeAdapter paramType = tFactory.adapt(param.getMCType());
      Z3ExprAdapter paramExpr = eFactory.mkConst(param.getName(), paramType);
      params.add(paramExpr);
    }

    // declare the object to which the method will be applied
    Z3ExprAdapter caller = eFactory.mkConst("_this_" + typeName, tFactory.adapt(type));

    return new ImmutablePair<>(caller, params);
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
    return OCLHelper.splitPreOD(method, od.get(), model, opConstraint);
  }

  public Solver makeSolver(List<IdentifiableBoolExpr> constraints) {
    return cd2SMTGenerator.makeSolver(constraints);
  }

  private List<String> getMethodParams(ASTOCLMethodSignature method) {
    return method.getOCLParamDeclarationList().stream()
        .map(ASTOCLParamDeclaration::getName)
        .collect(Collectors.toList());
  }
}
