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

import static de.monticore.symboltable.Symbols.sortSymbolsByPosition;

import java.util.Collection;
import java.util.Optional;

import de.monticore.symboltable.CommonScopeSpanningSymbol;
import de.monticore.types.types._ast.ASTReturnType;

public class OCLMethodDeclarationSymbol extends CommonScopeSpanningSymbol {

	public static final OCLMethodDeclarationKind KIND = OCLMethodDeclarationKind.INSTANCE;

	public OCLMethodDeclarationSymbol(String name) {
		super(name, KIND);
	}

	protected ASTReturnType returnType;

	public void setReturnType(ASTReturnType returnType){
		this.returnType = returnType; 

	}

	public ASTReturnType getReturnType(){
		return returnType;
	}

	public Collection<OCLMethodDeclarationSymbol> getOCLMethodDeclaration() {
		return sortSymbolsByPosition(getSpannedScope().resolveLocally(OCLMethodDeclarationSymbol.KIND));
	}
	
	public Optional<OCLParameterDeclarationSymbol> getOCLParamDecl(String name) {
		return getSpannedScope().resolve(name, OCLParameterDeclarationSymbol.KIND);
	}

	public Collection<OCLParameterDeclarationSymbol> getOCLParamDecl() {
		return sortSymbolsByPosition(getSpannedScope().resolveLocally(OCLParameterDeclarationSymbol.KIND));
	}
}
