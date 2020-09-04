/*
 *  * (c) https://github.com/MontiCore/monticore
 *  *
 *  * The license generally applicable for this project
 *  * can be found under https://github.com/MontiCore/monticore.
 */

package de.monticore.ocl.ocl._symboltable;

import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLArtifact;

public class OCLSymbolTableHelper {

  private OCLSymbolTableHelper() {
  }

  public static String getNameOfModel(final ASTOCLArtifact astFile) {
    return astFile.getName();
  }

  public static String getNameOfModel(final ASTOCLCompilationUnit compilationUnit) {
    return getNameOfModel(compilationUnit.getOCLArtifact());
  }
}
