/* (c) https://github.com/MontiCore/monticore */
package ocl.cli;


import de.monticore.prettyprint.IndentPrinter;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDCompilationUnit;
import de.monticore.umlcd4a.cd4analysis._parser.CD4AnalysisParser;
import de.se_rwth.commons.logging.Log;
import ocl.monticoreocl.ocl._visitors.CD4A2PlantUMLVisitor;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;;

public class CD4A2PlantUmlTest {

    @Test
    public void cd4a2plantUmlTest() {
        IndentPrinter printer = new IndentPrinter();
        CD4A2PlantUMLVisitor cdVisitor = new CD4A2PlantUMLVisitor(printer);
        CD4AnalysisParser parser = new CD4AnalysisParser();
        try {
            ASTCDCompilationUnit astCD = parser.parse("src/test/resources/example/CDs/PlantUMLTest.cd").orElse(null);
            assertNotNull(astCD);

            String expected = "@startuml\n" +
                    "class A\n" +
                    "class B extends A\n" +
                    "class C\n" +
                    "class D extends B\n" +
                    "A \"*\" <--> \"0..1\" B\n" +
                    "C \"0..1\" <--> \"*\" D\n" +
                    "A <-- \"*\" D\n" +
                    "@enduml";
            assertEquals(expected, cdVisitor.print2PlantUML(astCD));
        } catch (IOException e) {
            Log.error(e.getMessage());
        }
    }

    @Test
    public void cd4a2plantUml2Test() {
        String cdString = "classdiagram plantUMLTest {\n" +
                "\n" +
                "  public class A ;\n" +
                "  public class B extends A{int b;}\n" +
                "  public interface C {}\n" +
                "  public class D implements C,C{int b;}\n" +
                "\n" +
                "  association blub [*] A (left) <->  (right) B [0..1];\n" +
                "  association [0..1] C <-> D [*];\n" +
                "  association A <- D [*];\n" +
                "\n" +
                "}";
        String[] args = new String[]{"-printSrc", cdString, "-printTgt", "target/plantuml.txt", "-showAttributes", "-showNoCardinality", ""};
        try {
            OCLCDTool.main(args);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        File plantFile = Paths.get("target/plantuml.txt").toFile();
        assertTrue(plantFile.exists());
    }
}
