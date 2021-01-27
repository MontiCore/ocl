/*
 *  * (c) https://github.com/MontiCore/monticore
 *  *
 *  * The license generally applicable for this project
 *  * can be found under https://github.com/MontiCore/monticore.
 */

package de.monticore.ocl.ocl._symboltable;

import de.monticore.cd4analysis._symboltable.ICD4AnalysisGlobalScope;
import de.monticore.cdbasis._symboltable.CDTypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.ITypeSymbolResolver;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symboltable.modifiers.AccessModifier;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class CDTypeSymbolDelegate implements ITypeSymbolResolver {
  ICD4AnalysisGlobalScope cd4AnalysisGlobalScope;

  public CDTypeSymbolDelegate(ICD4AnalysisGlobalScope cd4AnalysisGlobalScope) {
    this.cd4AnalysisGlobalScope = cd4AnalysisGlobalScope;
  }

  @Override
  public List<TypeSymbol> resolveAdaptedTypeSymbol(boolean foundSymbols, String name, AccessModifier modifier, Predicate<TypeSymbol> predicate) {
    Optional<CDTypeSymbol> cdTypeSymbol = cd4AnalysisGlobalScope.resolveCDType(foundSymbols, name, modifier);
    if (cdTypeSymbol.isPresent()) {
      return Collections.singletonList(cdTypeSymbol.get());
    } else {
      return Collections.emptyList();
    }
  }
}
