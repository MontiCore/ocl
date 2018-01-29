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


import de.monticore.prettyprint.IndentPrinter;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDCompilationUnit;
import de.monticore.umlcd4a.cd4analysis._parser.CD4AnalysisParser;
import de.se_rwth.commons.logging.Log;
import ocl.monticoreocl.ocl._visitors.CD4A2PlantUMLVisitor;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;;

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
                    "class B\n" +
                    "class C\n" +
                    "class D\n" +
                    "A \"*\" <--> \"0..1\" B\n" +
                    "C \"0..1\" <--> \"*\" D\n" +
                    "A  <-- \"*\" D\n" +
                    "@enduml";
            assertEquals(expected, cdVisitor.print2PlantUML(astCD));
        } catch (IOException e) {
            Log.error(e.getMessage());
        }
    }
}
