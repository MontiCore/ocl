package de.monticore.ocl.tool;

import de.monticore.ocl2smt.ocldiff.OCLDiffGenerator;
import java.io.File;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OCLToolTest {

  private boolean partial;
  private String methodName;
  private String cd;

  private String cd2;

  private Path output;

  private String oclv1;

  @BeforeEach
  public void setup() {
    String path = "src/test/resources/tool/";
    partial = true;
    methodName = "Tool.run1";
    cd = path + "Tool.cd";
    oclv1 = path + "Tool.ocl";
    cd2 = path + "CD.cd";
  }

  @Test
  public void testInvariantWitness() {
    String[] witness = new String[] {"--w", "-cd", cd, "-ocl", oclv1};
    String[] diff = new String[] {"--diff", "-cd", cd, "-ocl", oclv1, "-nocl", oclv1};
    String[] cdDiff = new String[] {"--diff", "-cd", cd, "-ncd", cd, "-ocl", oclv1, "-nocl", oclv1};
    String[] opWitness = new String[] {"--ow", "-cd", cd, "-ocl", oclv1};
    String[] opWitness2 = new String[] {"--ow", "-cd", cd, "-ocl", oclv1, "-mn", methodName};
    String[] opdiff =
        new String[] {"--odiff", "-cd", cd, "-ocl", oclv1, "-nocl", oclv1, "-mn", methodName};

    OCLTool oclTool = new OCLTool();

    oclTool.run(witness);
    oclTool.run(diff);
    oclTool.run(cdDiff);
    oclTool.run(opWitness);
    oclTool.run(opdiff);
    oclTool.run(opWitness2);
  }

  @Test
  public void test() {
    OCLDiffGenerator.oclDiff(
        new File(cd2),
        new File(cd2),
        Set.of(new File(oclv1)),
        Set.of(new File(oclv1)),
        false,
        Path.of("output"));
  }
}
