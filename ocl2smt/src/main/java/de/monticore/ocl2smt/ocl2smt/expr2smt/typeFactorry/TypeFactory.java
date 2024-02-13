package de.monticore.ocl2smt.ocl2smt.expr2smt.typeFactorry;

import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.ocl2smt.ocl2smt.expr2smt.typeAdapter.TypeAdapter;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCType;

/***
 * this interface serve as factory for types.
 */
public interface TypeFactory {
  TypeAdapter mkBoolType();

  TypeAdapter mkStringType();

  TypeAdapter mkCharType();

  TypeAdapter mkInType();

  TypeAdapter mkDoubleType();

  TypeAdapter mkSetType(TypeAdapter elemType);

  TypeAdapter adapt(ASTCDType cdType);

  TypeAdapter adapt(ASTMCType mcType);

  TypeAdapter adapt(SymTypeExpression type);
}
