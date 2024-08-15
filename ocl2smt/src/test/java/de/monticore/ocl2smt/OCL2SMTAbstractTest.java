package de.monticore.ocl2smt;

import static de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy.Strategy.DEFAULT;
import static de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy.Strategy.*;
import static de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData.Strategy.ME;

import com.microsoft.z3.Context;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl.types3.OCLTypeCheck3;
import de.se_rwth.commons.logging.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.params.provider.Arguments;

public abstract class OCL2SMTAbstractTest {
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
    Log.init();
  }

  public Context buildContext() {
    Map<String, String> cfg = new HashMap<>();
    cfg.put("model", "true");
    return new Context(cfg);
  }

  public static Stream<Arguments> cd2smtStrategies() {
    return Stream.of(Arguments.of(SS, ME, DEFAULT));
    // Arguments.of(SS, ME, ONE2ONE),
    //  Arguments.of(DS, ME, DEFAULT),
    //  Arguments.of(DS, ME, ONE2ONE),
    //  Arguments.of(SSCOMB, SE, DEFAULT),
    // Arguments.of(SSCOMB, SE, ONE2ONE)););
  }
}
