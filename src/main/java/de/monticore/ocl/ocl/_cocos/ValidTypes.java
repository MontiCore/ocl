// (c) https://github.com/MontiCore/monticore

package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;

public class ValidTypes
    implements OCLASTOCLCompilationUnitCoCo {

  private DeriveSymTypeOfOCLCombineExpressions typeVisitor;

  public ValidTypes(DeriveSymTypeOfOCLCombineExpressions derLit) {
    typeVisitor = derLit;
  }

  @Override
  public void check(ASTOCLCompilationUnit node) {
    typeVisitor.calculateType(node);
  }
}
