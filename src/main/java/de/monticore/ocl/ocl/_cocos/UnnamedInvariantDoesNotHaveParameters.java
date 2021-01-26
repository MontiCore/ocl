/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.se_rwth.commons.logging.Log;

public class UnnamedInvariantDoesNotHaveParameters implements OCLASTOCLInvariantCoCo{

  @Override
  public void check(ASTOCLInvariant node) {
    if(!node.isPresentName()){
      if(node.sizeOCLParamDeclarations() > 0){
        Log.error(String.format("0xOCL25 Unnamed invariants are not allowed to have parameters"));
      }
    }
  }
}
