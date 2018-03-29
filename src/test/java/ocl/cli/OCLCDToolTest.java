/**
 * ******************************************************************************
 *  MontiCAR Modeling Family, www.se-rwth.de
 *  Copyright (c) 2017, Software Engineering Group at RWTH Aachen,
 *  All rights reserved.
 *
 *  This project is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version.
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * *******************************************************************************
 */
package ocl.cli;


import de.se_rwth.commons.logging.Log;
import ocl.cli.OCLCDTool;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;


public class OCLCDToolTest {

    @Ignore
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

    @Ignore
    @Test
    public void cdTool2Test() {
        String oclModel =
                "\"package example.typeInferringModels;\n" +
                "ocl cDToolFile {\n" +
                "  context Auction a inv Test:\n" +
                "      a.participants.size > 0s ;\n" +
                "      a.participants.sze > 0 ;\n" +
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

                        "    association participants [*] Auction (auctions) <-> (bidder) Person [*];\n" +
                        "}\"";
        String[] args = new String[]{"-ocl", oclModel, "-cd", cdModel, "-logErrTo", "target/err.log"};
        try {
            OCLCDTool.main(args);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        Assert.assertEquals(3, Log.getErrorCount());

        args = new String[]{"-ocl", oclModel, "-cd", cdModel, "-parseOnly"};
        try {
            OCLCDTool.main(args);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        Assert.assertEquals(0, Log.getErrorCount());

    }

    @Ignore
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

    @Ignore
    @Test
    public void cdTool4Test() {

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

}
