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

public class InvariantNameStartsWithCapitalLetterTest extends AbstractOCLTest{

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
	  public void invalidInvariantNameTest() {
	    String modelName = "example.cocos.invalid.invalidInvariantName";
	    String errorCode = "0xOCL03";
	    
	    Collection<Finding> expectedErrors = Arrays
	        .asList(
	        Finding.error(errorCode + " invariant name 'nameInv' should start with a capital letter.",
	            new SourcePosition(2, 2))
	        );
		  testModelForErrors(PARENT_DIR, modelName, expectedErrors);
	  }
	  
	  
	  
	  @Test
	  public void validInvariantNameTest() {
		  
		  String modelName = "example.cocos.valid.validInvariantName";
		  testModelNoErrors(PARENT_DIR, modelName);
	  }
}
