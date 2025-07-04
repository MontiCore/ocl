package de.monticore.symbols;

import de.monticore.symbols.basicsymbols.BasicSymbolsIncMapping;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;
import de.monticore.symbols.oosymbols._symboltable.OOTypeSymbol;

import java.util.Set;

public interface OOSymbolsIncMapping extends BasicSymbolsIncMapping {

  Set<OOTypeSymbol> getIncarnations(OOTypeSymbol typeSymbol);

  Set<FieldSymbol> getIncarnations(FieldSymbol fieldSymbol);

  Set<MethodSymbol> getIncarnations(MethodSymbol methodSymbol);
}
