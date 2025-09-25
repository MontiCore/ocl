package de.monticore.oclrefadaptation;

import de.monticore.cdconcretization.util.SymbolUtil;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symbols.oosymbols._symboltable.IOOSymbolsScope;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;
import de.monticore.symbols.oosymbols._symboltable.OOTypeSymbol;
import de.monticore.symbols.oosymbols.refmodel.IOOSymbolsBindings;
import de.monticore.symbols.oosymbols.refmodel.IOOSymbolsIncMapping;
import de.monticore.symbols.oosymbols.refmodel.IOOSymbolsLocalIncMapping;
import de.monticore.symboltable.IScope;
import de.monticore.symboltable.ISymbol;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Adapter for an {@link IOOSymbolsIncMapping} which resolves symbols in the reference CD scope
 * before looking up incarnation in the original mapping.<br>
 * This is necessary because at the moment, the CD4Code incarnation mapping relies on the AST nodes
 * being present in the symbols to be able to find incarnations. However, the reference symbols
 * from OCL reference artifacts are located in the OCL artifact scope and have no links to the
 * CD AST nodes.<br>
 * <br>
 * In the future, this can be solved by "exporting" an incarnation mapping/bindings representation
 * from the CD model that only relies on symbols, similar as we export a symbol table for a CD
 * model to use the symbols in other artifacts.
 */
public class CD4CAdaptedOOSymbolsMapping implements IOOSymbolsIncMapping {

  /** The original mapping we delegate to. */
  private final IOOSymbolsIncMapping delegate;

  public CD4CAdaptedOOSymbolsMapping(IOOSymbolsIncMapping delegate) {
    this.delegate = delegate;
  }

  @Override
  public IOOSymbolsScope getReferenceScope() {
    return delegate.getReferenceScope();
  }

  @Override
  public IOOSymbolsScope getConcreteScope() {
    return delegate.getConcreteScope();
  }

  @Override
  public String computeSymbolKey(ISymbol iSymbol) {
    return delegate.computeSymbolKey(iSymbol);
  }

  @Override
  public IOOSymbolsLocalIncMapping getScopedMapping(ISymbol iSymbol) {
    return delegate.getScopedMapping(iSymbol);
  }

  @Override
  public IOOSymbolsLocalIncMapping getScopedMapping(IScope iScope) {
    return delegate.getScopedMapping(iScope);
  }

  @Override
  public IOOSymbolsBindings getLocalOnlyBindings(String s) {
    return delegate.getLocalOnlyBindings(s);
  }

  @Override
  public IOOSymbolsBindings getScopedBindings(ISymbol iSymbol) {
    return delegate.getScopedBindings(iSymbol);
  }

  @Override
  public IOOSymbolsBindings getScopedBindings(IScope iScope) {
    return delegate.getScopedBindings(iScope);
  }

  @Override
  public Set<OOTypeSymbol> getIncarnations(OOTypeSymbol ooTypeSymbol) {
    Set<TypeSymbol> typeSymbols = getIncarnations((TypeSymbol) ooTypeSymbol);
    return typeSymbols.stream().
            filter(ts -> ts instanceof OOTypeSymbol)
            .map(ts -> (OOTypeSymbol) ts)
            .collect(Collectors.toSet());
  }

  @Override
  public Set<FieldSymbol> getIncarnations(FieldSymbol fieldSymbol) {
    return delegate.getIncarnations(fieldSymbol);
  }

  @Override
  public Set<MethodSymbol> getIncarnations(MethodSymbol methodSymbol) {
    return delegate.getIncarnations(methodSymbol);
  }

  @Override
  public Set<TypeSymbol> getIncarnations(TypeSymbol typeSymbol) {
    // Resolve the type symbol in the reference CD scope to make sure we have the correct
    // symbol that is accepted by the CD incarnation mapping!
    Optional<TypeSymbol> cd4cSymbol = getReferenceScope().resolveTypeDown(SymbolUtil
            .getFullNameWithoutCD(typeSymbol));
    if (cd4cSymbol.isEmpty() || !cd4cSymbol.get().isPresentAstNode()) {
      return Collections.emptySet();
    }
    return delegate.getIncarnations(cd4cSymbol.get());
  }

  @Override
  public Set<VariableSymbol> getIncarnations(VariableSymbol variableSymbol) {
    return delegate.getIncarnations(variableSymbol);
  }

  @Override
  public Set<FunctionSymbol> getIncarnations(FunctionSymbol functionSymbol) {
    return delegate.getIncarnations(functionSymbol);
  }
}
