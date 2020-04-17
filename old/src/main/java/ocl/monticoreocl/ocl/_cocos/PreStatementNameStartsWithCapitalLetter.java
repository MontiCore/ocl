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
import ocl.monticoreocl.ocl._ast.ASTOCLPreStatement;
import ocl.monticoreocl.ocl._cocos.OCLASTOCLPreStatementCoCo;

public class PreStatementNameStartsWithCapitalLetter implements OCLASTOCLPreStatementCoCo {

	@Override
	public void check(ASTOCLPreStatement astPreStatement){
		if (astPreStatement.isPresentName() && Character.isLowerCase(astPreStatement.getName().charAt(0))) {
			Log.error(String.format("0xOCL07 pre condition name '%s' must start in upper-case.", astPreStatement.getName()),
					astPreStatement.get_SourcePositionStart());
		}
	}
}
