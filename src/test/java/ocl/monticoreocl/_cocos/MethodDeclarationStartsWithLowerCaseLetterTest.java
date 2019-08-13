/* (c) https://github.com/MontiCore/monticore */

package ocl.monticoreocl._cocos;

import java.util.Arrays;
import java.util.Collection;

import ocl.monticoreocl.ocl._cocos.OCLCoCoChecker;
import ocl.monticoreocl.ocl._cocos.OCLCoCos;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import de.se_rwth.commons.SourcePosition;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;

public class MethodDeclarationStartsWithLowerCaseLetterTest extends AbstractOCLTest {

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
	  public void invalidMethodDeclarationNameTest() {
	    String modelName = "example.cocos.invalid.invalidMethodDeclarationName";
	    String errorCode = "0xOCL06";
	    
	    Collection<Finding> expectedErrors = Arrays
	        .asList(
	        Finding.error(errorCode + " method declaration name 'Min' should start with a capital letter.",
	            new SourcePosition(4, 6))
	        );
		  testModelForErrors(PARENT_DIR, modelName, expectedErrors);
	  }

	  @Ignore
	  @Test
	  public void validMethodDeclarationNameTest() {
		  
		  String modelName = "example.cocos.valid.validMethodDeclarationName";
		  testModelNoErrors(PARENT_DIR, modelName);
	  }

}
