/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.ocl._symboltable;

import de.monticore.expressions.oclexpressions._ast.ASTOCLInExpression;
import de.monticore.expressions.oclexpressions._ast.ASTOCLParamDeclaration;
import de.monticore.expressions.oclexpressions._symboltable.IOCLExpressionsScope;
import de.monticore.ocl.ocl._ast.*;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.symboltable.ImportStatement;
import de.monticore.types.check.DefsTypeBasic;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.typesymbols._symboltable.FieldSymbol;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

public class OCLSymbolTableCreator extends OCLSymbolTableCreatorTOP {

  DeriveSymTypeOfOCLCombineExpressions typeVisitor;

  public OCLSymbolTableCreator(IOCLScope enclosingScope) {
    super(enclosingScope);
  }

  public OCLSymbolTableCreator(Deque<? extends IOCLScope> scopeStack) {
    super(scopeStack);
  }

  public void setTypeVisitor(DeriveSymTypeOfOCLCombineExpressions typesCalculator) {
    if (typesCalculator != null) {
      this.typeVisitor = typesCalculator;
    }
    else {
      Log.error("0xA3201 The typesVisitor has to be set");
    }
  }

  @Override
  public void visit(final ASTOCLCompilationUnit compilationUnit) {
    super.visit(compilationUnit);

    final String oclFile = OCLSymbolTableHelper.getNameOfModel(compilationUnit);
    Log.debug("Building Symboltable for OCL: " + oclFile, OCLSymbolTableCreator.class.getSimpleName());

    final String compilationUnitPackage = Names.getQualifiedName(compilationUnit.getPackageList());

    // imports
    final List<ImportStatement> imports = compilationUnit.streamMCImportStatements().map(i -> new ImportStatement(i.getQName(), i.isStar())).collect(Collectors.toList());

    getCurrentScope().get().setAstNode(compilationUnit);

    final OCLArtifactScope enclosingScope = (OCLArtifactScope)compilationUnit.getEnclosingScope();
    enclosingScope.setImportList(imports);
    enclosingScope.setPackageName(compilationUnitPackage);
  }

  @Override
  public void endVisit(final ASTOCLCompilationUnit compilationUnit) {
    //removeCurrentScope();

    super.endVisit(compilationUnit);
  }

  @Override
  public void visit(ASTOCLMethodSignature node) {
    super.visit(node);
    registerFields(node.getOCLParameters().getParamsList(), node.getEnclosingScope());
  }

  @Override
  public void visit(ASTOCLConstructorSignature node) {
    super.visit(node);
    registerFields(node.getOCLParameters().getParamsList(), node.getEnclosingScope());
  }

  @Override
  public void visit(ASTOCLInvariant node) {
    super.visit(node);

    if (node.isPresentOCLParameters()) {
      registerFields(node.getOCLParameters().getParamsList(), node.getEnclosingScope());
    }
  }

  @Override
  public void endVisit(ASTOCLContextDefinition node) {
    if (node.isPresentOCLExtType()) {
      // TODO `this` is now the extType
    }
    else if (node.isPresentExpression()) {
      ASTOCLInExpression inExpression = (ASTOCLInExpression) node.getExpression();

      if (!handleOCLInExpressions(node.getEnclosingScope(), Collections.singletonList(inExpression), "OCLContextDefinition")) {
        return;
      }
    }

    super.endVisit(node);
  }

  public FieldSymbol handleParamDeclaration(ASTOCLParamDeclaration param) {
    final String paramName = param.getParam();
    typeVisitor.setScope(param.getOCLExtType().getEnclosingScope());
    param.getOCLExtType().accept(typeVisitor.getRealThis());
    if (!typeVisitor.getLastResult().isPresentLast()) {
      Log.error("0xA3250 The type of the OCLDeclaration of the OCLMethodDeclaration could not be calculated");
      return null;
    }
    return DefsTypeBasic.field(paramName, typeVisitor.getLastResult().getLast());
  }

  public void registerFields(List<ASTOCLParamDeclaration> params, IOCLExpressionsScope enclosingScope) {
    List<FieldSymbol> fields = new ArrayList<>();
    for (ASTOCLParamDeclaration param : params) {
      final FieldSymbol fieldSymbol = handleParamDeclaration(param);
      if (fieldSymbol == null) {
        return;
      }
      fields.add(fieldSymbol);
    }

    fields.forEach(f -> DefsTypeBasic.add2scope(enclosingScope, f));
  }

