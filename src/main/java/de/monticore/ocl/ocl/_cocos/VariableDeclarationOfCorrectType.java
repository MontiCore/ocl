/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.ocl._cocos; /* (c) https://github.com/MontiCore/monticore */

import de.monticore.ocl.oclexpressions._ast.ASTOCLVariableDeclaration;
import de.monticore.ocl.oclexpressions._cocos.OCLExpressionsASTOCLVariableDeclarationCoCo;
import de.monticore.ocl.types3.OCLSymTypeRelations;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

public class VariableDeclarationOfCorrectType
    implements OCLExpressionsASTOCLVariableDeclarationCoCo {

  @Override
  public void check(ASTOCLVariableDeclaration node) {
    if (!node.isPresentMCType()) {
      Log.error(
          String.format("0xOCL30 Variable at %s has no type.", node.get_SourcePositionStart()));
    } else {
      SymTypeExpression result = TypeCheck3.typeOf(node.getExpression());
      if (result.isObscureType()) {
        Log.error(
            String.format(
                "0xOCL31 Type of Variable at %s could not be calculated.",
                node.get_SourcePositionStart()));
      }
      SymTypeExpression type = TypeCheck3.symTypeFromAST(node.getMCType());
      if (type.isObscureType()) {
        Log.error(
            String.format(
                "0xOCL32 Type of Variable at %s could not be calculated.",
                node.get_SourcePositionStart()));
      }
      if (!OCLSymTypeRelations.isCompatible(type, result)) {
        Log.error(
            String.format(
                "0xOCL33 (%s): Type of variable %s is incompatible with expression type %s.",
                node.get_SourcePositionStart(), type.printFullName(), result.printFullName()));
      }
    }
  }
}
