/*
 *  * (c) https://github.com/MontiCore/monticore
 *  *
 *  * The license generally applicable for this project
 *  * can be found under https://github.com/MontiCore/monticore.
 */

/**
 *
 * (c) https://github.com/MontiCore/monticore
 *
 * The license generally applicable for this project
 * can be found under https://github.com/MontiCore/monticore.
 */
/**
 *
 * /* (c) https://github.com/MontiCore/monticore */
 *
 * The license generally applicable for this project
 * can be found under https://github.com/MontiCore/monticore.
 */
/* (c) https://github.com/MontiCore/monticore */
package ocl.cli;


import de.monticore.commonexpressions._ast.ASTEquivalentExpression;
import de.monticore.expressionsbasis._ast.ASTExpression;
import de.se_rwth.commons.logging.Log;
import ocl.cli.OCLCDTool;
import ocl.monticoreocl.ocl._parser.OCLParser;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
 import java.nio.file.Paths;

public class OCLCDToolTest {

    // ToDo these tests seem to work, but not on travis
    @Test
    public void cdToolTest() {

        String parentpath = Paths.get("src/test/resources").toAbsolutePath().toString();
        String oclModel = "example.typeInferringModels.cdToolFile";
        //String oclModel = "example.OCLArtifactModel";
        String[] args = new String[]{"-path", parentpath, "-ocl", oclModel};
        try {
            OCLCDTool.main(args);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        Assert.assertEquals(0, Log.getErrorCount());

        args = new String[]{"-path", parentpath, "-ocl", oclModel, "-parseOnly"};
        try {
            OCLCDTool.main(args);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        Assert.assertEquals(0, Log.getErrorCount());
    }

    @Test
    public void cdTool2Test() {
        String oclModel =
                "\"package example.typeInferringModels;\n" +
                "ocl cDToolFile {\n" +
                "  context Auction a inv Test:\n" +
                "      a.participants.size > 0s ;\n" +
                "      a.participants.sze > 0 ;\n" +
                "      3 m < 7 m/s ;\n" +
                "}\"";
        String cdModel =
                "\"package example.CDs;\n" +
                        "classdiagram auctionCD {\n" +
                        "    public class Auction {}\n" +
                        "    public class Person {}\n" +

                        "  class Class;\n" +
                        "  class Object;\n" +
                        "  class Collection {\n" +
                        "    boolean containsAll(Collection c);\n" +
                        "    boolean contains(Collection c);\n" +
                        "    int size();\n" +
                        "    boolean isEmpty();\n" +
                        "    Collection addAll(Collection c);\n" +
                        "  }\n" +
                        "  class List extends Collection {\n" +
                        "      boolean nonEmpty();\n" +
                        "      List addAll(List c);\n" +
                        "      Set asSet();\n" +
                        "      List add(Object o);\n" +
                        "  }\n" +
                        "  class Set extends Collection {\n" +
                        "      Set addAll(Set c);\n" +
                        "      List asList();\n" +
                        "      Set add(Object o);\n" +
                        "  }\n" +
                        "  class Optional {\n" +
                        "    boolean isAbsent();\n" +
                        "    boolean isPresent();\n" +
                        "  }\n" +

                        "  interface Number extends Amount;\n" +
                        "  class Integer implements Number;\n" +
                        "  class Double implements Number;\n" +
                        "  class Float implements Number;\n" +
                        "  class Boolean;\n" +
                        "  class Character;\n" +

                        "  interface Amount;\n" +
                        "  class Duration implements Amount;\n" +
                        "  class Length implements Amount;\n" +
                        "  class Acceleration implements Amount;\n" +
                        "  class Velocity implements Amount;\n" +

                        "    association participants [*] Auction (auctions) <-> (bidder) Person [*];\n" +
                        "}\"";
        String[] args = new String[]{"-ocl", oclModel, "-cd", cdModel, "-logErrTo", "target/err.log"};
        try {
            OCLCDTool.main(args);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        Assert.assertEquals(4, Log.getErrorCount());

        args = new String[]{"-ocl", oclModel, "-cd", cdModel, "-parseOnly"};
        try {
            OCLCDTool.main(args);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        Assert.assertEquals(0, Log.getErrorCount());

    }

    @Test
    public void cdTool3Test() {

        String parentpath = Paths.get("src/test/resources").toAbsolutePath().toString();
        String oclModel = "example.TGs_models.ArtifactModel_OCL";
        String[] args = new String[]{"-path", parentpath, "-ocl", oclModel};
        try {
            OCLCDTool.main(args);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        Assert.assertEquals(0, Log.getErrorCount());
    }

    @Test
    public void cdTool4Test() throws IOException {
        String parentpath = Paths.get("src/test/resources").toAbsolutePath().toString();
        String oclModel = "example.TGs_models.TG_Test";
        String[] args = new String[]{"-path", parentpath, "-ocl", oclModel};
        try {
            OCLCDTool.main(args);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        Assert.assertEquals(0, Log.getErrorCount());
    }

    @Ignore("This test fails here, but it works in the browser -> the browser cannot load models via model loader so the CD models must be pre-loaded")
    @Test
    public void cheerpJTest() {
        String[] args = new String[] {"-path", "", "-ocl", "example.ocl.Demo", "-preloadCD"};
        try {
            OCLCDTool.main(args);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
    }

    @Test
    public void oclMvWPhdTest() {

        String parentpath = Paths.get("src/test/resources/example/MvWPhd_models").toAbsolutePath().toString();
        String oclModel = "EmbeddedMontiArcOCL";
        String[] args = new String[]{"-path", parentpath, "-ocl", oclModel};
        try {
            OCLCDTool.main(args);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        Assert.assertEquals(0, Log.getErrorCount());
    }

    @Test
    public void oclTest1() {

        String parentpath = Paths.get("src/test/resources/example/MvWPhd_models").toAbsolutePath().toString();
        String oclModel = "Test1";
        String[] args = new String[]{"-path", parentpath, "-ocl", oclModel};
        try {
            OCLCDTool.main(args);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        Assert.assertEquals(0, Log.getErrorCount());
    }

    @Test
    public void oclTest6() {

        String parentpath = Paths.get("src/test/resources/example/MvWPhd_models").toAbsolutePath().toString();
        String oclModel = "Test6";
        String[] args = new String[]{"-path", parentpath, "-ocl", oclModel};
        try {
            OCLCDTool.main(args);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        Assert.assertEquals(0, Log.getErrorCount());
    }

}
