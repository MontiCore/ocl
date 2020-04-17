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
import ocl.monticoreocl.ocl._ast.ASTOCLFile;

import ocl.monticoreocl.ocl._cocos.OCLASTOCLFileCoCo;

public class FileNameStartsWithLowerCaseLetter implements OCLASTOCLFileCoCo {

	@Override 
	public void check(ASTOCLFile astFile){
		String fileName = astFile.isPresentFileName() ? astFile.getFileName() : "oclFile";
		boolean startsWithUpperCase = Character.isUpperCase(fileName.charAt(0));

		if (startsWithUpperCase) {
			// Issue warning...
			Log.warn(
					String.format("0xOCL02 file name '%s' should not start with a capital letter.", fileName),
					astFile.get_SourcePositionStart());
		}
	}
}
