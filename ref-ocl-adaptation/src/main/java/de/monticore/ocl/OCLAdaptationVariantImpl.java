package de.monticore.ocl;

import de.monticore.ast.ASTNode;
import de.monticore.expressions.commonexpressions.CommonExpressionsAdaptationVariant;
import de.monticore.expressions.expressionsbasis.ExpressionsBasisAdaptationVariant;
import de.monticore.refadaptation.Binding;
import de.monticore.refadaptation.Bindings;
import de.monticore.refadaptation.IAdaptationVariant;
import de.monticore.symbols.OOSymbolsBindings;
import de.monticore.symbols.basicsymbols.BasicSymbolsBindings;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;
import de.monticore.symbols.oosymbols._symboltable.OOTypeSymbol;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OCLAdaptationVariantImpl implements OCLAdaptationVariant, BasicSymbolsBindings, OOSymbolsBindings {

  // TODO implement
  // TODO decide if we want to delegate or implement the methods directly here
  private final Map<TypeSymbol, Binding<TypeSymbol>> typeBindings = new HashMap<>();
  private final Map<VariableSymbol, Binding<VariableSymbol>> variableBindings = new HashMap<>();
  private final Map<FunctionSymbol, Binding<FunctionSymbol>> functionBindings = new HashMap<>();
  private final Map<OOTypeSymbol, Binding<OOTypeSymbol>> typeSymbolBindings = new HashMap<>();
  private final Map<FieldSymbol, Binding<FieldSymbol>> fieldSymbolBindings = new HashMap<>();
  private final Bindings<MethodSymbol> methodSymbolBindings = new Bindings<>();

  @Override
  public <T extends ASTNode> Optional<T> getAdaptedNode(T refNode) {
    return Optional.empty();
  }

  @Override
  public void setAdaptedNode(ASTNode refNode, ASTNode adaptedNode) {

  }

  @Override
  public CommonExpressionsAdaptationVariant copy() {
    return null;
  }

  @Override
  public IAdaptationVariant merge(IAdaptationVariant otherVariant) {
    return null;
  }

  @Override
  public ExpressionsBasisAdaptationVariant merge(ExpressionsBasisAdaptationVariant otherVariant) {
    return null;
  }

  @Override
  public BasicSymbolsBindings getBasicSymbolsBindings() {
    return null;
  }

  @Override
  public CommonExpressionsAdaptationVariant merge(CommonExpressionsAdaptationVariant otherVariant) {
    return null;
  }

  @Override
  public OOSymbolsBindings getOOSymbolsBindings() {
    return null;
  }

  @Override
  public Optional<Binding<OOTypeSymbol>> getBinding(OOTypeSymbol typeSymbol) {
    return Optional.empty();
  }

  @Override
  public Optional<Binding<FieldSymbol>> getBinding(FieldSymbol fieldSymbol) {
    return Optional.empty();
  }

  @Override
  public Optional<Binding<MethodSymbol>> getBinding(MethodSymbol methodSymbol) {
    return Optional.empty();
  }

  @Override
  public void addOOTypeBinding(Binding<OOTypeSymbol> binding) {

  }

  @Override
  public void addFieldBinding(Binding<FieldSymbol> binding) {

  }

  @Override
  public void addMethodBinding(Binding<MethodSymbol> binding) {

  }

  @Override
  public Optional<Binding<TypeSymbol>> getBinding(TypeSymbol typeSymbol) {
    return Optional.empty();
  }

  @Override
  public void addTypeBinding(Binding<TypeSymbol> binding) {

  }

  @Override
  public Optional<Binding<VariableSymbol>> getBinding(VariableSymbol variableSymbol) {
    return Optional.empty();
  }

  @Override
  public void addVariableBinding(Binding<VariableSymbol> binding) {

  }

  @Override
  public Optional<Binding<FunctionSymbol>> getBinding(FunctionSymbol functionSymbol) {
    return Optional.empty();
  }

  @Override
  public void addFunctionBinding(Binding<FunctionSymbol> binding) {

  }
}
