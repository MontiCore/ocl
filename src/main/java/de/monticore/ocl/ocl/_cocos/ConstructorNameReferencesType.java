/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._cocos;

import de.monticore.ocl.ocl._ast.ASTOCLConstructorSignature;
import de.monticore.types.typesymbols._symboltable.TypeSymbol;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;

public class ConstructorNameReferencesType
    implements OCLASTOCLConstructorSignatureCoCo {

  @Override
  public void check(ASTOCLConstructorSignature astConstructorSig) {
    final Optional<TypeSymbol> typeSymbol = astConstructorSig.getEnclosingScope().resolveType(astConstructorSig.getReferenceType());
    if (!typeSymbol.isPresent()) {
      Log.warn(String.format("0xOCL0D constructor name '%s' after keyword 'new' has to reference a type, but could not be found.", astConstructorSig.getReferenceType()),
          astConstructorSig.get_SourcePositionStart());
    }
  }
}
