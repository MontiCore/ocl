package de.monticore.refadaptation;

public interface IAdaptationContext {

  /** Acts as a type safe factory method for variants. */
  IAdaptationVariant createVariant();

  /**
   * Creates a new adaptation context that is a fork of the current one.
   * Added binding swill not affect the original context.
   */
  IAdaptationContext fork();

  /**
   * Adds bindings from the given adaptation variant to the current context.
   * This enables handlers to pass constraints DOWNWARDS during adaptation.
   *
   * @param variant the adaptation variant containing the bindings to be added
   */
  void addBindings(IAdaptationVariant variant);
}
