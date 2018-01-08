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

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.se_rwth.commons.SourcePosition;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
//import ocl.monticoreocl.lang.AbstractTest;
import ocl.monticoreocl.ocl._cocos.OCLCoCoChecker;
import ocl.monticoreocl.ocl._cocos.OCLCoCos;

public class MethSignatureStartsWithCapitalLetterTest extends AbstractOCLTest {

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

	@Ignore
	@Test
	public void invalidMethodSignatureNameTest() {
		String modelName = "example.cocos.invalid.invalidMethSigName";
		String errorCode = "0xOCL10";

		Collection<Finding> expectedErrors = Arrays.asList(
				Finding.error(errorCode + " Method 'PersonalMsg' must start in lower-case.", new SourcePosition(2, 10)));
		testModelForErrors(PARENT_DIR, modelName, expectedErrors);
	}

	@Ignore
	@Test
	public void validMethodSignatureNameTest() {

		String modelName = "example.cocos.valid.validMethSigName";
		testModelNoErrors(PARENT_DIR, modelName);
	}

}
