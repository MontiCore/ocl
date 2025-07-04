package de.monticore.expressions.expressionsbasis;

import de.monticore.expressions.expressionsbasis._ast.ASTArguments;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisHandler;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisTraverser;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisVisitor2;
import de.monticore.refadaptation.AbstractAdaptationVisitor;
import de.monticore.refadaptation.Binding;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.TypeCheck3;

import java.util.Optional;
import java.util.Set;

public class ExpressionsBasisBindingVariantsVisitor extends AbstractAdaptationVisitor<ExpressionsBasisAdaptationContext> implements ExpressionsBasisVisitor2, ExpressionsBasisHandler {

  private ExpressionsBasisTraverser traverser;

  @Override
  public ExpressionsBasisTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(ExpressionsBasisTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void endVisit(ASTNameExpression refExpr) {
    SymTypeExpression expressionType = TypeCheck3.typeOf(refExpr);
    Optional<VariableSymbol> sourceSymbolOpt = expressionType.getSourceInfo().getSourceSymbol()
            .filter(s -> s instanceof VariableSymbol)
            .map(s -> (VariableSymbol) s);

    // TODO What symbols do we even expect here?
    /*
     * 1. name expressions can point to fields of a class if we process an invariant
     * 2. name expressions can point to fields of a class if we process an operation constraint
     * 3. name expressions can point to parameters of an operation if we process an operation constraint
     * ...
     */

    if (sourceSymbolOpt.isPresent()) {
      // If we have a VariableSymbol, get all incarnations and create variants for it
      VariableSymbol sourceSymbol = sourceSymbolOpt.get();
      System.out.println("variable Source symbol: " + sourceSymbol);
      System.out.println("symbol full name: " + sourceSymbol.getFullName());

      VariableSymbol refVarSymbol = sourceSymbolOpt.get();
      Set<VariableSymbol> incarnations = getAdaptationContext().getBasicSymbolsIncMapping().getIncarnations(refVarSymbol);
      // we have the incarnations which are possible in this context
      for (VariableSymbol variableIncarnation : incarnations) {
        ExpressionsBasisAdaptationVariant newVariant = getAdaptationContext().createVariant();
        newVariant.getBasicSymbolsBindings().addVariableBinding(Binding.createStrict(refVarSymbol, variableIncarnation));
        getAdaptations4Ast().addVariant(refExpr, newVariant);
      }
    } else {
      // no VariableSymbol, should not be adapted and just deepCloned
      // TODO Pass single variant upwards? How do we handle default cases with do adaptable code? .-> look at traverse, e.g. in EqualsExpression
      // alternative: a singleton instance EmptyVariant ??
      // TODO should we split variant and adapted AST nodes ?? or keep it mixed?
      getAdaptations4Ast().addVariant(refExpr, getAdaptationContext().createVariant());
    }
  }

  @Override
  public void visit(ASTLiteralExpression node) {
    // TODO Either we do not process these at all and introduce the convention:
    // - If no binding variant is present -> just use the reference node
    // OR
    // - we return an atomic "empty binding variant" and pass this upwards
    // TODO We need to decide in "traverse(AST...)" what we do if the list is empty -> default variant or is this a conflict?
  }

  @Override
  public void traverse(ASTArguments node) {
    /*
     * TODO: First achieve clean implementation of binary expressions (equals etc.) before
     *  tackling the n-ary variant of the traversal.
     *  At best, we can reuse the same utility method to traverse through n sub elements
     *  while applying "constraint propagation" of the bindings.
     */
  }
}
