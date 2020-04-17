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

import de.monticore.symboltable.types.CommonJFieldSymbol;
import de.monticore.umlcd4a.symboltable.references.CDTypeSymbolReference;

import javax.measure.unit.Unit;
import java.util.Optional;

public class OCLVariableDeclarationSymbol extends CommonJFieldSymbol<CDTypeSymbolReference> {

	public static final OCLVariableDeclarationKind KIND = new OCLVariableDeclarationKind();

	protected String varName;
	protected CDTypeSymbolReference typeReference;
	protected String varTypeName;
	protected Optional<Unit<?>> unit;

	public OCLVariableDeclarationSymbol(String varName, CDTypeSymbolReference typeReference) {
		super(varName, KIND, typeReference);
		this.varName = varName;
		this.typeReference = typeReference;
		this.varTypeName = "";
		if (typeReference!=null) {
			this.varTypeName = typeReference.getName();
		}
		this.unit = Optional.empty();
	}

	public String getExtendedName() {
		return "OCL field " + getName();
	}

	@Override
	public String toString() {
		return  OCLVariableDeclarationSymbol.class.getSimpleName() + " " + getName();
	}

	public void setName(String varName){
		this.varName = varName;
	}

	@Override
	public String getName(){
		return varName;
	}

	public Optional<Unit<?>> getUnit(){
		return unit;
	}

	public void setUnit(Unit<?> unit){
		this.unit = Optional.of(unit);
	}

	@Override
	public void setType(CDTypeSymbolReference typeReference){
		this.typeReference = typeReference;
	}

	@Override
	public CDTypeSymbolReference getType(){
		return typeReference;
	}

	public String getVarTypeName(){
		return varTypeName;
	}
}
