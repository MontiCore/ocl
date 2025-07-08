package de.monticore.expressions.commonexpressions;

import de.monticore.expressions.commonexpressions._ast.ASTEqualsExpression;
import de.monticore.expressions.commonexpressions._ast.ASTFieldAccessExpression;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsHandler;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsTraverser;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsVisitor2;
import de.monticore.refadaptation.AbstractAdaptationVisitor;
import de.monticore.refadaptation.Binding;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.TypeCheck3;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class CommonExpressionsBindingVariantsVisitor
        extends AbstractAdaptationVisitor<CommonExpressionsAdaptationContext>
        implements CommonExpressionsVisitor2, CommonExpressionsHandler {

  private CommonExpressionsTraverser traverser;

  @Override
  public CommonExpressionsTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(CommonExpressionsTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void endVisit(ASTFieldAccessExpression refExpr) {
    SymTypeExpression expressionType = TypeCheck3.typeOf(refExpr);
    Optional<VariableSymbol> sourceSymbolOpt = expressionType.getSourceInfo().getSourceSymbol()
            .filter(s -> s instanceof VariableSymbol)
            .map(s -> (VariableSymbol) s);
    if (sourceSymbolOpt.isPresent()) {
      VariableSymbol sourceSymbol = sourceSymbolOpt.get();
      System.out.println("FieldAccessExpression Variable Source symbol: " + sourceSymbol);
      System.out.println("symbol full name: " + sourceSymbol.getFullName());
    }

    /*
     * 2. get all variants of the parent expression
     */
    List<CommonExpressionsAdaptationVariant> parentVariants = getAdaptations4Ast().getVariants(refExpr.getExpression());

    // 2. for each variant we can now check the available FieldSymbols incarnations
    for (CommonExpressionsAdaptationVariant parentVariant : parentVariants) {
      // 2. if we have a field symbol, get all incarnations and create variants for it
      if (sourceSymbolOpt.isPresent()) {
        VariableSymbol refFieldSymbol = sourceSymbolOpt.get();
        Set<VariableSymbol> incarnations = getAdaptationContext().getBasicSymbolsIncMapping().getIncarnations(refFieldSymbol);
        if (incarnations.isEmpty()) {
          // no field symbol, use the constraints from the parent expression
          getAdaptations4Ast().addVariant(refExpr, parentVariant);
          continue;
        }
        // we have the incarnations which are possible in this context
        for (VariableSymbol fieldIncarnation : incarnations) {
          CommonExpressionsAdaptationVariant newVariant = parentVariant.copy();
          newVariant.getBasicSymbolsBindings().addVariableBinding(Binding.createStrict(refFieldSymbol, fieldIncarnation));
          getAdaptations4Ast().addVariant(refExpr, newVariant);
        }
      } else {
        // no field symbol, just pass the variants upwards
        getAdaptations4Ast().addVariant(refExpr, parentVariant);
      }
    }
  }

  @Override
  public void traverse(ASTEqualsExpression expr) {
    if (null != expr.getLeft()) {
      expr.getLeft().accept(getTraverser());
      List<CommonExpressionsAdaptationVariant> leftAdapted = getAdaptations4Ast().getVariants(expr.getLeft());
      CommonExpressionsAdaptationContext previousCtx = getAdaptationContext();
      for (CommonExpressionsAdaptationVariant leftResult : leftAdapted) {
        CommonExpressionsAdaptationContext localCtx = previousCtx.fork();
        localCtx.addBindings(leftResult);

        setAdaptationContext(localCtx);
        if (null != expr.getRight()) {
          expr.getRight().accept(getTraverser());
        }

        List<CommonExpressionsAdaptationVariant> rightAdapted = getAdaptations4Ast().getVariants(expr.getRight());
        if (rightAdapted.isEmpty()) {
          // conflict with existing bindings -> drop current leftResult
          getAdaptations4Ast().removeVariant(expr.getLeft(), leftResult);
        } else {
          for (CommonExpressionsAdaptationVariant rightResult : rightAdapted) {
            CommonExpressionsAdaptationVariant mergedVariant = leftResult.merge(rightResult);
            getAdaptations4Ast().addVariant(expr, mergedVariant);
          }
        }
      }
      // IMPORTANT: reset the adaptation context to the previous one
      setAdaptationContext(previousCtx);
    }
    /*
     * TODO We can generalize this method to K child nodes where constraints are propagated from each child to the next
     */
  }
}
