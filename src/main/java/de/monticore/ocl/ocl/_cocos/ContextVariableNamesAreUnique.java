package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.ocl._ast.ASTOCLContextDefinition;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;

public class ContextVariableNamesAreUnique implements OCLASTOCLInvariantCoCo{

  @Override
  public void check(ASTOCLInvariant node) {
    List<String> varNames = new ArrayList<>();
    for (ASTOCLContextDefinition contextDefinition : node.getOCLContextDefinitionList()){
      if (contextDefinition.isPresentMCType()){
        for (String s : varNames){
          if (s.equals(contextDefinition.getMCType().getClass().getName())){
            Log.error(String.format("0xOCL22 Variable name '%s' occurs twice in invariant ",
                    contextDefinition.getMCType().getClass().getName()));
          }
        }
        varNames.add(contextDefinition.getMCType().getClass().getName());
      }
      else if (contextDefinition.isPresentOCLParamDeclaration()){
        for (String s : varNames){
          if (s.equals(contextDefinition.getMCType().getClass().getName())){
            Log.error(String.format("0xOCL22 Variable name '%s' occurs twice in invariant ",
                    contextDefinition.getMCType().getClass().getName()));
        }
      }
        varNames.add(contextDefinition.getOCLParamDeclaration().getName());
      }
      //TODO: check "in" Expression (change grammar first)
    }
  }
}
