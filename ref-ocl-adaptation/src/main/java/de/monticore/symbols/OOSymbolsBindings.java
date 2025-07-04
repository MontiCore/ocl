package de.monticore.symbols;

import de.monticore.refadaptation.Binding;
import de.monticore.symbols.basicsymbols.BasicSymbolsBindings;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;
import de.monticore.symbols.oosymbols._symboltable.OOTypeSymbol;

import java.util.Optional;

public interface OOSymbolsBindings extends BasicSymbolsBindings {

  Optional<Binding<OOTypeSymbol>> getBinding(OOTypeSymbol typeSymbol);
  Optional<Binding<FieldSymbol>> getBinding(FieldSymbol fieldSymbol);
  Optional<Binding<MethodSymbol>> getBinding(MethodSymbol methodSymbol);

  void addOOTypeBinding(Binding<OOTypeSymbol> binding);

  void addFieldBinding(Binding<FieldSymbol> binding);

  void addMethodBinding(Binding<MethodSymbol> binding);
}
