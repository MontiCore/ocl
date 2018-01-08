/**
 * ******************************************************************************
 *  MontiCAR Modeling Family, www.se-rwth.de
 *  Copyright (c) 2017, Software Engineering Group at RWTH Aachen,
 *  All rights reserved.
 *
 *  This project is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version.
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * *******************************************************************************
 */
package ocl.monticoreocl.ocl._symboltable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import de.monticore.ast.ASTNode;
import de.monticore.oclexpressions._ast.ASTInExpr;
import de.monticore.symboltable.*;
import de.monticore.symboltable.types.references.ActualTypeArgument;
import de.monticore.types.TypesPrinter;
import de.monticore.types.types._ast.*;
import de.monticore.umlcd4a.symboltable.CDTypeSymbol;
import de.monticore.umlcd4a.symboltable.CDTypes;
import de.monticore.umlcd4a.symboltable.references.CDTypeSymbolReference;
import de.se_rwth.commons.Joiners;
import ocl.monticoreocl.ocl._ast.*;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.logging.Log;
import ocl.monticoreocl.ocl._visitors.OCLExpressionTypeInferingVisitor;
import ocl.monticoreocl.ocl._visitors.OCLTypeCheckingVisitor;

public class OCLSymbolTableCreator extends OCLSymbolTableCreatorTOP {

	public OCLSymbolTableCreator(final ResolvingConfiguration resolverConfig, final MutableScope enclosingScope) {
		super(resolverConfig, enclosingScope);
	}

	@Override
	public void visit(final ASTCompilationUnit compilationUnit) {
		Log.debug("Building Symboltable for OCL: " + compilationUnit.getOCLFile().getFileName(), OCLSymbolTableCreator.class.getSimpleName());
		String compilationUnitPackage = Names.getQualifiedName(compilationUnit.getPackage());

		// imports
		List<ImportStatement> imports = new ArrayList<>();
		for (ASTImportStatement astImportStatement : compilationUnit.getImportStatements()) {
			String qualifiedImport = Names.getQualifiedName(astImportStatement.getImportList());
			ImportStatement importStatement = new ImportStatement(qualifiedImport, astImportStatement.isStar());
			imports.add(importStatement);
		}

		ArtifactScope artifactScope = new ArtifactScope(Optional.empty(), compilationUnitPackage, imports);
		putOnStack(artifactScope);

	}

	@Override
	public void endVisit(final ASTCompilationUnit compilationUnit) {
		setEnclosingScopeOfNodes(compilationUnit);
		Log.debug("Setting enclosingScope: " + compilationUnit, OCLSymbolTableCreator.class.getSimpleName());
		Log.debug("endVisit of " + compilationUnit.getOCLFile().getFileName(), OCLSymbolTableCreator.class.getSimpleName());
	}

	@Override
	public void visit(final ASTOCLFile astFile) {
		final String oclName = astFile.getFileName();
		final OCLFileSymbol oclSymbol = new OCLFileSymbol(oclName);

		addToScopeAndLinkWithNode(oclSymbol, astFile);
	}

	@Override
	public void endVisit(final ASTOCLFile astFile) {
		Log.debug("Finished build of symboltable for OCL: " + astFile.getFileName(), OCLSymbolTableCreator.class.getSimpleName());

		removeCurrentScope();
	}

	@Override
	public void visit(final ASTOCLMethodSignature astMethSig) {
		/*final OCLMethodSignatureSymbol methSigSymbol = new OCLMethodSignatureSymbol(astMethSig.getMethodName());

		methSigSymbol.setMethodSignatureName(astMethSig.getMethodName());
		setClassNameOfMethodSignature(methSigSymbol, astMethSig);
		setReturnTypeOfMethodSignature(methSigSymbol, astMethSig);

		addToScopeAndLinkWithNode(methSigSymbol, astMethSig);*/
	}

	protected void setClassNameOfMethodSignature(final OCLMethodSignatureSymbol methSigSymbol, final ASTOCLMethodSignature astMethSig) {
	/*	String className = astMethSig.getClassName().get();
		if (className != null) {
			methSigSymbol.setClassName(className);
		}*/
	}

	protected void setReturnTypeOfMethodSignature(final OCLMethodSignatureSymbol methSigSymbol, final ASTOCLMethodSignature astMethSig) {
	/*	ASTType returnType = astMethSig.getType().get();
		if (returnType != null) {
			methSigSymbol.setReturnType(returnType);
		}*/
	}

	@Override
	public void endVisit(final ASTOCLMethodSignature astMethSig) {
		removeCurrentScope();
	}

