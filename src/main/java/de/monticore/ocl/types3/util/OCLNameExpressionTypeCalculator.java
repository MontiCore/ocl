// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3.util;

import de.monticore.types3.util.OOWithinScopeBasicSymbolsResolver;
import de.monticore.types3.util.WithinScopeBasicSymbolsResolver;

/** @deprecated use OCLWithinScopeBasicSymbolResolver */
@Deprecated(forRemoval = true)
public class OCLNameExpressionTypeCalculator extends OOWithinScopeBasicSymbolsResolver {

  public static void init() {
    OCLNameExpressionTypeCalculator oclResolver = new OCLNameExpressionTypeCalculator();
    WithinScopeBasicSymbolsResolver.delegate = oclResolver;
  }

}
