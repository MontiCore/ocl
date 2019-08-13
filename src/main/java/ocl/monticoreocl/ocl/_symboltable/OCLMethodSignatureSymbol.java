/* (c) https://github.com/MontiCore/monticore */
package ocl.monticoreocl.ocl._symboltable;

import static de.monticore.symboltable.Symbols.sortSymbolsByPosition;

import java.util.Collection;
import java.util.Optional;

import de.monticore.symboltable.CommonScopeSpanningSymbol;
import de.monticore.types.types._ast.ASTReturnType;

public class OCLMethodSignatureSymbol extends CommonScopeSpanningSymbol {

	public static final OCLMethodSignatureKind KIND = OCLMethodSignatureKind.INSTANCE;

	protected ASTReturnType returnType;
	protected String className;
	protected String methodSignatureName;

	public OCLMethodSignatureSymbol(String name) {
		super(name, KIND);
	}

	public void setMethodSignatureName(String methodSignatureName){
		this.methodSignatureName = methodSignatureName; 
	}

	public String getmethodSignatureName(){
		return methodSignatureName;
	}

	public void setReturnType(ASTReturnType returnType){
		this.returnType = returnType;
	}

	public ASTReturnType getReturnType(){
		return returnType;	 
	}

	public void setClassName(String className){
		this.className = className;
	}

	public String getClassName(){
		return className; 
	}

	public Optional<OCLThrowsClauseSymbol> getOCLThrowsClause(String name) {
		return getSpannedScope().resolveLocally(name,OCLThrowsClauseSymbol.KIND);
	}

	public Collection<OCLThrowsClauseSymbol> getOCLThrowsClause() {
		return sortSymbolsByPosition(getSpannedScope().resolveLocally(OCLThrowsClauseSymbol.KIND));
	}

	
	public Optional<OCLParameterDeclarationSymbol> getOCLParamDecl(String name) {
		return getSpannedScope().resolve(name, OCLParameterDeclarationSymbol.KIND);
	}

	public Collection<OCLParameterDeclarationSymbol> getOCLParamDecl() {
		return sortSymbolsByPosition(getSpannedScope().resolveLocally(OCLParameterDeclarationSymbol.KIND));
	}

	
	public Optional<OCLVariableDeclarationSymbol> getOCLVariableDecl(String name) {
		return getSpannedScope().resolve(name, OCLVariableDeclarationSymbol.KIND);
	}

	public Collection<OCLVariableDeclarationSymbol> getOCLVariableDecl() {
		return sortSymbolsByPosition(getSpannedScope().resolveLocally(OCLVariableDeclarationSymbol.KIND));
	}

	
	public Optional<OCLMethodDeclarationSymbol> getOCLMethodDecl(String name) {
		return getSpannedScope().resolve(name, OCLMethodDeclarationSymbol.KIND);
	}

	public Collection<OCLMethodDeclarationSymbol> getOCLMethodDecl() {
		return sortSymbolsByPosition(getSpannedScope().resolveLocally(OCLMethodDeclarationSymbol.KIND));
	}


	public Collection<OCLPreStatementSymbol> getOCLPreStatement() {
		return getSpannedScope().resolveLocally(OCLPreStatementSymbol.KIND);
	}

	public Optional<OCLPreStatementSymbol> getOCLPreStatement(String name) {
		return getSpannedScope().resolve(name, OCLPreStatementSymbol.KIND);
	}


	public Collection<OCLPostStatementSymbol> getOCLPostStatement() {
		return getSpannedScope().resolveLocally(OCLPreStatementSymbol.KIND);
	}

	public Optional<OCLPostStatementSymbol> getOCLPostStatement(String name) {
		return getSpannedScope().resolve(name, OCLPostStatementSymbol.KIND);
	}

}
