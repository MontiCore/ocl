/*
 *  * (c) https://github.com/MontiCore/monticore
 *  *
 *  * The license generally applicable for this project
 *  * can be found under https://github.com/MontiCore/monticore.
 */

package de.monticore.ocl.ocl._symboltable;

import de.monticore.ocl.ocl._ast.ASTOCLArtifact;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.symboltable.ImportStatement;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;

import java.util.ArrayList;
import java.util.List;

public class OCLSymbolTableHelper {

  private OCLSymbolTableHelper() {
  }

  public static String getNameOfModel(final ASTOCLArtifact astFile) {
    return astFile.getName();
  }

  public static String getNameOfModel(final ASTOCLCompilationUnit compilationUnit) {
    return getNameOfModel(compilationUnit.getOCLArtifact());
  }

  public static List<ImportStatement> getImportStatements (List<ASTMCImportStatement> input) {
    List<ImportStatement> imports = new ArrayList<>();
    for (ASTMCImportStatement importStatement : input) {
      imports.add(new ImportStatement(importStatement.getQName(), importStatement.isStar()));
    }
    return imports;
  }
}
