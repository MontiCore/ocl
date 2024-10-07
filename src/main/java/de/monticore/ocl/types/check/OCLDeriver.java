/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.types.check;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.types.check.*;

/**
 * @deprecated This class is no longer acceptable since we use <b>Type Check 3</b> to calculate the
 *     type of expressions and literals related to OCL. Use {@link OCLTraverserProvider} instead.
 */
@Deprecated
public class OCLDeriver extends AbstractDerive {

  public OCLDeriver() {
    this(OCLMill.inheritanceTraverser());
  }

  public OCLDeriver(OCLTraverser traverser) {
    super(traverser);
    init(traverser);
  }

  public void init(OCLTraverser traverser) {
    // initializes visitors used for calculating types
    DeriveSymTypeOfExpression deriveSymTypeOfExpression = new DeriveSymTypeOfExpression();
    deriveSymTypeOfExpression.setTypeCheckResult(typeCheckResult);
    traverser.add4ExpressionsBasis(deriveSymTypeOfExpression);
    traverser.setExpressionsBasisHandler(deriveSymTypeOfExpression);

    DeriveSymTypeOfCommonExpressions deriveSymTypeOfCommonExpressions =
        new DeriveSymTypeOfCommonExpressions();
    deriveSymTypeOfCommonExpressions.setTypeCheckResult(typeCheckResult);
    traverser.add4CommonExpressions(deriveSymTypeOfCommonExpressions);
    traverser.setCommonExpressionsHandler(deriveSymTypeOfCommonExpressions);

    DeriveSymTypeOfSetExpressions deriveSymTypeOfSetExpressions =
        new DeriveSymTypeOfSetExpressions();
    deriveSymTypeOfSetExpressions.setTypeCheckResult(typeCheckResult);
    traverser.add4SetExpressions(deriveSymTypeOfSetExpressions);
    traverser.setSetExpressionsHandler(deriveSymTypeOfSetExpressions);

    DeriveSymTypeOfOCLExpressions deriveSymTypeOfOCLExpressions =
        new DeriveSymTypeOfOCLExpressions();
    deriveSymTypeOfOCLExpressions.setTypeCheckResult(typeCheckResult);
    traverser.setOCLExpressionsHandler(deriveSymTypeOfOCLExpressions);

    DeriveSymTypeOfLiterals deriveSymTypeOfLiterals = new DeriveSymTypeOfLiterals();
    deriveSymTypeOfLiterals.setTypeCheckResult(typeCheckResult);
    traverser.add4MCLiteralsBasis(deriveSymTypeOfLiterals);

    DeriveSymTypeOfMCCommonLiterals deriveSymTypeOfMCCommonLiterals =
        new DeriveSymTypeOfMCCommonLiterals();
    deriveSymTypeOfMCCommonLiterals.setTypeCheckResult(typeCheckResult);
    traverser.add4MCCommonLiterals(deriveSymTypeOfMCCommonLiterals);

    DeriveSymTypeOfOCL deriveSymTypeOfOCL = new DeriveSymTypeOfOCL();
    deriveSymTypeOfOCL.setTypeCheckResult(typeCheckResult);
    traverser.setOCLHandler(deriveSymTypeOfOCL);

    DeriveSymTypeOfOptionalOperators deriveSymTypeOfOptionalOperators =
        new DeriveSymTypeOfOptionalOperators();
    deriveSymTypeOfOptionalOperators.setTypeCheckResult(typeCheckResult);
    traverser.setOptionalOperatorsHandler(deriveSymTypeOfOptionalOperators);

    SynthesizeSymTypeFromMCSimpleGenericTypes synthesizeSymTypeFromMCSimpleGenericTypes =
        new SynthesizeSymTypeFromMCSimpleGenericTypes();
    synthesizeSymTypeFromMCSimpleGenericTypes.setTypeCheckResult(typeCheckResult);
    traverser.add4MCSimpleGenericTypes(synthesizeSymTypeFromMCSimpleGenericTypes);
    traverser.setMCSimpleGenericTypesHandler(synthesizeSymTypeFromMCSimpleGenericTypes);

    SynthesizeSymTypeFromMCBasicTypes synthesizeSymTypeFromMCBasicTypes =
        new SynthesizeSymTypeFromMCBasicTypes();
    synthesizeSymTypeFromMCBasicTypes.setTypeCheckResult(typeCheckResult);
    traverser.add4MCBasicTypes(synthesizeSymTypeFromMCBasicTypes);
    traverser.setMCBasicTypesHandler(synthesizeSymTypeFromMCBasicTypes);

    SynthesizeSymTypeFromMCCollectionTypes synthesizeSymTypeFromMCCollectionTypes =
        new SynthesizeSymTypeFromMCCollectionTypes();
    synthesizeSymTypeFromMCCollectionTypes.setTypeCheckResult(typeCheckResult);
    traverser.add4MCCollectionTypes(synthesizeSymTypeFromMCCollectionTypes);
    traverser.setMCCollectionTypesHandler(synthesizeSymTypeFromMCCollectionTypes);
  }
}
