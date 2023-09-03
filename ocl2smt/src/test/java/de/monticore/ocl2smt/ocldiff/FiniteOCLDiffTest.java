/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.ocldiff;

import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy;
import de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy;
import de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceData;
import de.monticore.ocl2smt.helpers.IOHelper;
import de.monticore.ocl2smt.ocldiff.invariantDiff.OCLInvDiffResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

public class FiniteOCLDiffTest extends OCLDiffAbstractTest {
  private final String TARGET_DIR = "target/generated-test/oclDiff/";

  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
  }

  @Disabled
  @Test
  public void testOCLDiffOneCDFinite() throws IOException {
    CD2SMTMill.init(
        ClassStrategy.Strategy.FINITEDS,
        InheritanceData.Strategy.ME,
        AssociationStrategy.Strategy.DEFAULT);
    OCLInvDiffResult diff = computeDiffOneCDFinite("MinAuction.cd", "Old.ocl", "new.ocl", 10);
    IOHelper.printInvDiffResult(diff, Path.of(TARGET_DIR + "finite/diffOneCD"));
    Assertions.assertEquals(4, diff.getDiffWitness().size());
  }
}
