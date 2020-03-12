/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._cocos;

import de.monticore.expressions.oclexpressions._ast.ASTOCLVariableDeclaration;
import de.monticore.expressions.oclexpressions._cocos.OCLExpressionsASTOCLVariableDeclarationCoCo;
import de.se_rwth.commons.logging.Log;

public class VariableDeclarationStartsWithLowerCaseLetter
    implements OCLExpressionsASTOCLVariableDeclarationCoCo {

  @Override
  public void check(ASTOCLVariableDeclaration astVariableDeclaration) {
    for (String name : astVariableDeclaration.getNameList()) {
      boolean startsWithUpperCase = Character.isUpperCase(name.charAt(0));

      if (startsWithUpperCase) {
        Log.error(
            String.format("0xOCL09 variable declaration name '%s' must start with a lower-case letter.", name),
            astVariableDeclaration.get_SourcePositionStart());
      }
    }
  }
}
