// (c) https://github.com/MontiCore/monticore

package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.oclexpressions._cocos.IterateExpressionVariableUsageIsCorrect;
import de.monticore.ocl.setexpressions._cocos.SetComprehensionHasGenerator;
import de.monticore.ocl.types.check.OCLTypeCalculator;

public class OCLCoCos {

  public static OCLCoCoChecker createChecker() {
    return createChecker(new OCLTypeCalculator());
  }

  public static OCLCoCoChecker createChecker(OCLTypeCalculator typeCalculator) {
    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new MethSignatureStartsWithLowerCaseLetter());
    checker.addCoCo(new ConstructorNameStartsWithCapitalLetter());
    checker.addCoCo(new InvariantNameStartsWithCapitalLetter());
    checker.addCoCo(new ParameterNamesUnique());
    checker.addCoCo(new IterateExpressionVariableUsageIsCorrect());
    checker.addCoCo(new ConstructorNameReferencesType());
    checker.addCoCo(new ExpressionValidCoCo(typeCalculator));
    checker.addCoCo(new ExpressionHasNoSideEffect());
    checker.addCoCo(new ContextVariableNamesAreUnique());
    checker.addCoCo(new ContextHasOnlyOneType());
    checker.addCoCo(new SetComprehensionHasGenerator());
    checker.addCoCo(new UnnamedInvariantDoesNotHaveParameters());
    checker.addCoCo(new PreAndPostConditionsAreBooleanType(typeCalculator));
    return checker;
  }
}
