/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._cocos;

import de.monticore.expressions.oclexpressions._ast.ASTOCLInExpression;
import de.monticore.ocl.ocl._ast.ASTOCLContextDefinition;
import de.se_rwth.commons.logging.Log;

public class ExpressionInContextDefinitionHasToBeLetInExpression
    implements OCLASTOCLContextDefinitionCoCo {

  @Override
  public void check(ASTOCLContextDefinition astoclContextDefinition) {
    if (astoclContextDefinition.isPresentExpression()) {
      if (!(astoclContextDefinition.getExpression() instanceof ASTOCLInExpression)) {
        Log.error(
            String.format("0xOCL0B the expression in an ContextDefinition can only be a OCLInExpression, but was %s.", astoclContextDefinition.getExpression().getClass().getName()));
      }
    }
  }
}
