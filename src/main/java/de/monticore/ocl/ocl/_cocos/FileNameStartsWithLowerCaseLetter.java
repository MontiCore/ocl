/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.ocl._ast.ASTOCLFile;
import de.se_rwth.commons.logging.Log;

public class FileNameStartsWithLowerCaseLetter implements OCLASTOCLFileCoCo {

  @Override
  public void check(ASTOCLFile astFile) {
    String fileName = astFile.isPresentFileName() ? astFile.getFileName() : "oclFile";
    boolean startsWithUpperCase = Character.isUpperCase(fileName.charAt(0));

    if (startsWithUpperCase) {
      // Issue warning...
      Log.error(
          String.format("0xOCL02 file name '%s' should not start with a capital letter.", fileName),
          astFile.get_SourcePositionStart());
    }
  }
}
