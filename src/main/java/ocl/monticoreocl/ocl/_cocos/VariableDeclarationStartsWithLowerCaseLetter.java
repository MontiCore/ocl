/* (c) https://github.com/MontiCore/monticore */
package ocl.monticoreocl.ocl._cocos;

import de.se_rwth.commons.logging.Log;
import ocl.monticoreocl.ocl._ast.ASTOCLVariableDeclaration;
import ocl.monticoreocl.ocl._cocos.OCLASTOCLVariableDeclarationCoCo;

public class VariableDeclarationStartsWithLowerCaseLetter implements OCLASTOCLVariableDeclarationCoCo {

	@Override 
	public void check(ASTOCLVariableDeclaration astVariableDeclaration){
		String varName = astVariableDeclaration.getName();
		boolean startsWithUpperCase = Character.isUpperCase(varName.charAt(0));

		if (startsWithUpperCase) {
			Log.error(
					String.format("0xOCL09 variable declaration name '%s' must start with a lower-case letter.", varName),
					astVariableDeclaration.get_SourcePositionStart());
		}
	}
}
