package de.monticore.ocl.tool;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OCLToolTest {

  private String methodName;
  private String cd;

  private String od;
  private String oclv1;

  @BeforeEach
  public void setup() {
    String path = "src/test/resources/tool/";
    methodName = "Tool.run1";
    cd = path + "Tool.cd";
    oclv1 = path + "Tool.ocl";
    od = path + "Example.od";
  }

  @Test
  public void testPossibleCommand() {
    String[] witness = new String[] {"--w", "-cd", cd, "-ocl", oclv1};
    String[] diff = new String[] {"--diff", "-cd", cd, "-ocl", oclv1, "-nocl", oclv1};
    String[] cdDiff = new String[] {"--diff", "-cd", cd, "-ncd", cd, "-ocl", oclv1, "-nocl", oclv1};
    String[] opWitness = new String[] {"--ow", "-cd", cd, "-ocl", oclv1};
    String[] opWitness2 = new String[] {"--ow", "-cd", cd, "-ocl", oclv1, "-mn", methodName};
    String[] opdiff =
        new String[] {"--odiff", "-cd", cd, "-ocl", oclv1, "-nocl", oclv1, "-mn", methodName};
    String[] witnessWithOdExamples = new String[] {"--w", "-cd", cd, "-ocl", oclv1, "-od", od};
    String[] diffWithOdExamples =
        new String[] {"--diff", "-cd", cd, "-ocl", oclv1, "-nocl", oclv1, "-od", od, "-nod", od};

    OCLTool oclTool = new OCLTool();

    oclTool.run(witness);
    oclTool.run(diff);
    oclTool.run(cdDiff);
    oclTool.run(opWitness);
    oclTool.run(opdiff);
    oclTool.run(opWitness2);
    oclTool.run(witnessWithOdExamples);
    oclTool.run(diffWithOdExamples);
  }
}
