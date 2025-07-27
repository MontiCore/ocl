package de.monticore.types.mcbasictypes.refadaptation;

import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCReturnType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;

/**
 * Factory interface for creating instances of {@link ASTMCType} and {@link ASTMCReturnType}
 * from a given {@link SymTypeExpression}.<br>
 * <br>
 * Sometimes during reference artifact adaptation, we may not be able to adapt an existing
 * reference type for a concrete scenario, but have to create an AST node representing a type
 * from scratch. For example, when we adapt an OCL method signature, based on a concrete
 * method symbol, we may need to create return types and parameter types using just the information
 * from the method symbol (which is represented as a {@link SymTypeExpression}).
 * This is basically the reverse operation of the type check process.<br>
 * <br>
 * By extracting this factory interface, we stay open for extension, e.g. language developers
 * can add an implementation adding support for more types.
 */
public interface MCTypeFactory {

  /**
   * Creates an {@link ASTMCType} instance representing the type defined by given
   * {@link SymTypeExpression}.
   *
   * @param symTypeExpression the type expression to create the AST type from
   * @return an instance of {@link ASTMCType} representing the given type expression
   *
   * @implSpec This method is required to return an {@link ASTMCType} such that
   *   {@link de.monticore.types3.TypeCheck3#symTypeFromAST(ASTMCType)} returns a
   *   {@code SymTypeExpression} equal to the given parameter when compared using
   *   {@link SymTypeExpression#deepEquals(SymTypeExpression)}.
   */
  ASTMCType createMCType(SymTypeExpression symTypeExpression);

  /**
   * Creates an {@link ASTMCReturnType} instance representing the return type defined by
   * given {@link SymTypeExpression}.
   *
   * @param symTypeExpression the type expression to create the AST return type from
   * @return an instance of {@link ASTMCReturnType} representing the given type expression
   *
   * @implSpec This method is required to return an {@link ASTMCReturnType} such that
   *  {@link de.monticore.types3.TypeCheck3#symTypeFromAST(ASTMCReturnType)} returns a
   *  {@code SymTypeExpression} equal to the given parameter when compared using
   *  {@link SymTypeExpression#deepEquals(SymTypeExpression)}.
   */
  ASTMCReturnType createMCReturnType(SymTypeExpression symTypeExpression);
}
