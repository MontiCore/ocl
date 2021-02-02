/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;

public class OCLCoCos {

  public static OCLCoCoChecker createChecker() {
    return createChecker(new DeriveSymTypeOfOCLCombineExpressions());
  }

  public static OCLCoCoChecker createChecker(DeriveSymTypeOfOCLCombineExpressions typeChecker) {
    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new MethSignatureStartsWithLowerCaseLetter());
    checker.addCoCo(new ConstructorNameStartsWithCapitalLetter());
    checker.addCoCo(new InvariantNameStartsWithCapitalLetter());
    checker.addCoCo(new ParameterNamesUnique());
    checker.addCoCo(new IterateExpressionVariableUsageIsCorrect());
    checker.addCoCo(new ConstructorNameReferencesType());
    checker.addCoCo(new ValidTypes(typeChecker));
    checker.addCoCo(new ExpressionHasNoSideEffect());
    checker.addCoCo(new ContextVariableNamesAreUnique());
    checker.addCoCo(new ContextHasOnlyOneType());
    checker.addCoCo(new SetComprehensionHasGenerator());
    checker.addCoCo(new UnnamedInvariantDoesNotHaveParameters());
    checker.addCoCo(new PreAndPostConditionsAreBooleanType(typeChecker));
    return checker;
  }
}
