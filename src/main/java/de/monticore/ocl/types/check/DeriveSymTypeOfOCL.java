/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.types.check;

import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import de.monticore.ocl.ocl._visitor.OCLHandler;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.types.check.AbstractDeriveFromExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCType;

public class DeriveSymTypeOfOCL
  extends AbstractDeriveFromExpression
  implements OCLHandler {

  protected OCLTraverser traverser;

  public void traverse(ASTMCType node) {
    node.accept(getTraverser());
  }

  @Override
  public void traverse(ASTOCLOperationConstraint node) {
    OCLHandler.super.traverse(node);
  }

  @Override
  public void traverse(ASTOCLInvariant node) {
    OCLHandler.super.traverse(node);
  }

  @Override public OCLTraverser getTraverser() {
    return traverser;
  }

  public void setTraverser(OCLTraverser traverser) {
    this.traverser = traverser;
  }
}
