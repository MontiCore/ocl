package de.monticore.oclrefadaptation;

import de.monticore.cdbasis._symboltable.ICDBasisScope;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.expressions.commonexpressions._ast.ASTEqualsExpression;
import de.monticore.expressions.commonexpressions._ast.ASTFieldAccessExpression;
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

    ASTEqualsExpression equalsExpr = (ASTEqualsExpression) operationConstraint.getPostCondition(0);
    ASTFieldAccessExpression fieldAccessExpr = (ASTFieldAccessExpression) equalsExpr.getLeft();
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

      ASTOCLOperationConstraint constraint = (ASTOCLOperationConstraint) refOCL.getOCLArtifact().getOCLConstraint(0);
      ASTEqualsExpression equalsExpr = (ASTEqualsExpression) constraint.getPostConditionList().get(0);

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

  @Nested
  class Banking {
    @Test
    void singleInc() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "banking/singleInc/BankingConc.cd",
              "banking/BankingRef.cd",
              "banking/BankingRef.ocl",
              "banking/singleInc/BankingOut.ocl");
    }

    /**
     * In this example, we have two incarnations of 'Account', 'BankAccount' and 'PrivateAccount'.
     * Also, we have two incarnations of 'Bank.overallBalance', 'overallPrivateAccountsBalance' and
     * 'overallBusinessAccountsBalance'. However, we do not want to get adapted OCL that combines
     * usage of the 'Bank.privateAccount' association role with the field 'Bank.overallBusinessAccountsBalance'.
     * Therefore, we manually add bindings via stereotype to the two field incarnations binding the
     * 'Account' type either to 'BusinessAccount' or 'PrivateAccount'.
     */
    @Test
    void multiIncWithBind() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "banking/multiIncWithBind/BankingConc.cd",
              "banking/BankingRef.cd",
              "banking/BankingRef.ocl",
              "banking/multiIncWithBind/BankingOut.ocl");
    }
  }

  @Nested
  class Simple {
    @Test
    void simpleExamples() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "simple/SimpleConc.cd",
              "simple/SimpleRef.cd",
              "simple/SimpleRef.ocl",
              "simple/SimpleOut.ocl");
    }

    @Test
    void queryMethodRename() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "simple/SimpleConc.cd",
              "simple/SimpleRef.cd",
              "simple/QueryMethodRef.ocl",
              "simple/QueryMethodOut.ocl");
    }

    @Test
    void typeMI() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "simple/TypeMI.cd",
              "simple/SimpleRef.cd",
              "simple/SimpleRef.ocl",
              "simple/typeMIOut.ocl");
    }
  }

  @Nested
  class Auction {
    @Test
    void singleInc() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "auction/singleInc/AuctionConc.cd",
              "auction/AuctionRef.cd",
              "auction/quantifiers2Ref.ocl",
              "auction/singleInc/quantifiers2Out.ocl");
    }

    @Test
    void multiInc() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "auction/multiInc/AuctionConc.cd",
              "auction/AuctionRef.cd",
              "auction/quantifiers2Ref.ocl",
              "auction/multiInc/quantifiers2Out.ocl");
    }
  }

  @Nested
  class TaskManagement {

    @Test
    void singleIncBehindSchedule() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "tasks/singleInc/TaskManagementConc.cd",
              "tasks/TaskManagementRef.cd",
              "tasks/BehindScheduleRef.ocl",
              "tasks/singleInc/BehindScheduleOut.ocl");
    }

    @Test
    void singleIncRemainingWorkload() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "tasks/singleInc/TaskManagementConc.cd",
              "tasks/TaskManagementRef.cd",
              "tasks/RemainingWorkloadRef.ocl",
              "tasks/singleInc/RemainingWorkloadOut.ocl");
    }

    @Test
    void singleIncSimpleAssocChaining() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "tasks/singleInc/TaskManagementConc.cd",
              "tasks/TaskManagementRef.cd",
              "tasks/SimpleAssocChainingRef.ocl",
              "tasks/singleInc/SimpleAssocChainingOut.ocl");
    }

    @Test
    void singleIncAssocChainingWorkaround() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "tasks/singleInc/TaskManagementConc.cd",
              "tasks/TaskManagementRef.cd",
              "tasks/AssocChainingWorkaroundRef.ocl",
              "tasks/singleInc/AssocChainingWorkaroundOut.ocl");
    }
  }
}
