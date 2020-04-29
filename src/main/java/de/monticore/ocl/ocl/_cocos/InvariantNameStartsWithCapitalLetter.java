/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.se_rwth.commons.logging.Log;

public class InvariantNameStartsWithCapitalLetter
    implements OCLASTOCLInvariantCoCo {

  @Override
  public void check(ASTOCLInvariant astInv) {
    if (astInv.isPresentName() && Character.isLowerCase(astInv.getName().charAt(0))) {
      Log.error(
          String.format("0xOCL03 invariant name '%s' should start with a capital letter.",
              astInv.getName()),
          astInv.get_SourcePositionStart());
    }
  }
}
