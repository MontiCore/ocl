package de.monticore.refadaptation;

import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;

import java.util.HashMap;
import java.util.Map;

public class Bindings<T> {

  private final Map<TypeSymbol, Binding<TypeSymbol>> typeBindings = new HashMap<>();

  // TODO add compatibility checks etc
}
