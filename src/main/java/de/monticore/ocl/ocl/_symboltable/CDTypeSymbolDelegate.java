/*
 *  * (c) https://github.com/MontiCore/monticore
 *  *
 *  * The license generally applicable for this project
 *  * can be found under https://github.com/MontiCore/monticore.
 */

package de.monticore.ocl.ocl._symboltable;

import de.monticore.cd.cd4analysis._symboltable.CD4AnalysisGlobalScope;
import de.monticore.cd.cd4analysis._symboltable.CDTypeSymbol;
import de.monticore.symboltable.modifiers.AccessModifier;
import de.monticore.types.typesymbols._symboltable.TypeSymbol;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class CDTypeSymbolDelegate implements
    de.monticore.types.typesymbols._symboltable.ITypeSymbolResolvingDelegate {
  CD4AnalysisGlobalScope cd4AnalysisGlobalScope;

  public CDTypeSymbolDelegate(CD4AnalysisGlobalScope cd4AnalysisGlobalScope) {
    this.cd4AnalysisGlobalScope = cd4AnalysisGlobalScope;
  }

  @Override
  public List<TypeSymbol> resolveAdaptedTypeSymbol(boolean foundSymbols, String name, AccessModifier modifier, Predicate<TypeSymbol> predicate) {
    final Optional<CDTypeSymbol> cdTypeSymbol = cd4AnalysisGlobalScope.resolveCDType(foundSymbols, name, modifier);
    if (cdTypeSymbol.isPresent()) {
      MyOCLTypeSymbolAdapter oclTypeSymbol = new MyOCLTypeSymbolAdapter(cdTypeSymbol.get());
      return Collections.singletonList(oclTypeSymbol);
    } else {
      return Collections.emptyList();
    }
  }


}
