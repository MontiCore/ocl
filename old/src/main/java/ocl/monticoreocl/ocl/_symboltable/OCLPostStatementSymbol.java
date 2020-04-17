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
package ocl.monticoreocl.ocl._symboltable;

import de.monticore.symboltable.CommonSymbol;

public class OCLPostStatementSymbol extends CommonSymbol {

	public static final OCLPostStatementKind KIND = OCLPostStatementKind.INSTANCE;

	public OCLPostStatementSymbol(String name) {
		super(name, KIND);
	}
}
