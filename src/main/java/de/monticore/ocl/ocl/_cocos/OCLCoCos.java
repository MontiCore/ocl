/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;

public class OCLCoCos {

  public static OCLCoCoChecker createChecker(DeriveSymTypeOfOCLCombineExpressions typeChecker) {
    return new OCLCoCoChecker()
        .addCoCo(new FileNameStartsWithLowerCaseLetter())
        .addCoCo(new MethSignatureStartsWithLowerCaseLetter())
        .addCoCo(new ConstructorNameStartsWithCapitalLetter())
        .addCoCo(new InvariantNameStartsWithCapitalLetter())
        .addCoCo(new ParameterNamesUnique())
        .addCoCo(new IterateExpressionVariableUsageIsCorrect())
        .addCoCo(new ConstructorNameReferencesType())
        .addCoCo(new ValidTypes(typeChecker))
        .addCoCo(new ExpressionHasNoSideEffect())
        .addCoCo(new ContextVariableNamesAreUnique())
        .addCoCo(new ContextHasOnlyOneType())
        .addCoCo(new SetComprehensionHasGenerator())
        .addCoCo(new PreAndPostConditionsAreBooleanType(typeChecker))
        ;
  }
}
