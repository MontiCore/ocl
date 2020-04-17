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

public class OCLInvariantSymbol extends CommonScopeSpanningSymbol {

	public static final OCLInvariantKind KIND = OCLInvariantKind.INSTANCE;

	protected String className;
	protected String classObject;
	protected boolean context = false;
	protected boolean importing = false;

	public OCLInvariantSymbol(String name) {
		super(name, KIND);
	}

	public void setContext(boolean context){
		this.context = context;
	}

	public void setImport(boolean importing){
		this.importing = importing;

	}

	public boolean getContext(){
		return context;
	}

	public boolean getImport(){
		return importing;
	}

	public void setClassN(String className){
		this.className = className;
	}

	public String getClassN(){
		return className;
	}

	public void setClassO(String classObject){
		this.classObject=classObject;

	}

	public String getClassO(){
		return classObject;
	}

	public Collection<OCLVariableDeclarationSymbol> getOCLVariableDecl() {
		return getSpannedScope().resolveLocally(OCLVariableDeclarationSymbol.KIND);
	}

	public Optional<OCLVariableDeclarationSymbol> getOCLVariableDecl(String name) {
		return getSpannedScope().resolve(name, OCLVariableDeclarationSymbol.KIND);
	}


	public Collection<OCLMethodDeclarationSymbol> getOCLMethodDecl() {
		return getSpannedScope().resolveLocally(OCLMethodDeclarationSymbol.KIND);
	}

	public Optional<OCLMethodDeclarationSymbol> getOCLMethodDecl(String name) {
		return getSpannedScope().resolve(name, OCLMethodDeclarationSymbol.KIND);
	}

	public Collection<OCLParameterDeclarationSymbol> getOCLParamDecl() {
		return sortSymbolsByPosition(getSpannedScope().resolveLocally(OCLParameterDeclarationSymbol.KIND));
	}

	public Optional<OCLParameterDeclarationSymbol> getOCLParamDecl(String name) {
		return getSpannedScope().resolve(name, OCLParameterDeclarationSymbol.KIND);
	} 
}
