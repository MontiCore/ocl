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



import ocl.monticoreocl.ocl._ast.ASTOCLParameterDeclaration;
import ocl.monticoreocl.ocl._cocos.OCLASTOCLParameterDeclarationCoCo;

public class ParameterDeclarationNameStartsWithLowerCaseLetter implements OCLASTOCLParameterDeclarationCoCo {

	@Override
	public void check(ASTOCLParameterDeclaration astParameterDeclaration){
		String parameterName = astParameterDeclaration.getName();
		boolean startsWithUpperCase = Character.isUpperCase(parameterName.charAt(0));

		if (startsWithUpperCase) {
			Log.error(String.format("0xOCL06 parameter name '%s' must start in lower-case.", parameterName),
					astParameterDeclaration.get_SourcePositionStart());
		}
	}
}
