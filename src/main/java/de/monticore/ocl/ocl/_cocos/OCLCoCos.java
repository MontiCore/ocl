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
        .addCoCo(new PreStatementNameStartsWithCapitalLetter())
        .addCoCo(new PostStatementNameStartsWithCapitalLetter())
        .addCoCo(new ParameterNamesUnique())
        .addCoCo(new ExpressionInOperationConstraintHasToBeLetInExpression())
        .addCoCo(new ExpressionInContextDefinitionHasToBeLetInExpression())

        .addCoCo(new ConstructorNameReferencesType())
        .addCoCo(new ValidTypes(typeChecker))
        ;
  }
}
