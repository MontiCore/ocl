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
package ocl.monticoreocl;


import de.se_rwth.commons.logging.Log;
import ocl.cli.OCLCDTool;
import org.junit.Assert;
import org.junit.Test;

import java.nio.file.Path;
import java.nio.file.Paths;


public class OCLCDToolTest {

    @Test
    public void cdToolTest() {

        String parentpath = Paths.get("src/test/resources").toAbsolutePath().toString();
        String oclModel = "example.typeInferringModels.CDToolFile";
        //String oclModel = "example.OCLArtifactModel";
        String[] args = new String[]{"-path", parentpath, "-ocl", oclModel};
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
                "}\"";
        String cdModel =
                "\"package example.CDs;\n" +
                        "classdiagram AuctionCD {\n" +
                        "    public class Auction {}\n" +
                        "    public class Person {}\n" +
                        "    public class Collection {\n" +
                        "      int size();\n" +
                        "    }\n" +
                        "    public class Set extends Collection {}\n  " +
                        "    public class Length {}\n" +
                        "    public class Class {}\n" +
                        "    interface Number;\n" +
                        "    class Integer implements Number {}\n" +
                        "    association participants [*] Auction (auctions) <-> (bidder) Person [*];\n" +
                        "}\"";
        String[] args = new String[]{"-ocl", oclModel, "-cd", cdModel};
        try {
            OCLCDTool.main(args);
        } catch (Exception e) {
            Log.error(e.getMessage());
        }
        Assert.assertEquals(4, Log.getErrorCount());

    }

}
