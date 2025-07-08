package de.monticore.symbols;

import de.monticore.refmodels.IncMappingUtils;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;
import de.monticore.symbols.oosymbols._symboltable.OOTypeSymbol;

import java.util.Set;

public class OOSymbolsRestrictedIncMapping implements OOSymbolsLocalIncMapping {

  private final OOSymbolsLocalIncMapping originalMapping;
  private final OOSymbolsBindings bindings;

  public OOSymbolsRestrictedIncMapping(OOSymbolsLocalIncMapping originalMapping, OOSymbolsBindings bindings) {
    this.originalMapping = originalMapping;
    this.bindings = bindings;
  }

  @Override
  public Set<OOTypeSymbol> getIncarnations(OOTypeSymbol typeSymbol) {
    return IncMappingUtils.getRestrictIncarnations(
            originalMapping::getIncarnations,
            bindings::getBinding,
            typeSymbol
    );
  }

  @Override
  public Set<FieldSymbol> getIncarnations(FieldSymbol fieldSymbol) {
    return IncMappingUtils.getRestrictIncarnations(
            originalMapping::getIncarnations,
            bindings::getBinding,
            fieldSymbol
    );
  }

  @Override
  public Set<MethodSymbol> getIncarnations(MethodSymbol methodSymbol) {
    return IncMappingUtils.getRestrictIncarnations(
            originalMapping::getIncarnations,
            bindings::getBinding,
            methodSymbol
    );
  }

  @Override
  public Set<TypeSymbol> getIncarnations(TypeSymbol typeSymbol) {
    return IncMappingUtils.getRestrictIncarnations(
            originalMapping::getIncarnations,
            bindings::getBinding,
            typeSymbol
    );
  }

  @Override
  public Set<VariableSymbol> getIncarnations(VariableSymbol variableSymbol) {
    return IncMappingUtils.getRestrictIncarnations(
            originalMapping::getIncarnations,
            bindings::getBinding,
            variableSymbol
    );
  }

  @Override
  public Set<FunctionSymbol> getIncarnations(FunctionSymbol functionSymbol) {
    return IncMappingUtils.getRestrictIncarnations(
            originalMapping::getIncarnations,
            bindings::getBinding,
            functionSymbol
    );
  }
}
