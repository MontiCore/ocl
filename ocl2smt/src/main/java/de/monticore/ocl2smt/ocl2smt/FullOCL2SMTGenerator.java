package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl2smt.ocl2smt.expressionconverter.FullOCLOPExpressionConverter;
import de.monticore.ocl2smt.ocl2smt.expressionconverter.OCLExpressionConverter;
import de.monticore.ocl2smt.ocldiff.operationDiff.OCLOPWitness;
import de.monticore.ocl2smt.ocldiff.operationDiff.OPConstraint;
import de.monticore.ocl2smt.util.OCLType;
import de.monticore.ocl2smt.util.TypeConverter;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.se_rwth.commons.SourcePosition;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

public class FullOCL2SMTGenerator {
    //TODO::  fix name

  protected  FullOCLOPExpressionConverter fullExprConv ;
  private final Context ctx;

        public FullOCL2SMTGenerator(ASTCDCompilationUnit ast, Context ctx) {
            fullExprConv = new FullOCLOPExpressionConverter(ast, ctx);
            this.ctx = ctx;
        }

        public OCL2SMTGenerator(ASTCDCompilationUnit ast, de.monticore.ocl2smt.ocl2smt.OCL2SMTGenerator ocl2SMTGenerator) {
            expression2SMT = new OCLExpressionConverter(ast, ocl2SMTGenerator);
            this.ctx = ocl2SMTGenerator.ctx;
        }

        public Context getCtx() {
            return ctx;
        }

        public CD2SMTGenerator getCD2SMTGenerator() {
            return expression2SMT.getCd2smtGenerator();
        }
        /**
         * Convert an ASTOCLArtifact in a Set of SMT BoolExpr
         *
         * @param astOclArtifact ocl Artifact to transform
         * @return the list of SMT BoolExpr

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
            Expr<? extends Sort> obj = expression2SMT.declVariable(oclType, node.getName());
            expression2SMT.setOCLContext(obj, oclType);
            return obj;
        }

        protected IdentifiableBoolExpr convertInv(ASTOCLInvariant invariant) {
            expression2SMT.init();
            SourcePosition srcPos = invariant.get_SourcePositionStart();

            // convert parameter declaration  in context
            Function<BoolExpr, BoolExpr> invCtx = openInvScope(invariant);

            // convert the inv body
            BoolExpr inv = invCtx.apply(expression2SMT.convertBoolExpr(invariant.getExpression()));

            // add general invConstraints
            for (BoolExpr constr : expression2SMT.getGenConstraint()) {
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

        // TODO:: fix   OCLOperationSignature = OCLMethodSignature | OCLConstructorSignature
        void openOpScope(ASTOCLOperationSignature node, OCLExpressionConverter opConverter) {
            ASTOCLMethodSignature method = (ASTOCLMethodSignature) node;

            OCLType type = OCLType.buildOCLType(method.getMethodName().getParts(0));
            // declare the object to which the method will be applied
            Expr<? extends Sort> obj = opConverter.declVariable(type, type.getName() + "__This");

            opConverter.setOCLContext(obj, type);
        }

        private BoolExpr convertPreCond(ASTOCLOperationConstraint node, FullOCLOPExpressionConverter opConverter) {
            opConverter.enterPreCond();

            // TODO:fix if many pre conditions
            BoolExpr pre = opConverter.convertBoolExpr(node.getPreCondition(0));
            for (BoolExpr constr : opConverter.getGenConstraint()) {
                pre = ctx.mkAnd(pre, constr);
            }

            opConverter.exitPreCond();
            return pre;
        }

        private BoolExpr convertPostCond(
                ASTOCLOperationConstraint node, FullOCLOPExpressionConverter opConverter) {
            // TODO : fix if many Post conditions
            BoolExpr post = opConverter.convertBoolExpr(node.getPostCondition(0));
            for (BoolExpr constr : opConverter.getGenConstraint()) {
                post = ctx.mkAnd(post, constr);
            }

            return post;
        }

        public OPConstraint convertOpConst(ASTOCLOperationConstraint node) {
            FullOCLOPExpressionConverter opConverter = new FullOCLOPExpressionConverter(expression2SMT);
            opConverter.init();
            expression2SMT = opConverter; // TODO: fix that
            openOpScope(node.getOCLOperationSignature(), opConverter);

            // convert pre and post conditions
            BoolExpr pre = convertPreCond(node, opConverter);
            BoolExpr post = convertPostCond(node, opConverter);

            IdentifiableBoolExpr preConstr =
                    IdentifiableBoolExpr.buildIdentifiable(
                            pre, node.getPreCondition(0).get_SourcePositionStart(), Optional.of("pre"));

            IdentifiableBoolExpr postConstr =
                    IdentifiableBoolExpr.buildIdentifiable(
                            post, node.getPostCondition(0).get_SourcePositionStart(), Optional.of("post"));

            return new OPConstraint(preConstr, postConstr);
        }

        public Optional<ASTODArtifact> buildOd(Model model, String ODName, boolean partial) {
            return expression2SMT.getCd2smtGenerator().smt2od(model, partial, ODName);
        }

        public OCLOPWitness buildOPOd(
                Model model, String odName, ASTOCLMethodSignature method, boolean partial) {
            Optional<ASTODArtifact> od = buildOd(model, odName, partial);
            assert od.isPresent();
            return OCL2SMTStrategy.splitPreOD(method, od.get(), model, expression2SMT.getConstrData());
        }

        public Solver makeSolver(List<IdentifiableBoolExpr> constraints) {
            return expression2SMT.getCd2smtGenerator().makeSolver(constraints);
        }
    }

}