	@Override
	public void visit(final ASTOCLConstructorSignature astClass) {
		final OCLConstructorSignatureSymbol classSymbol = new OCLConstructorSignatureSymbol(astClass.getReferenceType());
		addToScopeAndLinkWithNode(classSymbol, astClass);
	}

	@Override
	public void endVisit(final ASTOCLConstructorSignature astDefinition) {
		removeCurrentScope();
	}

	@Override
	public void visit(final ASTOCLThrowsClause astThrowsClause) {
		final OCLThrowsClauseSymbol throwsClauseSymbol = new OCLThrowsClauseSymbol(astThrowsClause.getThrowables().get(0));
		addToScopeAndLinkWithNode(throwsClauseSymbol, astThrowsClause);
	}

	@Override
	public void visit(final ASTOCLParameterDeclaration astParamDecl) {
		/*final OCLParameterDeclarationSymbol paramDeclSymbol = new OCLParameterDeclarationSymbol(astParamDecl.getVarName().get());

		setTypeOfParameter(paramDeclSymbol, astParamDecl);
		setNameOfParameter(paramDeclSymbol, astParamDecl);

		addToScopeAndLinkWithNode(paramDeclSymbol, astParamDecl);*/
	}

	protected void setTypeOfParameter(final OCLParameterDeclarationSymbol paramDeclSymbol, final ASTOCLParameterDeclaration astParamDecl) {
/*		if (astParamDecl != null) {
			if (astParamDecl.typeIsPresent()) {
				paramDeclSymbol.setType(astParamDecl.getType().get());
			}
		}*/
	}

	protected void setNameOfParameter(final OCLParameterDeclarationSymbol paramDeclSymbol, final ASTOCLParameterDeclaration astParamDecl) {
	/*	if (astParamDecl != null) {
			paramDeclSymbol.setName(astParamDecl.getVarName().get());
		}*/
	}

	@Override
	public void visit(final ASTOCLInvariant astInvariant) {
		String invName = "invariantName";
		if (astInvariant.nameIsPresent()) {
			invName = astInvariant.getName();
		}
		final OCLInvariantSymbol invSymbol = new OCLInvariantSymbol(invName);
		final ASTOCLClassContext astClassContext = astInvariant.getOCLClassContext();

		setClassName(invSymbol, astInvariant);
		setClassObject(invSymbol, astInvariant);
		setClassContextIsPresent(invSymbol, astClassContext);

		addToScopeAndLinkWithNode(invSymbol, astInvariant);
	}

	protected void setClassContextIsPresent(final OCLInvariantSymbol invSymbol, ASTOCLClassContext astClassContext) {
		if (astClassContext.isContext()) {
			invSymbol.setContext(astClassContext.isContext());
		} else if (astClassContext.isImport()) {
			invSymbol.setImport(astClassContext.isImport());
		}
	}

	protected void setClassName(final OCLInvariantSymbol invSymbol, final ASTOCLInvariant astInvariant) {
/*		if (astInvariant.oCLClassContextIsPresent()) {
			ASTOCLContextDefinition astContext = astInvariant.getOCLClassContext().get().getContextDefinitions(0);
			if(astContext.classNameIsPresent()) {
				invSymbol.setClassN(astContext.getClassName().get().toString());
			} else {
				invSymbol.setClassN(TypesPrinter.printType(astContext.getType().get()));
			}
		}*/
	}

	protected void setClassObject(final OCLInvariantSymbol invSymbol, final ASTOCLInvariant astInvariant) {
/*		if (astInvariant.oCLClassContextIsPresent()) {
			ASTOCLContextDefinition astContext = astInvariant.getOCLClassContext().get().getContextDefinitions(0);
			if(!astContext.getVarNames().isEmpty()) {
				invSymbol.setClassO(astContext.getVarNames().get(0));
			}
		}*/
	}

	@Override
	public void endVisit(final ASTOCLInvariant astInvariant) {
		OCLTypeCheckingVisitor.checkInvariants(astInvariant, currentScope().get());
		removeCurrentScope();
	}

	@Override
	public void visit(final ASTOCLMethodDeclaration astMethodDeclaration) {
		/*final OCLMethodDeclarationSymbol methDeclSymbol = new OCLMethodDeclarationSymbol(astMethodDeclaration.getName().get());
		setReturnTypeOfMethodDecl(methDeclSymbol, astMethodDeclaration);
		addToScopeAndLinkWithNode(methDeclSymbol, astMethodDeclaration);*/
	}

	public void setReturnTypeOfMethodDecl(final OCLMethodDeclarationSymbol methDeclSymbol, final ASTOCLMethodDeclaration astMethodDeclaration) {
	/*	if (astMethodDeclaration != null) {
			methDeclSymbol.setReturnType(astMethodDeclaration.getReturnType().get());
		}*/
	}

