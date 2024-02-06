package de.monticore.ocl2smt.ocl2smt.expr2smt.typeFactorry;

import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeAdapter.TypeAdapter;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCType;

public interface TypeFactory<T> {
  TypeAdapter<T> mkBoolType();

  TypeAdapter<T> mkStringType();

  TypeAdapter<T> mkCharType();

  TypeAdapter<T> mkInType();

  TypeAdapter<T> mkDoubleType();

  TypeAdapter<T> mkSetType(T elemType);

  TypeAdapter<T> adapt(ASTCDType cdType);

  TypeAdapter<T> adapt(ASTMCType mcType);

  TypeAdapter<T> adapt(SymTypeExpression type);
}
