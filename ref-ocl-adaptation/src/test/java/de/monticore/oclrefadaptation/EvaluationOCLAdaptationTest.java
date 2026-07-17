package de.monticore.oclrefadaptation;

import de.monticore.cdconformance.CDConfParameter;
import de.monticore.expressions.commonexpressions._ast.ASTEqualsExpression;
import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public class EvaluationOCLAdaptationTest extends AbstractOCLAdapterTest {

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
    // todo https://git.rwth-aachen.de/monticore/monticore/-/work_items/5099
    @Disabled
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
    // todo https://git.rwth-aachen.de/monticore/monticore/-/work_items/5099
    @Disabled
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
    // todo https://git.rwth-aachen.de/monticore/monticore/-/work_items/5099
    @Disabled
    @Test
    void accountSubclasses() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "banking/accountSubclasses/BankingConc.cd",
              "banking/BankingRef.cd",
              "banking/BankingRef.ocl",
              "banking/accountSubclasses/BankingOut.ocl");
    }

    @Test
    void balanceMultiInc() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "banking/balanceMultiInc/BankingConc.cd",
              "banking/balanceMultiInc/BankingRef.cd",
              "banking/balanceMultiInc/BankingRef.ocl",
              "banking/balanceMultiInc/BankingOut.ocl");
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
              "simple/typeMI/TypeMIOut.ocl");
    }
  }

  @Nested
  class Auction {
    // todo https://git.rwth-aachen.de/monticore/monticore/-/work_items/5099
    @Disabled
    @Test
    void singleInc() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "auction/singleInc/AuctionConc.cd",
              "auction/AuctionRef.cd",
              "auction/quantifiers2Ref.ocl",
              "auction/singleInc/quantifiers2Out.ocl");
    }

    // todo https://git.rwth-aachen.de/monticore/monticore/-/work_items/5099
    @Disabled
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

    // todo https://git.rwth-aachen.de/monticore/monticore/-/work_items/5099
    @Disabled
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
    // todo https://git.rwth-aachen.de/monticore/monticore/-/work_items/5099
    @Disabled
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
      testAdaptedEqualsExpected(
              "observer/subjectMultiInc/ObserverConc.cd",
              "observer/ObserverRef.cd",
              "observer/ObserverRef.ocl",
              "observer/subjectMultiInc/ObserverOut.ocl");
    }

    @Test
    void observerMultiInc() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "observer/observerMultiInc/ObserverConc.cd",
              "observer/ObserverRef.cd",
              "observer/ObserverRef.ocl",
              "observer/observerMultiInc/ObserverOut.ocl");
    }


    /**
     * Demonstrates adaptation of multiple 'register' method and 'observers' associations can lead
     * to undesired combinations. e.g., using 'normalListeners' assoc with 'registerHighPrio' method.
     */
    @Test
    void registerMethodAndAssocMultiInc() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "observer/registerAndAssocMultiInc/ObserverConc.cd",
              "observer/ObserverRef.cd",
              "observer/ObserverRef.ocl",
              "observer/registerAndAssocMultiInc/ObserverOut.ocl");
    }

    /**
     * Same as {@link #registerMethodAndAssocMultiInc()} but with manual binding via stereotypes so
     * only the desired association is used together with normal/highPrio register methods.
     */
    @Test
    void registerMethodAndAssocMultiIncManualMethodBind() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "observer/registerAndAssocMultiIncManualBind/ObserverMethodBindingConc.cd",
              "observer/ObserverRef.cd",
              "observer/ObserverRef.ocl",
              "observer/registerAndAssocMultiIncManualBind/ObserverMethodBindingOut.ocl");
    }

    /**
     * Same result as {@link #registerMethodAndAssocMultiIncManualMethodBind()} but here we do not
     * attach method bindings to the associations, but association bindings to the methods.
     * The effect is the same: only the desired association is used together with
     * normal/highPrio register methods.
     */
    /*
     * TODO: To solve this, we must add manual "field bindings" via stereotype for the fields
     *  generated by the 'CreateFieldFromAllRolesWithRefST' trafo. OCL adapt only works on
     *  OOSymbolsIncMapping so it won't recognize association bindings, even if we properly support
     *  them in CDConformanceChecker (which we don't yet). So, we must extend the transformation
     *  to turn for example <<bind="refAssoc=concAssoc">> into two field bindings as follows:
     *  <<bind="LeftTypeRef.leftRoleRef=LeftTypeConc.leftRoleConc",
     *    bind="RightTypeRef.rightRoleRef=RightTypeConc.rightRoleConc">>
     */
    @Disabled("Does not wok yet because we cannot handle association bindings in OCL adapt yet.")
    @Test
    void registerMethodAndAssocMultiIncManualAssocBind() {
      confParameters.add(CDConfParameter.STRICT_PARAMETER_ORDER);
      testAdaptedEqualsExpected(
              "observer/registerAndAssocMultiIncManualBind/ObserverAssocBindingConc.cd",
              "observer/ObserverRef.cd",
              "observer/ObserverRef.ocl",
              "observer/registerAndAssocMultiIncManualBind/ObserverAssocBindingOut.ocl");
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
