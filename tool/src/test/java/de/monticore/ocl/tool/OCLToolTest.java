package de.monticore.ocl.tool;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
  public void testPrettyPrint() throws IOException {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    System.setOut(new PrintStream(out));
    OCLTool.main(new String[] {
        "-i", oclv1,
        "-pp"
    });
    String printed = out.toString().trim();
    assertNotNull(printed);
    Optional<ASTOCLCompilationUnit> astOpt = OCLMill.parser().parse_String(printed);
    assertEquals(0, Log.getFindingsCount());
    assertTrue(astOpt.isPresent());
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
    //  oclTool.run(opWitness); todo enable when  implement op constrains
    // oclTool.run(opdiff);
    // oclTool.run(opWitness2);
    oclTool.run(witnessWithOdExamples);
    oclTool.run(diffWithOdExamples);
  }
}
