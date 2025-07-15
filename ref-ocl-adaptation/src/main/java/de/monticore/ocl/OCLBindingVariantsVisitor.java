package de.monticore.ocl;

import de.monticore.ast.ASTNode;
import de.monticore.expressions.commonexpressions.CommonExpressionsAdaptationVariant;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl.ocl._visitor.OCLHandler;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.ocl._visitor.OCLVisitor2;
import de.monticore.refadaptation.AbstractAdaptationHandler;
import de.monticore.refadaptation.Binding;
import de.monticore.symbols.OOSymbolsBindings;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OCLBindingVariantsVisitor
        extends AbstractAdaptationHandler<OCLAdaptationContext, OCLAdaptationVariant>
        implements OCLVisitor2, OCLHandler {

  private OCLTraverser traverser;

  @Override
  public OCLTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(OCLTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void handle(ASTOCLCompilationUnit node) {
    getAdaptations4Ast().clearVariants(node);
    OCLHandler.super.handle(node);
  }

  @Override
  public void endVisit(ASTOCLCompilationUnit node) {
    passChildVariantsUpwards(node, node.getOCLArtifact());
  }

  @Override
  public void handle(ASTOCLArtifact node) {
    getAdaptations4Ast().clearVariants(node);
    OCLHandler.super.handle(node);
  }

  @Override
  public void endVisit(ASTOCLArtifact refArtifact) {
    // Adds a SINGLE variant for the artifact combining all the adapted constraints
    aggregateChildVariants(refArtifact, refArtifact.getOCLConstraintList());
  }

  @Override
  public void handle(ASTOCLMethodSignature node) {
    getAdaptations4Ast().clearVariants(node);
    OCLHandler.super.handle(node);
  }

  @Override
  public void traverse(ASTOCLInvariant refInvariant) {
    List<ASTNode> nodesForConstraintPropagation = new ArrayList<>();
    nodesForConstraintPropagation.addAll(refInvariant.getOCLContextDefinitionList());
    // TODO What are param declarations ?
    nodesForConstraintPropagation.add(refInvariant.getExpression());
    List<OCLAdaptationVariant> variants = traverseAndPropagateConstraints(nodesForConstraintPropagation);
    getAdaptations4Ast().addVariants(refInvariant, variants);
  }

  @Override
  public void traverse(ASTOCLOperationConstraint refConstraint) {
    List<ASTNode> nodesForConstraintPropagation = new ArrayList<>();
    nodesForConstraintPropagation.add(refConstraint.getOCLOperationSignature());
    nodesForConstraintPropagation.addAll(refConstraint.getPreConditionList());
    nodesForConstraintPropagation.addAll(refConstraint.getPostConditionList());
    // TODO Variable declaration list / ?? is this "let"
    List<OCLAdaptationVariant> variants = traverseAndPropagateConstraints(nodesForConstraintPropagation);
    getAdaptations4Ast().addVariants(refConstraint, variants);
  }

  @Override
  public void endVisit(ASTOCLMethodSignature refMethodSignature) {
    MethodSymbol refMethodSymbol = OCLAdaptationUtils.resolveMethodSymbol(getAdaptationContext(), refMethodSignature);
    System.out.println("Method Symbol: " + refMethodSymbol);
    System.out.println("symbol full name: " + refMethodSymbol.getFullName());
    Set<MethodSymbol> incarnations = getAdaptationContext().getOOSymbolsIncMapping().getIncarnations(refMethodSymbol);
    if (incarnations.isEmpty()) {
      // no field symbol, use the constraints from the parent expression
      // TODO Should we handle this as an error? the reference method could be optional and really have no incarnation -> so just ignore the constraint?
      // TODO I think returning no variant at all is the correct approach here
      getAdaptations4Ast().addVariant(refMethodSignature, getAdaptationContext().createVariant());
    } else {
      // we have the incarnations which are possible in this context
      for (MethodSymbol methodIncarnation : incarnations) {
        CommonExpressionsAdaptationVariant newVariant = getAdaptationContext().createVariant();
        // 1. Add strict binding for the selected method
        // (Implicitly adds type bindings for declaring type, return type and parameter types)
        newVariant.getOOSymbolsBindings().addMethodBinding(Binding.createStrict(refMethodSymbol, methodIncarnation));
        // 2. Add bindings from the original model attached to the method
        OOSymbolsBindings bindingsFromModel = getAdaptationContext().getOriginalOOSymbolsIncMapping().getScopedBindings(methodIncarnation);
        newVariant.getOOSymbolsBindings().addAll(bindingsFromModel);
        getAdaptations4Ast().addVariant(refMethodSignature, newVariant);
      }
    }
  }
}
