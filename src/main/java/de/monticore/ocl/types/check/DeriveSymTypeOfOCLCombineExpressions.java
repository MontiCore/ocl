/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.types.check;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._symboltable.IExpressionsBasisScope;
import de.monticore.literals.mccommonliterals._ast.ASTSignedLiteral;
import de.monticore.ocl.expressions.oclexpressions._ast.ASTOCLExpressionsNode;
import de.monticore.literals.mcliteralsbasis._ast.ASTLiteral;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._visitor.OCLDelegatorVisitor;
import de.monticore.types.check.*;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;

import java.util.Optional;

/**
 * Delegator Visitor to test the combination of the grammars
 */
public class DeriveSymTypeOfOCLCombineExpressions
    extends OCLDelegatorVisitor
    implements ITypesCalculator {

  private TypeCheckResult typeCheckResult;

  private DeriveSymTypeOfExpression deriveSymTypeOfExpression;

  private DeriveSymTypeOfCommonExpressions deriveSymTypeOfCommonExpressions;

  private DeriveSymTypeOfSetExpressions deriveSymTypeOfSetExpressions;

  private DeriveSymTypeOfOCLExpressions deriveSymTypeOfOCLExpressions;

  private DeriveSymTypeOfLiterals deriveSymTypeOfLiterals;

  private DeriveSymTypeOfMCCommonLiterals deriveSymTypeOfMCCommonLiterals;

  private DeriveSymTypeOfOCL deriveSymTypeOfOCL;

  private DeriveSymTypeOfOptionalOperators deriveSymTypeOfOptionalOperators;

  private SynthesizeSymTypeFromMCSimpleGenericTypes synthesizeSymTypeFromMCSimpleGenericTypes;

  private SynthesizeSymTypeFromMCBasicTypes synthesizeSymTypeFromMCBasicTypes;

  private SynthesizeSymTypeFromMCCollectionTypes synthesizeSymTypeFromMCCollectionTypes;

  public DeriveSymTypeOfOCLCombineExpressions() {
    setRealThis(this);
    this.typeCheckResult = new TypeCheckResult();
    init();
  }

  public Optional<SymTypeExpression> calculateType(ASTMCType type) {
    type.accept(getRealThis());
    if (getTypeCheckResult().isPresentCurrentResult()) {
      return Optional.of(getTypeCheckResult().getCurrentResult());
    }
    else {
      return Optional.empty();
    }
  }

  public Optional<SymTypeExpression> calculateType(ASTOCLCompilationUnit node) {
    node.accept(getRealThis());
    if (getTypeCheckResult().isPresentCurrentResult()) {
      return Optional.of(getTypeCheckResult().getCurrentResult());
    }
    else {
      return Optional.empty();
    }
  }

  /**
   * main method to calculate the type of an expression
   */
  public Optional<SymTypeExpression> calculateType(ASTExpression e) {
    e.accept(getRealThis());
    if (getTypeCheckResult().isPresentCurrentResult()) {
      return Optional.of(getTypeCheckResult().getCurrentResult());
    }
    else {
      return Optional.empty();
    }
  }

  public Optional<SymTypeExpression> calculateType(ASTOCLExpressionsNode e) {
    e.accept(getRealThis());
    if (getTypeCheckResult().isPresentCurrentResult()) {
      return Optional.of(getTypeCheckResult().getCurrentResult());
    }
    else {
      return Optional.empty();
    }
  }

  /**
   * main method to calculate the type of a literal
   */
  @Override
  public Optional<SymTypeExpression> calculateType(ASTLiteral lit) {
    lit.accept(getRealThis());
    if (getTypeCheckResult().isPresentCurrentResult()) {
      return Optional.of(getTypeCheckResult().getCurrentResult());
    }
    else {
      return Optional.empty();
    }
  }

  @Override
  public Optional<SymTypeExpression> calculateType(ASTSignedLiteral lit) {
    lit.accept(getRealThis());
    if (getTypeCheckResult().isPresentCurrentResult()) {
      return Optional.of(getTypeCheckResult().getCurrentResult());
    }
    else {
      return Optional.empty();
    }
  }

  /**
   * initialize the typescalculator
   */
  @Override
  public void init() {
    //initializes visitors used for typechecking
    deriveSymTypeOfExpression = new DeriveSymTypeOfExpression();
    deriveSymTypeOfExpression.setTypeCheckResult(typeCheckResult);
    setExpressionsBasisVisitor(deriveSymTypeOfExpression);

    deriveSymTypeOfCommonExpressions = new DeriveSymTypeOfCommonExpressions();
    deriveSymTypeOfCommonExpressions.setTypeCheckResult(typeCheckResult);
    setCommonExpressionsVisitor(deriveSymTypeOfCommonExpressions);

    deriveSymTypeOfSetExpressions = new DeriveSymTypeOfSetExpressions();
    deriveSymTypeOfSetExpressions.setTypeCheckResult(typeCheckResult);
    setSetExpressionsVisitor(deriveSymTypeOfSetExpressions);

    deriveSymTypeOfOCLExpressions = new DeriveSymTypeOfOCLExpressions();
    deriveSymTypeOfOCLExpressions.setTypeCheckResult(typeCheckResult);
    setOCLExpressionsVisitor(deriveSymTypeOfOCLExpressions);

    deriveSymTypeOfLiterals = new DeriveSymTypeOfLiterals();
    deriveSymTypeOfLiterals.setTypeCheckResult(typeCheckResult);
    setMCLiteralsBasisVisitor(deriveSymTypeOfLiterals);

    deriveSymTypeOfMCCommonLiterals = new DeriveSymTypeOfMCCommonLiterals();
    deriveSymTypeOfMCCommonLiterals.setTypeCheckResult(typeCheckResult);
    setMCCommonLiteralsVisitor(deriveSymTypeOfMCCommonLiterals);

    deriveSymTypeOfOCL = new DeriveSymTypeOfOCL();
    deriveSymTypeOfOCL.setTypeCheckResult(typeCheckResult);
    setOCLVisitor(deriveSymTypeOfOCL);

    deriveSymTypeOfOptionalOperators = new DeriveSymTypeOfOptionalOperators();
    deriveSymTypeOfOptionalOperators.setTypeCheckResult(typeCheckResult);
    setOptionalOperatorsVisitor(deriveSymTypeOfOptionalOperators);

    synthesizeSymTypeFromMCSimpleGenericTypes = new SynthesizeSymTypeFromMCSimpleGenericTypes();
    synthesizeSymTypeFromMCSimpleGenericTypes.setTypeCheckResult(typeCheckResult);
    setMCSimpleGenericTypesVisitor(synthesizeSymTypeFromMCSimpleGenericTypes);

    synthesizeSymTypeFromMCBasicTypes = new SynthesizeSymTypeFromMCBasicTypes();
    synthesizeSymTypeFromMCBasicTypes.setTypeCheckResult(typeCheckResult);
    setMCBasicTypesVisitor(synthesizeSymTypeFromMCBasicTypes);

    synthesizeSymTypeFromMCCollectionTypes = new SynthesizeSymTypeFromMCCollectionTypes();
    synthesizeSymTypeFromMCCollectionTypes.setTypeCheckResult(typeCheckResult);
    setMCCollectionTypesVisitor(synthesizeSymTypeFromMCCollectionTypes);
  }

  public TypeCheckResult getTypeCheckResult(){
    return typeCheckResult;
  }

  public void setTypeCheckResult(TypeCheckResult typeCheckResult){
    this.typeCheckResult = typeCheckResult;
  }
}
