package de.monticore.ocl;

import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.oosymbols._symboltable.IOOSymbolsScope;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;
import de.monticore.symboltable.modifiers.AccessModifier;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

public class OCLAdaptationUtils {

  private OCLAdaptationUtils() {
  }

  public static MethodSymbol resolveMethodSymbol(IOOSymbolsScope scope, ASTOCLMethodSignature oclMethodSignature) {
    //  TODO we have to manually resolve the MethodSymbol here the method name here, check parametrs -> respect imports ??
    TypeSymbol returnTypeSymbol = TypeCheck3.symTypeFromAST(oclMethodSignature.getMCReturnType()).getTypeInfo();
    // TODO get type symbols of parameters

    // TODO check imports and qualify name if necessary
    String methodName = oclMethodSignature.getMethodName().getQName();
    return scope.resolveMethodDown(methodName, AccessModifier.ALL_INCLUSION, symbol -> {
            if (!symbol.getType().getTypeInfo().getFullName().equals(returnTypeSymbol.getFullName())) {
              return false;
            }
            // TODO check parameter types (or name of we ignore param types. see CDCOnfParameter)
            return true;
          }).orElseGet(() -> {
            Log.error("0xA1235 Could not resolve method symbol for " + methodName + " in scope "
                    + scope.getName());
            return null;
          });
  }
}
