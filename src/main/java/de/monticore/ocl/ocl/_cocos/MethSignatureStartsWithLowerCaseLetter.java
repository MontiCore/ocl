// (c) https://github.com/MontiCore/monticore

package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.se_rwth.commons.logging.Log;

public class MethSignatureStartsWithLowerCaseLetter implements OCLASTOCLMethodSignatureCoCo {

  @Override
  public void check(ASTOCLMethodSignature astMethSig) {
    String methodName = astMethSig.getMethodName().getParts(0);
    if (!Character.isLowerCase(methodName.charAt(0))) {
      Log.error(
          String.format(
              "0xOCL05 Method '%s' must start in lower-case.", astMethSig.getMethodName()));
    }
  }
}
