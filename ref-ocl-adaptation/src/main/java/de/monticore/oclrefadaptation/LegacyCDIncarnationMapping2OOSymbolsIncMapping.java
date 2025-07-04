package de.monticore.oclrefadaptation;

import de.monticore.cdconformance.inc.CDIncarnationMapping;
import de.monticore.symbols.OOSymbolsIncMapping;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;
import de.monticore.symbols.oosymbols._symboltable.OOTypeSymbol;

import java.util.Set;

// TODO CDIncarnationMapping in cd4a should be a supertype of OOSymbolsIncMapping
@Deprecated
public class LegacyCDIncarnationMapping2OOSymbolsIncMapping implements OOSymbolsIncMapping {

  private CDIncarnationMapping cdIncarnationMapping;
  public LegacyCDIncarnationMapping2OOSymbolsIncMapping(CDIncarnationMapping cdIncarnationMapping) {
    this.cdIncarnationMapping = cdIncarnationMapping;
  }

  @Override
  public Set<OOTypeSymbol> getIncarnations(OOTypeSymbol typeSymbol) {
    return null;
  }

  @Override
  public Set<FieldSymbol> getIncarnations(FieldSymbol fieldSymbol) {
    return null;
  }

  @Override
  public Set<MethodSymbol> getIncarnations(MethodSymbol methodSymbol) {
    return null;
  }

  @Override
  public Set<TypeSymbol> getIncarnations(TypeSymbol typeSymbol) {
    return null;
  }

  @Override
  public Set<VariableSymbol> getIncarnations(VariableSymbol variableSymbol) {
    return null;
  }

  @Override
  public Set<FunctionSymbol> getIncarnations(FunctionSymbol functionSymbol) {
    return null;
  }
}
