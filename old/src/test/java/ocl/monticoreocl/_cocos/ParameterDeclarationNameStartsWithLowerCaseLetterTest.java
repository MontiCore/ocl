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

import ocl.monticoreocl.ocl._cocos.OCLCoCoChecker;
import ocl.monticoreocl.ocl._cocos.OCLCoCos;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.se_rwth.commons.SourcePosition;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;

public class ParameterDeclarationNameStartsWithLowerCaseLetterTest extends AbstractOCLTest {

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
	  public void invalidParameterDeclarationNameTest() {
	    String modelName = "example.cocos.invalid.invalidParameterDeclarationName";
	    String errorCode = "0xOCL06";
	    
	    Collection<Finding> expectedErrors = Arrays
	        .asList(
	       Finding.error(errorCode + " parameter name 'Name' must start in lower-case.",
	        	            new SourcePosition(2, 39))  
	        );
		  testModelForErrors(PARENT_DIR, modelName, expectedErrors);
	  }
	  
	  
	  
	  @Test
	  public void validParameterDeclarationNameTest() {
		  
		  String modelName = "example.cocos.valid.validParameterDeclarationName";
		  testModelNoErrors(PARENT_DIR, modelName);
	  }

}
