package de.monticore.ocl2smt;

import static de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy.Strategy.DEFAULT;
import static de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy.Strategy.ONE2ONE;
import static de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy.Strategy.*;
import static de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData.Strategy.ME;
import static de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData.Strategy.SE;

import com.microsoft.z3.Context;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl.types3.OCLTypeCheck3;
import de.se_rwth.commons.logging.LogStub;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.provider.Arguments;

public abstract class OCL2SMTAbstractTest {

  @BeforeAll
  public static void initSeed() {
    CD2SMTGenerator.setSeed(42);
  }

  protected void initMills() {
    OCLMill.reset();
    OCLMill.init();
    OCLMill.globalScope().clear();

    CD4CodeMill.reset();
    CD4CodeMill.init();
    CD4CodeMill.globalScope().clear();

    CD2SMTMill.initDefault();

    // init OCL TypeCheck again after other mills
    OCLTypeCheck3.init();
  }

  protected void initLogger() {
    LogStub.init();
  }

  @AfterEach
  protected void assertNoFindings() {
    // todo https://git.rwth-aachen.de/monticore/monticore/-/issues/4518
    // disabled, as OCL2SMT currently throws a bunch of warnings,
    // it seemingly has never been tested with Log (some CoCos fail as well)
    // assertTrue(
    //    Log.getFindings().isEmpty(),
    //    Log.getFindings().stream()
    //        .map(Finding::buildMsg)
    //        .collect(Collectors.joining(System.lineSeparator())));
  }

  public Context buildContext() {
    Map<String, String> cfg = new LinkedHashMap<>();
    cfg.put("model", "true");
    return new Context(cfg);
  }

  public static Stream<Arguments> cd2smtStrategies() {
    return Stream.of(
        Arguments.of(SS, ME, DEFAULT),
        Arguments.of(SS, ME, ONE2ONE),
        Arguments.of(DS, ME, DEFAULT),
        Arguments.of(DS, ME, ONE2ONE),
        Arguments.of(SSCOMB, SE, DEFAULT),
        Arguments.of(SSCOMB, SE, ONE2ONE));
  }
}
