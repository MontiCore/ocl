// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.ocl._ast.ASTOCLContextDefinition;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.types.mcsimplegenerictypes.MCSimpleGenericTypesMill;
import de.se_rwth.commons.logging.Log;
import java.util.ArrayList;
import java.util.List;

public class ContextVariableNamesAreUnique implements OCLASTOCLInvariantCoCo {

  @Override
  public void check(ASTOCLInvariant node) {
    List<String> varNames = new ArrayList<>();
    for (ASTOCLContextDefinition contextDefinition : node.getOCLContextDefinitionList()) {
      if (contextDefinition.isPresentMCType()) {
        for (String s : varNames) {
          if (s.equals(
              contextDefinition
                  .getMCType()
                  .printType(MCSimpleGenericTypesMill.mcSimpleGenericTypesPrettyPrinter())
                  .toLowerCase())) {
            Log.error(
                String.format(
                    "0xOCL22 Variable name '%s' occurs twice in invariant ",
                    contextDefinition
                        .getMCType()
                        .printType(MCSimpleGenericTypesMill.mcSimpleGenericTypesPrettyPrinter())
                        .toLowerCase()));
          }
        }
        varNames.add(
            contextDefinition
                .getMCType()
                .printType(MCSimpleGenericTypesMill.mcSimpleGenericTypesPrettyPrinter())
                .toLowerCase());
      } else if (contextDefinition.isPresentOCLParamDeclaration()) {
        for (String s : varNames) {
          if (s.equals(contextDefinition.getOCLParamDeclaration().getName())) {
            Log.error(
                String.format(
                    "0xOCL22 Variable name '%s' occurs twice in invariant ",
                    contextDefinition.getOCLParamDeclaration().getName()));
          }
        }
        varNames.add(contextDefinition.getOCLParamDeclaration().getName());
      } else if (contextDefinition.isPresentGeneratorDeclaration()) {
        for (String s : varNames) {
          if (s.equals(contextDefinition.getGeneratorDeclaration().getName())) {
            Log.error(
                String.format(
                    "0xOCL22 Variable name '%s' occurs twice in invariant ",
                    contextDefinition.getGeneratorDeclaration().getName()));
          }
        }
        varNames.add(contextDefinition.getGeneratorDeclaration().getName());
      }
    }
  }
}
