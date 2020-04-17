/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._cocos;

import de.monticore.expressions.oclexpressions._ast.ASTOCLParamDeclaration;
import de.monticore.ocl.ocl._ast.ASTOCLParameters;
import de.se_rwth.commons.Joiners;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ParameterNamesUnique
    implements OCLASTOCLParametersCoCo {

  @Override
  public void check(ASTOCLParameters astParameterDeclaration) {
    final List<String> duplicateNames = astParameterDeclaration.getParamsList()
        .stream().collect(Collectors.groupingBy(ASTOCLParamDeclaration::getParam)).entrySet()
        .stream().filter(e -> e.getValue().size() > 1).map(Map.Entry::getKey).collect(Collectors.toList());
    if (!duplicateNames.isEmpty()) {
      Log.error(String.format("0xOCL0C parameters need to have unique names, but following names are used more than once (%s).", Joiners.COMMA.join(duplicateNames)),
          astParameterDeclaration.get_SourcePositionStart());
    }
  }
}
