/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.types.check;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._symboltable.IExpressionsBasisScope;
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

  private LastResult lastResult = new LastResult();

  public LastResult getLastResult() {
    return lastResult;
  }

  public DeriveSymTypeOfOCLCombineExpressions(IExpressionsBasisScope scope) {
    this.realThis = this;

    initializeTypeVisitor();

    deriveSymTypeOfExpression = new DeriveSymTypeOfExpression();
    deriveSymTypeOfExpression.setLastResult(lastResult);
    setExpressionsBasisVisitor(deriveSymTypeOfExpression);

    deriveSymTypeOfCommonExpressions = new DeriveSymTypeOfCommonExpressions();
    deriveSymTypeOfCommonExpressions.setLastResult(lastResult);
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
    deriveSymTypeOfLiterals.setResult(lastResult);
    setMCLiteralsBasisVisitor(deriveSymTypeOfLiterals);

    deriveSymTypeOfMCCommonLiterals = new DeriveSymTypeOfMCCommonLiterals();
    deriveSymTypeOfMCCommonLiterals.setResult(lastResult);
    setMCCommonLiteralsVisitor(deriveSymTypeOfMCCommonLiterals);

    deriveSymTypeOfOCL = new DeriveSymTypeOfOCL();
    deriveSymTypeOfOCL.setLastResult(lastResult);
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
    result.ifPresent(r -> lastResult.setLast(r));
  }

  public Optional<SymTypeExpression> calculateType(ASTOCLCompilationUnit node) {
    node.accept(realThis);
    Optional<SymTypeExpression> result = Optional.empty();
    if (lastResult.isPresentLast()) {
      result = Optional.ofNullable(lastResult.getLast());
    }
    lastResult.setLastAbsent();
    return result;
  }

  /**
   * main method to calculate the type of an expression
   */
  public Optional<SymTypeExpression> calculateType(ASTExpression e) {
    e.accept(realThis);
    Optional<SymTypeExpression> result = Optional.empty();
    if (lastResult.isPresentLast()) {
      result = Optional.ofNullable(lastResult.getLast());
    }
    lastResult.setLastAbsent();
    return result;
  }

  public Optional<SymTypeExpression> calculateType(ASTOCLExpressionsNode e) {
    e.accept(realThis);
    Optional<SymTypeExpression> result = Optional.empty();
    if (lastResult.isPresentLast()) {
      result = Optional.ofNullable(lastResult.getLast());
    }
    lastResult.setLastAbsent();
    return result;
  }

  /**
   * main method to calculate the type of a literal
   */
  @Override
  public Optional<SymTypeExpression> calculateType(ASTLiteral lit) {
    lit.accept(realThis);
    Optional<SymTypeExpression> result = Optional.empty();
    if (lastResult.isPresentLast()) {
      result = Optional.ofNullable(lastResult.getLast());
    }
    lastResult.setLastAbsent();
    return result;
  }

  @Override
  public OCLDelegatorVisitor getRealThis() {
    return realThis;
  }

  /**
   * set the last result of all calculators to the same object
   */
  public void setLastResult(LastResult lastResult) {
    deriveSymTypeOfExpression.setLastResult(lastResult);
    deriveSymTypeOfCommonExpressions.setLastResult(lastResult);
    //deriveSymTypeOfSetExpressions.setLastResult(lastResult);
    //deriveSymTypeOfOCLExpressions.setLastResult(lastResult);
    deriveSymTypeOfLiterals.setResult(lastResult);
    deriveSymTypeOfMCCommonLiterals.setResult(lastResult);
    deriveSymTypeOfOCL.setLastResult(lastResult);
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
