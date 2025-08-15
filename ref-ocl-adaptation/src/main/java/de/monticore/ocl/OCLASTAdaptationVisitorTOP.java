package de.monticore.ocl;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl.ocl._visitor.OCLVisitor2;
import de.monticore.ocl.oclexpressions._ast.ASTOCLVariableDeclaration;
import de.monticore.ocl.setexpressions._ast.ASTGeneratorDeclaration;
import de.monticore.refadapt.AbstractAdaptationVisitor;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedName;
import de.monticore.types.mcbasictypes._ast.ASTMCReturnType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.umlstereotype._ast.ASTStereotype;

import java.util.List;
import java.util.Optional;

import static de.monticore.refadapt.RefAdaptationUtils.deepCloneComments;

// NOTE: Could be generated
public class OCLASTAdaptationVisitorTOP extends AbstractAdaptationVisitor<IOCLAdaptationContext> implements OCLVisitor2 {

  @Override
  public void endVisit(ASTOCLCompilationUnit refCompilationUnit) {
    List<IOCLAdaptationVariant> variants = getVariants4Ast().getVariants(refCompilationUnit);
    for (IOCLAdaptationVariant variant : variants) {
      ASTOCLCompilationUnit adaptedNode = adapt(refCompilationUnit, variant);
      variant.setAdaptedNode(refCompilationUnit, adaptedNode);
    }
  }

  protected ASTOCLCompilationUnit adapt(ASTOCLCompilationUnit original, IOCLAdaptationVariant variant) {
    ASTOCLCompilationUnit adapted = OCLMill.oCLCompilationUnitBuilder().uncheckedBuild();
    for (int i=0; i<original.getPackageList().size(); i++) {
      adapted.addPackage(original.getPackage(i));
    }
    for (ASTMCImportStatement importStatement : original.getMCImportStatementList()) {
      Optional<ASTMCImportStatement> adaptedImport = variant.getAdaptedNode(importStatement);
      adapted.addMCImportStatement(adaptedImport.orElseGet(importStatement::deepClone));
    }
    Optional<ASTOCLArtifact> adaptedArtifact = variant.getAdaptedNode(original.getOCLArtifact());
    adapted.setOCLArtifact(adaptedArtifact.orElseGet(original.getOCLArtifact()::deepClone));

    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTOCLArtifact refArtifact) {
    List<IOCLAdaptationVariant> variants = getVariants4Ast().getVariants(refArtifact);
    for (IOCLAdaptationVariant variant : variants) {
      ASTOCLArtifact adaptedNode = adapt(refArtifact, variant);
      variant.setAdaptedNode(refArtifact, adaptedNode);
    }
  }

  protected ASTOCLArtifact adapt(ASTOCLArtifact original, IOCLAdaptationVariant variant) {
    ASTOCLArtifact adapted = OCLMill.oCLArtifactBuilder().uncheckedBuild();
    adapted.setName(original.getName());
    for (ASTOCLConstraint constraint : original.getOCLConstraintList()) {
      Optional<ASTOCLConstraint> adaptedImport = variant.getAdaptedNode(constraint);
      adapted.addOCLConstraint(adaptedImport.orElseGet(constraint::deepClone));
    }

    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTOCLInvariant refInvariant) {
    /*
     * Get all result variants that were found during traversal of the invariant.
     * Each entry "AdaptationVariant" holds a consistent combination of all adapted
     * sub-nodes/expressions and the bindings that were used to adapt them.
     */
    List<IOCLAdaptationVariant> variants = getVariants4Ast().getVariants(refInvariant);
    for (IOCLAdaptationVariant variant : variants) {
      ASTOCLInvariant adaptedNode = adapt(refInvariant, variant);
      variant.setAdaptedNode(refInvariant, adaptedNode);
    }
  }

