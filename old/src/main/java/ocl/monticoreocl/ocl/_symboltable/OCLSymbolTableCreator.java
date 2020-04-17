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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import de.monticore.ast.ASTNode;
import de.monticore.expressionsbasis._ast.ASTExpression;
import de.monticore.numberunit.prettyprint.UnitsPrinter;
import ocl.monticoreocl.ocl._types.FindInExpressionHelper;
import ocl.monticoreocl.oclexpressions._ast.ASTInExpr;
import de.monticore.symboltable.*;
import de.monticore.symboltable.types.references.ActualTypeArgument;
import de.monticore.types.TypesPrinter;
import de.monticore.types.types._ast.*;
import de.monticore.umlcd4a.symboltable.CDTypes;
import de.monticore.umlcd4a.symboltable.references.CDTypeSymbolReference;
import de.se_rwth.commons.Joiners;
import ocl.monticoreocl.ocl._ast.*;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.logging.Log;
import ocl.monticoreocl.ocl._types.OCLExpressionTypeInferingVisitor;
import ocl.monticoreocl.ocl._types.TypeInferringHelper;
import ocl.monticoreocl.oclexpressions._ast.ASTName2;
import ocl.monticoreocl.oclexpressions._ast.ASTOCLComprehensionExpressionStyle;

import javax.measure.unit.Unit;

public class OCLSymbolTableCreator extends OCLSymbolTableCreatorTOP {

	public OCLSymbolTableCreator(final ResolvingConfiguration resolverConfig, final MutableScope enclosingScope) {
		super(resolverConfig, enclosingScope);
	}

	@Override
	public void visit(final ASTCompilationUnit compilationUnit) {
		String oclFile = compilationUnit.getOCLFile().isPresentFileName() ? compilationUnit.getOCLFile().getFileName() : "oclFile";
		Log.debug("Building Symboltable for OCL: " + oclFile, OCLSymbolTableCreator.class.getSimpleName());
		String compilationUnitPackage = Names.getQualifiedName(compilationUnit.getPackageList());

		// imports
		List<ImportStatement> imports = new ArrayList<>();
		for (ASTImportStatement astImportStatement : compilationUnit.getImportStatementList()) {
			String qualifiedImport = Names.getQualifiedName(astImportStatement.getImportList());
			ImportStatement importStatement = new ImportStatement(qualifiedImport, true);
			imports.add(importStatement);
		}

		ArtifactScope artifactScope = new ArtifactScope(Optional.empty(), compilationUnitPackage, imports);
		putOnStack(artifactScope);

	}

	@Override
	public void endVisit(final ASTCompilationUnit compilationUnit) {
		setEnclosingScopeOfNodes(compilationUnit);
		Log.debug("Setting enclosingScope: " + compilationUnit, OCLSymbolTableCreator.class.getSimpleName());
		String oclFile = compilationUnit.getOCLFile().isPresentFileName() ? compilationUnit.getOCLFile().getFileName() : "oclFile";
		Log.debug("endVisit of " + oclFile, OCLSymbolTableCreator.class.getSimpleName());
	}

	@Override
	public void visit(final ASTOCLFile astFile) {
		final String oclFile = astFile.isPresentFileName() ? astFile.getFileName() : "oclFile";
		final OCLFileSymbol oclSymbol = new OCLFileSymbol(oclFile);
		addToScopeAndLinkWithNode(oclSymbol, astFile);
	}

	@Override
	public void endVisit(final ASTOCLFile astFile) {
		String oclFile = astFile.isPresentFileName() ? astFile.getFileName() : "oclFile";
		Log.debug("Finished build of symboltable for OCL: " + oclFile, OCLSymbolTableCreator.class.getSimpleName());
		removeCurrentScope();
	}

