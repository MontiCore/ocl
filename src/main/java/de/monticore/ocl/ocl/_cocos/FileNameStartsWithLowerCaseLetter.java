/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.ocl._ast.ASTOCLArtifact;
import de.se_rwth.commons.logging.Log;

public class FileNameStartsWithLowerCaseLetter implements OCLASTOCLArtifactCoCo {

  @Override
  public void check(ASTOCLArtifact astFile) {
    String fileName = astFile.getName() ;
    boolean startsWithUpperCase = Character.isUpperCase(fileName.charAt(0));

    if (startsWithUpperCase) {
      // Issue warning...
      Log.error(
          String.format("0xOCL02 file name '%s' should not start with a capital letter.", fileName));
    }
  }
}
