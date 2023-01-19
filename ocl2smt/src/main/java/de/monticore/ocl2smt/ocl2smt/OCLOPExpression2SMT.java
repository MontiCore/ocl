package de.monticore.ocl2smt.ocl2smt;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Model;
import com.microsoft.z3.Sort;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.expressions.commonexpressions._ast.ASTBracketExpression;
import de.monticore.expressions.commonexpressions._ast.ASTFieldAccessExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.ocl.oclexpressions._ast.ASTIfThenElseExpression;
import de.monticore.ocl.oclexpressions._ast.ASTImpliesExpression;
import de.monticore.ocl.oclexpressions._ast.ASTOCLAtPreQualification;
import de.monticore.ocl2smt.helpers.OCLHelper;
import de.monticore.ocl2smt.util.OCLType;
import de.monticore.ocl2smt.util.OPDiffResult;
import de.monticore.ocl2smt.util.SMTSet;
import de.monticore.ocl2smt.util.TypeConverter;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;

import java.util.Optional;
import java.util.function.Function;

public class OCLOPExpression2SMT extends OCLExpression2SMT {
    protected OCL2SMTStrategy strategy = new OCL2SMTStrategy();

    public OCLOPExpression2SMT(OCLExpression2SMT expr) {
        super(expr.cd2smtGenerator.getClassDiagram(), expr.ctx);
    }

    public OPDiffResult splitPreOD(ASTODArtifact od, Model model) {
        return strategy.splitPreOD(od, model, constrData);
    }

    @Override
    protected Optional<Expr<? extends Sort>> convertGenExprOpt(ASTExpression node) {
        Expr<? extends Sort> res;
        if (node instanceof ASTLiteralExpression) {
            res = constConverter.convert((ASTLiteralExpression) node);
        } else if (node instanceof ASTBracketExpression) {
            res = convertBracket((ASTBracketExpression) node);
        } else if (node instanceof ASTNameExpression) {
            res = convertName((ASTNameExpression) node);
        } else if (node instanceof ASTFieldAccessExpression) {
            res = convertFieldAcc((ASTFieldAccessExpression) node);
        } else if (node instanceof ASTIfThenElseExpression) {
            res = convertIfTEl((ASTIfThenElseExpression) node);
        } else if (node instanceof ASTImpliesExpression) {
            res = convertImpl((ASTImpliesExpression) node);
        } else if (node instanceof ASTOCLAtPreQualification) {
            res = convertAt((ASTOCLAtPreQualification) node);
        } else {
            return Optional.empty();
        }
        return Optional.of(res);
    }


    protected Expr<? extends Sort> convertAt(ASTOCLAtPreQualification node) {
        strategy.enterPre();
        return convertExpr(node.getExpression());
    }

    @Override
    protected Expr<? extends Sort> convertName(ASTNameExpression node) {
        boolean isPre = strategy.isPreStrategy();
        strategy.exitPre();
        Expr<? extends Sort> res = null;

        if (constrData.containsVar(node.getName())) {
            res = constrData.getVar(node.getName());
        }
        if (constrData.isPresentContext()) {
            Optional<Expr<? extends Sort>> attr = getContextAttribute(node, isPre);
            if (attr.isPresent()) {
                res = attr.get();
            }
            Optional<Expr<? extends Sort>> obj = getContextLink(node, isPre);
            if (obj.isPresent()) {
                res = obj.get();
            }
        }

        if (res == null){
            res = declVariable(
                    TypeConverter.buildOCLType((VariableSymbol) node.getDefiningSymbol().get()),
                    node.getName());
        }

        return res;
    }

    @Override
    protected SMTSet convertFieldAccAssoc(ASTFieldAccessExpression node) {
        boolean isPre = strategy.isPreStrategy();
        strategy.exitPre();
        if (!(node.getExpression() instanceof ASTFieldAccessExpression)) {

            Expr<? extends Sort> auction = convertExpr(node.getExpression());
            OCLType type1 = constConverter.getType(auction);
            ASTCDAssociation association = OCLHelper.getAssociation(type1, node.getName(), getCD());
            OCLType type2 = OCLHelper.getOtherType(association, type1);

            Function<Expr<? extends Sort>, BoolExpr> auction_per_set =
                    per ->
                            strategy.evaluateLink(
                                    association, auction, per, cd2smtGenerator, constConverter, isPre);

            return new SMTSet(auction_per_set, type2);
        }

        SMTSet pSet = convertSet(node.getExpression());

        OCLType type1 = pSet.getType();
        ASTCDAssociation person_parent = OCLHelper.getAssociation(type1, node.getName(), getCD());
        OCLType type2 = OCLHelper.getOtherType(person_parent, type1);

        Function<Expr<? extends Sort>, SMTSet> function =
                obj1 ->
                        new SMTSet(
                                obj2 ->
                                        strategy.evaluateLink(
                                                person_parent, obj1, obj2, cd2smtGenerator, constConverter, isPre),
                                type2);

        return pSet.collectAll(function, ctx);
    }

    @Override
    protected Expr<? extends Sort> convertFieldAcc(ASTFieldAccessExpression node) {
        boolean isPre = strategy.isPreStrategy();
        strategy.exitPre();
        Expr<? extends Sort> obj = convertExpr(node.getExpression());
        OCLType type = constConverter.getType(obj);
        return strategy.getAttribute(obj, type, node.getName(), cd2smtGenerator, isPre);
    }

    private Optional<Expr<? extends Sort>> getContextAttribute(
            ASTNameExpression node, boolean isPre) {
        // TODO::update to takeCare when the attribute is inherited

        return Optional.ofNullable(
                strategy.getAttribute(
                        constrData.getOClContextValue(),
                        constrData.getOCLContextType(),
                        node.getName(),
                        cd2smtGenerator,
                        isPre));
    }

    /** this function is use to get a Linked object of the Context */
    private Optional<Expr<? extends Sort>> getContextLink(ASTNameExpression node, boolean isPre) {
        // TODO::update to takeCare when the assoc is inherited
        ASTCDAssociation association =
                OCLHelper.getAssociation(constrData.getOCLContextType(), node.getName(), getCD());
        if (association == null) {
            return Optional.empty();
        }

        // declare the linked object
        OCLType type2 = OCLHelper.getOtherType(association, constrData.getOCLContextType());
        String name = strategy.mkObjName(node.getName(), isPre);
        Expr<? extends Sort> expr = constConverter.declObj(type2, name);


        // add association constraints to the general constraints
        constrData.genConstraints.add(
                strategy.evaluateLink(
                        association,
                        constrData.getOClContextValue(),
                        expr,
                        cd2smtGenerator,
                        constConverter,
                        isPre));

        return Optional.of(expr);
    }

}
