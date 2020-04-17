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
import org.junit.Test;

import de.se_rwth.commons.SourcePosition;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
import ocl.monticoreocl.ocl._cocos.OCLCoCoChecker;
import ocl.monticoreocl.ocl._cocos.OCLCoCos;


public class ConstructorNameStartsWithCapitalLetterTest extends AbstractOCLTest {

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
	    String modelName = "example.cocos.invalid.invalidConstructorName";
	    String errorCode = "0xOCL01";
	    
	    Collection<Finding> expectedErrors = Arrays
	        .asList(
	        Finding.error(errorCode + " constructor name 'auction' after keyword 'new' should not start in lower-case.",
	            new SourcePosition(2,10))
	        );
		  testModelForErrors(PARENT_DIR, modelName, expectedErrors);
	  }
	  
	  
	  
	  @Test
	  public void validConstructorSignatureNameTest() {
		  
		  String modelName = "example.cocos.valid.validConstructorName";
		  testModelNoErrors(PARENT_DIR, modelName);
	  }

}
