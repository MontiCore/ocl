/*
 *  * (c) https://github.com/MontiCore/monticore
 *  *
 *  * The license generally applicable for this project
 *  * can be found under https://github.com/MontiCore/monticore.
 */

package de.monticore.ocl.ocl._symboltable;

import de.monticore.cd4analysis._symboltable.ICD4AnalysisScope;

import java.util.List;

public interface IOCLwithCDGlobalScope
extends ICD4AnalysisScope, IOCLScope {

  @Override
  List<? extends IOCLwithCDGlobalScope> getSubScopes();

  @Override
  IOCLwithCDGlobalScope getEnclosingScope();

  @Override
  default int getSymbolsSize() {
    return 0;
  }
}