	@Override
	public void endVisit(final ASTOCLMethodDeclaration astInvariant) {
		removeCurrentScope();
	}

	@Override
	public void visit(final ASTOCLPreStatement astPreStatement) {
		final OCLPreStatementSymbol preSymbol = new OCLPreStatementSymbol(astPreStatement.getName());
		addToScopeAndLinkWithNode(preSymbol, astPreStatement);
	}

	@Override
	public void visit(final ASTOCLPostStatement astPostStatement) {
		final OCLPostStatementSymbol postSymbol = new OCLPostStatementSymbol(astPostStatement.getName());
		addToScopeAndLinkWithNode(postSymbol, astPostStatement);
	}



	/**
	 *  ********** VariableDeclarationSymbols **********
	 */

	@Override
	public void visit(final ASTOCLClassContext astClassContext) {
/*		if (astClassContext.getContextDefinitions().size() == 1) {
			ASTOCLContextDefinition astContext = astClassContext.getContextDefinitions(0);
			if (astContext.typeIsPresent()) {
				ASTType astType = astContext.getType().get();
				addVarDeclSymbol("this", astType, astContext);
			} else if (astContext.classNameIsPresent()) {
				String typeName = astContext.getClassName().get().toString();
				OCLVariableDeclarationSymbol varDecl = addVarDeclSymbol("this", typeName, astContext);
				Optional<CDTypeSymbolReference> superTypeRef = varDecl.getType().getSuperClass();
				if(superTypeRef.isPresent()) {
					addVarDeclSymbol("super", superTypeRef.get(), astContext);
				}
			}
		}*/
	}

	@Override
	public void visit(final ASTOCLContextDefinition astContext) {
/*		if(!astContext.getVarNames().isEmpty()) {
			if (astContext.typeIsPresent()) {
				ASTType astType = astContext.getType().get();
				astContext.getVarNames().forEach(name -> addVarDeclSymbol(name, astType, astContext));
			} else if (astContext.classNameIsPresent()) {
				String typeName = astContext.getClassName().get().toString();
				astContext.getVarNames().forEach(name -> addVarDeclSymbol(name, typeName, astContext));
			}
		}*/
	}

	@Override
	public void endVisit(final ASTInExpr inExpr) {
/*		if (inExpr.oCLInWithTypeIsPresent()) {
			ASTOCLInWithType inWithType = inExpr.getOCLInWithType().get();
			String name = inWithType.getVarName();
			String typeName ;
			if(inWithType.classNameIsPresent()) {
				typeName = inWithType.getClassName().get();
			} else { //Type is present
				typeName = TypesPrinter.printType(inWithType.getType().get());
			}
			addVarDeclSymbol(name, typeName, inExpr);
		} else if (inExpr.oCLInWithOutTypeIsPresent()) {
			ASTOCLInWithOutType inWithOutType = inExpr.getOCLInWithOutType().get();
			String name = inWithOutType.getName();
			CDTypeSymbolReference type = OCLExpressionTypeInferingVisitor.getTypeFromExpression(inExpr, currentScope().get());
			addVarDeclSymbol(name, type, inExpr);
		}*/
	}

	@Override
	public void endVisit(final ASTOCLVariableDeclaration astVariableDeclaration) {
	/*	if (astVariableDeclaration.oCLNestedContainerIsPresent()) { // List<..> myVar = ..
			handleNestedContainer(astVariableDeclaration);
		} else if (astVariableDeclaration.classNameIsPresent()) { // MyClass myVar = ..
			handleVarClassName(astVariableDeclaration);
		} else if (astVariableDeclaration.typeIsPresent()) { // int myVar = ..
			handleVarType(astVariableDeclaration);
		} else { // myVar = ..
			handleTypeNotPresent(astVariableDeclaration);
		}*/
	}

	protected void handleNestedContainer(ASTOCLVariableDeclaration astVariableDeclaration) {
/*		ASTOCLNestedContainer nestedContainer = astVariableDeclaration.getOCLNestedContainer().get();
		String name = astVariableDeclaration.getVarName().get();
		CDTypeSymbolReference typeReference = getTypeRefFromNestedContainer(nestedContainer);
		addVarDeclSymbol(name, typeReference, astVariableDeclaration);
		*/// Todo: cross-check with expression?
	}

	protected void handleVarClassName(ASTOCLVariableDeclaration astVariableDeclaration) {
	/*	String name = astVariableDeclaration.getVarName().get();
		String typeName = astVariableDeclaration.getClassName().get();
		addVarDeclSymbol(name, typeName, astVariableDeclaration);
		*/// Todo: cross-check with expression?
	}

