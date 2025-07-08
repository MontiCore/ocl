package de.monticore.oclrefadaptation;

import de.monticore.cdconformance.inc.CDIncarnationBindings;
import de.monticore.refadaptation.Binding;
import de.monticore.symbols.OOSymbolsBindings;
import de.monticore.symbols.basicsymbols.BasicSymbolsBindings;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;
import de.monticore.symbols.oosymbols._symboltable.OOTypeSymbol;
import de.monticore.symboltable.IScope;
import de.monticore.symboltable.ISymbol;

import java.util.Optional;
import java.util.Set;

@Deprecated
public class LegacyCDIncarnationBindings2OOSymbolsBindings implements OOSymbolsBindings {

  private final CDIncarnationBindings cdIncarnationBindings;
  private final ISymbol contextSymbol;
  private final IScope scope;
  private final String contextSymbolKey;

  public LegacyCDIncarnationBindings2OOSymbolsBindings(CDIncarnationBindings cdIncarnationBindings, ISymbol contextSymbol, IScope scope, String contextSymbolKey) {
    this.cdIncarnationBindings = cdIncarnationBindings;
    this.contextSymbol = contextSymbol;
    this.scope = scope;
    this.contextSymbolKey = contextSymbolKey;
  }

  @Override
  public OOSymbolsBindings copy() {
    throw new UnsupportedOperationException("read only");
  }

  @Override
  public Optional<Binding<TypeSymbol>> getBinding(TypeSymbol typeSymbol) {
    Set<TypeSymbol> conElements;
    if (contextSymbol != null) {
      conElements = cdIncarnationBindings.getBindings(contextSymbol, typeSymbol);
    } else if (scope != null) {
      conElements = cdIncarnationBindings.getBindings(scope, typeSymbol);
    } else {
      throw new IllegalStateException("No context symbol or scope provided for binding lookup.");
    }
    return createBinding(typeSymbol, conElements);
  }

  private <T> Optional<Binding<T>> createBinding(T refSymbol, Set<T> concreteSymbols) {
    if (concreteSymbols.isEmpty()) {
      return Optional.empty();
    } else if (concreteSymbols.size() == 1) {
      return Optional.of(Binding.createStrict(refSymbol, concreteSymbols.iterator().next()));
    } else {
      // TODO ??
      throw new IllegalStateException("Unsupported binding to multiple incarnations ");
    }
  }

  @Override
  public Set<Binding<TypeSymbol>> getTypeBindings() {
    throw new UnsupportedOperationException();
  }

  @Override
  public void addTypeBinding(Binding<TypeSymbol> binding) {
    throw new UnsupportedOperationException("read only");
  }

  @Override
  public Optional<Binding<VariableSymbol>> getBinding(VariableSymbol variableSymbol) {
    throw new UnsupportedOperationException("no variable symbols supported yet");
  }

  @Override
  public Optional<Binding<FunctionSymbol>> getBinding(FunctionSymbol functionSymbol) {
    if (functionSymbol instanceof MethodSymbol) {
      return getBinding((MethodSymbol) functionSymbol).map(Binding::cast);
    } else {
      throw new UnsupportedOperationException("Only method symbols supported yet");
    }
  }

  @Override
  public Optional<Binding<OOTypeSymbol>> getBinding(OOTypeSymbol typeSymbol) {
    return getBinding(typeSymbol).map(Binding::cast);
  }

  @Override
  public Optional<Binding<FieldSymbol>> getBinding(FieldSymbol fieldSymbol) {
    Set<FieldSymbol> conElements;
    if (contextSymbol != null) {
      conElements = cdIncarnationBindings.getBindings(contextSymbol, fieldSymbol);
    } else if (scope != null) {
      conElements = cdIncarnationBindings.getBindings(scope, fieldSymbol);
    } else {
      throw new IllegalStateException("No context symbol or scope provided for binding lookup.");
    }
    return createBinding(fieldSymbol, conElements);
  }

  @Override
  public Optional<Binding<MethodSymbol>> getBinding(MethodSymbol methodSymbol) {
    Set<MethodSymbol> conElements;
    if (contextSymbol != null) {
      conElements = cdIncarnationBindings.getBindings(contextSymbol, methodSymbol);
    } else if (scope != null) {
      conElements = cdIncarnationBindings.getBindings(scope, methodSymbol);
    } else {
      throw new IllegalStateException("No context symbol or scope provided for binding lookup.");
    }
    return createBinding(methodSymbol, conElements);
  }

  @Override
  public void addOOTypeBinding(Binding<OOTypeSymbol> binding) {
    throw new UnsupportedOperationException("read only");
  }

  @Override
  public void addFieldBinding(Binding<FieldSymbol> binding) {
    throw new UnsupportedOperationException("read only");
  }

  @Override
  public void addMethodBinding(Binding<MethodSymbol> binding) {
    throw new UnsupportedOperationException("read only");
  }

  @Override
  public void addVariableBinding(Binding<VariableSymbol> binding) {
    throw new UnsupportedOperationException("read only");
  }

  @Override
  public void addFunctionBinding(Binding<FunctionSymbol> binding) {
    throw new UnsupportedOperationException("read only");
  }

  @Override
  public void addAll(OOSymbolsBindings bindings) {
    throw new UnsupportedOperationException("read only");
  }

  @Override
  public void addAll(BasicSymbolsBindings bindings) {
    throw new UnsupportedOperationException("read only");
  }

  @Override
  public Set<Binding<OOTypeSymbol>> getOOTypeBindings() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Binding<FunctionSymbol>> getFunctionBindings() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Binding<VariableSymbol>> getVariableBindings() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Binding<FieldSymbol>> getFieldBindings() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<Binding<MethodSymbol>> getMethodBindings() {
    throw new UnsupportedOperationException();
  }
}
