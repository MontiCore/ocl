package de.monticore.ocl;

import com.google.common.collect.ListMultimap;
import de.monticore.ast.ASTNode;
import de.monticore.refadapt.AbstractAdaptationVariant;
import de.monticore.refmodel.BindingConflictException;
import de.monticore.refadapt.IASTAdaptation;
import de.monticore.refadapt.IAdaptationVariant;
import de.monticore.symbols.oosymbols.refmodel.IOOSymbolsBindings;
import de.monticore.symbols.basicsymbols.refmodel.IBasicSymbolsBindings;

import java.util.Map;
import java.util.Set;

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
          Set<ASTNode> coveredRefNodes,
          Map<ASTNode, ASTNode> adaptedNodes,
          ListMultimap<ASTNode, IASTAdaptation<? extends ASTNode>> astAdaptations,
          ListMultimap<ASTNode, IAdaptationVariant> childVariants) {
    super(coveredRefNodes, adaptedNodes, astAdaptations, childVariants);
    this.ooSymbolsBindings = ooSymbolsBindings.copy();
  }

  @Override
  public OCLAdaptationVariant copy() {
    return new OCLAdaptationVariant(
            ooSymbolsBindings,
            coveredRefNodes,
            adaptedNodes,
            astAdaptations,
            childVariants);
  }

  @Override
  public OCLAdaptationVariant merge(IAdaptationVariant otherVariant) throws BindingConflictException {
    // 1. ensure type is correct
    // TODO improve generic types so we know it is an OCLAdaptationVariant we merge with!
    if (!(otherVariant instanceof IOCLAdaptationVariant)) {
      throw new IllegalArgumentException("Cannot merge with " + otherVariant.getClass().getSimpleName() +
              ". Expected an instance of OCLAdaptationContext.");
    }
    IOCLAdaptationVariant otherOCLVariant = (IOCLAdaptationVariant) otherVariant;

    // 2. check general merge conditions
    checkMergeConflicts(otherVariant);
    // 3. check binding conflicts
    if (hasConflictingBindings(otherOCLVariant)) {
      throw new BindingConflictException();
    }

    OCLAdaptationVariant merged = copy();
    merged.addAllCoveredRefNodes(otherOCLVariant.getCoveredRefNodes());
    merged.addAllChildVariants(otherVariant);
    merged.addAdaptedNodes(otherVariant.getAdaptedNodes());
    merged.addAllASTAdaptations(otherVariant);
    // since OOSymbols extends BasicSymbols the basic symbols are added by OOSymbolBindings as well
    merged.getOOSymbolsBindings().addAll(otherOCLVariant.getOOSymbolsBindings());
    return merged;
  }

  /**
   * Checks whether this variant has conflicting bindings with the given other variant.
   *
   * @param otherVariant the other variant to check for conflicting bindings
   * @return true if there are conflicting bindings, false otherwise
   */
  protected boolean hasConflictingBindings(IOCLAdaptationVariant otherVariant) {
    // NOTE: if the language would use symbols form other languages these bindings would need
    // to be checked as well
    return getOOSymbolsBindings().isConflicting(otherVariant.getOOSymbolsBindings());
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
