package de.monticore.types.mcbasictypes.refadaptation;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.types.MCTypeFacade;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypePrimitive;
import de.monticore.types.mcbasictypes.MCBasicTypesMill;
import de.monticore.types.mcbasictypes._ast.ASTConstantsMCBasicTypes;
import de.monticore.types.mcbasictypes._ast.ASTMCReturnType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import org.apache.commons.lang3.NotImplementedException;

// TODO Discuss again this implementation vs. OCLMCTypeFactory (parser based)
public class MCBasicTypesTypeFactory implements MCTypeFactory {

  @Override
  public ASTMCType createMCType(SymTypeExpression symTypeExpression) {
    if (symTypeExpression.isPrimitive()) {
      return OCLMill.mCPrimitiveTypeBuilder()
              .setPrimitive(getPrimitiveConstant(symTypeExpression.asPrimitive()))
              .build();
    } else if (symTypeExpression.isObjectType()) {
      return MCTypeFacade.getInstance().createQualifiedType(symTypeExpression.printFullName());
    } else {
      throw new NotImplementedException("Unsupported type: " + symTypeExpression);
    }
  }

  @Override
  public ASTMCReturnType createMCReturnType(SymTypeExpression symTypeExpression) {
    if (symTypeExpression.isVoidType()) {
      return MCBasicTypesMill.mCReturnTypeBuilder()
              .setMCVoidType(MCTypeFacade.getInstance().createVoidType())
              .build();
    } else {
      return MCBasicTypesMill.mCReturnTypeBuilder()
              .setMCType(createMCType(symTypeExpression))
              .build();
    }
  }

  /*
   * NOTE: This links the names of primitive types in the 'BasicSymbols' language to the
   * hardcoded integer constants used in the 'MCBasicTypes' language.
   */
  private int getPrimitiveConstant(SymTypePrimitive primitive) {
    switch (primitive.getPrimitiveName()) {
      case BasicSymbolsMill.BOOLEAN:
        return ASTConstantsMCBasicTypes.BOOLEAN;
      case BasicSymbolsMill.INT:
        return ASTConstantsMCBasicTypes.INT;
      case BasicSymbolsMill.LONG:
        return ASTConstantsMCBasicTypes.LONG;
      case BasicSymbolsMill.FLOAT:
        return ASTConstantsMCBasicTypes.FLOAT;
      case BasicSymbolsMill.DOUBLE:
        return ASTConstantsMCBasicTypes.DOUBLE;
      default:
        throw new NotImplementedException("Primitive type not supported: " + primitive.getPrimitiveName());
    }
  }
}
