package de.monticore.ocl;

import de.monticore.ast.Comment;
import de.monticore.cd.facade.MCQualifiedNameFacade;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLArtifact;
import de.monticore.ocl.ocl._ast.ASTOCLConstraint;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.monticore.refadaptation.Binding;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symbols.oosymbols._symboltable.MethodSymbol;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.types.mcbasictypes.refadaptation.MCTypeFactory;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OCLASTAdaptationVisitor extends OCLASTAdaptationVisitorTOP {

  private static final String LOG_NAME = OCLASTAdaptationVisitor.class.getName();

  private final MCTypeFactory typeFactory;

  public OCLASTAdaptationVisitor(MCTypeFactory typeFactory) {
    this.typeFactory = typeFactory;
  }

  @Override
  protected ASTOCLArtifact adapt(ASTOCLArtifact original, OCLAdaptationVariant variant) {
    ASTOCLArtifact adapted = super.adapt(original, variant);
    List<ASTOCLConstraint> allAdaptedConstraints = new ArrayList<>();
    for (ASTOCLConstraint refConstraint : original.getOCLConstraintList()) {
      /*
       * NOTE: The knowledge that a variant of a OCLArtifact contains multiple child variants
       * for each OCLConstraint is a TIGHT COUPLING between this visitor and the related
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
    adapted.setOCLConstraintList(allAdaptedConstraints);
    return adapted;
  }

  @Override
  protected ASTOCLInvariant adapt(ASTOCLInvariant original, OCLAdaptationVariant variant) {
    // TODO 3. find a useful name for the refInvariant
    // TODO maybe something better than counting. We could use infix replacement & suffixes again...
    return super.adapt(original, variant);
  }

  @Override
  protected ASTOCLMethodSignature adapt(ASTOCLMethodSignature original, OCLAdaptationVariant variant) {
    MethodSymbol refMethodSymbol = OCLAdaptationUtils.resolveMethodSymbol(getAdaptationContext()
            .getOriginalOOSymbolsIncMapping().getReferenceScope(), original);
    // NOTE: we still reuse the generated "adapt" method instead of "deepClone". In case we have no
    // binding for the method symbol, we still want be able to adapt, e.g. only a return type
    ASTOCLMethodSignature adapted = super.adapt(original, variant);

    Optional<Binding<MethodSymbol>> binding = variant.getOOSymbolsBindings().getBinding(refMethodSymbol);
    if (binding.isPresent()) {
      // a field binding attached to a adaptedSignature is always required to be strict (??)
      MethodSymbol methodSymbolInc = binding.get().getStrictConcreteElement();
      adapted.setMethodName(MCQualifiedNameFacade.createQualifiedName(methodSymbolInc.getFullName()));

      // 2. use the adapted return type
      adapted.setMCReturnType(typeFactory.createMCReturnType(methodSymbolInc.getType()));

      // 3. use the adapted parameters
      adapted.clearOCLParamDeclarations();
      for (VariableSymbol paramSymbol : methodSymbolInc.getParameterList()) {
        ASTMCType paramType = typeFactory.createMCType(paramSymbol.getType());
        adapted.addOCLParamDeclaration(OCLMill.oCLParamDeclarationBuilder()
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
    return adapted;
  }
}
