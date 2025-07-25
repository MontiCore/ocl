package de.monticore.refadaptation;

import com.google.common.base.Preconditions;
import de.se_rwth.commons.logging.Log;

/**
 * Holds the adaptation context for the adaptation currently running.
 * This class is used to access adn change the adaptation context
 * from within the adaptation visitor methods.
 * This is required because visitors are independent of each other and need to switch the context
 * sometimes during traversal to enforce additional constraints on other elements.
 * For example, an equals expression may enforce that the left and right hand side
 * are adapted with the same bindings.
 */
public class AdaptationContextHolder {

  private IAdaptationContext context;

  public AdaptationContextHolder() {
  }

  public AdaptationContextHolder(IAdaptationContext context) {
    setContext(context);
  }

  public void setContext(IAdaptationContext context) {
    Preconditions.checkNotNull(context);
    this.context = context;
  }

  public IAdaptationContext getContext() {
    if (context == null) {
      Log.error("0xFD335 internal error: adaptationContext not set."
              + " Make sure a context is set before using the traverser."
      );
    }
    return context;
  }
}
