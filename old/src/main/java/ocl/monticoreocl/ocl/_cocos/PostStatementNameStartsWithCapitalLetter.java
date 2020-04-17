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
import ocl.monticoreocl.ocl._ast.ASTOCLPostStatement;
import ocl.monticoreocl.ocl._cocos.OCLASTOCLPostStatementCoCo;

public class PostStatementNameStartsWithCapitalLetter implements OCLASTOCLPostStatementCoCo {

	@Override
	public void check(ASTOCLPostStatement astPostStatement){
		if (astPostStatement.isPresentName() && Character.isLowerCase(astPostStatement.getName().charAt(0))) {
			Log.error(String.format("0xOCL07 post condition name '%s' must start in upper-case.", astPostStatement.getName()),
					astPostStatement.get_SourcePositionStart());
		}
	}
}
