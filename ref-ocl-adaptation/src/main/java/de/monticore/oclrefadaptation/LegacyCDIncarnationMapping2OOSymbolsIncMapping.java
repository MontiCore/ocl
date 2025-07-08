package de.monticore.oclrefadaptation;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cd4codebasis._ast.ASTCDMethod;
import de.monticore.cdbasis._ast.ASTCDAttributeTOP;
import de.monticore.cdconcretization.util.SymbolUtil;
import de.monticore.cdconformance.inc.CDIncarnationMapping;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.symbols.OOSymbolsBindings;
import de.monticore.symbols.OOSymbolsIncMapping;
import de.monticore.symbols.OOSymbolsLocalIncMapping;
import de.monticore.symbols.OOSymbolsRestrictedIncMapping;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols._symboltable.FieldSymbol;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;
import de.monticore.symbols.oosymbols._symboltable.OOTypeSymbol;
import de.monticore.symboltable.IScope;
import de.monticore.symboltable.ISymbol;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

// TODO CDIncarnationMapping in cd4a should be a supertype of OOSymbolsIncMapping
@Deprecated
public class LegacyCDIncarnationMapping2OOSymbolsIncMapping implements OOSymbolsIncMapping {

  private CDIncarnationMapping cdIncarnationMapping;
  public LegacyCDIncarnationMapping2OOSymbolsIncMapping(CDIncarnationMapping cdIncarnationMapping) {
    this.cdIncarnationMapping = cdIncarnationMapping;
  }

  @Override
  public String computeSymbolKey(ISymbol symbol) {
    return cdIncarnationMapping.computeSymbolKey(symbol);
  }

  @Override
  public OOSymbolsLocalIncMapping getScopedMapping(ISymbol contextSymbol) {
    return new OOSymbolsRestrictedIncMapping(this, getScopedBindings(contextSymbol));
  }

  @Override
  public OOSymbolsLocalIncMapping getScopedMapping(IScope scope) {
    return new OOSymbolsRestrictedIncMapping(this, getScopedBindings(scope));
  }

  @Override
  public OOSymbolsBindings getScopedBindings(String contextSymbolKey) {
    return new LegacyCDIncarnationBindings2OOSymbolsBindings(cdIncarnationMapping, null, null, contextSymbolKey);
  }

  @Override
  public OOSymbolsBindings getScopedBindings(ISymbol contextSymbol) {
    return new LegacyCDIncarnationBindings2OOSymbolsBindings(cdIncarnationMapping, contextSymbol, null, contextSymbol.getFullName());
  }

  @Override
  public OOSymbolsBindings getScopedBindings(IScope scope) {
    return new LegacyCDIncarnationBindings2OOSymbolsBindings(cdIncarnationMapping, null, scope, null);
  }

  @Override
  public Set<TypeSymbol> getIncarnations(TypeSymbol typeSymbol) {
    // TODO solve this in another way
    TypeSymbol cd4cSymbol = CD4CodeMill.globalScope().resolveType(typeSymbol.getFullName()).orElseThrow();
    // TODO move to CDIncarnationMapping
    if (!cd4cSymbol.isPresentAstNode()) {
      return Collections.emptySet();
    }
    return cdIncarnationMapping.getIncarnations(cd4cSymbol);
  }

  @Override
  public Set<VariableSymbol> getIncarnations(VariableSymbol variableSymbol) {
    // TODO solve this in another way
    Optional<FieldSymbol> cd4cSymbolOpt = CD4CodeMill.globalScope().resolveField(variableSymbol.getFullName());
    // TODO move to CDIncarnationMapping
    if (cd4cSymbolOpt.isEmpty() || !cd4cSymbolOpt.get().isPresentAstNode()) {
      return Collections.emptySet();
    }
    return new HashSet<>(getIncarnations(cd4cSymbolOpt.get()));
  }

  @Override
  public Set<FunctionSymbol> getIncarnations(FunctionSymbol functionSymbol) {
    if (functionSymbol instanceof MethodSymbol) {
      return new HashSet<>(getIncarnations((MethodSymbol) functionSymbol));
    } else {
      throw new UnsupportedOperationException("only method symbols supported yet");
    }
  }

  @Override
  public Set<OOTypeSymbol> getIncarnations(OOTypeSymbol typeSymbol) {
    Set<TypeSymbol> typeSymbols = getIncarnations((TypeSymbol) typeSymbol);
    return typeSymbols.stream()
        .filter(ts -> ts instanceof OOTypeSymbol)
        .map(ts -> (OOTypeSymbol) ts)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<FieldSymbol> getIncarnations(FieldSymbol fieldSymbol) {
    return cdIncarnationMapping.getIncarnations(SymbolUtil.cdAttributeFromFieldSymbol(fieldSymbol))
        .stream()
        .map(ASTCDAttributeTOP::getSymbol)
        .collect(Collectors.toSet());
  }

  @Override
  public Set<MethodSymbol> getIncarnations(MethodSymbol methodSymbol) {
    return cdIncarnationMapping.getIncarnations(SymbolUtil.cdMethodFromMethodSymbol(methodSymbol))
        .stream()
        .map(ASTCDMethod::getSymbol)
        .collect(Collectors.toSet());
  }
}