  private boolean handleOCLInExpressions(IOCLExpressionsScope scope, List<ASTOCLInExpression> exprList, String astType) {
    for (ASTOCLInExpression expr : exprList) {
      final List<String> varNameList = expr.getVarNameList();

      if (expr.isPresentOCLExtType()) {
        typeVisitor.setScope(expr.getEnclosingScope());
        expr.getOCLExtType().accept(typeVisitor.getRealThis());
      }
      else {
        this.typeVisitor.setScope(expr.getEnclosingScope());
        expr.accept(typeVisitor.getRealThis());
      }

      if (typeVisitor.getLastResult().isPresentLast()) {
        final SymTypeExpression last = typeVisitor.getLastResult().getLast();
        varNameList.stream().map(name -> DefsTypeBasic.field(name, last)).forEach(f -> DefsTypeBasic.add2scope(scope, f));
      }
      else {
        Log.error("0xA32A0 The type of the Expression of the OCLInExpression of the " + astType + " could not be calculated");
        return false;
      }
    }
    return true;
  }

  /*
  @Override
  public void visit(final ASTOCLMethodSignature astMethSig) {
    final OCLMethodSignatureSymbol methSigSymbol =
        new OCLMethodSignatureSymbol(astMethSig.getMethodName().getPart(1));
    methSigSymbol.setClassName(astMethSig.getMethodName().getPart(0));

    if (astMethSig.isPresentReturnType()) {
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
    }
    else if (astClassContext.isImport()) {
      invSymbol.setImport(astClassContext.isImport());
    }
  }

  protected void setClassName(final OCLInvariantSymbol invSymbol, final ASTOCLInvariant astInvariant) {
    if (astInvariant.isPresentOCLClassContext()) {
      ASTOCLContextDefinition astContext = astInvariant.getOCLClassContext().getOCLContextDefinition(0);
      if (astContext.isPresentType()) {
        invSymbol.setClassN(TypesPrinter.printType(astContext.getType()));
      }
    }
  }

  protected void setClassObject(final OCLInvariantSymbol invSymbol, final ASTOCLInvariant astInvariant) {
    if (astInvariant.isPresentOCLClassContext()) {
      ASTOCLContextDefinition astContext = astInvariant.getOCLClassContext().getOCLContextDefinition(0);
      if (astContext.getVarNamesList().size() == 0) {
        invSymbol.setClassO("this");
      }
      else {
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
    if (astMethodDeclaration.isPresentReturnType()) {
      methDeclSymbol.setReturnType(astMethodDeclaration.getReturnType());
    }
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
   * ********** VariableDeclarationSymbols **********
   */
/*
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
      }
      catch (Exception e) {
        break;
      }
      Map<String, CDTypeSymbolReference> inTypes = inExpressionHelper.getInTypes();
      undefinedPrev = undefined;
      undefined = inTypes.values().stream().filter(s -> s.getStringRepresentation().equals("Class")).count();
      inTypes.forEach((name, type) -> addVarDeclSymbol(name, type, node));
      if (undefined == 0) {
        break;
      }
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
                " in " + containerType + " at " + astInExpr.get_SourcePositionStart()
            , astInExpr.get_SourcePositionStart(), astInExpr.get_SourcePositionEnd());
      }
      else {
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
    }
    else {
      ASTExpression astExpression = astVariableDeclaration.getExpression();
      OCLExpressionTypeInferingVisitor exprVisitor = new OCLExpressionTypeInferingVisitor(currentScope().get());
      CDTypeSymbolReference typeReference = exprVisitor.getTypeFromExpression(astExpression);

      OCLVariableDeclarationSymbol declSymbol = addVarDeclSymbol(astVariableDeclaration.getName(), typeReference, astVariableDeclaration);
      if (exprVisitor.getReturnUnit().isPresent()) {
        declSymbol.setUnit(exprVisitor.getReturnUnit().get());
      }
    }
  }

  /**
   * ********** Helper Methods **********
   */
/*
  private OCLVariableDeclarationSymbol addVarDeclSymbol(String name, CDTypeSymbolReference typeReference, ASTNode node) {
    // Check if an Variable with name already exists
    Optional<OCLVariableDeclarationSymbol> previousVarDecl = currentScope().get().resolve(name, OCLVariableDeclarationSymbol.KIND);
    if (previousVarDecl.isPresent()) {
      currentScope().get().remove(previousVarDecl.get());
    }
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
    if (astType instanceof ASTPrimitiveType) {
      String typeName = CDTypes.primitiveToWrapper(astType.toString());
      typeReference = createTypeRef(typeName, node);
      typeReference.setStringRepresentation(typeName);
    }
    if (astType instanceof ASTSimpleReferenceType) {
      ASTSimpleReferenceType astSimpleType = (ASTSimpleReferenceType) astType;
      String typeName = Joiners.DOT.join(astSimpleType.getNameList());
      typeReference = createTypeRef(typeName, node);
      typeReference.setStringRepresentation(TypesPrinter.printSimpleReferenceType(astSimpleType));
      if (UnitsPrinter.isSupported(typeName)) {
        CDTypeSymbolReference amountType = createTypeRef("Number", node);
        TypeInferringHelper.addActualArgument(amountType, typeReference);
        typeReference = amountType;
      }
      else {
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
  }*/
}
