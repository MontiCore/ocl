package de.monticore.oclrefadaptation;

import de.monticore.cdbasis._symboltable.ICDBasisScope;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.ocl.ocl._ast.ASTOCLConstraint;
import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.TypeCheck3;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class EvaluationOCLAdaptationTest extends AbstractOCLAdapterTest {

  @Test
  void typeComparisonPrototype() {
    confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
    parseModels(
            "builder/BuilderConc.cd",
            "builder/BuilderRef.cd",
            "builder/constraints/BuildResultPropertiesRef.ocl",
            "builder/constraints/BuildResultPropertiesOut.ocl");

    //OCLAdapter oclAdapter = new OCLAdapter(confParameters);
    //List<ASTOCLCompilationUnit> adaptedOCLList = oclAdapter.adapt(conCD, refCD, "ref", List.of(refOCL));
    //ASTOCLCompilationUnit adaptedOCL = adaptedOCLList.get(0);

    ASTOCLConstraint constraint = refOCL.getOCLArtifact().getOCLConstraintList().get(0);
    assertTrue(constraint instanceof ASTOCLOperationConstraint);
    ASTOCLOperationConstraint operationConstraint = (ASTOCLOperationConstraint) constraint;
    ASTOCLMethodSignature signature = (ASTOCLMethodSignature) operationConstraint.getOCLOperationSignature();
    SymTypeExpression returnTypeExpr = TypeCheck3.symTypeFromAST(signature.getMCReturnType());
    TypeSymbol returntypeSymbol = returnTypeExpr.getTypeInfo();

    System.out.println("return type symbol full name: " + returntypeSymbol.getFullName());
    System.out.println("return type symbol class: " + returntypeSymbol.getClass());

    ICDBasisScope refCDScope = refCD.getEnclosingScope();
    refCDScope.resolveTypeDown(returntypeSymbol.getFullName())
            .ifPresent(typeSymbol -> {
              System.out.println("resolved type symbol full name: " + typeSymbol.getFullName());
              System.out.println("resolved type symbol class: " + typeSymbol.getClass());
            });
  }


  @Nested
  class Builder {
    @Test
    void buildResultProperties() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "builder/BuilderConc.cd",
              "builder/BuilderRef.cd",
              "builder/constraints/BuildResultPropertiesRef.ocl",
              "builder/constraints/BuildResultPropertiesOut.ocl");
    }

    @Test
    void buildResultPropertiesSwapped() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "builder/BuilderConc.cd",
              "builder/BuilderRef.cd",
              "builder/constraints/BuildResultPropertiesSwappedRef.ocl",
              "builder/constraints/BuildResultPropertiesSwappedOut.ocl");
    }

    @Test
    void methodCallChangedBuildResult() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "builder/BuilderConc.cd",
              "builder/BuilderRef.cd",
              "builder/constraints/MethodCallChangesBuildResultRef.ocl",
              "builder/constraints/MethodCallChangesBuildResultOut.ocl");
    }

    @Test
    void methodCallChangedBuildResultSwapped() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "builder/BuilderConc.cd",
              "builder/BuilderRef.cd",
              "builder/constraints/MethodCallChangesBuildResultSwappedRef.ocl",
              "builder/constraints/MethodCallChangesBuildResultSwappedOut.ocl");
    }
  }
}
