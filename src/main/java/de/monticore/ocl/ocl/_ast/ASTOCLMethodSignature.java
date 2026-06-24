// (c) https://github.com/MontiCore/monticore

package de.monticore.ocl.ocl._ast;

import de.monticore.ocl.ocl._symboltable.OCLOperationConstraintData;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.TypeCheck3;
import de.monticore.types3.util.WithinScopeBasicSymbolsResolver;
import de.se_rwth.commons.Names;

import java.util.List;
import java.util.Optional;

public class ASTOCLMethodSignature extends ASTOCLMethodSignatureTOP {
  
  @Override
  public OCLOperationConstraintData getOperationData() {
    List<SymTypeExpression> params =
        this.oCLParamDeclarations.stream().map(x -> TypeCheck3.symTypeFromAST(x.getMCType()))
            .toList();
    SymTypeExpression returnType = TypeCheck3.symTypeFromAST(this.getMCReturnType());
    
    String qualifier = Names.getQualifier(this.methodName.getQName());
    String methodName = Names.getSimpleName(this.methodName.getQName());
    Optional<SymTypeExpression> resolvedClass =
        WithinScopeBasicSymbolsResolver.resolveType(this.getEnclosingScope(), qualifier);
    if (resolvedClass.isPresent()) {
      String fqn = resolvedClass.get().getTypeInfo().getFullName() + "." + methodName;
      return new OCLOperationConstraintData(returnType, fqn, params);
    }
    return new OCLOperationConstraintData(returnType, this.getMethodName().getQName(), params);
  }
}
