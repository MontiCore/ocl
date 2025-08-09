package de.monticore.oclrefadaptation;

import de.monticore.cd4analysis.trafo.CDAssociationCreateFieldsFromAllRoles;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._symboltable.ICDBasisScope;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.expressions.commonexpressions._ast.ASTEqualsExpression;
import de.monticore.expressions.commonexpressions._ast.ASTFieldAccessExpression;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLConstraint;
import de.monticore.ocl.ocl._ast.ASTOCLMethodSignature;
import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.TypeCheck3;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

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

    /**
     * Insight: Instead of letting the tool applying complicated adaptations because we have
     * multiple incarnations of the 'Account' type, we can simply subclass 'Account'. This way
     * out OCL constraint for the total balance only requires simple adaptation and the balance
     * is still computed over all accounts (because of usual inheritance).
     */
    @Test
    void accountSubclasses() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "banking/accountSubclasses/BankingConc.cd",
              "banking/BankingRef.cd",
              "banking/BankingRef.ocl",
              "banking/accountSubclasses/BankingOut.ocl");
    }
  }

  @Nested
  class Simple {
    @Test
    void simpleExamples() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "simple/singleInc/SimpleConc.cd",
              "simple/SimpleRef.cd",
              "simple/SimpleRef.ocl",
              "simple/singleInc/SimpleOut.ocl");
    }

    /**
     * Demonstrates adaptation of query methods (here in FieldAccessExpression).
     */
    @Test
    void queryMethodRename() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "simple/singleInc/SimpleConc.cd",
              "simple/SimpleRef.cd",
              "simple/QueryMethodRef.ocl",
              "simple/singleInc/QueryMethodOut.ocl");
    }

    /**
     * Demonstrates adaptation of query methods (here in FieldAccessExpression).
     * Same as {@link #queryMethodRename()} but with a different syntax (using equivalent operator
     * from OCLExpressions language)
     */
    @Test
    void queryMethodRename1() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "simple/singleInc/SimpleConc.cd",
              "simple/SimpleRef.cd",
              "simple/QueryMethodRef1.ocl",
              "simple/singleInc/QueryMethodOut1.ocl");
    }

    @Test
    void queryMethodInNameExpression() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "simple/singleInc/SimpleConc.cd",
              "simple/SimpleRef.cd",
              "simple/QueryMethodInNameExprRef.ocl",
              "simple/singleInc/QueryMethodInNameExprOut.ocl");
    }

    @Test
    void typeMI() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "simple/typeMI/TypeMI.cd",
              "simple/SimpleRef.cd",
              "simple/SimpleRef.ocl",
              "simple/typeMI/typeMIOut.ocl");
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

  @Nested
  class Singleton {
    @Test
    void multiInc() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "singleton/SingletonConc.cd",
              "singleton/SingletonRef.cd",
              "singleton/SingletonRef.ocl",
              "singleton/SingletonOut.ocl");
    }
  }

  @Nested
  class Observer {
    @Test
    void singleInc() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "observer/singleInc/ObserverConc.cd",
              "observer/ObserverRef.cd",
              "observer/ObserverRef.ocl",
              "observer/singleInc/ObserverOut.ocl");
    }

    /**
     * Adds an additional 'Event' parameter to the 'update' and 'publishEvent' methods.
     */
    @Disabled("Disabled because the additional parameter is not adapted correctly yet. See comments in model")
    @Test
    void additionalParam() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      confParameters.add(CDConfParameter.ALLOW_ADDITIONAL_PARAMETERS);
      testAdaptedEqualsExpected(
              "observer/additionalParam/AdditionalEventParam.cd",
              "observer/ObserverRef.cd",
              "observer/ObserverRef.ocl",
              "observer/additionalParam/ObserverOut.ocl");
    }

    @Test
    void additionalParamPartiallyWorking() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      confParameters.add(CDConfParameter.ALLOW_ADDITIONAL_PARAMETERS);
      testAdaptedEqualsExpected(
              "observer/additionalParam/partiallyWorking/AdditionalEventParam.cd",
              "observer/ObserverRef.cd",
              "observer/ObserverRef.ocl",
              "observer/additionalParam/partiallyWorking/ObserverOut.ocl");
    }

    @Test
    void subjectMultiInc() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      confParameters.add(CDConfParameter.ALLOW_ADDITIONAL_PARAMETERS);
      testAdaptedEqualsExpected(
              "observer/subjectMultiInc/ObserverConc.cd",
              "observer/ObserverRef.cd",
              "observer/ObserverRef.ocl",
              "observer/subjectMultiInc/ObserverOut.ocl");
    }

    @Test
    void observerMultiInc() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      confParameters.add(CDConfParameter.ALLOW_ADDITIONAL_PARAMETERS);
      testAdaptedEqualsExpected(
              "observer/observerMultiInc/ObserverConc.cd",
              "observer/ObserverRef.cd",
              "observer/ObserverRef.ocl",
              "observer/observerMultiInc/ObserverOut.ocl");
    }
  }


  @Nested
  class Evaluation3 {

    /**
     * Uses a concrete CD that matches exactly the reference CD, except for a different name.
     * (The different name is required as we get name conflicts when resolving symbols during
     * conformance checking.)
     */
    @Test
    void noChanges() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "evaluation3/noChanges/UserConc.cd",
              "evaluation3/UserRef.cd",
              "evaluation3/InvariantsRef.ocl",
              "evaluation3/noChanges/InvariantsOut.ocl");
    }

    @Test
    void singleInc1() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "evaluation3/singleInc1/UserConc.cd",
              "evaluation3/UserRef.cd",
              "evaluation3/InvariantsRef.ocl",
              "evaluation3/singleInc1/InvariantsOut.ocl");
    }
  }
}
