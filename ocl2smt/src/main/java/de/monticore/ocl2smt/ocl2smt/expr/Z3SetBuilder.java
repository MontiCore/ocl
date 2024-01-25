/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocl2smt.expr;

import com.microsoft.z3.Context;
import de.monticore.ocl2smt.ocl2smt.expressionconverter.OCLExpressionConverter;
import de.monticore.ocl2smt.util.OCLType;
import de.se_rwth.commons.logging.Log;
import java.util.List;
import java.util.function.Function;

public class Z3SetBuilder extends Z3ExprBuilder {
  private final Function<ExprBuilder, ExprBuilder> setFunction;
  private final OCLType type;

  OCLExpressionConverter exprConv;

  public Z3SetBuilder(
      Function<ExprBuilder, ExprBuilder> setFunction,
      OCLType type,
      OCLExpressionConverter exprConv) {
    super(exprConv.getCd2smtGenerator().getContext());
    this.setFunction = setFunction;
    this.type = type;

    this.exprConv = exprConv;
  }

  private Z3SetBuilder mkSetOperation(Z3SetBuilder rightSet, OPERATION op) {
    if (!rightSet.type.equals(this.type)) {
      Log.error("set intersection of Set from different Type not implemented");
    }
    Function<ExprBuilder, ExprBuilder> setFunction;
    switch (op) {
      case UNION:
        setFunction =
            obj -> ExprMill.exprBuilder(ctx).mkOr(this.contains(obj), rightSet.contains(obj));
        break;
      case INTERSECTION:
        setFunction =
            obj -> ExprMill.exprBuilder(ctx).mkAnd(this.contains(obj), rightSet.contains(obj));
        break;
      case MINUS:
        setFunction =
            obj -> ExprMill.exprBuilder(ctx).mkAnd(this.contains(obj), rightSet.notIn(obj));
        break;
      default:
        Log.error("the Set Operation " + op + " is not implemented ");
        setFunction = s -> ExprMill.exprBuilder(ctx).mkBool(true);
    }
    return new Z3SetBuilder(setFunction, this.type, exprConv);
  }

  public Z3SetBuilder mkSetUnion(Z3SetBuilder rightSet) {
    return mkSetOperation(rightSet, OPERATION.UNION);
  }

  public Z3SetBuilder mkSetIntersect(Z3SetBuilder rightSet) {
    return mkSetOperation(rightSet, OPERATION.INTERSECTION);
  }

  public Z3SetBuilder mkSetMinus(Z3SetBuilder rightSet) {
    return mkSetOperation(rightSet, OPERATION.MINUS);
  }

  public OCLType getType() {
    return type;
  }

  public ExprBuilder contains(ExprBuilder expr) {
    /*  if (!literalConverter.getType(expr).equals(type)) {
      Log.error(
          "the obj "
              + expr
              + " with the Type "
              + literalConverter.getType(expr).getName()
              + " cannot be in the set  with sort "
              + type.getName());
    }*/
    return setFunction.apply(expr);
  }

  public ExprBuilder containsAll(Z3SetBuilder set) {
    ExprBuilder expr = exprConv.declVariable(set.getType(), "expr111");
    return exprConv.mkForall(
        List.of(expr),
        ExprMill.exprBuilder(ctx).mkImplies(set.contains(expr), this.contains(expr)));
  }

  public ExprBuilder isEmpty(Context ctx) {
    ExprBuilder expr = exprConv.declVariable(type, "expr11");
    return exprConv.mkForall(List.of(expr), this.notIn(expr));
  }

  public ExprBuilder notIn(ExprBuilder expr) {
    return ExprMill.exprBuilder(ctx).mkNot(contains(expr));
  }

  public Z3SetBuilder collectAll(Function<ExprBuilder, Z3SetBuilder> function) {
    ExprBuilder expr = exprConv.declVariable(type, "xollector");
    return new Z3SetBuilder(
        kii ->
            exprConv
                .mkExists(
                    List.of(expr),
                    ExprMill.exprBuilder(ctx)
                        .mkAnd(this.contains(expr), function.apply(expr).contains(kii)))
                .expr(),
        function.apply(expr).type,
        exprConv);
  }

  @Override
  public ExprBuilder mkEq(ExprBuilder set1, ExprBuilder set2) {

    ExprBuilder expr = exprConv.declVariable(this.getType(), "const");
    this.expr =
        exprConv
            .mkForall(
                List.of(expr),
                ExprMill.exprBuilder(ctx)
                    .mkEq(this.contains(expr), ((Z3SetBuilder) set2).contains(expr)))
            .expr();
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  @Override
  public ExprBuilder mkNeq(ExprBuilder set1, ExprBuilder set2) {
    ExprBuilder expr = exprConv.declVariable(this.getType(), "const");
    this.expr =
        ctx.mkNot(
            exprConv
                .mkForall(
                    List.of(expr),
                    ExprMill.exprBuilder(ctx)
                        .mkEq(this.contains(expr), ((Z3SetBuilder) set2).contains(expr)))
                .expr());
    this.kind = ExpressionKind.BOOL;
    return this;
  }

  enum OPERATION {
    UNION,
    INTERSECTION,
    MINUS
  }
}
