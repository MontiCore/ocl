/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.expressions.oclexpressionsbasis._ast.ASTLetinExpr;
import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import de.se_rwth.commons.logging.Log;

public class ExpressionInOperationConstraintHasToBeLetInExpression
    implements OCLASTOCLOperationConstraintCoCo {

  @Override
  public void check(ASTOCLOperationConstraint astoclOperationConstraint) {
    if (astoclOperationConstraint.isPresentExpression()) {
      if (!(astoclOperationConstraint.getExpression() instanceof ASTLetinExpr)) {
        Log.error(
            String.format("0xOCL0A the expression in an OperationConstraint can only be a OCLInExpression, but was %s.", astoclOperationConstraint.getExpression().getClass().getName()));
      }
    }
  }
}
