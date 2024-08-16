// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._cocos;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import de.monticore.types.check.*;
import de.monticore.types3.SymTypeRelations;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

public class PreAndPostConditionsAreBooleanType implements OCLASTOCLOperationConstraintCoCo {

  @Override
  public void check(ASTOCLOperationConstraint node) {
    // check preconditions
    for (ASTExpression e : node.getPreConditionList()) {
      SymTypeExpression type = TypeCheck3.typeOf(e);
      if (type.isObscureType()) {
        Log.error(
            "0xOCL07 type of precondition expression "
                + OCLMill.prettyPrint(e, false)
                + " could not be calculated.");
      } else if (!SymTypeRelations.isBoolean(type)) {
        Log.error(
            "0xOCL06 type of precondition expression "
                + OCLMill.prettyPrint(e, false)
                + " has to be boolean, but is "
                + type.printFullName());
      }
    }

    // check postconditions
    for (ASTExpression e : node.getPostConditionList()) {
      SymTypeExpression type = TypeCheck3.typeOf(e);
      if (type.isObscureType()) {
        Log.error(
            "0xOCL07 type of postcondition expression "
                + OCLMill.prettyPrint(e, false)
                + " could not be calculated.");
      } else if (!SymTypeRelations.isBoolean(type)) {
        Log.error(
            "0xOCL06 type of postcondition expression "
                + OCLMill.prettyPrint(e, false)
                + " has to be boolean, but is "
                + type.printFullName());
      }
    }
  }
}
