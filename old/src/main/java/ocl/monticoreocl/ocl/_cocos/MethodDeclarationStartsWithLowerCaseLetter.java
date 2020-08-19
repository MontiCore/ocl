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

import ocl.monticoreocl.ocl._ast.ASTOCLMethodDeclaration;

import ocl.monticoreocl.ocl._cocos.OCLASTOCLMethodDeclarationCoCo;

public class MethodDeclarationStartsWithLowerCaseLetter implements OCLASTOCLMethodDeclarationCoCo {

	@Override 
	public void check(ASTOCLMethodDeclaration astMethodDeclaration){
		String methName = astMethodDeclaration.getName();
		boolean startsWithUpperCase = Character.isUpperCase(methName.charAt(0));

		if (startsWithUpperCase) {
			Log.error(
					String.format("0xOCL04 method declaration name '%s' must start with a capital letter.", methName));
		}
	}
}
