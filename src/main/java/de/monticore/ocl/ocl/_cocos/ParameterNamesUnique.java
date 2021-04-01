// (c) https://github.com/MontiCore/monticore

package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.ocl._ast.*;
import de.se_rwth.commons.Joiners;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ParameterNamesUnique implements OCLASTOCLConstraintCoCo {

  @Override
  public void check(ASTOCLConstraint astoclConstraint) {
    if(astoclConstraint instanceof ASTOCLInvariant){
      final List<String> duplicateNames = ((ASTOCLInvariant) astoclConstraint).getOCLParamDeclarationList()
              .stream().collect(Collectors.groupingBy(ASTOCLParamDeclaration::getName)).entrySet()
              .stream().filter(e -> e.getValue().size() > 1).map(Map.Entry::getKey).collect(Collectors.toList());
      if (!duplicateNames.isEmpty()) {
        Log.error(String.format("0xOCL0C parameters need to have unique names, but following names are used more than once (%s).", Joiners.COMMA.join(duplicateNames)),
                astoclConstraint.get_SourcePositionStart());
      }
    }
    else if(astoclConstraint instanceof ASTOCLOperationConstraint){
      ASTOCLOperationConstraint astoclOperationConstraint = (ASTOCLOperationConstraint) astoclConstraint;
      if(astoclOperationConstraint.getOCLOperationSignature() instanceof ASTOCLMethodSignature){
        final List<String> duplicateNames = ((ASTOCLMethodSignature) astoclOperationConstraint.getOCLOperationSignature()).getOCLParamDeclarationList()
                .stream().collect(Collectors.groupingBy(ASTOCLParamDeclaration::getName)).entrySet()
                .stream().filter(e -> e.getValue().size() > 1).map(Map.Entry::getKey).collect(Collectors.toList());
        if (!duplicateNames.isEmpty()) {
          Log.error(String.format("0xOCL0C parameters need to have unique names, but following names are used more than once (%s).", Joiners.COMMA.join(duplicateNames)),
                  astoclConstraint.get_SourcePositionStart());
        }
      }
      else if(astoclOperationConstraint.getOCLOperationSignature() instanceof ASTOCLConstructorSignature){
        final List<String> duplicateNames = ((ASTOCLConstructorSignature) astoclOperationConstraint.getOCLOperationSignature()).getOCLParamDeclarationList()
                .stream().collect(Collectors.groupingBy(ASTOCLParamDeclaration::getName)).entrySet()
                .stream().filter(e -> e.getValue().size() > 1).map(Map.Entry::getKey).collect(Collectors.toList());
        if (!duplicateNames.isEmpty()) {
          Log.error(String.format("0xOCL0C parameters need to have unique names, but following names are used more than once (%s).", Joiners.COMMA.join(duplicateNames)),
                  astoclConstraint.get_SourcePositionStart());
        }
      }
    }
  }
}
