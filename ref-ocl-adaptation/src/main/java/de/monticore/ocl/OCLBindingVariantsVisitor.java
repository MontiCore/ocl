package de.monticore.ocl;

import de.monticore.ast.ASTNode;
import de.monticore.expressions.commonexpressions.CommonExpressionsAdaptationVariant;
import de.monticore.expressions.commonexpressions.CommonExpressionsBindingVariantsVisitor;
import de.monticore.expressions.expressionsbasis.ExpressionsBasisAdaptationVariant;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl.ocl._visitor.OCLHandler;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.ocl._visitor.OCLVisitor2;
import de.monticore.refadaptation.AbstractAdaptationHandler;
import de.monticore.refadaptation.AbstractAdaptationVisitor;
import de.monticore.refadaptation.Binding;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;
import de.monticore.symboltable.modifiers.AccessModifier;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
  public void traverse(ASTOCLCompilationUnit node) {
    OCLHandler.super.traverse(node);
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
    // 1. TODO we have to manually resolve the MethodSymbol here the method name here, check parametrs -> respect imports ??

    // 2. TODO then we can do the usual getIncarnations and create variants for each incarnation of the method

    TypeSymbol returnTypeSymbol = TypeCheck3.symTypeFromAST(refMethodSignature.getMCReturnType()).getTypeInfo();
    // TODO get typ symbols of parameters

    // TODO check imports and qualify name if necessary
    String methodName = refMethodSignature.getMethodName().getQName();
    Optional<MethodSymbol> resolvedMethodSymbol = getAdaptationContext().getOOSymbolsGlobalScope()
            .resolveMethod(methodName, AccessModifier.ALL_INCLUSION, symbol -> {
      if (!symbol.getType().getTypeInfo().getFullName().equals(returnTypeSymbol.getFullName())) {
        return false;
      }
      // TODO check parameter types (or name of we ignore param types. see CDCOnfParameter)
      return true;
    });
    if (resolvedMethodSymbol.isEmpty()) {
      Log.error("0xA1235 Could not resolve method symbol for " + methodName + " in scope "
              + getAdaptationContext().getOOSymbolsGlobalScope().getName());
      return;
    }
    MethodSymbol refMethodSymbol = resolvedMethodSymbol.get();
    System.out.println("Method Symbol: " + refMethodSymbol);
    System.out.println("symbol full name: " + refMethodSymbol.getFullName());
    Set<MethodSymbol> incarnations = getAdaptationContext().getOOSymbolsIncMapping().getIncarnations(refMethodSymbol);
    if (incarnations.isEmpty()) {
      // no field symbol, use the constraints from the parent expression
      // TODO Should we handle this as an error? the rferenc method could be optional and really have no incarnation -> so just ignore the constraint?
      // TODO I think returning no variant at all is the correct approach here
      getAdaptations4Ast().addVariant(refMethodSignature, getAdaptationContext().createVariant());
    } else {
      // we have the incarnations which are possible in this context
      for (MethodSymbol methodIncarnation : incarnations) {
        CommonExpressionsAdaptationVariant newVariant = getAdaptationContext().createVariant();
        newVariant.getOOSymbolsBindings().addMethodBinding(Binding.createStrict(refMethodSymbol, methodIncarnation));
        getAdaptations4Ast().addVariant(refMethodSignature, newVariant);
      }
    }
  }
}
