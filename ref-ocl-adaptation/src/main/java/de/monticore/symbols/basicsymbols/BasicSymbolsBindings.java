package de.monticore.symbols.basicsymbols;

import de.monticore.refadaptation.Binding;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;

import java.util.Optional;

public interface BasicSymbolsBindings {

  Optional<Binding<TypeSymbol>> getBinding(TypeSymbol typeSymbol);

  void addTypeBinding(Binding<TypeSymbol> binding);

  Optional<Binding<VariableSymbol>> getBinding(VariableSymbol variableSymbol);

  void addVariableBinding(Binding<VariableSymbol> binding);

  Optional<Binding<FunctionSymbol>> getBinding(FunctionSymbol functionSymbol);

  void addFunctionBinding(Binding<FunctionSymbol> binding);
}
