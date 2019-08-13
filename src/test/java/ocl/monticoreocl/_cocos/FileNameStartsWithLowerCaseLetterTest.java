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

public class FileNameStartsWithLowerCaseLetterTest extends AbstractOCLTest { 
	
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
	  public void invalidFileNameTest() {
	    String modelName = "example.cocos.invalid.invalidFileName";
	    String errorCode = "0xOCL02";
	    
	    Collection<Finding> expectedErrors = Arrays
	        .asList(
	        Finding.error(errorCode + " file name 'Association1' should not start with a capital letter.",
	            new SourcePosition(1, 0))
	        );
	    testModelForErrors(PARENT_DIR, modelName, expectedErrors);
	  }
	  
	  @Test
	  public void validFileNameTest() {
		  
		  String modelName = "example.cocos.valid.validFileName";
		  testModelNoErrors(PARENT_DIR, modelName);
	  }
}
