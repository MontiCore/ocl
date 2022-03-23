/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.types.check;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.literals.mcliteralsbasis._ast.ASTLiteral;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.types.check.*;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedName;
import de.monticore.types.mcbasictypes._ast.ASTMCReturnType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.visitor.ITraverser;

public class OCLTypeCalculator implements IDerive, ISynthesize {

  protected ITraverser traverser;
  protected TypeCheckResult typeCheckResult;

  public OCLTypeCalculator() {
    this(OCLMill.traverser());
  }

  public OCLTypeCalculator(OCLTraverser traverser) {
    this.traverser = traverser;
    this.init(traverser);
  }

  protected ITraverser getTraverser() {
    return traverser;
  }

  protected TypeCheckResult getTypeCheckResult() {
    return typeCheckResult;
  }

  @Override
  public TypeCheckResult deriveType(ASTExpression expr) {
    this.getTypeCheckResult().reset();
    expr.accept(this.getTraverser());
    return this.getTypeCheckResult().copy();
  }

  @Override
  public TypeCheckResult deriveType(ASTLiteral lit) {
    this.getTypeCheckResult().reset();
    lit.accept(this.getTraverser());
    return this.getTypeCheckResult().copy();
  }

  @Override
  public TypeCheckResult synthesizeType(ASTMCType type) {
    this.getTypeCheckResult().reset();
    type.accept(this.getTraverser());
    return this.getTypeCheckResult().copy();
  }

  @Override
  public TypeCheckResult synthesizeType(ASTMCReturnType type) {
    this.getTypeCheckResult().reset();
    type.accept(this.getTraverser());
    return this.getTypeCheckResult().copy();
  }

  @Override
  public TypeCheckResult synthesizeType(ASTMCQualifiedName qName) {
    this.getTypeCheckResult().reset();
    qName.accept(this.getTraverser());
    return this.getTypeCheckResult().copy();
  }

  protected void init(OCLTraverser traverser) {
    this.typeCheckResult = new TypeCheckResult();

    // initializes visitors used for calculating types
    DeriveSymTypeOfExpression deriveSymTypeOfExpression = new DeriveSymTypeOfExpression();
    deriveSymTypeOfExpression.setTypeCheckResult(typeCheckResult);
    traverser.add4ExpressionsBasis(deriveSymTypeOfExpression);
    traverser.setExpressionsBasisHandler(deriveSymTypeOfExpression);

    DeriveSymTypeOfCommonExpressions deriveSymTypeOfCommonExpressions = new DeriveSymTypeOfCommonExpressions();
    deriveSymTypeOfCommonExpressions.setTypeCheckResult(typeCheckResult);
    traverser.add4CommonExpressions(deriveSymTypeOfCommonExpressions);
    traverser.setCommonExpressionsHandler(deriveSymTypeOfCommonExpressions);

    DeriveSymTypeOfSetExpressions deriveSymTypeOfSetExpressions = new DeriveSymTypeOfSetExpressions();
    deriveSymTypeOfSetExpressions.setTypeCheckResult(typeCheckResult);
    traverser.add4SetExpressions(deriveSymTypeOfSetExpressions);
    traverser.setSetExpressionsHandler(deriveSymTypeOfSetExpressions);

    DeriveSymTypeOfOCLExpressions deriveSymTypeOfOCLExpressions = new DeriveSymTypeOfOCLExpressions();
    deriveSymTypeOfOCLExpressions.setTypeCheckResult(typeCheckResult);
    traverser.setOCLExpressionsHandler(deriveSymTypeOfOCLExpressions);

    DeriveSymTypeOfLiterals deriveSymTypeOfLiterals = new DeriveSymTypeOfLiterals();
    deriveSymTypeOfLiterals.setTypeCheckResult(typeCheckResult);
    traverser.add4MCLiteralsBasis(deriveSymTypeOfLiterals);

    DeriveSymTypeOfMCCommonLiterals deriveSymTypeOfMCCommonLiterals = new DeriveSymTypeOfMCCommonLiterals();
    deriveSymTypeOfMCCommonLiterals.setTypeCheckResult(typeCheckResult);
    traverser.add4MCCommonLiterals(deriveSymTypeOfMCCommonLiterals);

    DeriveSymTypeOfOCL deriveSymTypeOfOCL = new DeriveSymTypeOfOCL();
    deriveSymTypeOfOCL.setTypeCheckResult(typeCheckResult);
    traverser.setOCLHandler(deriveSymTypeOfOCL);

    DeriveSymTypeOfOptionalOperators deriveSymTypeOfOptionalOperators = new DeriveSymTypeOfOptionalOperators();
    deriveSymTypeOfOptionalOperators.setTypeCheckResult(typeCheckResult);
    traverser.setOptionalOperatorsHandler(deriveSymTypeOfOptionalOperators);

    SynthesizeSymTypeFromMCSimpleGenericTypes synthesizeSymTypeFromMCSimpleGenericTypes = new SynthesizeSymTypeFromMCSimpleGenericTypes();
    synthesizeSymTypeFromMCSimpleGenericTypes.setTypeCheckResult(typeCheckResult);
    traverser.add4MCSimpleGenericTypes(synthesizeSymTypeFromMCSimpleGenericTypes);
    traverser.setMCSimpleGenericTypesHandler(synthesizeSymTypeFromMCSimpleGenericTypes);

    SynthesizeSymTypeFromMCBasicTypes4OCL synthesizeSymTypeFromMCBasicTypes = new SynthesizeSymTypeFromMCBasicTypes4OCL();
    synthesizeSymTypeFromMCBasicTypes.setTypeCheckResult(typeCheckResult);
    traverser.add4MCBasicTypes(synthesizeSymTypeFromMCBasicTypes);
    traverser.setMCBasicTypesHandler(synthesizeSymTypeFromMCBasicTypes);

    SynthesizeSymTypeFromMCCollectionTypes synthesizeSymTypeFromMCCollectionTypes = new SynthesizeSymTypeFromMCCollectionTypes();
    synthesizeSymTypeFromMCCollectionTypes.setTypeCheckResult(typeCheckResult);
    traverser.add4MCCollectionTypes(synthesizeSymTypeFromMCCollectionTypes);
    traverser.setMCCollectionTypesHandler(synthesizeSymTypeFromMCCollectionTypes);
  }
}