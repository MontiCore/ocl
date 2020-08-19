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

import java.util.Optional;

/**
 * Delegator Visitor to test the combination of the grammars
 */
public class DeriveSymTypeOfOCLCombineExpressions
    extends OCLDelegatorVisitor
    implements ITypesCalculator {

  private OCLDelegatorVisitor realThis;

  private DeriveSymTypeOfExpression deriveSymTypeOfExpression;

  private DeriveSymTypeOfCommonExpressions deriveSymTypeOfCommonExpressions;

  //private DeriveSymTypeOfSetExpressions deriveSymTypeOfSetExpressions;

  //private DeriveSymTypeOfOCLExpressions deriveSymTypeOfOCLExpressions;

  private DeriveSymTypeOfLiterals deriveSymTypeOfLiterals;

  private DeriveSymTypeOfMCCommonLiterals deriveSymTypeOfMCCommonLiterals;

  private DeriveSymTypeOfOCL deriveSymTypeOfOCL;

  private TypeCheckResult lastResult = new TypeCheckResult();

  public TypeCheckResult getLastResult() {
    return lastResult;
  }

  public DeriveSymTypeOfOCLCombineExpressions(IExpressionsBasisScope scope) {
    this.realThis = this;

    initializeTypeVisitor();

    deriveSymTypeOfExpression = new DeriveSymTypeOfExpression();
    deriveSymTypeOfExpression.setTypeCheckResult(lastResult);
    setExpressionsBasisVisitor(deriveSymTypeOfExpression);

    deriveSymTypeOfCommonExpressions = new DeriveSymTypeOfCommonExpressions();
    deriveSymTypeOfCommonExpressions.setTypeCheckResult(lastResult);
    setCommonExpressionsVisitor(deriveSymTypeOfCommonExpressions);

    /*deriveSymTypeOfSetExpressions = new DeriveSymTypeOfSetExpressions();
    deriveSymTypeOfSetExpressions.setScope(scope);
    deriveSymTypeOfSetExpressions.setLastResult(lastResult);
    setSetExpressionsVisitor(deriveSymTypeOfSetExpressions);*/

    /*
    deriveSymTypeOfOCLExpressions = new DeriveSymTypeOfOCLExpressions();
    deriveSymTypeOfOCLExpressions.setLastResult(lastResult);
    setOCLExpressionsVisitor(deriveSymTypeOfOCLExpressions);
     */

    deriveSymTypeOfLiterals = new DeriveSymTypeOfLiterals();
    deriveSymTypeOfLiterals.setTypeCheckResult(lastResult);
    setMCLiteralsBasisVisitor(deriveSymTypeOfLiterals);

    deriveSymTypeOfMCCommonLiterals = new DeriveSymTypeOfMCCommonLiterals();
    deriveSymTypeOfMCCommonLiterals.setTypeCheckResult(lastResult);
    setMCCommonLiteralsVisitor(deriveSymTypeOfMCCommonLiterals);

    deriveSymTypeOfOCL = new DeriveSymTypeOfOCL();
    deriveSymTypeOfOCL.setTypeCheckResult(lastResult);
    setOCLVisitor(deriveSymTypeOfOCL);

    //setScope(scope);
  }

  protected DeriveSymTypeOfExpression getExpressionsBasisVisitorAsTypeVisitor() {
    return (DeriveSymTypeOfExpression) getExpressionsBasisVisitor().get();
  }

  protected DeriveSymTypeOfCommonExpressions getCommonExpressionsVisitorAsTypeVisitor() {
    return (DeriveSymTypeOfCommonExpressions) getCommonExpressionsVisitor().get();
  }

  /*protected DeriveSymTypeOfSetExpressions getSetExpressionsVisitorAsTypeVisitor() {
    return (DeriveSymTypeOfSetExpressions) getSetExpressionsVisitor().get();
  }*/

  /*
  public DeriveSymTypeOfOCLExpressions getOCLExpressionsVisitorAsTypeVisitor() {
    return (DeriveSymTypeOfOCLExpressions) getOCLExpressionsVisitor().get();
  }*/

  protected SynthesizeSymTypeFromMCCollectionTypes getMCCollectionTypesVisitorAsSynthesizedType() {
    return (SynthesizeSymTypeFromMCCollectionTypes) getMCCollectionTypesVisitor().get();
  }

  @Override
  public void handle(ASTMCQualifiedType node) {
    getMCCollectionTypesVisitor().get().handle(node);
    final Optional<SymTypeExpression> result = getMCCollectionTypesVisitorAsSynthesizedType().getResult();
    result.ifPresent(r -> lastResult.setCurrentResult(r));
  }

  public Optional<SymTypeExpression> calculateType(ASTOCLCompilationUnit node) {
    node.accept(realThis);
    Optional<SymTypeExpression> result = Optional.empty();
    if (lastResult.isPresentCurrentResult()) {
      result = Optional.ofNullable(lastResult.getCurrentResult());
    }
    lastResult.setCurrentResultAbsent();
    return result;
  }

  /**
   * main method to calculate the type of an expression
   */
  public Optional<SymTypeExpression> calculateType(ASTExpression e) {
    e.accept(realThis);
    Optional<SymTypeExpression> result = Optional.empty();
    if (lastResult.isPresentCurrentResult()) {
      result = Optional.ofNullable(lastResult.getCurrentResult());
    }
    lastResult.setCurrentResultAbsent();
    return result;
  }

  public Optional<SymTypeExpression> calculateType(ASTOCLExpressionsNode e) {
    e.accept(realThis);
    Optional<SymTypeExpression> result = Optional.empty();
    if (lastResult.isPresentCurrentResult()) {
      result = Optional.ofNullable(lastResult.getCurrentResult());
    }
    lastResult.setCurrentResultAbsent();
    return result;
  }

  /**
   * main method to calculate the type of a literal
   */
  @Override
  public Optional<SymTypeExpression> calculateType(ASTLiteral lit) {
    lit.accept(realThis);
    Optional<SymTypeExpression> result = Optional.empty();
    if (lastResult.isPresentCurrentResult()) {
      result = Optional.ofNullable(lastResult.getCurrentResult());
    }
    lastResult.setCurrentResultAbsent();
    return result;
  }

  @Override
  public Optional<SymTypeExpression> calculateType(ASTSignedLiteral lit) {
    lit.accept(realThis);
    Optional<SymTypeExpression> result = Optional.empty();
    if (lastResult.isPresentCurrentResult()) {
      result = Optional.ofNullable(lastResult.getCurrentResult());
    }
    lastResult.setCurrentResultAbsent();
    return result;
  }

  @Override
  public OCLDelegatorVisitor getRealThis() {
    return realThis;
  }

  /**
   * set the last result of all calculators to the same object
   */
  public void setLastResult(TypeCheckResult lastResult) {
    deriveSymTypeOfExpression.setTypeCheckResult(lastResult);
    deriveSymTypeOfCommonExpressions.setTypeCheckResult(lastResult);
    //deriveSymTypeOfSetExpressions.setLastResult(lastResult);
    //deriveSymTypeOfOCLExpressions.setLastResult(lastResult);
    deriveSymTypeOfLiterals.setTypeCheckResult(lastResult);
    deriveSymTypeOfMCCommonLiterals.setTypeCheckResult(lastResult);
    deriveSymTypeOfOCL.setTypeCheckResult(lastResult);
  }

  /*
  @Override
  public LastResult getLastResult() {
    return lastResult;
  }

   */

  /**
   * set the scope of the typescalculator, important for resolving for e.g. NameExpression
   */

  public void setScope(IExpressionsBasisScope scope) {
    //rawSetScope(scope);
  }
  /*
  @Override
  public void rawSetScope(IExpressionsBasisScope scope) {
    //deriveSymTypeOfExpression.rawSetScope(scope);
    //getExpressionsBasisVisitorAsTypeVisitor().rawSetScope(scope);
    //deriveSymTypeOfCommonExpressions.rawSetScope(scope);
    //getCommonExpressionsVisitorAsTypeVisitor().rawSetScope(scope);
    //deriveSymTypeOfSetExpressions.rawSetScope(scope);
    //getSetExpressionsVisitorAsTypesCalculator().rawSetScope(scope);
    //deriveSymTypeOfOCLExpressions.rawSetScope(scope);
    //getOCLExpressionsVisitorAsTypeVisitor().rawSetScope(scope);
  }
   */

  /**
   * initialize the typescalculator
   */
  @Override
  public void init() {
    deriveSymTypeOfExpression = new DeriveSymTypeOfExpression();
    deriveSymTypeOfCommonExpressions = new DeriveSymTypeOfCommonExpressions();
    //deriveSymTypeOfSetExpressions = new DeriveSymTypeOfSetExpressions();
    //deriveSymTypeOfOCLExpressions = new DeriveSymTypeOfOCLExpressions();
    deriveSymTypeOfLiterals = new DeriveSymTypeOfLiterals();
    deriveSymTypeOfMCCommonLiterals = new DeriveSymTypeOfMCCommonLiterals();
    deriveSymTypeOfOCL = new DeriveSymTypeOfOCL();

    initializeTypeVisitor();

    setLastResult(lastResult);
  }

  protected void initializeTypeVisitor() {
    //final SynthesizeSymTypeFromMCCollectionTypes mCCollectionTypesVisitor = new SynthesizeSymTypeFromMCCollectionTypes();
    //setMCBasicTypesVisitor(mCCollectionTypesVisitor);
    //setMCCollectionTypesVisitor(mCCollectionTypesVisitor);
  }
}
