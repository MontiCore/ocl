/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl2smt.util.*;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.se_rwth.commons.SourcePosition;
import de.se_rwth.commons.logging.Log;
import java.util.*;
import java.util.function.Function;

public class OCL2SMTGenerator {
  private final OCLExpression2SMT expression2SMT;

  private final Context ctx;

  public OCL2SMTGenerator(ASTCDCompilationUnit ast, Context ctx) {

     expression2SMT = new OCLExpression2SMT(ast, ctx);

    this.ctx = ctx;
  }

  public OCL2SMTGenerator(
      ASTCDCompilationUnit ast, OCL2SMTGenerator ocl2SMTGenerator) {
    expression2SMT = new OCLExpression2SMT(ast, ocl2SMTGenerator);
    this.ctx = ocl2SMTGenerator.ctx;
  }

  public Context getCtx() {
    return ctx;
  }

  public CD2SMTGenerator getCD2SMTGenerator() {
    return expression2SMT.cd2smtGenerator;
  }
  /**
   * Convert an ASTOCLArtifact in a Set of SMT BoolExpr
   *
   * @param astOclArtifact ocl Artifact to transform
   * @return the list of SMT BoolExpr
   */
  public List<OCLConstraint> ocl2smt(ASTOCLArtifact astOclArtifact) {
    List<OCLConstraint> constraints = new ArrayList<>();
    for (ASTOCLConstraint constraint : astOclArtifact.getOCLConstraintList()) {
      constraints.add(convertConstr(constraint));
    }
    return constraints;
  }

  /**
   * convert a single OCLConstraint into A SMT BoolExpr
   *
   * @param constraint constraint OCLConstraint to Convert
   * @return the Constraint in SMT
   */
  public OCLConstraint convertConstr(ASTOCLConstraint constraint) {
    expression2SMT.init();
    OCLConstraint res = null;
    if (constraint instanceof ASTOCLInvariant) {
      res = convertInv((ASTOCLInvariant) constraint);
    } else if (constraint instanceof ASTOCLOperationConstraint) {
      res = convertOpConst((ASTOCLOperationConstraint) constraint);
    } else {
      Log.error(
          "the conversion of  ASTOCLConstraint of type   ASTOCLMethodSignature "
              + "and ASTOCLConstructorSignature in SMT is not implemented");
    }
    return res;
  }
  // TODO:: fix context Decalration (OCLContextDefinition = MCType | GeneratorDeclaration
  // |OCLParamDeclaration)
  protected Expr<? extends Sort> convertCtxParDec(ASTOCLParamDeclaration node) {
    OCLType oclType = TypeConverter.buildOCLType(node.getMCType());
    Expr<? extends Sort> obj = expression2SMT.declVariable(oclType, node.getName());
    expression2SMT.constrData.setOCLContext(obj, oclType);
    return obj;
  }

  protected OCLConstraint convertInv(ASTOCLInvariant invariant) {
    SourcePosition srcPos = invariant.get_SourcePositionStart();

    // convert parameter declaration  in context
    Function<BoolExpr, BoolExpr> invCtx = openInvScope(invariant);

    // convert the inv body
    BoolExpr inv = invCtx.apply(expression2SMT.convertBoolExpr(invariant.getExpression()));

    // add general invConstraints
    for (BoolExpr constr : expression2SMT.constrData.genConstraints) {
      inv = ctx.mkAnd(inv, constr);
    }

    Optional<String> name =
        invariant.isPresentName() ? Optional.ofNullable(invariant.getName()) : Optional.empty();

    return new OCLConstraint(IdentifiableBoolExpr.buildIdentifiable(inv, srcPos, name));
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
  // TODO:: fix   OCLOperationSignature = OCLMethodSignature | OCLConstructorSignature
  void openOpScope(ASTOCLOperationSignature node,OCLExpression2SMT opConverter) {
    ASTOCLMethodSignature method = (ASTOCLMethodSignature) node;

    OCLType type = OCLType.buildOCLType(method.getMethodName().getParts(0));
    // declare the object to which the method will be applied
    Expr<? extends Sort> obj = opConverter.declVariable(type, type.getName() + "__This");

    opConverter.constrData.setOCLContext(obj, type);
  }

  private BoolExpr convertPreCond(ASTOCLOperationConstraint node, OCLOPExpression2SMT opConverter) {
    //expression2SMT.strategy.enterPreCond();

    // TODO:fix if many pre conditions
    BoolExpr pre = opConverter.convertBoolExpr(node.getPreCondition(0));
    for (BoolExpr constr : opConverter.constrData.genConstraints) {
      pre = ctx.mkAnd(pre, constr);
    }

   // expression2SMT.strategy.exitPreCond();
    return pre;
  }

  private BoolExpr convertPostCond(ASTOCLOperationConstraint node,OCLOPExpression2SMT opConverter) {
    // TODO : fix if many Post conditions
    BoolExpr post = opConverter.convertBoolExpr(node.getPostCondition(0));
    for (BoolExpr constr : opConverter.constrData.genConstraints) {
      post = ctx.mkAnd(post, constr);
    }

    return post;
  }

  public OCLConstraint convertOpConst(ASTOCLOperationConstraint node) {
    OCLOPExpression2SMT opConverter = new OCLOPExpression2SMT(expression2SMT);
    openOpScope(node.getOCLOperationSignature(),opConverter);

    // convert pre and post conditions
    BoolExpr pre = convertPreCond(node,opConverter);
    BoolExpr post = convertPostCond(node,opConverter);

    IdentifiableBoolExpr preConstr =
        IdentifiableBoolExpr.buildIdentifiable(
            pre, node.getPreCondition(0).get_SourcePositionStart(), Optional.of("pre"));

    IdentifiableBoolExpr postConstr =
        IdentifiableBoolExpr.buildIdentifiable(
            post, node.getPostCondition(0).get_SourcePositionStart(), Optional.of("post"));

    return new OCLConstraint(preConstr, postConstr);
  }

  public Optional<ASTODArtifact> buildOd(Model model, String ODName, boolean partial) {
    return expression2SMT.cd2smtGenerator.smt2od(model, partial, ODName);
  }

}
