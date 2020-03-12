/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._cocos;

public class OCLCoCos {

  public static OCLCoCoChecker createChecker() {
    return new OCLCoCoChecker()
        .addCoCo(new FileNameStartsWithLowerCaseLetter())
        .addCoCo(new MethSignatureStartsWithLowerCaseLetter())
        .addCoCo(new ConstructorNameStartsWithCapitalLetter())
        .addCoCo(new InvariantNameStartsWithCapitalLetter())
        .addCoCo(new MethodDeclarationStartsWithLowerCaseLetter())
        .addCoCo(new PreStatementNameStartsWithCapitalLetter())
        .addCoCo(new PostStatementNameStartsWithCapitalLetter())
        .addCoCo(new ParameterDeclarationNameStartsWithLowerCaseLetter())
        .addCoCo(new VariableDeclarationStartsWithLowerCaseLetter())
        .addCoCo(new ExpressionInOperationConstraintHasToBeLetInExpression())
        .addCoCo(new ExpressionInContextDefinitionHasToBeLetInExpression())
        ;
  }
}
