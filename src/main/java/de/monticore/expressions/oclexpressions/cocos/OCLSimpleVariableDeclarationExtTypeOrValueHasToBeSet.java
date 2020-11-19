/*
 (c) https://github.com/MontiCore/monticore
 */

package de.monticore.expressions.oclexpressions.cocos;

import de.monticore.expressions.oclexpressions._ast.ASTOCLVariableDeclaration;
import de.monticore.expressions.oclexpressions._cocos.OCLExpressionsASTOCLVariableDeclarationCoCo;
import de.se_rwth.commons.logging.Log;

public class OCLSimpleVariableDeclarationExtTypeOrValueHasToBeSet
    implements OCLExpressionsASTOCLVariableDeclarationCoCo {
  @Override
  public void check(ASTOCLVariableDeclaration node) {
    if (!(node.isPresentOCLExtType() || node.isPresentValue())) {
      Log.error("0xA3700 At least one of the ExtType or the value of the VariableDeclartion has to be set");
    }
  }
}
