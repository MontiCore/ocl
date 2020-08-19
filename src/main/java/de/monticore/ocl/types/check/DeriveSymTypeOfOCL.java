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

import java.util.Optional;

public class DeriveSymTypeOfOCL extends DeriveSymTypeOfExpression
    implements OCLVisitor, ITypesCalculator {

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

  /*
  @Override
  public void traverse(MCType node) {
    node.accept(getRealThis());
  }

  @Override
  public void traverse(ASTOCLOperationConstraint node) {
    ((ITypesCalculator) realThis).setScope(node.getEnclosingScope());

    OCLVisitor.super.traverse(node);
  }

  @Override
  public void traverse(ASTOCLInvariant node) {
    ((ITypesCalculator) realThis).setScope(node.getEnclosingScope());

    OCLVisitor.super.traverse(node);
  }
   */

  @Override
  public Optional<SymTypeExpression> calculateType(ASTExpression ex) {
    return ((ITypesCalculator) realThis).calculateType(ex);
  }

  @Override
  public Optional<SymTypeExpression> calculateType(ASTLiteral lit) {
    return ((ITypesCalculator) realThis).calculateType(lit);
  }

  @Override
  public Optional<SymTypeExpression> calculateType(ASTSignedLiteral lit) {
    return ((ITypesCalculator) realThis).calculateType(lit);
  }

  @Override public void init() {
    
  }
}
