package de.monticore.ocl;

import de.monticore.ast.Comment;
import de.monticore.cd.facade.MCQualifiedNameFacade;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl.ocl._visitor.OCLVisitor2;
import de.monticore.refadaptation.AbstractAdaptationVisitor;
import de.monticore.refadaptation.Binding;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;
import de.monticore.types.MCTypeFacade;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCReturnType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.lang3.NotImplementedException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OCLAdaptationVisitor extends AbstractAdaptationVisitor<OCLAdaptationContext> implements OCLVisitor2 {

  private static final String LOG_NAME = OCLAdaptationVisitor.class.getName();

  @Override
  public void endVisit(ASTOCLCompilationUnit refCompilationUnit) {
    List<OCLAdaptationVariant> variants = getAdaptations4Ast().getVariants(refCompilationUnit);
    for (OCLAdaptationVariant variant : variants) {
      ASTOCLCompilationUnit adaptedCompilationUnit = refCompilationUnit.deepClone();
      Optional<ASTOCLArtifact> adaptedArtifact = variant.getAdaptedNode(refCompilationUnit.getOCLArtifact());
      adaptedArtifact.ifPresent(adaptedCompilationUnit::setOCLArtifact);
      // Set the adapted node for the compilation unit
      variant.setAdaptedNode(refCompilationUnit, adaptedCompilationUnit);
    }
  }

  @Override
  public void endVisit(ASTOCLArtifact refArtifact) {
    List<OCLAdaptationVariant> variants = getAdaptations4Ast().getVariants(refArtifact);
    for (OCLAdaptationVariant variant : variants) {
      ASTOCLArtifact adaptedArtifact = refArtifact.deepClone();

      List<ASTOCLConstraint> allAdaptedConstraints = new ArrayList<>();
      for (ASTOCLConstraint refConstraint : refArtifact.getOCLConstraintList()) {
        /*
         * NOTE: The knowledge that a variant of a OCLArtifact contains multiple child variants
         * for each OCLConstraint is a tight coupling between this visitor and the related
         * OCLAdaptation visitor. This can be considered bad design, but is required to enable us
         * to perform two different visitors runs: 1. one to find all variants 2. one to adapt the
         * AST (saving unnecessary deepClone calls)!
         */
        List<OCLAdaptationVariant> constraintVariants = variant.getChildVariants(refConstraint);
        List<ASTOCLConstraint> adaptedConstraints = constraintVariants.stream().map(v -> v.getAdaptedNode(refConstraint))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        if (!adaptedConstraints.isEmpty()) {
          // separator between constraints so they are grouped by reference constraint
          adaptedConstraints.get(0).add_PreComment(new Comment("=========="));
        }
        allAdaptedConstraints.addAll(adaptedConstraints);
      }
      adaptedArtifact.setOCLConstraintList(allAdaptedConstraints);
      variant.setAdaptedNode(refArtifact, adaptedArtifact);
    }
  }

  @Override
  public void endVisit(ASTOCLInvariant refInvariant) {
    /*
     * Get all result variants that were found during traversal of the expression.
     * Each entry "AdaptationVariant" holds a consistent combination of all adapted
     * sub-nodes/expressions and the bindings that were used to adapt them.
     */
    List<OCLAdaptationVariant> invariantVariants = getAdaptations4Ast().getVariants(refInvariant);

    // Create a new OCLAdaptationVariant for each variant with the adapted context adn expression
    int i =0;
    for (OCLAdaptationVariant variant : invariantVariants) {
      // TODO deepClone vs Builder & custom "adapt" implementation
      ASTOCLInvariant adaptedInvariant = refInvariant.deepClone();
      adaptedInvariant.clearOCLContextDefinitions();

      // 1. add the adapted context definitions
      for (ASTOCLContextDefinition refConstraint : refInvariant.getOCLContextDefinitionList()) {
        List<OCLAdaptationVariant> contextDefVariants = getAdaptations4Ast().getVariants(refConstraint);
        List<ASTOCLContextDefinition> adaptedConstraints = contextDefVariants.stream()
                .map(v -> v.getAdaptedNode(refConstraint))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
        adaptedInvariant.addAllOCLContextDefinitions(adaptedConstraints);
      }

      // 2. set the adapted expression
      Optional<ASTExpression> expression = variant.getAdaptedNode(refInvariant.getExpression());
      expression.ifPresent(adaptedInvariant::setExpression);

      // 3. find a useful name for the refInvariant
      if (refInvariant.getName() != null && !refInvariant.getName().isBlank()) {
        // TODO maybe something better than counting. We could use infix replacement & suffixes again...
        if (invariantVariants.size() > 1) {
          // only add suffix if we have multiple variants
          adaptedInvariant.setName(refInvariant.getName() + "_" + i);
        }
      }

      // store adapted expression in variant
      variant.setAdaptedNode(refInvariant, adaptedInvariant);
    }
  }

  @Override
  public void endVisit(ASTOCLOperationConstraint refConstraint) {
    List<OCLAdaptationVariant> variants = getAdaptations4Ast().getVariants(refConstraint);
    for (OCLAdaptationVariant variant : variants) {
      // TODO deepClone vs Builder & custom "adapt" implementation
      ASTOCLOperationConstraint adaptedInvariant = refConstraint.deepClone();

      // 1. use the adapted signature
      Optional<ASTOCLOperationSignature> adaptedSignature = variant.getAdaptedNode(refConstraint.getOCLOperationSignature());
      adaptedSignature.ifPresent(adaptedInvariant::setOCLOperationSignature);

      // 2. add the adapted pre-conditions
      adaptedInvariant.clearPreCondition();
      for (ASTExpression refPreCondition : refConstraint.getPreConditionList()) {
        Optional<ASTExpression> adaptedPreCondition = variant.getAdaptedNode(refPreCondition);
        adaptedPreCondition.ifPresent(adaptedInvariant::addPreCondition);
      }

      // 2. add the adapted post-conditions
      adaptedInvariant.clearPostCondition();
      for (ASTExpression refPostCondition : refConstraint.getPostConditionList()) {
        Optional<ASTExpression> adaptedPostCondition = variant.getAdaptedNode(refPostCondition);
        adaptedPostCondition.ifPresent(adaptedInvariant::addPostCondition);
      }

      // store adapted expression in variant
      variant.setAdaptedNode(refConstraint, adaptedInvariant);
    }
  }

  @Override
  public void endVisit(ASTOCLMethodSignature refMethodSignature) {
    MethodSymbol refMethodSymbol = OCLAdaptationUtils.resolveMethodSymbol(getAdaptationContext(), refMethodSignature);
    List<OCLAdaptationVariant> variants = getAdaptations4Ast().getVariants(refMethodSignature);
    for (OCLAdaptationVariant variant : variants) {
      // TODO deepClone vs Builder & custom "adapt" implementation
      ASTOCLMethodSignature adaptedSignature = refMethodSignature.deepClone();

      Optional<Binding<MethodSymbol>> binding = variant.getOOSymbolsBindings().getBinding(refMethodSymbol);
      if (binding.isPresent()) {
        // a field binding attached to a adaptedSignature is always required to be strict (??)
        MethodSymbol methodSymbolInc = binding.get().getStrictConcreteElement();
        adaptedSignature.setMethodName(MCQualifiedNameFacade.createQualifiedName(methodSymbolInc.getFullName()));

        // 2. use the adapted return type
        adaptedSignature.setMCReturnType(createReturnTypeFromSymTypeExpression(methodSymbolInc.getType()));

        // 3. use the adapted parameters
        adaptedSignature.clearOCLParamDeclarations();
        for (VariableSymbol paramSymbol : methodSymbolInc.getParameterList()) {
          ASTMCType paramType = createTypeFromSymTypeExpression(paramSymbol.getType());
          adaptedSignature.addOCLParamDeclaration(OCLMill.oCLParamDeclarationBuilder()
                          .setMCType(paramType)
                          .setName(paramSymbol.getName())
                  .build()
          );
        }
      } else {
        // This is not an error. it is completely normal for fields that are not declared in the
        // reference model
        Log.debug("No binding found for MethodSymbol: " + refMethodSymbol.getFullName()
                + ". Using original method: " + refMethodSymbol, LOG_NAME);
      }

      // store adapted signature in variant
      variant.setAdaptedNode(refMethodSignature, adaptedSignature);
    }
  }

  private ASTMCType createTypeFromSymTypeExpression(SymTypeExpression symTypeExpr) {
    // TODO Is there an easy way to construct ASTMCType objects from SymTypeExpressions?
    //  I think we should use visitor for type adaptation / construction as well. This
    //  way we stay opn for extension
    if (symTypeExpr.isPrimitive()) {
      throw new NotImplementedException("Primitive types are not supported yet: " + symTypeExpr);
    } else if (symTypeExpr.isObjectType()) {
      return MCTypeFacade.getInstance().createQualifiedType(symTypeExpr.printFullName());
    } else {
      throw new NotImplementedException("unsupported type: " + symTypeExpr);
    }
  }

  private ASTMCReturnType createReturnTypeFromSymTypeExpression(SymTypeExpression symTypeExpr) {
    // TODO Is there an easy way to construct ASTMCType objects from SymTypeExpressions?
    //  I think we should use visitor for type adaptation / construction as well. This
    //  way we stay opn for extension
    if (symTypeExpr.isVoidType()) {
      return OCLMill.mCReturnTypeBuilder()
              .setMCVoidType(MCTypeFacade.getInstance().createVoidType())
              .build();
    } else {
      return OCLMill.mCReturnTypeBuilder()
              .setMCType(createTypeFromSymTypeExpression(symTypeExpr))
              .build();
    }
  }
}
