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
import ocl.monticoreocl.ocl._ast.ASTOCLInvariant;

import ocl.monticoreocl.ocl._cocos.OCLASTOCLInvariantCoCo;

public class InvariantNameStartsWithCapitalLetter implements OCLASTOCLInvariantCoCo {

	@Override
	public void check(ASTOCLInvariant astInv){
		if (astInv.isPresentName() && Character.isLowerCase(astInv.getName().charAt(0))) {
			Log.warn(
					String.format("0xOCL03 invariant name '%s' should start with a capital letter.",
					astInv.getName()),
					astInv.get_SourcePositionStart());
		}

	}
}
