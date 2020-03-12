/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.ocl._ast.ASTOCLPreStatement;
import de.se_rwth.commons.logging.Log;

public class PreStatementNameStartsWithCapitalLetter
    implements OCLASTOCLPreStatementCoCo {

  @Override
  public void check(ASTOCLPreStatement astPreStatement) {
    if (astPreStatement.isPresentName() && Character.isLowerCase(astPreStatement.getName().charAt(0))) {
      Log.error(String.format("0xOCL08 pre condition name '%s' must start in upper-case.", astPreStatement.getName()),
          astPreStatement.get_SourcePositionStart());
    }
  }
}
