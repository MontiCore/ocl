/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt;

import static de.monticore.cd2smt.Helper.CDHelper.createCDSymTab;
import static de.monticore.cd2smt.Helper.CDHelper.getASTCDType;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.oclexpressions._ast.*;
import de.monticore.ocl.setexpressions._ast.*;
import de.monticore.ocl2smt.helpers.OCLCDHelper;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.util.*;
import de.monticore.ocl2smt.visitors.NameExpressionVisitor;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.se_rwth.commons.SourcePosition;
import de.se_rwth.commons.logging.Log;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
// TODO: add documentation
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

public class OCL2SMTGenerator {
  // TODO:strategy for conversion of @pre
private final OCLExpression2SMT expression2SMT ;
private final Context ctx ;


  public OCL2SMTGenerator(ASTCDCompilationUnit astcdCompilationUnit, Context ctx) {
    expression2SMT = new OCLExpression2SMT(astcdCompilationUnit,ctx);
    this.ctx   = ctx ;
  }

  public OCL2SMTGenerator(
      ASTCDCompilationUnit astcdCompilationUnit, OCL2SMTGenerator ocl2SMTGenerator) {
   expression2SMT = new OCLExpression2SMT(astcdCompilationUnit,ocl2SMTGenerator);
   this.ctx = ocl2SMTGenerator.ctx ;
  }

  public Context getCtx() {
    return ctx;
  }

 public CD2SMTGenerator getCD2SMTGenerator(){
    return expression2SMT.cd2smtGenerator;
 }

  public List<OCLConstraint> ocl2smt(ASTOCLArtifact astoclArtifact) {
    List<OCLConstraint> constraints = new ArrayList<>();
    for (ASTOCLConstraint constraint : astoclArtifact.getOCLConstraintList()) {
      constraints.add(convertConstr(constraint));
    }
    return constraints;
  }

  public OCLConstraint convertConstr(ASTOCLConstraint constraint) {
    ConstConverter.reset(ctx);
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


  protected Expr<? extends Sort> convertCtxParDec(ASTOCLParamDeclaration node) {
    OCLType oclType = TypeConverter.buildOCLType(node.getMCType());
    Expr<? extends Sort> obj = expression2SMT.declVariable(oclType, node.getName());
    expression2SMT.constrData.setOCLContext(obj, oclType); // TODO: do that in  the  ConvertInv
    return obj;
  }
  protected OCLConstraint convertInv(ASTOCLInvariant invariant) {
    SourcePosition srcPos = invariant.get_SourcePositionStart();

    // convert parameter declaration  in context
    Function<BoolExpr, BoolExpr> invCtx = openInvScope(invariant);

    // convert inv Body in the Invariant context
    BoolExpr inv = invCtx.apply(expression2SMT.convertBoolExpr(invariant.getExpression()));

    // add general invConstraints
    for (BoolExpr constr :expression2SMT.constrData.genConstraints) {
      inv = ctx.mkAnd(inv, constr);
    }

    Optional<String> name =
        invariant.isPresentName() ? Optional.ofNullable(invariant.getName()) : Optional.empty();

    return new OCLConstraint(IdentifiableBoolExpr.buildIdentifiable(inv, srcPos, name));
  }
  // TODO:: fix context Decalration (OCLContextDefinition = MCType | GeneratorDeclaration |
  // OCLParamDeclaration)
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
  void openOpScope(ASTOCLOperationSignature node) {
    ASTOCLMethodSignature method = (ASTOCLMethodSignature) node;

    OCLType type = OCLType.buildOCLType(method.getMethodName().getParts(0));
    Expr<? extends Sort> obj = expression2SMT.declVariable(type, type.getName() + "__");

    expression2SMT.constrData.setOCLContext(obj, type);
  }

  public BoolExpr convertPreCond(ASTOCLOperationConstraint node) {
    expression2SMT.constrData.initPre();
    BoolExpr pre = expression2SMT.convertBoolExpr(node.getPreCondition(0));
    for (BoolExpr constr : expression2SMT.constrData.genConstraints) {
      pre = ctx.mkAnd(pre, constr);
    }

    return pre;
  }

  public BoolExpr convertPostCond(ASTOCLOperationConstraint node) {
    expression2SMT.constrData.initPost();
    BoolExpr post = expression2SMT.convertBoolExpr(node.getPostCondition(0));
    for (BoolExpr constr : expression2SMT.constrData.genConstraints) {
      post = ctx.mkAnd(post, constr);
    }
    return post;
  }

  public OCLConstraint convertOpConst(ASTOCLOperationConstraint node) {
    expression2SMT.constrData.initOpConst();
    openOpScope(node.getOCLOperationSignature());

    BoolExpr pre = convertPreCond(node);
    BoolExpr post = convertPostCond(node);

    IdentifiableBoolExpr preConstr =
        IdentifiableBoolExpr.buildIdentifiable(
            pre, node.getPreCondition(0).get_SourcePositionStart(), Optional.of("pre"));

    IdentifiableBoolExpr postConstr =
        IdentifiableBoolExpr.buildIdentifiable(
            post, node.getPostCondition(0).get_SourcePositionStart(), Optional.of("post"));
    return new OCLConstraint(preConstr, postConstr);
  }


}
