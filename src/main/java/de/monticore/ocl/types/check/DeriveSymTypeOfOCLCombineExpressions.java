/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.types.check;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.literals.mccommonliterals._ast.ASTSignedLiteral;
import de.monticore.literals.mcliteralsbasis._ast.ASTLiteral;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.oclexpressions._ast.ASTOCLExpressionsNode;
import de.monticore.types.check.*;
import de.monticore.types.mcbasictypes._ast.ASTMCType;

import java.util.Optional;

/**
 * Delegator Visitor to test the combination of the grammars
 */
public class DeriveSymTypeOfOCLCombineExpressions
  implements IDerive {

  protected OCLTraverser traverser;

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
    this.typeCheckResult = new TypeCheckResult();
    init();
  }

  @Override
  public Optional<SymTypeExpression> getResult() {
    if(typeCheckResult.isPresentCurrentResult()){
      return Optional.ofNullable(typeCheckResult.getCurrentResult());
    }
    return Optional.empty();
  }

  /**
   * initialize the typescalculator
   */
  @Override
  public void init() {
    traverser = OCLMill.traverser();

    //initializes visitors used for typechecking
    deriveSymTypeOfExpression = new DeriveSymTypeOfExpression();
    deriveSymTypeOfExpression.setTypeCheckResult(typeCheckResult);
    traverser.add4ExpressionsBasis(deriveSymTypeOfExpression);
    traverser.setExpressionsBasisHandler(deriveSymTypeOfExpression);

    deriveSymTypeOfCommonExpressions = new DeriveSymTypeOfCommonExpressions();
    deriveSymTypeOfCommonExpressions.setTypeCheckResult(typeCheckResult);
    traverser.add4CommonExpressions(deriveSymTypeOfCommonExpressions);
    traverser.setCommonExpressionsHandler(deriveSymTypeOfCommonExpressions);

    deriveSymTypeOfSetExpressions = new DeriveSymTypeOfSetExpressions();
    deriveSymTypeOfSetExpressions.setTypeCheckResult(typeCheckResult);
    traverser.add4SetExpressions(deriveSymTypeOfSetExpressions);
    traverser.setSetExpressionsHandler(deriveSymTypeOfSetExpressions);

    deriveSymTypeOfOCLExpressions = new DeriveSymTypeOfOCLExpressions();
    deriveSymTypeOfOCLExpressions.setTypeCheckResult(typeCheckResult);
    traverser.setOCLExpressionsHandler(deriveSymTypeOfOCLExpressions);

    deriveSymTypeOfLiterals = new DeriveSymTypeOfLiterals();
    deriveSymTypeOfLiterals.setTypeCheckResult(typeCheckResult);
    traverser.add4MCLiteralsBasis(deriveSymTypeOfLiterals);

    deriveSymTypeOfMCCommonLiterals = new DeriveSymTypeOfMCCommonLiterals();
    deriveSymTypeOfMCCommonLiterals.setTypeCheckResult(typeCheckResult);
    traverser.add4MCCommonLiterals(deriveSymTypeOfMCCommonLiterals);

    deriveSymTypeOfOCL = new DeriveSymTypeOfOCL();
    deriveSymTypeOfOCL.setTypeCheckResult(typeCheckResult);
    traverser.setOCLHandler(deriveSymTypeOfOCL);

    deriveSymTypeOfOptionalOperators = new DeriveSymTypeOfOptionalOperators();
    deriveSymTypeOfOptionalOperators.setTypeCheckResult(typeCheckResult);
    traverser.setOptionalOperatorsHandler(deriveSymTypeOfOptionalOperators);

    synthesizeSymTypeFromMCSimpleGenericTypes = new SynthesizeSymTypeFromMCSimpleGenericTypes();
    synthesizeSymTypeFromMCSimpleGenericTypes.setTypeCheckResult(typeCheckResult);
    traverser.add4MCSimpleGenericTypes(synthesizeSymTypeFromMCSimpleGenericTypes);
    traverser.setMCSimpleGenericTypesHandler(synthesizeSymTypeFromMCSimpleGenericTypes);

    synthesizeSymTypeFromMCBasicTypes = new SynthesizeSymTypeFromMCBasicTypes();
    synthesizeSymTypeFromMCBasicTypes.setTypeCheckResult(typeCheckResult);
    traverser.add4MCBasicTypes(synthesizeSymTypeFromMCBasicTypes);
    traverser.setMCBasicTypesHandler(synthesizeSymTypeFromMCBasicTypes);

    synthesizeSymTypeFromMCCollectionTypes = new SynthesizeSymTypeFromMCCollectionTypes();
    synthesizeSymTypeFromMCCollectionTypes.setTypeCheckResult(typeCheckResult);
    traverser.add4MCCollectionTypes(synthesizeSymTypeFromMCCollectionTypes);
    traverser.setMCCollectionTypesHandler(synthesizeSymTypeFromMCCollectionTypes);
  }

  public TypeCheckResult getTypeCheckResult() {
    return typeCheckResult;
  }

  public void setTypeCheckResult(TypeCheckResult typeCheckResult) {
    this.typeCheckResult = typeCheckResult;
  }

  @Override public OCLTraverser getTraverser() {
    return traverser;
  }

  public void setTraverser(OCLTraverser traverser) {
    this.traverser = traverser;
  }
}
