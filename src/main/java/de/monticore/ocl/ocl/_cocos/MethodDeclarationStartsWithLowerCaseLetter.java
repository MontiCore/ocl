/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl._cocos;

import de.monticore.expressions.oclexpressions._ast.ASTOCLMethodDeclaration;
import de.monticore.expressions.oclexpressions._cocos.OCLExpressionsASTOCLMethodDeclarationCoCo;
import de.se_rwth.commons.logging.Log;

public class MethodDeclarationStartsWithLowerCaseLetter implements OCLExpressionsASTOCLMethodDeclarationCoCo {

	@Override 
	public void check(ASTOCLMethodDeclaration astMethodDeclaration){
		String methName = astMethodDeclaration.getName();
		boolean startsWithUpperCase = Character.isUpperCase(methName.charAt(0));

		if (startsWithUpperCase) {
			Log.error(
					String.format("0xOCL04 method declaration name '%s' must start with a capital letter.", methName),
					astMethodDeclaration.get_SourcePositionStart());
		}
	}
}
