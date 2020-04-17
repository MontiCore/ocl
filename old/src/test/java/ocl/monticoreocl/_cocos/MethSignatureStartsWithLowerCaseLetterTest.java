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

package ocl.monticoreocl._cocos;

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

public class MethSignatureStartsWithLowerCaseLetterTest extends AbstractOCLTest {

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

	@Test
	public void validMethodSignatureNameTest() {

		String modelName = "example.cocos.valid.validMethSigName";
		testModelNoErrors(PARENT_DIR, modelName);
	}

}
