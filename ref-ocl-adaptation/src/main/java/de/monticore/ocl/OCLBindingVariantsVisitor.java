package de.monticore.ocl;

import de.monticore.ast.ASTNode;
import de.monticore.expressions.commonexpressions.CommonExpressionsAdaptationVariant;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.refadaptation.Binding;
import de.monticore.refadaptation.BindingConflictException;
import de.monticore.symbols.OOSymbolsBindings;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OCLBindingVariantsVisitor extends OCLBindingVariantsVisitorTOP {

  @Override
  public void traverse(ASTOCLInvariant refInvariant) {
    List<ASTNode> children = new ArrayList<>();
    children.addAll(refInvariant.getOCLContextDefinitionList());
    // TODO What are param declarations ?
    children.add(refInvariant.getExpression());
    traverseForConsistentVariants(refInvariant, children);
  }

  @Override
  public void traverse(ASTOCLContextDefinition node) {
    List<ASTNode> children = new ArrayList<>();
    if (node.isPresentMCType()) {
      children.add(node.getMCType());
    }
    if (node.isPresentGeneratorDeclaration()) {
      children.add(node.getGeneratorDeclaration());
    }
    if (node.isPresentOCLParamDeclaration()) {
      children.add(node.getOCLParamDeclaration());
    }
    traverseForConsistentVariants(node, children);
  }

  @Override
  public void traverse(ASTOCLOperationConstraint refConstraint) {
    List<ASTNode> children = new ArrayList<>();
    children.add(refConstraint.getOCLOperationSignature());
    children.addAll(refConstraint.getPreConditionList());
    children.addAll(refConstraint.getPostConditionList());
    // TODO Variable declaration list / ?? is this "let"
    traverseForConsistentVariants(refConstraint, children);
  }

  @Override
  public void traverse(ASTOCLParamDeclaration refParamDeclaration) {
    List<ASTNode> children = new ArrayList<>();
    children.add(refParamDeclaration.getMCType());
    if (refParamDeclaration.isPresentExpression()) {
      children.add(refParamDeclaration.getExpression());
    }
    traverseForConsistentVariants(refParamDeclaration, children);
  }

  @Override
  public void endVisit(ASTOCLMethodSignature refMethodSignature) {
    MethodSymbol refMethodSymbol = OCLAdaptationUtils.resolveMethodSymbol(getAdaptationContext()
            .getOriginalOOSymbolsIncMapping().getReferenceScope(), refMethodSignature);
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
        addVariantForMethodIncarnation(refMethodSignature, refMethodSymbol, methodIncarnation);
      }
    }
  }

  protected void addVariantForMethodIncarnation(
          ASTOCLMethodSignature refMethodSignature,
          MethodSymbol refMethodSymbol,
          MethodSymbol methodIncarnation) {
    CommonExpressionsAdaptationVariant newVariant = getAdaptationContext().createVariant();
    // 1. Add strict binding for the selected method
    // (Implicitly adds type bindings for declaring type, return type and parameter types)
    try {
      newVariant.getOOSymbolsBindings().addMethodBinding(Binding.createStrict(refMethodSymbol, methodIncarnation));
    } catch (BindingConflictException e) {
      // This is unexpected as the current adaptation context should only return incarnations
      // that are valid in the current context, i.e., no conflicts with existing bindings.
      Log.warn("getIncarnations returned incarnation that conflicts with existing binding: "
              + methodIncarnation.getFullName() + " in " + refMethodSignature.get_SourcePositionStart(), e);
      return;
    }
    // 2. Add bindings from the original model attached to the method
    OOSymbolsBindings bindingsFromModel = getAdaptationContext().getOriginalOOSymbolsIncMapping().getScopedBindings(methodIncarnation);
    try {
      newVariant.getOOSymbolsBindings().addAll(bindingsFromModel);
    } catch (BindingConflictException e) {
      // This is unexpected in context of OCL. There is no obvious reason why there could be
      // bindings on this level of the AST that conflict with bindings of the method...
      Log.warn("Ignoring incarnation due to binding conflict: "
              + methodIncarnation.getFullName() + " in " + refMethodSignature.get_SourcePositionStart(), e);
      return;
    }
    // 3. Manually, add bindings for the VariableSymbols representing the method parameters so they can be adapted later on
    // TODO These bindings should be available frm the OOSymbolsBinding in the future since
    //  parameters are naturally VariableSymbols enclosed in the scope of the method
    for (int i=0; i<refMethodSignature.getOCLParamDeclarationList().size(); i++) {
      // STRONG assumption: incarnation parameters are in same order as reference parameters
      VariableSymbol refSymbol = refMethodSignature.getOCLParamDeclaration(i).getSymbol();
      VariableSymbol conSymbol = methodIncarnation.getParameterList().get(i);
      try {
        newVariant.getBasicSymbolsBindings().addVariableBinding(Binding.createStrict(refSymbol, conSymbol));
      } catch (BindingConflictException e) {
        // This is unexpected in context of OCL. There is no obvious reason why there could be
        // bindings on this level of the AST that conflict with bindings of the method...
        Log.warn("Ignoring incarnation due to binding conflict caused by parameter VariableSymbol: "
                + methodIncarnation.getFullName() + " in " + refMethodSignature.get_SourcePositionStart(), e);
        return;
      }
    }
    getAdaptations4Ast().addVariant(refMethodSignature, newVariant);
  }

  @Override
  public void endVisit(ASTOCLArtifact refArtifact) {
    // Adds a SINGLE variant for the artifact combining all the adapted constraints
    aggregateChildVariants(refArtifact, refArtifact.getOCLConstraintList());
  }

  @Override
  public void endVisit(ASTOCLCompilationUnit node) {
    passChildVariantsUpwards(node, node.getOCLArtifact());
  }
}
