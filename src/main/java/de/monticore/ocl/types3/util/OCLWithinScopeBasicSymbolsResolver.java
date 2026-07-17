// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3.util;

import de.monticore.types3.util.OOWithinScopeBasicSymbolsResolver;

/**
 * @deprecated use super class
 */
@Deprecated(forRemoval = true)
public class OCLWithinScopeBasicSymbolsResolver extends OOWithinScopeBasicSymbolsResolver {

  public static void init() {
    setDelegate(new OCLWithinScopeBasicSymbolsResolver());
  }

}
