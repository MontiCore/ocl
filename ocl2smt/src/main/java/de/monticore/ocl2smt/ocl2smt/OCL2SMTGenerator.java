/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl.setexpressions._ast.ASTGeneratorDeclaration;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.Z3ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.Z3ExprFactory;
import de.monticore.ocl2smt.ocl2smt.expr2smt.expr2z3.Z3TypeFactory;
import de.monticore.ocl2smt.ocl2smt.oclExpr2smt.OCLExprConverter;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.se_rwth.commons.SourcePosition;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class OCL2SMTGenerator {
  protected OCLExprConverter<Z3ExprAdapter> exprConv;
  protected Z3ExprFactory eFactory;
  protected Z3TypeFactory tFactory;
  protected CD2SMTGenerator cd2SMTGenerator;

  public OCL2SMTGenerator(ASTCDCompilationUnit ast, Context ctx) {
    cd2SMTGenerator = CD2SMTMill.cd2SMTGenerator();
    cd2SMTGenerator.cd2smt(ast, ctx);

    tFactory = new Z3TypeFactory(cd2SMTGenerator);
    eFactory = new Z3ExprFactory(tFactory, cd2SMTGenerator);
    exprConv = new OCLExprConverter<>(eFactory, tFactory);
  }

  public OCL2SMTGenerator(ASTCDCompilationUnit ast, OCL2SMTGenerator ocl2SMTGenerator) {
    tFactory = new Z3TypeFactory(ocl2SMTGenerator.getCD2SMTGenerator());
    eFactory = new Z3ExprFactory(tFactory, ocl2SMTGenerator.getCD2SMTGenerator());
    exprConv = new OCLExprConverter<>(eFactory, tFactory);
  }

  public CD2SMTGenerator getCD2SMTGenerator() {
    return cd2SMTGenerator;
  }
  /**
   * Convert an ASTOCLArtifact in a Set of SMT BoolExpr
   *
   * @param astOclArtifact ocl Artifact to transform
   * @return the list of SMT BoolExpr
   */
  public List<IdentifiableBoolExpr> inv2smt(ASTOCLArtifact astOclArtifact) {
    List<IdentifiableBoolExpr> constraints = new ArrayList<>();
    for (ASTOCLConstraint constraint : astOclArtifact.getOCLConstraintList()) {
      if (constraint instanceof ASTOCLInvariant)
        constraints.add(convertInv((ASTOCLInvariant) constraint));
    }
    return constraints;
  }

  protected Z3ExprAdapter convertCtxParDec(ASTOCLParamDeclaration node) {
    return exprConv.mkConst(node.getName(), tFactory.adapt(node.getMCType()));
  }

  protected Pair<Z3ExprAdapter, Z3ExprAdapter> convertGenDec(ASTGeneratorDeclaration node) {
    Z3ExprAdapter expr =
        exprConv.mkConst(node.getName(), tFactory.adapt(node.getSymbol().getType()));
    Z3ExprAdapter set = exprConv.convertExpr(node.getExpression());

    return new ImmutablePair<>(expr, eFactory.mkContains(set, expr));
  }

  public IdentifiableBoolExpr convertInv(ASTOCLInvariant invariant) {
    SourcePosition srcPos = invariant.get_SourcePositionStart();

    // convert parameter declaration  in context
    Function<Z3ExprAdapter, Z3ExprAdapter> invCtx = openInvScope(invariant);

    // convert the inv body
    Z3ExprAdapter inv = invCtx.apply(exprConv.convertExpr(invariant.getExpression()));

    Z3ExprAdapter fullInv = eFactory.mkAnd(inv, mkAnd(inv.getGenConstraint()));

    Optional<String> name =
        invariant.isPresentName() ? Optional.ofNullable(invariant.getName()) : Optional.empty();
    exprConv.reset();
    return IdentifiableBoolExpr.buildIdentifiable(
        (BoolExpr) fullInv.getExpr().simplify(), srcPos, name);
  }

  protected Function<Z3ExprAdapter, Z3ExprAdapter> openInvScope(ASTOCLInvariant invariant) {
    List<Z3ExprAdapter> vars = new ArrayList<>();
    Z3ExprAdapter varConstraint = eFactory.mkBool(true);
    for (ASTOCLContextDefinition invCtx : invariant.getOCLContextDefinitionList()) {
      if (invCtx.isPresentOCLParamDeclaration()) {
        vars.add(convertCtxParDec(invCtx.getOCLParamDeclaration()));
      }
      if (invCtx.isPresentGeneratorDeclaration()) {
        Pair<Z3ExprAdapter, Z3ExprAdapter> res = convertGenDec(invCtx.getGeneratorDeclaration());
        varConstraint = eFactory.mkAnd(varConstraint, res.getRight());
        vars.add(res.getLeft());
      }
    }
    Z3ExprAdapter varConstraint2 = varConstraint;
    if (!vars.isEmpty()) {
      return bool -> eFactory.mkForall(vars, eFactory.mkImplies(varConstraint2, bool));
    }
    return bool -> bool;
  }

  public Optional<ASTODArtifact> buildOd(Model model, String ODName, boolean partial) {
    return cd2SMTGenerator.smt2od(model, partial, ODName);
  }

  public Solver makeSolver(List<IdentifiableBoolExpr> constraints) {
    return cd2SMTGenerator.makeSolver(constraints);
  }

  public Context getCtx() {
    return cd2SMTGenerator.getContext();
  }

  private Z3ExprAdapter mkAnd(List<Z3ExprAdapter> constraints) {
    return constraints.stream().reduce(eFactory.mkBool(true), eFactory::mkAnd);
  }

  public void closeCtx() {
    if (getCtx() != null) {
      try {
        getCtx().close();
        // Might throw an error if the ctx was never opened ("Context closed")
      } catch (Z3Exception ignored) {
      }
    }
  }
}