  protected ASTOCLInvariant adapt(ASTOCLInvariant original, IOCLAdaptationVariant variant) {
    ASTOCLInvariant adapted = OCLMill.oCLInvariantBuilder().uncheckedBuild();

    for (ASTStereotype stereotype : original.getStereotypeList()) {
      Optional<ASTStereotype> adaptedStereotype = variant.getAdaptedNode(stereotype);
      adapted.addStereotype(adaptedStereotype.orElseGet(stereotype::deepClone));
    }
    for (ASTOCLContextDefinition contextDef : original.getOCLContextDefinitionList()) {
      Optional<ASTOCLContextDefinition> adaptedContextDef = variant.getAdaptedNode(contextDef);
      adapted.addOCLContextDefinition(adaptedContextDef.orElseGet(contextDef::deepClone));
    }
    if (original.isPresentName()) {
      adapted.setName(original.getName());
    } else {
      adapted.setNameAbsent();
    }
    for (ASTOCLParamDeclaration paramDecl : original.getOCLParamDeclarationList()) {
      Optional<ASTOCLParamDeclaration> adaptedParamDecl = variant.getAdaptedNode(paramDecl);
      adapted.addOCLParamDeclaration(adaptedParamDecl.orElseGet(paramDecl::deepClone));
    }
    Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
    adapted.setExpression(adaptedExpression.orElseGet(original.getExpression()::deepClone));
    adapted.setContext(original.isContext());
    adapted.setImport(original.isImport());

    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTOCLContextDefinition node) {
    List<IOCLAdaptationVariant> variants = getVariants4Ast().getVariants(node);
    for (IOCLAdaptationVariant variant : variants) {
      ASTOCLContextDefinition adaptedNode = adapt(node, variant);
      variant.setAdaptedNode(node, adaptedNode);
    }
  }

  protected ASTOCLContextDefinition adapt(ASTOCLContextDefinition original, IOCLAdaptationVariant variant) {
    ASTOCLContextDefinition adapted = OCLMill.oCLContextDefinitionBuilder().uncheckedBuild();

    if (original.isPresentMCType()) {
      Optional<ASTMCType> adaptedType = variant.getAdaptedNode(original.getMCType());
      adapted.setMCType(adaptedType.orElseGet(original.getMCType()::deepClone));
    } else {
      adapted.setMCTypeAbsent();
    }
    if (original.isPresentGeneratorDeclaration()) {
      Optional<ASTGeneratorDeclaration> adaptedGenerator = variant.getAdaptedNode(original.getGeneratorDeclaration());
      adapted.setGeneratorDeclaration(adaptedGenerator.orElseGet(original.getGeneratorDeclaration()::deepClone));
    } else {
      adapted.setGeneratorDeclarationAbsent();
    }
    if (original.isPresentOCLParamDeclaration()) {
      Optional<ASTOCLParamDeclaration> adaptedParamDecl = variant.getAdaptedNode(original.getOCLParamDeclaration());
      adapted.setOCLParamDeclaration(adaptedParamDecl.orElseGet(original.getOCLParamDeclaration()::deepClone));
    } else {
      adapted.setOCLParamDeclarationAbsent();
    }

    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTOCLOperationConstraint refConstraint) {
    List<IOCLAdaptationVariant> variants = getVariants4Ast().getVariants(refConstraint);
    for (IOCLAdaptationVariant variant : variants) {
      ASTOCLOperationConstraint adaptedNode = adapt(refConstraint, variant);
      variant.setAdaptedNode(refConstraint, adaptedNode);
    }
  }

