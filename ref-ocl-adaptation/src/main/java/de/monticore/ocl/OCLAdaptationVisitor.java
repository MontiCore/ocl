package de.monticore.ocl;

import de.monticore.ast.Comment;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl.ocl._visitor.OCLVisitor2;
import de.monticore.refadaptation.AbstractAdaptationVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OCLAdaptationVisitor extends AbstractAdaptationVisitor<OCLAdaptationContext> implements OCLVisitor2 {

  @Override
  public void endVisit(ASTOCLCompilationUnit refCompilationUnit) {
    List<OCLAdaptationVariant> variants = getAdaptations4Ast().getVariants(refCompilationUnit.getOCLArtifact());
    for (OCLAdaptationVariant variant : variants) {
      ASTOCLCompilationUnit adaptedCompilationUnit = refCompilationUnit.deepClone();
      Optional<ASTOCLArtifact> adaptedArtifact = variant.getAdaptedNode(refCompilationUnit.getOCLArtifact());
      adaptedArtifact.ifPresent(adaptedCompilationUnit::setOCLArtifact);
      // Set the adapted node for the compilation unit
      variant.setAdaptedNode(refCompilationUnit, adaptedCompilationUnit);
      // link variant to compilation unit
      getAdaptations4Ast().addVariant(refCompilationUnit, variant);
    }
  }

  @Override
  public void endVisit(ASTOCLArtifact refArtifact) {
    ASTOCLArtifact adaptedArtifact = refArtifact.deepClone();
    List<ASTOCLConstraint> allAdaptedConstraints = new ArrayList<>();
    for (ASTOCLConstraint refConstraint : refArtifact.getOCLConstraintList()) {
      List<OCLAdaptationVariant> variants = getAdaptations4Ast().getVariants(refConstraint);
      List<ASTOCLConstraint> adaptedConstraints = variants.stream().map(v -> v.getAdaptedNode(refConstraint))
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
    // Add a SINGLE variant for the artifact combining all the adapted constraints
    OCLAdaptationVariant variant = getAdaptationContext().createVariant();
    variant.setAdaptedNode(refArtifact, adaptedArtifact);
    getAdaptations4Ast().addVariant(refArtifact, variant);
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
}
