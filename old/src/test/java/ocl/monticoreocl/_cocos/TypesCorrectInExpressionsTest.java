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

import de.se_rwth.commons.SourcePosition;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
import ocl.monticoreocl._cocos.AbstractOCLTest;
import ocl.monticoreocl.ocl._cocos.OCLCoCoChecker;
import ocl.monticoreocl.ocl._cocos.OCLCoCos;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
    public void invalidTypesTest() {
        String modelName = "example.cocos.invalid.invalidTypes";
        String errorCode = "0xCET01";

        Collection<Finding> expectedErrors = Arrays
                .asList( // switched order to get better error codes
                        Finding.error("0xCET03 Units mismatch on infix expression at invalidTypes.ocl:<6,8> left:  right: m",
                                new SourcePosition(6,8)),
                        Finding.error(errorCode + " Types mismatch on infix expression at invalidTypes.ocl:<8,11> left: String right: Set<String>",
                                new SourcePosition(8,11))
                );
        testModelForErrors(PARENT_DIR, modelName, expectedErrors);
    }


    @Ignore("destroyed test due to removing generics")
    @Test
    public void validTypesTest() {

        String modelName = "example.cocos.valid.validTypes";
        testModelNoErrors(PARENT_DIR, modelName);
    }
}
