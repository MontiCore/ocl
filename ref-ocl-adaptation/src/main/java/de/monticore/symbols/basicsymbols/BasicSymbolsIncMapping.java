package de.monticore.symbols.basicsymbols;

import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols._symboltable.OOTypeSymbol;

import java.util.Set;

public interface BasicSymbolsIncMapping {

  Set<TypeSymbol> getIncarnations(TypeSymbol typeSymbol);

  Set<VariableSymbol> getIncarnations(VariableSymbol variableSymbol);

  Set<FunctionSymbol> getIncarnations(FunctionSymbol functionSymbol);
}