  protected ASTOCLOperationConstraint adapt(ASTOCLOperationConstraint original, IOCLAdaptationVariant variant) {
    ASTOCLOperationConstraint adapted = OCLMill.oCLOperationConstraintBuilder().uncheckedBuild();

    for (ASTStereotype stereotype : original.getStereotypeList()) {
      Optional<ASTStereotype> adaptedStereotype = variant.getAdaptedNode(stereotype);
      adapted.addStereotype(adaptedStereotype.orElseGet(stereotype::deepClone));
    }
    Optional<ASTOCLOperationSignature> adaptedSignature = variant.getAdaptedNode(original.getOCLOperationSignature());
    adapted.setOCLOperationSignature(adaptedSignature.orElseGet(original.getOCLOperationSignature()::deepClone));
    for (ASTOCLVariableDeclaration variableDecl : original.getOCLVariableDeclarationList()) {
      Optional<ASTOCLVariableDeclaration> adaptedVariableDecl = variant.getAdaptedNode(variableDecl);
      adapted.addOCLVariableDeclaration(adaptedVariableDecl.orElseGet(variableDecl::deepClone));
    }
    for (ASTExpression preCondition : original.getPreConditionList()) {
      Optional<ASTExpression> adaptedPreCondition = variant.getAdaptedNode(preCondition);
      adapted.addPreCondition(adaptedPreCondition.orElseGet(preCondition::deepClone));
    }
    for (ASTExpression postCondition : original.getPostConditionList()) {
      Optional<ASTExpression> adaptedPostCondition = variant.getAdaptedNode(postCondition);
      adapted.addPostCondition(adaptedPostCondition.orElseGet(postCondition::deepClone));
    }

    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTOCLMethodSignature node) {
    List<IOCLAdaptationVariant> variants = getVariants4Ast().getVariants(node);
    for (IOCLAdaptationVariant variant : variants) {
      ASTOCLMethodSignature adaptedNode = adapt(node, variant);
      variant.setAdaptedNode(node, adaptedNode);
    }
  }

  protected ASTOCLMethodSignature adapt(ASTOCLMethodSignature original, IOCLAdaptationVariant variant) {
    ASTOCLMethodSignature adapted = OCLMill.oCLMethodSignatureBuilder().uncheckedBuild();

    if (original.isPresentMCReturnType()) {
      Optional<ASTMCReturnType> adaptedParamDecl = variant.getAdaptedNode(original.getMCReturnType());
      adapted.setMCReturnType(adaptedParamDecl.orElseGet(original.getMCReturnType()::deepClone));
    } else {
      adapted.setMCReturnTypeAbsent();
    }
    Optional<ASTMCQualifiedName> adaptedMethodName = variant.getAdaptedNode(original.getMethodName());
    adapted.setMethodName(adaptedMethodName.orElseGet(original.getMethodName()::deepClone));
    for (ASTOCLParamDeclaration paramDecl : original.getOCLParamDeclarationList()) {
      Optional<ASTOCLParamDeclaration> adaptedParamDecl = variant.getAdaptedNode(paramDecl);
      adapted.addOCLParamDeclaration(adaptedParamDecl.orElseGet(paramDecl::deepClone));
    }
    for (ASTMCQualifiedName throwable : original.getThrowablesList()) {
      Optional<ASTMCQualifiedName> adaptedThrowable = variant.getAdaptedNode(throwable);
      adapted.addThrowables(adaptedThrowable.orElseGet(throwable::deepClone));
    }

    deepCloneComments(adapted, original);
    return adapted;
  }

  @Override
  public void endVisit(ASTOCLParamDeclaration node) {
    List<IOCLAdaptationVariant> variants = getVariants4Ast().getVariants(node);
    for (IOCLAdaptationVariant variant : variants) {
      ASTOCLParamDeclaration adaptedNode = adapt(node, variant);
      variant.setAdaptedNode(node, adaptedNode);
    }
  }

  protected ASTOCLParamDeclaration adapt(ASTOCLParamDeclaration original, IOCLAdaptationVariant variant) {
    ASTOCLParamDeclaration adapted = OCLMill.oCLParamDeclarationBuilder().uncheckedBuild();

    Optional<ASTMCType> adaptedMCType = variant.getAdaptedNode(original.getMCType());
    adapted.setMCType(adaptedMCType.orElseGet(original.getMCType()::deepClone));
    adapted.setName(original.getName());
    if (original.isPresentExpression()) {
      Optional<ASTExpression> adaptedExpression = variant.getAdaptedNode(original.getExpression());
      adapted.setExpression(adaptedExpression.orElseGet(original.getExpression()::deepClone));
    } else {
      adapted.setExpressionAbsent();
    }

    deepCloneComments(adapted, original);
    return adapted;
  }
}
