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
import de.monticore.expressionsbasis._ast.ASTExpression;
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
		final String oclName = astFile.fileNameIsPresent() ? astFile.getFileName() : "oclFile";
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
		final OCLMethodSignatureSymbol methSigSymbol =
                new OCLMethodSignatureSymbol(astMethSig.getMethodName().getParts().get(1));
        methSigSymbol.setClassName(astMethSig.getMethodName().getParts().get(0));

        if(astMethSig.returnTypeIsPresent()) {
			ASTReturnType returnType = astMethSig.getReturnType();
			methSigSymbol.setReturnType(returnType);
		}
		addToScopeAndLinkWithNode(methSigSymbol, astMethSig);
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
		final OCLParameterDeclarationSymbol paramDeclSymbol = new OCLParameterDeclarationSymbol(astParamDecl.getName());

		paramDeclSymbol.setName(astParamDecl.getName());
		paramDeclSymbol.setType(astParamDecl.getType());
		paramDeclSymbol.setClassName(astParamDecl.getType().getClass().getName());

		addToScopeAndLinkWithNode(paramDeclSymbol, astParamDecl);
	}

	@Override
	public void visit(final ASTOCLInvariant astInvariant) {
		String invName = "invariantName";
		if (astInvariant.nameIsPresent()) {
			invName = astInvariant.getName();
		}
		final OCLInvariantSymbol invSymbol = new OCLInvariantSymbol(invName);

		if (astInvariant.oCLClassContextIsPresent()) {
			final ASTOCLClassContext astClassContext = astInvariant.getOCLClassContext();
			setClassContextIsPresent(invSymbol, astClassContext);
		}

		setClassName(invSymbol, astInvariant);
		setClassObject(invSymbol, astInvariant);
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
		if (astInvariant.oCLClassContextIsPresent()) {
			ASTOCLContextDefinition astContext = astInvariant.getOCLClassContext().getOCLContextDefinitions(0);
			if(astContext.typeIsPresent()) {
				invSymbol.setClassN(TypesPrinter.printType(astContext.getType()));
			}
		}
	}

	protected void setClassObject(final OCLInvariantSymbol invSymbol, final ASTOCLInvariant astInvariant) {
		if (astInvariant.oCLClassContextIsPresent()) {
			ASTOCLContextDefinition astContext = astInvariant.getOCLClassContext().getOCLContextDefinitions(0);
			if (astContext.getVarNames().size() == 0) {
				invSymbol.setClassO("this");
			} else {
				invSymbol.setClassO(astContext.getVarNames().get(0));
			}
		}
	}

	@Override
	public void endVisit(final ASTOCLInvariant astInvariant) {
		OCLTypeCheckingVisitor.checkInvariants(astInvariant, currentScope().get());
		removeCurrentScope();
	}

	@Override
	public void visit(final ASTOCLMethodDeclaration astMethodDeclaration) {
		final OCLMethodDeclarationSymbol methDeclSymbol = new OCLMethodDeclarationSymbol(astMethodDeclaration.getName());
		setReturnTypeOfMethodDecl(methDeclSymbol, astMethodDeclaration);
		addToScopeAndLinkWithNode(methDeclSymbol, astMethodDeclaration);
	}

	public void setReturnTypeOfMethodDecl(final OCLMethodDeclarationSymbol methDeclSymbol, final ASTOCLMethodDeclaration astMethodDeclaration) {
		if (astMethodDeclaration.returnTypeIsPresent())
			methDeclSymbol.setReturnType(astMethodDeclaration.getReturnType());
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
		if (astClassContext.getOCLContextDefinitions().size() == 1) {
			ASTOCLContextDefinition astContext = astClassContext.getOCLContextDefinitions(0);
			if (astContext.getVarNames().size() < 2) {
				if (astContext.typeIsPresent()) {
					ASTType astType = astContext.getType();
					addVarDeclSymbol("this", astType, astContext);
				}
				else if (astContext.expressionIsPresent()) {
					CDTypeSymbolReference typeReference = OCLExpressionTypeInferingVisitor.getTypeFromExpression(astContext.getExpression(), currentScope().get());
					addVarDeclSymbol("this", typeReference, astContext);
				}
			}
		}
	}

	@Override
	public void visit(final ASTOCLContextDefinition astContextDef) {
		List<String> varNames = astContextDef.getVarNames();
		if (astContextDef.typeIsPresent()) {
			ASTType astType = astContextDef.getType();
			varNames.forEach(name -> addVarDeclSymbol(name, astType, astContextDef));
		}
	}

	@Override
	public void endVisit(final ASTOCLContextDefinition astContextDef) {
		List<String> varNames = astContextDef.getVarNames();
		if (astContextDef.expressionIsPresent() && !astContextDef.typeIsPresent()) {
			ASTExpression astExpression = astContextDef.getExpression();
			CDTypeSymbolReference typeReference = OCLExpressionTypeInferingVisitor.getTypeFromExpression(astExpression, currentScope().get());
			varNames.forEach(name -> addVarDeclSymbol(name, typeReference, astContextDef));
		}
	}

	@Override
	public void visit(final ASTInExpr astInExpr) {
		List<String> varNames = astInExpr.getVarNames();
		if (astInExpr.typeIsPresent()) {
			ASTType astType = astInExpr.getType().get();
			varNames.forEach(name -> addVarDeclSymbol(name, astType, astInExpr));
		}
	}

	@Override
	public void endVisit(final ASTInExpr astInExpr) {
		List<String> varNames = astInExpr.getVarNames();
		if (astInExpr.expressionIsPresent() && !astInExpr.typeIsPresent()) {
			ASTExpression astExpression = astInExpr.getExpression().get();
			CDTypeSymbolReference typeReference = OCLExpressionTypeInferingVisitor.getTypeFromExpression(astExpression, currentScope().get());
			varNames.forEach(name -> addVarDeclSymbol(name, typeReference, astInExpr));
		}
	}

	@Override
	public void endVisit(final ASTOCLVariableDeclaration astVariableDeclaration) {
		if (astVariableDeclaration.typeIsPresent()) {
			ASTType astType = astVariableDeclaration.getType();
			addVarDeclSymbol(astVariableDeclaration.getName(), astType, astVariableDeclaration);
			//Todo: cross check with expression Type?
		} else {
			ASTExpression astExpression = astVariableDeclaration.getExpression();
			CDTypeSymbolReference typeReference = OCLExpressionTypeInferingVisitor.getTypeFromExpression(astExpression, currentScope().get());
			addVarDeclSymbol(astVariableDeclaration.getName(), typeReference, astVariableDeclaration);
		}
	}

	/**
	 *  ********** Helper Methods **********
	 */

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

	private OCLVariableDeclarationSymbol addVarDeclSymbol(String name, ASTType astType, ASTNode node) {
		CDTypeSymbolReference typeReference = createTypeRef(astType, node);
		return addVarDeclSymbol(name, typeReference, node);
	}

	private CDTypeSymbolReference createTypeRef(String typeName, ASTNode node) {
		CDTypeSymbolReference typeReference = new CDTypeSymbolReference(typeName, this.getFirstCreatedScope());
		// Check if type was found in CD loaded CD models
		if (!typeReference.existsReferencedSymbol()) {
			Log.error("This type could not be found: " + typeName + " at " + node.get_SourcePositionStart());
		}
		return typeReference;
	}

	private CDTypeSymbolReference createTypeRef(ASTType astType, ASTNode node) {
		CDTypeSymbolReference typeReference = null;
		if (astType instanceof  ASTPrimitiveType) {
			String typeName = CDTypes.primitiveToWrapper(astType.toString());
			typeReference = createTypeRef(typeName, node);
			typeReference.setStringRepresentation(typeName);
		}
		if (astType instanceof  ASTSimpleReferenceType) {
			ASTSimpleReferenceType astSimpleType = (ASTSimpleReferenceType) astType;
			String typeName = Joiners.DOT.join(astSimpleType.getNames());
			typeReference = createTypeRef(typeName, node);
			typeReference.setStringRepresentation(TypesPrinter.printSimpleReferenceType(astSimpleType));
			addActualArguments(typeReference, astSimpleType, node);
		}

		if (typeReference == null) {
			Log.error("Error 0xOCLS1 No type reference could be created for: " + astType + " at " + node.get_SourcePositionStart());
		}
		return typeReference;
	}

	private CDTypeSymbolReference addActualArguments(CDTypeSymbolReference typeReference,
													 ASTSimpleReferenceType astType, ASTNode node) {
		if (astType.typeArgumentsIsPresent()) {
			List<ASTTypeArgument> arguments = astType.getTypeArguments().get().getTypeArguments();
			List<ActualTypeArgument> actualTypeArguments = new ArrayList<>();

			for (ASTTypeArgument argument : arguments) {
				CDTypeSymbolReference argumentReferenceType = createTypeRef((ASTType) argument, node);
				ActualTypeArgument actualTypeArgument = new ActualTypeArgument(argumentReferenceType);
				actualTypeArguments.add(actualTypeArgument);
			}

			typeReference.setActualTypeArguments(actualTypeArguments);
		}
		return typeReference;
	}
}