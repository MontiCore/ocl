/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._cocos;

import de.monticore.expressions.oclexpressions._ast.ASTOCLParamDeclaration;
import de.monticore.expressions.oclexpressions._cocos.OCLExpressionsASTOCLParamDeclarationCoCo;
import de.se_rwth.commons.logging.Log;

public class ParameterDeclarationNameStartsWithLowerCaseLetter
    implements OCLExpressionsASTOCLParamDeclarationCoCo {

  @Override
  public void check(ASTOCLParamDeclaration astParameterDeclaration) {
    String parameterName = astParameterDeclaration.getParam();
    boolean startsWithUpperCase = Character.isUpperCase(parameterName.charAt(0));

    if (startsWithUpperCase) {
      Log.error(String.format("0xOCL06 parameter name '%s' must start in lower-case.", parameterName),
          astParameterDeclaration.get_SourcePositionStart());
    }
  }
}
