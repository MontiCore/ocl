// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._cocos;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import de.monticore.types.check.*;
import de.se_rwth.commons.logging.Log;

public class PreAndPostConditionsAreBooleanType implements OCLASTOCLOperationConstraintCoCo {

  protected IDerive deriver;
  protected ITypeRelations typeRelations;
  
  public PreAndPostConditionsAreBooleanType(IDerive deriver) {
    this(deriver, new TypeRelations());
  }
  
  public PreAndPostConditionsAreBooleanType(IDerive deriver, ITypeRelations typeRelations) {
    this.deriver = deriver;
    this.typeRelations = typeRelations;
  }

  @Override
  public void check(ASTOCLOperationConstraint node) {
    // check preconditions
    for (ASTExpression e : node.getPreConditionList()) {
      TypeCheckResult type = deriver.deriveType(e);
      if (!type.isPresentResult()) {
        Log.error("0xOCL07 type of precondition expression " + e + " could not be calculated.");
      } else if (!typeRelations.isBoolean(type.getResult())) {
        Log.error(
            "0xOCL06 type of precondition expression "
                + e
                + " has to be boolean, but is "
                + type.getResult().print());
      }
    }

    // check postconditions
    for (ASTExpression e : node.getPostConditionList()) {
      TypeCheckResult type = deriver.deriveType(e);
      if (!type.isPresentResult()) {
        Log.error("0xOCL07 type of postcondition expression " + e + " could not be calculated.");
      } else if (!typeRelations.isBoolean(type.getResult())) {
        Log.error(
            "0xOCL06 type of postcondition expression "
                + e
                + " has to be boolean, but is "
                + type.getResult().print());
      }
    }
  }
}