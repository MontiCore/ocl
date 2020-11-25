/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.types.check;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.literals.mccommonliterals._ast.ASTSignedLiteral;
import de.monticore.literals.mcliteralsbasis._ast.ASTLiteral;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import de.monticore.ocl.ocl._visitor.OCLVisitor;
import de.monticore.types.check.DeriveSymTypeOfExpression;
import de.monticore.types.check.ITypesCalculator;
import de.monticore.types.check.TypeCheckResult;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCType;

import java.util.Optional;

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
