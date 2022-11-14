// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._cocos;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import de.monticore.ocl.types.check.OCLDeriver;
import de.monticore.ocl.types.check.OCLTypeCheck;
import de.monticore.types.check.IDerive;
import de.monticore.types.check.TypeCheckResult;
import de.se_rwth.commons.logging.Log;

public class PreAndPostConditionsAreBooleanType implements OCLASTOCLOperationConstraintCoCo {

  protected IDerive deriver;

  protected PreAndPostConditionsAreBooleanType() {
    this(new OCLDeriver());
  }

  public PreAndPostConditionsAreBooleanType(IDerive deriver) {
    this.deriver = deriver;
  }

  @Override
  public void check(ASTOCLOperationConstraint node) {
    //check preconditions
    for (ASTExpression e : node.getPreConditionList()) {
      TypeCheckResult type = deriver.deriveType(e);
      if (!type.isPresentResult()) {
        Log.error("0xOCL07 type of precondition expression " + e + " could not be calculated.");
      } else if (!OCLTypeCheck.isBoolean(type.getResult())) {
        Log.error("0xOCL06 type of precondition expression " + e +
          " has to be boolean, but is " + type.getResult().print());
      }
    }

    //check postconditions
    for (ASTExpression e : node.getPostConditionList()) {
      TypeCheckResult type = deriver.deriveType(e);
      if (!type.isPresentResult()) {
        Log.error("0xOCL07 type of postcondition expression " + e + " could not be calculated.");
      } else if (!OCLTypeCheck.isBoolean(type.getResult())) {
        Log.error("0xOCL06 type of postcondition expression " + e +
          " has to be boolean, but is " + type.getResult().print());
      }
    }
  }
}
