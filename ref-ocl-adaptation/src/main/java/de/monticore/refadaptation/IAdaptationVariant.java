package de.monticore.refadaptation;

import com.google.common.collect.ListMultimap;
import de.monticore.ast.ASTNode;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IAdaptationVariant {

  <T extends ASTNode> Optional<T> getAdaptedNode(T refNode);

  void setAdaptedNode(ASTNode refNode, ASTNode adaptedNode);

  Map<ASTNode, ASTNode> getAdaptedNodes();

  void addAdaptedNodes(Map<ASTNode, ASTNode> adaptedNodes);

  /**
   * Creates a copy of this adaptation variant.<br>
   * This is necessary to enable visitors of composed languages to create new variants without
   * knowing the actual implementation (which is defined by the top level language)
   *
   * @return a new instance of IAdaptationVariant that is a copy of this one.
   */
  IAdaptationVariant copy();

  // TODO Throw exception in case bindings or child variants conflict
  IAdaptationVariant merge(IAdaptationVariant otherVariant) throws BindingConflictException;

  /**
   * Adds a child variant for the given reference node.<br>
   *
   * @param refNode
   * @param childVariant
   */
  void addChildVariant(ASTNode refNode, IAdaptationVariant childVariant);
  void addChildVariants(ASTNode refNode, Collection<? extends IAdaptationVariant> childVariants);

  void addAllChildVariants(IAdaptationVariant variant);

  <T extends IAdaptationVariant> List<T> getChildVariants(ASTNode refNode);

  ListMultimap<ASTNode, IAdaptationVariant> getAllChildVariants();
}
