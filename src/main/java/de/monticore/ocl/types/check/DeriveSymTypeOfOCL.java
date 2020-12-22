/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.types.check;

import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import de.monticore.ocl.ocl._visitor.OCLVisitor;
import de.monticore.types.check.DeriveSymTypeOfExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCType;

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

  public void traverse(ASTMCType node) {
    node.accept(getRealThis());
  }

  @Override
  public void traverse(ASTOCLOperationConstraint node) {
    OCLVisitor.super.traverse(node);
  }

  @Override
  public void traverse(ASTOCLInvariant node) {
    OCLVisitor.super.traverse(node);
  }
}