	protected void handleVarType(ASTOCLVariableDeclaration astVariableDeclaration) {
	/*	String name = astVariableDeclaration.getVarName().get();
		ASTType astType = astVariableDeclaration.getType().get();
		addVarDeclSymbol(name, astType, astVariableDeclaration);
		*/// Todo: cross-check with expression?
	}

	protected void handleTypeNotPresent(ASTOCLVariableDeclaration astVariableDeclaration) {
		/*ASTOCLExpression oclExpr = astVariableDeclaration.getOCLExpression().get();
		CDTypeSymbolReference typeReference = OCLExpressionTypeInferingVisitor.getTypeFromExpression(oclExpr, currentScope().get());
		String name = astVariableDeclaration.getVarName().get();
		addVarDeclSymbol(name, typeReference, astVariableDeclaration);*/
	}

	/**
	 *  ********** Helper Methods **********
	 */

/*	private CDTypeSymbolReference getTypeRefFromNestedContainer(ASTOCLNestedContainer astoclNestedContainer) {
		ASTOCLContainerOrName containerOrName = astoclNestedContainer.getOCLContainerOrName();
		CDTypeSymbolReference typeReference;

		if (containerOrName.nameIsPresent()) {
			typeReference = createTypeRef(containerOrName.getName().get(), astoclNestedContainer);
		} else {
			int container = containerOrName.getContainer();
			String typeName;
			if (container == 20) {
				typeName = "Set";
			} else if (container == 12) {
				typeName = "List";
			} else {    //if(container == 1) {
				typeName = "Collection";
			}
			typeReference = createTypeRef(typeName, astoclNestedContainer);
			addActualArguments(typeReference, astoclNestedContainer);
		}

		return typeReference;
	}*/

/*	private void addActualArguments(CDTypeSymbolReference typeReference, ASTOCLNestedContainer astoclNestedContainer) {
		if (astoclNestedContainer.getArguments().size() > 0) {
			String stringRepresentation = typeReference.getStringRepresentation() + "<";
			List<ActualTypeArgument> actualTypeArguments = new ArrayList<>();
			for (ASTOCLNestedContainer container: astoclNestedContainer.getArguments()) {
				CDTypeSymbolReference argumentReferenceType = getTypeRefFromNestedContainer(container);
				ActualTypeArgument actualTypeArgument = new ActualTypeArgument(argumentReferenceType);
				actualTypeArguments.add(actualTypeArgument);
				stringRepresentation += ", " + argumentReferenceType.getStringRepresentation();
			}
			stringRepresentation += ">";
			stringRepresentation = stringRepresentation.replace("<, ", "<");
			typeReference.setStringRepresentation(stringRepresentation);
			typeReference.setActualTypeArguments(actualTypeArguments);
		}
	}*/




	private OCLVariableDeclarationSymbol addVarDeclSymbol(String name, CDTypeSymbolReference typeReference, ASTNode node){
		// Check if an Variable with name already exists
		Optional<OCLVariableDeclarationSymbol> previousVarDecl = currentScope().get().resolve(name, OCLVariableDeclarationSymbol.KIND);
		if(previousVarDecl.isPresent())
			currentScope().get().remove(previousVarDecl.get());
		// Then overwrite
		OCLVariableDeclarationSymbol newVarDecl = new OCLVariableDeclarationSymbol(name, typeReference);
		addToScopeAndLinkWithNode(newVarDecl, node);
		return newVarDecl;
	}

	private OCLVariableDeclarationSymbol addVarDeclSymbol(String name, String typeName, ASTNode node) {
		CDTypeSymbolReference typeReference = createTypeRef(typeName, node);
		return addVarDeclSymbol(name, typeReference, node);
	}

	private OCLVariableDeclarationSymbol addVarDeclSymbol(String name, ASTType astType, ASTNode node) {
		String typeName = TypesPrinter.printType(astType);
		CDTypeSymbolReference typeReference = createTypeRef(typeName, node);
		typeReference.setAstNode(astType);
		return addVarDeclSymbol(name, typeReference, node);
	}

	private CDTypeSymbolReference createTypeRef(String typeName, ASTNode node) {
		// map int to Integer , etc.
		typeName = CDTypes.primitiveToWrapper(typeName);
		CDTypeSymbolReference typeReference = new CDTypeSymbolReference(typeName, this.getFirstCreatedScope());
		typeReference.setStringRepresentation(typeName);
		// Check if type was found in CD loaded CD models
		if (!typeReference.existsReferencedSymbol()) {
			Log.error("This type could not be found: " + typeName, node.get_SourcePositionStart());
		}
		return typeReference;
	}


}