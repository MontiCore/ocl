/* (c) https://github.com/MontiCore/monticore */
package ocl.monticoreocl.ocl._symboltable;

import de.monticore.symboltable.CommonSymbol;

public class OCLThrowsClauseSymbol extends CommonSymbol {

	public static final OCLThrowsClauseKind KIND = OCLThrowsClauseKind.INSTANCE;

	public OCLThrowsClauseSymbol(String name) {
		super(name, KIND);
	}
}
