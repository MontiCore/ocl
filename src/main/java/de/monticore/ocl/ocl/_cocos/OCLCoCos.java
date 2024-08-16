// (c) https://github.com/MontiCore/monticore

package de.monticore.ocl.ocl._cocos;

public class OCLCoCos {

  public static OCLCoCoChecker createChecker() {
    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new MethSignatureStartsWithLowerCaseLetter());
    checker.addCoCo(new ConstructorNameStartsWithCapitalLetter());
    checker.addCoCo(new InvariantNameStartsWithCapitalLetter());
    checker.addCoCo(new ParameterNamesUnique());
    checker.addCoCo(new ConstructorNameReferencesType());
    checker.addCoCo(new ExpressionValidCoCo());
    checker.addCoCo(new ExpressionHasNoSideEffect());
    checker.addCoCo(new ContextVariableNamesAreUnique());
    checker.addCoCo(new ContextHasOnlyOneType());
    checker.addCoCo(new UnnamedInvariantDoesNotHaveParameters());
    checker.addCoCo(new PreAndPostConditionsAreBooleanType());
    checker.addCoCo(new VariableDeclarationOfCorrectType());
    return checker;
  }
}
