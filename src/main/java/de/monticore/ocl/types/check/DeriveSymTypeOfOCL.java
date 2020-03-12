/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.types.check;

import de.monticore.ocl.ocl._ast.ASTOCLExtType;
import de.monticore.ocl.ocl._visitor.OCLVisitor;
import de.monticore.types.check.DeriveSymTypeOfExpression;
import de.monticore.types.check.LastResult;

public class DeriveSymTypeOfOCL extends DeriveSymTypeOfExpression
    implements OCLVisitor {

  private OCLVisitor realThis;

  @Override
  public void setRealThis(OCLVisitor realThis) {
    this.realThis = realThis;
  }

  @Override
  public OCLVisitor getRealThis() {
    return realThis;
  }

  public DeriveSymTypeOfOCL() {
    realThis = this;
  }

  public void setLastResult(LastResult lastResult) {
    this.lastResult = lastResult;
  }

  @Override
  public void traverse(ASTOCLExtType node) {
    node.accept(getRealThis());
  }
}
