/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import de.se_rwth.commons.logging.Log;

public class PostStatementNameStartsWithCapitalLetter
    implements OCLASTOCLOperationConstraintCoCo {

  @Override
  public void check(ASTOCLOperationConstraint astoclOperationConstraint) {
    if (astoclOperationConstraint.isPresentPost()){
      String postName = astoclOperationConstraint.getPost();
      if (Character.isLowerCase(postName.charAt(0))) {
        Log.error(String.format("0xOCL07 post condition name '%s' must start in upper-case.", postName),
                astoclOperationConstraint.get_SourcePositionStart());
      }
    }
  }
}
