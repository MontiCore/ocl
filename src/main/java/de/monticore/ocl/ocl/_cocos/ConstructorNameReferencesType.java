// (c) https://github.com/MontiCore/monticore

package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.ocl._ast.ASTOCLConstructorSignature;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;

public class ConstructorNameReferencesType
    implements OCLASTOCLConstructorSignatureCoCo {

  @Override
  public void check(ASTOCLConstructorSignature astConstructorSig) {
    final Optional<TypeSymbol> typeSymbol = astConstructorSig.getEnclosingScope().resolveType(astConstructorSig.getName());
    if (!typeSymbol.isPresent()) {
      Log.error(String.format("0xOCL0D constructor name '%s' after keyword 'new' has to reference a type, but could not be found.", astConstructorSig.getName()),
          astConstructorSig.get_SourcePositionStart());
    }
  }
}
