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
import de.monticore.types.types._ast.ASTType;

public class OCLParameterDeclarationSymbol extends CommonSymbol{

	public static final OCLParameterDeclarationKind KIND = OCLParameterDeclarationKind.INSTANCE;

	protected ASTType type;
	protected String className;
	protected String name;

	public OCLParameterDeclarationSymbol(String name) {
		super(name, KIND);
	}

	public void setType(ASTType type){
		this.type = type;

	}

	public ASTType getType(){
		return type;
	}

	public void setName(String name){
		this.name = name;

	}

	public String getName(){
		return name;
	}

	public void setClassName(String className){
		this.className=className;

	}

	public String getClassName(){
		return className;
	}
}
