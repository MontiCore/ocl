/*
 *  * (c) https://github.com/MontiCore/monticore
 *  *
 *  * The license generally applicable for this project
 *  * can be found under https://github.com/MontiCore/monticore.
 */

package de.monticore.ocl.ocl._symboltable;

import de.monticore.cd.cd4analysis._symboltable.CDTypeSymbol;
import de.monticore.types.typesymbols._symboltable.ITypeSymbolsScope;
import de.monticore.types.typesymbols._symboltable.TypeSymbol;

public class MyOCLTypeSymbolAdapter extends TypeSymbol {
  CDTypeSymbol delegate;

  public MyOCLTypeSymbolAdapter(CDTypeSymbol delegate) {
    super(delegate.getName());
    this.delegate = delegate;
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public ITypeSymbolsScope getEnclosingScope() {
    return delegate.getEnclosingScope();
  }

}