	@Override
	public void visit(final ASTOCLMethodSignature astMethSig) {
		final OCLMethodSignatureSymbol methSigSymbol =
                new OCLMethodSignatureSymbol(astMethSig.getMethodName().getPart(1));
        methSigSymbol.setClassName(astMethSig.getMethodName().getPart(0));

        if(astMethSig.isPresentReturnType()) {
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
		final OCLThrowsClauseSymbol throwsClauseSymbol = new OCLThrowsClauseSymbol(astThrowsClause.getThrowables(0));
		addToScopeAndLinkWithNode(throwsClauseSymbol, astThrowsClause);
	}

	@Override
	public void visit(final ASTOCLParameterDeclaration astParamDecl) {
		final OCLParameterDeclarationSymbol paramDeclSymbol = new OCLParameterDeclarationSymbol(astParamDecl.getName());

		paramDeclSymbol.setName(astParamDecl.getName());
		paramDeclSymbol.setType(astParamDecl.getType());
		paramDeclSymbol.setClassName(astParamDecl.getType().getClass().getName());

		addVarDeclSymbol(astParamDecl.getName(), astParamDecl.getType(), astParamDecl);

		addToScopeAndLinkWithNode(paramDeclSymbol, astParamDecl);
	}

	MutableScope scope;
	@Override
	public void visit(final ASTOCLInvariant astInvariant) {
		String invName = "invariantName";
		if (astInvariant.isPresentName()) {
			invName = astInvariant.getName();
		}
		final OCLInvariantSymbol invSymbol = new OCLInvariantSymbol(invName);

		if (astInvariant.isPresentOCLClassContext()) {
			final ASTOCLClassContext astClassContext = astInvariant.getOCLClassContext();
			setClassContextIsPresent(invSymbol, astClassContext);
		}

		setClassName(invSymbol, astInvariant);
		setClassObject(invSymbol, astInvariant);
		addToScopeAndLinkWithNode(invSymbol, astInvariant);
		scope = (MutableScope) astInvariant.getSpannedScope();
	}

	protected void setClassContextIsPresent(final OCLInvariantSymbol invSymbol, ASTOCLClassContext astClassContext) {
		if (astClassContext.isContext()) {
			invSymbol.setContext(astClassContext.isContext());
		} else if (astClassContext.isImport()) {
			invSymbol.setImport(astClassContext.isImport());
		}
	}

	protected void setClassName(final OCLInvariantSymbol invSymbol, final ASTOCLInvariant astInvariant) {
		if (astInvariant.isPresentOCLClassContext()) {
			ASTOCLContextDefinition astContext = astInvariant.getOCLClassContext().getOCLContextDefinition(0);
			if(astContext.isPresentType()) {
				invSymbol.setClassN(TypesPrinter.printType(astContext.getType()));
			}
		}
	}

	protected void setClassObject(final OCLInvariantSymbol invSymbol, final ASTOCLInvariant astInvariant) {
		if (astInvariant.isPresentOCLClassContext()) {
			ASTOCLContextDefinition astContext = astInvariant.getOCLClassContext().getOCLContextDefinition(0);
			if (astContext.getVarNamesList().size() == 0) {
				invSymbol.setClassO("this");
			} else {
				invSymbol.setClassO(astContext.getVarNames(0));
			}
		}
	}

	@Override
	public void endVisit(final ASTOCLInvariant astInvariant) {
		removeCurrentScope();
	}

	@Override
	public void visit(final ASTOCLMethodDeclaration astMethodDeclaration) {
		final OCLMethodDeclarationSymbol methDeclSymbol = new OCLMethodDeclarationSymbol(astMethodDeclaration.getName());
		setReturnTypeOfMethodDecl(methDeclSymbol, astMethodDeclaration);
		addToScopeAndLinkWithNode(methDeclSymbol, astMethodDeclaration);
	}

	public void setReturnTypeOfMethodDecl(final OCLMethodDeclarationSymbol methDeclSymbol, final ASTOCLMethodDeclaration astMethodDeclaration) {
		if (astMethodDeclaration.isPresentReturnType())
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
		if (astClassContext.getOCLContextDefinitionList().size() == 1) {
			ASTOCLContextDefinition astContext = astClassContext.getOCLContextDefinition(0);
			if (astContext.getVarNamesList().size() < 2) {
				if (astContext.isPresentType()) {
					ASTType astType = astContext.getType();
					addVarDeclSymbol("this", astType, astContext);
				}
				else if (astContext.isPresentExpression()) {
					OCLExpressionTypeInferingVisitor exprVisitor = new OCLExpressionTypeInferingVisitor(currentScope().get());
					CDTypeSymbolReference typeReference = exprVisitor.getTypeFromExpression(astContext.getExpression());
					addVarDeclSymbol("this", typeReference, astContext);
				}
			}
		}
	}

	@Override
	public void visit(final ASTOCLContextDefinition astContextDef) {
		List<String> varNames = astContextDef.getVarNamesList();
		if (astContextDef.isPresentType()) {
			ASTType astType = astContextDef.getType();
			varNames.forEach(name -> addVarDeclSymbol(name, astType, astContextDef));
		}
	}

	@Override
	public void endVisit(final ASTOCLContextDefinition astContextDef) {
		List<String> varNames = astContextDef.getVarNamesList();
		if (astContextDef.isPresentExpression() && !astContextDef.isPresentType()) {
			ASTExpression astExpression = astContextDef.getExpression();
			OCLExpressionTypeInferingVisitor exprVisitor = new OCLExpressionTypeInferingVisitor(currentScope().get());
			CDTypeSymbolReference typeReference = exprVisitor.getTypeFromExpression(astExpression);
			varNames.forEach(name -> addVarDeclSymbol(name, typeReference, astContextDef));
		}
	}
  // This methods tries to infer the types of variables defined later in sets, e.g., in
	// combSubs = { { (10+d).toString | d in chain} |
	//                   chain in partition1A };
	// the algorithm needs to look ahead to infer the type of chain as `chain in partitiona1A` comes after `d in chain`
	@Override
	public void visit(ASTOCLComprehensionExpressionStyle node) {
		long undefined = Long.MAX_VALUE - 1;
		long undefinedPrev = Long.MAX_VALUE;
		while (undefined < undefinedPrev) {
			FindInExpressionHelper inExpressionHelper;
			try { // looking ahead may crash
				inExpressionHelper = new FindInExpressionHelper(node, scope);
			} catch(Exception e) {
				break;
			}
			Map<String, CDTypeSymbolReference> inTypes = inExpressionHelper.getInTypes();
			undefinedPrev = undefined;
			undefined = inTypes.values().stream().filter(s -> s.getStringRepresentation().equals("Class")).count();
			inTypes.forEach( (name, type) -> addVarDeclSymbol(name, type, node));
			if (undefined == 0)
				break;
		}
	}

	@Override
	public void visit(final ASTInExpr astInExpr) {
		List<String> varNames = astInExpr.getVarNameList();
		if (astInExpr.isPresentType()) {
			ASTType astType = astInExpr.getType();
			varNames.forEach(name -> addVarDeclSymbol(name, astType, astInExpr));
		}
	}

	@Override
	public void endVisit(final ASTInExpr astInExpr) {
		List<String> varNames = astInExpr.getVarNameList();
		if (astInExpr.isPresentExpression() && !astInExpr.isPresentType()) {
			ASTExpression astExpression = astInExpr.getExpression();
			OCLExpressionTypeInferingVisitor exprVisitor = new OCLExpressionTypeInferingVisitor(currentScope().get());
			CDTypeSymbolReference containerType = exprVisitor.getTypeFromExpression(astExpression);
			if (containerType.getActualTypeArguments().isEmpty()) {
				Log.error("0xOCLS3 Could not resolve type from InExpression, " + astInExpr.getVarNameList() +
						" in " + containerType + " at " +  astInExpr.get_SourcePositionStart()
						, astInExpr.get_SourcePositionStart(), astInExpr.get_SourcePositionEnd());
			} else {
				CDTypeSymbolReference varType = (CDTypeSymbolReference) containerType.getActualTypeArguments().get(0).getType();
				varNames.forEach(name -> addVarDeclSymbol(name, varType, astInExpr));
			}

		}
	}

	@Override
	public void endVisit(final ASTOCLVariableDeclaration astVariableDeclaration) {
		if (astVariableDeclaration.isPresentType()) {
			ASTType astType = astVariableDeclaration.getType();
			addVarDeclSymbol(astVariableDeclaration.getName(), astType, astVariableDeclaration);
			//Todo: cross check with expression Type?
		} else {
			ASTExpression astExpression = astVariableDeclaration.getExpression();
			OCLExpressionTypeInferingVisitor exprVisitor = new OCLExpressionTypeInferingVisitor(currentScope().get());
			CDTypeSymbolReference typeReference = exprVisitor.getTypeFromExpression(astExpression);

			OCLVariableDeclarationSymbol declSymbol = addVarDeclSymbol(astVariableDeclaration.getName(), typeReference, astVariableDeclaration);
			if(exprVisitor.getReturnUnit().isPresent())
				declSymbol.setUnit(exprVisitor.getReturnUnit().get());
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
			Log.error("0xOCLS2 This type could not be found: " + typeName + " at " + node.get_SourcePositionStart()
					, node.get_SourcePositionStart(), node.get_SourcePositionEnd());
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
			String typeName = Joiners.DOT.join(astSimpleType.getNameList());
			typeReference = createTypeRef(typeName, node);
			typeReference.setStringRepresentation(TypesPrinter.printSimpleReferenceType(astSimpleType));
			if(UnitsPrinter.isSupported(typeName)) {
				CDTypeSymbolReference amountType = createTypeRef("Number", node);
				TypeInferringHelper.addActualArgument(amountType, typeReference);
				typeReference = amountType;
			} else {
				addActualArguments(typeReference, astSimpleType, node);
			}
		}

		if (typeReference == null) {
			Log.error("0xOCLS1 No type reference could be created for: " + astType + " at " + node.get_SourcePositionStart()
					, node.get_SourcePositionStart(), node.get_SourcePositionEnd());
		}
		return typeReference;
	}

	private CDTypeSymbolReference addActualArguments(CDTypeSymbolReference typeReference,
													 ASTSimpleReferenceType astType, ASTNode node) {
		if (astType.isPresentTypeArguments()) {
			List<ASTTypeArgument> arguments = astType.getTypeArguments().getTypeArgumentList();
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
