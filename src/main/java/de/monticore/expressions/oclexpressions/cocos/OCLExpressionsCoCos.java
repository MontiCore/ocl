/*
 (c) https://github.com/MontiCore/monticore
 */

package de.monticore.expressions.oclexpressions.cocos;

import de.monticore.expressions.oclexpressions._cocos.OCLExpressionsCoCoChecker;

public class OCLExpressionsCoCos {
  public static OCLExpressionsCoCoChecker getCheckerForAllCoCos() {
    OCLExpressionsCoCoChecker checker = new OCLExpressionsCoCoChecker();

    addExpressionTypeCoCos(checker);
    addTypeCheckCoCos(checker);
    addOptionalCoCos(checker);

    return checker;
  }

  private static void addTypeCheckCoCos(OCLExpressionsCoCoChecker checker) {
    checker.addCoCo(new OCLTypeIfConditionType());
  }

  private static void addExpressionTypeCoCos(OCLExpressionsCoCoChecker checker) {
    checker.addCoCo(new OCLInExpressionExpressionIsNotPrimitive());
  }

  private static void addOptionalCoCos(OCLExpressionsCoCoChecker checker) {
    checker.addCoCo(new OCLSimpleVariableDeclarationExtTypeOrValueHasToBeSet());
  }
}
