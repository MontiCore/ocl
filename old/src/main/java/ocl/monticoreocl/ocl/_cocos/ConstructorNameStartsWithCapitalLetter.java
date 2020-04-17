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


import ocl.monticoreocl.ocl._ast.ASTOCLConstructorSignature;
import ocl.monticoreocl.ocl._cocos.OCLASTOCLConstructorSignatureCoCo;

public class ConstructorNameStartsWithCapitalLetter implements OCLASTOCLConstructorSignatureCoCo {

	@Override
	public void check(ASTOCLConstructorSignature astConstructorSig){
		if (Character.isLowerCase(astConstructorSig.getReferenceType().charAt(0))) {
			Log.warn(String.format("0xOCL01 constructor name '%s' after keyword 'new' should not start in lower-case.", astConstructorSig.getReferenceType()),
					astConstructorSig.get_SourcePositionStart());
		}
	}
}
