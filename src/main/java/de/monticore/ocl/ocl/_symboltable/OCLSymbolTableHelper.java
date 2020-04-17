/*
 *  * (c) https://github.com/MontiCore/monticore
 *  *
 *  * The license generally applicable for this project
 *  * can be found under https://github.com/MontiCore/monticore.
 */

package de.monticore.ocl.ocl._symboltable;

import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLFile;

public class OCLSymbolTableHelper {

  private OCLSymbolTableHelper() {
  }

  public static String getNameOfModel(final ASTOCLFile astFile) {
    return astFile.isPresentFileName() ? astFile.getFileName() : "oclFile";
  }

  public static String getNameOfModel(final ASTOCLCompilationUnit compilationUnit) {
    return getNameOfModel(compilationUnit.getOCLFile());
  }
}
