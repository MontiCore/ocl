// (c) https://github.com/MontiCore/monticore

package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.ocl._ast.ASTOCLConstructorSignature;
import de.se_rwth.commons.logging.Log;

public class ConstructorNameStartsWithCapitalLetter implements OCLASTOCLConstructorSignatureCoCo {

  @Override
  public void check(ASTOCLConstructorSignature astConstructorSig) {
    if (Character.isLowerCase(astConstructorSig.getName().charAt(0))) {
      Log.error(
          String.format(
              "0xOCL01 constructor name '%s' after keyword 'new' should not start in lower-case.",
              astConstructorSig.getName()));
    }
  }
}
