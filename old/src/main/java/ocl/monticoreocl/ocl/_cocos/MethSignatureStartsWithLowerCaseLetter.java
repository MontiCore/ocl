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
package ocl.monticoreocl.ocl._cocos;

import de.se_rwth.commons.logging.Log;
import ocl.monticoreocl.ocl._ast.ASTOCLMethodSignature;
import ocl.monticoreocl.ocl._cocos.OCLASTOCLMethodSignatureCoCo;

import java.util.ArrayList;

public class MethSignatureStartsWithLowerCaseLetter implements OCLASTOCLMethodSignatureCoCo {

	@Override
	public void check(ASTOCLMethodSignature astMethSig){
		String methodName = astMethSig.getMethodName().getPart(1);
		if (!Character.isLowerCase(methodName.charAt(0))) {
			Log.error(String.format("0xOCL05 Method '%s' must start in lower-case.", astMethSig.getMethodName()),
					astMethSig.get_SourcePositionStart());
		}
	}
}
