package de.monticore.ocl;

import com.google.common.collect.ListMultimap;
import de.monticore.ast.ASTNode;
import de.monticore.refadaptation.AbstractAdaptationVariant;
import de.monticore.refadaptation.BindingConflictException;
import de.monticore.refadaptation.IASTAdaptation;
import de.monticore.refadaptation.IAdaptationVariant;
import de.monticore.symbols.IOOSymbolsBindings;
import de.monticore.symbols.basicsymbols.IBasicSymbolsBindings;

import java.util.Map;

public class OCLAdaptationVariant extends AbstractAdaptationVariant implements IOCLAdaptationVariant {

  protected final IOOSymbolsBindings ooSymbolsBindings;

  /**
   * Constructor for creating an OCLAdaptationVariant with a copy of the given
   * OOSymbolsBindings.
   *
   * @param ooSymbolsBindings the OOSymbolsBindings to initialize this variant with.
   */
  public OCLAdaptationVariant(IOOSymbolsBindings ooSymbolsBindings) {
    super();
    this.ooSymbolsBindings = ooSymbolsBindings.copy();
  }

  /**
   * Constructor for creating an OCLAdaptationVariant with the given internal information. All
   * parameters are copied to ensure immutability.
   *
   * @param ooSymbolsBindings
   * @param adaptedNodes
   * @param astAdaptations
   * @param childVariants
   */
  protected OCLAdaptationVariant(
          IOOSymbolsBindings ooSymbolsBindings,
          Map<ASTNode, ASTNode> adaptedNodes,
          ListMultimap<ASTNode, IASTAdaptation<? extends ASTNode>> astAdaptations,
          ListMultimap<ASTNode, IAdaptationVariant> childVariants) {
    super(adaptedNodes, astAdaptations, childVariants);
    this.ooSymbolsBindings = ooSymbolsBindings.copy();
  }

  @Override
  public IOCLAdaptationVariant copy() {
    return new OCLAdaptationVariant(
            ooSymbolsBindings,
            adaptedNodes,
            astAdaptations,
            childVariants);
  }

  @Override
  public IAdaptationVariant merge(IAdaptationVariant otherVariant) throws BindingConflictException {
    if (!(otherVariant instanceof IOCLAdaptationVariant)) {
      throw new IllegalArgumentException("Cannot merge with " + otherVariant.getClass().getSimpleName() +
              ". Expected an instance of OCLAdaptationContext.");
    }
    IOCLAdaptationVariant otherOCLVariant = (IOCLAdaptationVariant) otherVariant;

    if (isConflicting(otherOCLVariant)) {
      // TODO dedicated exception? -> conflict is not only a binding conflict
      throw new BindingConflictException();
    }

    IOCLAdaptationVariant merged = copy();
    merged.addAllChildVariants(otherVariant); // TODO check for conflicts!
    merged.addAdaptedNodes(otherVariant.getAdaptedNodes());
    merged.addAllASTAdaptations(otherVariant);
    // TODO remove? since OOSymbols extends BasicSymbols the basic symbols are added by OOSymbolBindings as well
    //merged.getBasicSymbolsBindings().addAll(otherOCLVariant.getBasicSymbolsBindings());
    merged.getOOSymbolsBindings().addAll(otherOCLVariant.getOOSymbolsBindings());
    return merged;
  }

  @Override
  public boolean isConflicting(IAdaptationVariant otherVariant) {
    // TODO improve generic types so we know it is an OCLAdaptationVariant we merge with!
    if (!(otherVariant instanceof IOCLAdaptationVariant)) {
      throw new IllegalArgumentException("Cannot merge with " + otherVariant.getClass().getSimpleName() +
              ". Expected an instance of OCLAdaptationContext.");
    }
    return getOOSymbolsBindings().isConflicting(((IOCLAdaptationVariant) otherVariant).getOOSymbolsBindings());
  }

  @Override
  public IBasicSymbolsBindings getBasicSymbolsBindings() {
    return ooSymbolsBindings;
  }

  @Override
  public IOOSymbolsBindings getOOSymbolsBindings() {
    return ooSymbolsBindings;
  }
}
