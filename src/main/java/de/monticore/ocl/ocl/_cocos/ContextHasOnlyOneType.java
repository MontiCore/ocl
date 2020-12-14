package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.ocl._ast.ASTOCLContextDefinition;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.se_rwth.commons.logging.Log;

public class ContextHasOnlyOneType implements OCLASTOCLInvariantCoCo{

  @Override
  public void check(ASTOCLInvariant node) {
    boolean typeFound = false;
    for (ASTOCLContextDefinition contextDefinition : node.getOCLContextDefinitionList()){
      if (contextDefinition.isPresentMCType()){
        if (typeFound){
          Log.error(String.format("0xOCL23 Invariant uses more than one MCType as Context Definition." ));
        }
        else {
          typeFound = true;
        }
      }
    }
  }
}
