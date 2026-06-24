// (c) https://github.com/MontiCore/monticore

package de.monticore.ocl.ocl._ast;

import de.monticore.ocl.ocl._symboltable.OCLOperationConstraintData;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types3.TypeCheck3;
import de.monticore.types3.util.WithinScopeBasicSymbolsResolver;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Optional;

public class ASTOCLConstructorSignature extends ASTOCLConstructorSignatureTOP {
  
  @Override
  public OCLOperationConstraintData getOperationData() {
    List<SymTypeExpression> params =
        this.oCLParamDeclarations.stream().map(x -> TypeCheck3.symTypeFromAST(x.getMCType()))
            .toList();
    Optional<SymTypeExpression> returnType =
        WithinScopeBasicSymbolsResolver.resolveType(this.getEnclosingScope(), this.getName());
    String fqn = this.getName();
    if (returnType.isEmpty()) {
      Log.error("0x0C199 constructor name '" + this.getName()
          + "' has to reference a type, but could not be found.");
      return new OCLOperationConstraintData(SymTypeExpressionFactory.createObscureType(), fqn, params);
      
    }
    else {
      fqn = returnType.get().getTypeInfo().getFullName();
    }
    return new OCLOperationConstraintData(returnType.get(), fqn, params);
  }
}
