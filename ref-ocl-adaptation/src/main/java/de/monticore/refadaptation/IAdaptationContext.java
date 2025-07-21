package de.monticore.refadaptation;

/**
 * An adaptation context represents the constraints under which a certain AST or subtree of an AST
 * is adapted.<br>
 * Language-specific subtypes of this interface can add access to bindings and incarnation mappings
 * of languages whose symbols are used in the adaptation process.
 */
public interface IAdaptationContext {

  // TODO add generic parameters C and V to have better type safety
  //  (might not be possible because languages can have multiple

  /**
   * Creates a new adaptation variant that has the same bindings as the current context.<br>
   *
   * This acts as a type-safe factory method for variants and ensures developers do not forget to
   * add the constraints to the variant under which it was created.
   */
  IAdaptationVariant createVariant();

  /**
   * Creates a new adaptation context that is a fork of the current one.
   * This means that the new context will inherit all bindings from the current context,
   * and reference the same original incarnation mappings.
   * Added bindings will not affect the original context.
   */
  IAdaptationContext fork();

  /**
   * Adds all bindings from the given adaptation variant to the current context.
   * This enables handlers to pass constraints DOWNWARDS during adaptation and restrict the
   * number of possible variants.
   *
   * @param variant the adaptation variant containing the bindings to be added
   */
  void addBindings(IAdaptationVariant variant) throws BindingConflictException;
}
