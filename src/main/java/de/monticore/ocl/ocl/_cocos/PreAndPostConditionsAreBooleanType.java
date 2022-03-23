// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._cocos;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import de.monticore.ocl.types.check.OCLTypeCalculator;
import de.monticore.ocl.types.check.OCLTypeCheck;
import de.monticore.types.check.TypeCheckResult;
import de.se_rwth.commons.logging.Log;

public class PreAndPostConditionsAreBooleanType implements OCLASTOCLOperationConstraintCoCo {

  protected OCLTypeCalculator typeCalculator;

  protected PreAndPostConditionsAreBooleanType() {
    this(new OCLTypeCalculator());
  }

  public PreAndPostConditionsAreBooleanType(OCLTypeCalculator typeCalculator) {
    this.typeCalculator = typeCalculator;
  }

  @Override
  public void check(ASTOCLOperationConstraint node) {
    //check preconditions
    for (ASTExpression e : node.getPreConditionList()) {
      TypeCheckResult type = typeCalculator.deriveType(e);
      if (!type.isPresentCurrentResult()) {
        Log.error("type of precondition expression " + e + " could not be calculated.");
      }
      if (!OCLTypeCheck.isBoolean(type.getCurrentResult())) {
        Log.error("type of precondition expression " + e +
          " has to be boolean, but is " + type.getCurrentResult().print());
      }
    }

    //check postconditions
    for (ASTExpression e : node.getPostConditionList()) {
      TypeCheckResult type = typeCalculator.deriveType(e);
      if (!type.isPresentCurrentResult()) {
        Log.error("type of postcondition expression " + e + " could not be calculated.");
      }
      if (!OCLTypeCheck.isBoolean(type.getCurrentResult())) {
        Log.error("type of postcondition expression " + e +
          " has to be boolean, but is " + type.getCurrentResult().print());
      }
    }
  }
}
