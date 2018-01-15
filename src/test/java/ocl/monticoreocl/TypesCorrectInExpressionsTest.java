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

import de.se_rwth.commons.SourcePosition;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
import ocl.monticoreocl.ocl._cocos.OCLCoCoChecker;
import ocl.monticoreocl.ocl._cocos.OCLCoCos;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;

/**
 * Created by Ferdinand Mehlan on 15.01.2018.
 */
public class TypesCorrectInExpressionsTest extends AbstractOCLTest {

    @Override
    protected OCLCoCoChecker getChecker() {
        return OCLCoCos.createChecker();
    }

    @BeforeClass
    public static void init() {
        Log.enableFailQuick(false);
    }

    @Before
    public void setUp() {
        Log.getFindings().clear();
    }

    @Test
    public void invalidConstructorSignatureNameTest() {
        String modelName = "example.cocos.invalid.invalidTypes";
        String errorCode = "0xCET01";

        Collection<Finding> expectedErrors = Arrays
                .asList(
                        Finding.error(errorCode + " left and right type of infix expression do not match: StringReader:<6,8>",
                                new SourcePosition(6,8))
                );
        testModelForErrors(PARENT_DIR, modelName, expectedErrors);
    }



    @Test
    public void validConstructorSignatureNameTest() {

        String modelName = "example.cocos.valid.validTypes";
        testModelNoErrors(PARENT_DIR, modelName);
    }
}
