package de.monticore.ocl;

import de.monticore.ast.ASTNode;
import de.monticore.expressions.commonexpressions.CommonExpressionsAdaptationVariant;
import de.monticore.expressions.expressionsbasis.ExpressionsBasisAdaptationVariant;
import de.monticore.refadaptation.AbstractAdaptationVariant;
import de.monticore.refadaptation.IAdaptationVariant;
import de.monticore.symbols.OOSymbolsBindings;
import de.monticore.symbols.OOSymbolsBindingsImpl;
import de.monticore.symbols.basicsymbols.BasicSymbolsBindings;

import java.util.HashMap;
import java.util.Map;

public class OCLAdaptationVariantImpl extends AbstractAdaptationVariant implements OCLAdaptationVariant {

  protected final OOSymbolsBindings ooSymbolsBindings;

  public OCLAdaptationVariantImpl() {
    this(new OOSymbolsBindingsImpl(), new HashMap<>());
  }

  protected OCLAdaptationVariantImpl(OOSymbolsBindings ooSymbolsBindings, Map<ASTNode, ASTNode> adaptedNodes) {
    super(adaptedNodes);
    this.ooSymbolsBindings = ooSymbolsBindings;
  }

  @Override
  public OCLAdaptationVariant copy() {
    return new OCLAdaptationVariantImpl(ooSymbolsBindings, adaptedNodes);
  }

  @Override
  public IAdaptationVariant merge(IAdaptationVariant otherVariant) {
    OCLAdaptationVariant merged = copy();
    merged.addAdaptedNodes(otherVariant.getAdaptedNodes());
    return merged;
  }

  @Override
  public OCLAdaptationVariant merge(ExpressionsBasisAdaptationVariant otherVariant) {
    OCLAdaptationVariant merged = copy();
    merged.addAdaptedNodes(otherVariant.getAdaptedNodes());
    merged.getBasicSymbolsBindings().addAll(otherVariant.getBasicSymbolsBindings());
    return merged;
  }

  @Override
  public CommonExpressionsAdaptationVariant merge(CommonExpressionsAdaptationVariant otherVariant) {
    OCLAdaptationVariant merged = copy();
    merged.addAdaptedNodes(otherVariant.getAdaptedNodes());
    merged.getBasicSymbolsBindings().addAll(otherVariant.getBasicSymbolsBindings());
    merged.getOOSymbolsBindings().addAll(otherVariant.getOOSymbolsBindings());
    return merged;
  }

  @Override
  public BasicSymbolsBindings getBasicSymbolsBindings() {
    return ooSymbolsBindings;
  }

  @Override
  public OOSymbolsBindings getOOSymbolsBindings() {
    return ooSymbolsBindings;
  }
}
