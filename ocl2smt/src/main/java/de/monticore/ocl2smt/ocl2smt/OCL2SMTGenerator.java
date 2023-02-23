/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl2smt.ocl2smt.expressionconverter.OCLExpressionConverter;
import de.monticore.ocl2smt.util.*;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.se_rwth.commons.SourcePosition;
import java.util.*;
import java.util.function.Function;

public class OCL2SMTGenerator {
  protected OCLExpressionConverter exprConv;
  private final Context ctx;

  public OCL2SMTGenerator(ASTCDCompilationUnit ast, Context ctx) {
    exprConv = new OCLExpressionConverter(ast, ctx);
    this.ctx = ctx;
  }

  public OCL2SMTGenerator(ASTCDCompilationUnit ast, OCL2SMTGenerator ocl2SMTGenerator) {
    exprConv = new OCLExpressionConverter(ast, ocl2SMTGenerator);
    this.ctx = ocl2SMTGenerator.ctx;
  }

  public Context getCtx() {
    return ctx;
  }

  public CD2SMTGenerator getCD2SMTGenerator() {
    return exprConv.getCd2smtGenerator();
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

  // TODO:: fix context Decalration (OCLContextDefinition = MCType | GeneratorDeclaration
  // |OCLParamDeclaration)
  protected Expr<? extends Sort> convertCtxParDec(ASTOCLParamDeclaration node) {
    OCLType oclType = TypeConverter.buildOCLType(node.getMCType());
    return exprConv.declVariable(oclType, node.getName());
  }

  protected IdentifiableBoolExpr convertInv(ASTOCLInvariant invariant) {
    exprConv.init();
    SourcePosition srcPos = invariant.get_SourcePositionStart();

    // convert parameter declaration  in context
    Function<BoolExpr, BoolExpr> invCtx = openInvScope(invariant);

    // convert the inv body
    BoolExpr inv = invCtx.apply(exprConv.convertBoolExpr(invariant.getExpression()));

    // add general invConstraints
    for (BoolExpr constr : exprConv.getGenConstraints()) {
      inv = ctx.mkAnd(inv, constr);
    }

    Optional<String> name =
        invariant.isPresentName() ? Optional.ofNullable(invariant.getName()) : Optional.empty();

    return IdentifiableBoolExpr.buildIdentifiable(inv, srcPos, name);
  }

  protected Function<BoolExpr, BoolExpr> openInvScope(ASTOCLInvariant invariant) {
    List<Expr<? extends Sort>> vars = new ArrayList<>();
    for (ASTOCLContextDefinition invCtx : invariant.getOCLContextDefinitionList()) {
      if (invCtx.isPresentOCLParamDeclaration()) {
        vars.add(convertCtxParDec(invCtx.getOCLParamDeclaration()));
      }
    }

    if (vars.size() > 0) {
      return bool -> ctx.mkForall(vars.toArray(new Expr[0]), bool, 0, null, null, null, null);
    }
    return bool -> bool;
  }

  public Optional<ASTODArtifact> buildOd(Model model, String ODName, boolean partial) {
    return exprConv.getCd2smtGenerator().smt2od(model, partial, ODName);
  }

  public Solver makeSolver(List<IdentifiableBoolExpr> constraints) {
    return exprConv.getCd2smtGenerator().makeSolver(constraints);
  }
}
