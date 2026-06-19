package de.monticore.ocl.codegen.javagen;

import de.monticore.codegen.javagen.SymTypeExpression2JavaConverter;
import de.monticore.ocl.codegen.DomainTypeUtil;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;

import java.util.ArrayList;
import java.util.List;

public class OCLSymTypeExpression2JavaConverter extends SymTypeExpression2JavaConverter {

  public static void init(){
    delegate = new OCLSymTypeExpression2JavaConverter();
  }

  @Override
  protected SymTypeExpression _getAsJavaType(SymTypeExpression modelType) {
    SymTypeExpression tmp = super._getAsJavaType(modelType);
    if(DomainTypeUtil.getInstance().isDomainType(tmp)){
      if(tmp.isObjectType()){
        TypeSymbol ti = tmp.getTypeInfo();
        String packagePrefix = "";
        String fn = ti.getFullName();
        if(fn.contains(".")){
          int lastPoint = fn.lastIndexOf('.');
          packagePrefix = fn.substring(0, lastPoint).toLowerCase() + ".";
        }

        tmp = SymTypeExpressionFactory.createTypeObject(new TypeSymbol(
            packagePrefix + ti.getName()
        ));
      } else if(tmp.isGenericType()){
        List<SymTypeExpression> newArgs = new ArrayList<>();
        // Recursively convert all generic arguments (e.g. the T in List<T>)
        for(SymTypeExpression arg : tmp.asGenericType().getArgumentList()){
          newArgs.add(_getAsJavaType(arg));
        }
        // Reconstruct the generic type with the newly converted arguments
        tmp = SymTypeExpressionFactory.createGenerics(tmp.getTypeInfo(), newArgs);
      }
    }

    return tmp;
  }
}
