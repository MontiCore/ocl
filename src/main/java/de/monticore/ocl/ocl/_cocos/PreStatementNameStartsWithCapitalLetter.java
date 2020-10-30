/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import de.se_rwth.commons.logging.Log;

public class PreStatementNameStartsWithCapitalLetter
    implements OCLASTOCLOperationConstraintCoCo {

  @Override
  public void check(ASTOCLOperationConstraint astoclOperationConstraint) {
    if (astoclOperationConstraint.isPresentPre()){
      String preName = astoclOperationConstraint.getPre();
      if (Character.isLowerCase(preName.charAt(0))) {
        Log.error(String.format("0xOCL07 pre condition name '%s' must start in upper-case.", preName),
                astoclOperationConstraint.get_SourcePositionStart());
      }
    }
  }
}
