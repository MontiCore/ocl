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

public class PreStatementStartsWithCapitalLetterTest extends AbstractOCLTest {

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
	  public void invalidMethodSignatureNameTest() {
	    String modelName = "example.cocos.invalid.invalidPreStatementName";
	    String errorCode = "0xOCL07";
	    
	    Collection<Finding> expectedErrors = Arrays
	        .asList(
	        Finding.error(errorCode + " " + "pre condition name 'ias1' must start in upper-case.",
	            new SourcePosition(3, 2)),
	        Finding.error(errorCode + " " + "post condition name 'ias2' must start in upper-case.",
	            new SourcePosition(4, 2))
	        );
		  testModelForErrors(PARENT_DIR, modelName, expectedErrors);
	  }
	  
	  
	  
	  @Test
	  public void validMethodSignatureNameTest() {
		  
		  String modelName = "example.cocos.valid.validPreStatementName";
		  testModelNoErrors(PARENT_DIR, modelName);
	  }

}
