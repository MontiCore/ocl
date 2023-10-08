// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.util;

import de.monticore.class2mc.Class2MCResolver;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._symboltable.*;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.oclexpressions._symboltable.OCLExpressionsSymbolTableCompleter;
import de.monticore.ocl.setexpressions._symboltable.SetExpressionsSymbolTableCompleter;
import de.monticore.ocl.types.check.types3wrapper.TypeCheck3AsOCLDeriver;
import de.monticore.ocl.types.check.types3wrapper.TypeCheck3AsOCLSynthesizer;
import de.monticore.ocl.util.library.*;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbolDeSer;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbolDeSer;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbolDeSer;
import de.monticore.symboltable.ImportStatement;
import de.se_rwth.commons.logging.Log;

/**
 * Contains helpers that execute MontiCore API that are almost always called together
 *
 * @since 24.03.21
 */
public class SymbolTableUtil {

  public static void prepareMill() {
    OCLMill.reset();
    OCLMill.init();
    OCLMill.globalScope().clear();
    BasicSymbolsMill.initializePrimitives();

    Class2MCResolver resolver = new Class2MCResolver();
    OCLMill.globalScope().addAdaptedTypeSymbolResolver(resolver);

    addOclpLibrary();
  }

  public static void addOclpLibrary() {
    // todo replace after fix of
    // https://git.rwth-aachen.de/monticore/monticore/-/issues/3508
    CollectionType c = new CollectionType();
    ListType l = new ListType();
    SetType s = new SetType();
    OptionalType o = new OptionalType();
    GlobalQueries g = new GlobalQueries();
    c.addCollectionType();
    l.addListType();
    s.addSetType();
    o.addOptionalType();
    c.addMethodsAndFields();
    l.addMethodsAndFields();
    s.addMethodsAndFields();
    g.addMethodsAndFields();
    o.addMethodsAndFields();
    /*
    IOCLGlobalScope gs = OCLMill.globalScope();
    OCLSymbols2Json olcSym2json = new OCLSymbols2Json();
    gs.addSubScope(olcSym2json.load(ClassLoader.getSystemResource("collectiontypes/List.sym")));
    gs.addSubScope(olcSym2json.load(ClassLoader.getSystemResource("collectiontypes/Optional.sym")));
    gs.addSubScope(olcSym2json.load(ClassLoader.getSystemResource("collectiontypes/Set.sym")));
    gs.addSubScope(
        olcSym2json.load(ClassLoader.getSystemResource("collectiontypes/Collection.sym")));
    gs.addSubScope(olcSym2json.load(ClassLoader.getSystemResource("functions/GlobalQueries.sym")));
     */
  }

  public static void runSymTabGenitor(ASTOCLCompilationUnit ast) {
    OCLScopesGenitorDelegator genitor = OCLMill.scopesGenitorDelegator();
    addDefaultImports(genitor.createFromAST(ast));
  }

  public static void addDefaultImports(IOCLArtifactScope scope) {
    scope.addImports(new ImportStatement("java.lang", true));
  }

  public static void runSymTabCompleter(ASTOCLCompilationUnit ast) {
    OCLSymbolTableCompleter stCompleter =
        new OCLSymbolTableCompleter(ast.getMCImportStatementList(), ast.getPackage());
    stCompleter.setSynthesizer(new TypeCheck3AsOCLSynthesizer());
    OCLExpressionsSymbolTableCompleter stCompleter2 =
        new OCLExpressionsSymbolTableCompleter(ast.getMCImportStatementList(), ast.getPackage());
    stCompleter2.setDeriver(new TypeCheck3AsOCLDeriver());
    stCompleter2.setSynthesizer(new TypeCheck3AsOCLSynthesizer());
    SetExpressionsSymbolTableCompleter stCompleter3 =
        new SetExpressionsSymbolTableCompleter(ast.getMCImportStatementList(), ast.getPackage());
    stCompleter3.setDeriver(new TypeCheck3AsOCLDeriver());
    stCompleter3.setSynthesizer(new TypeCheck3AsOCLSynthesizer());

    OCLTraverser t = OCLMill.traverser();
    t.add4BasicSymbols(stCompleter);
    t.add4OCL(stCompleter);
    t.setOCLHandler(stCompleter);
    t.setOCLExpressionsHandler(stCompleter2);
    t.setSetExpressionsHandler(stCompleter3);
    t.add4BasicSymbols(stCompleter2);
    t.add4OCLExpressions(stCompleter2);
    t.add4BasicSymbols(stCompleter3);
    t.add4SetExpressions(stCompleter3);
    ast.accept(t);
  }

  public static void addTypeSymbol(String symbolFqn) {
    OCLMill.globalScope().putSymbolDeSer(symbolFqn, new TypeSymbolDeSer());
  }

  public static void addFunctionSymbol(String symbolFqn) {
    OCLMill.globalScope().putSymbolDeSer(symbolFqn, new FunctionSymbolDeSer());
  }

  public static void addVariableSymbol(String symbolFqn) {
    OCLMill.globalScope().putSymbolDeSer(symbolFqn, new VariableSymbolDeSer());
  }

  public static void ignoreSymbolKind(String symbolFqn) {
    ((OCLDeSer) OCLMill.globalScope().getDeSer()).ignoreSymbolKind(symbolFqn);
  }

  public static void addCd4cSymbols() {
    addTypeSymbol("de.monticore.cdbasis._symboltable.CDTypeSymbol");
    addFunctionSymbol("de.monticore.cd4codebasis._symboltable.CDMethodSignatureSymbol");
    addVariableSymbol("de.monticore.symbols.oosymbols._symboltable.FieldSymbol");
    ignoreSymbolKind("de.monticore.cdassociation._symboltable.CDAssociationSymbol");
    ignoreSymbolKind("de.monticore.cdassociation._symboltable.CDRoleSymbol");
  }

  public static void loadSymbolFile(String filePath) {
    Log.debug("Read symbol file \"" + filePath + "\"", "SymbolTableUtil");
    OCLSymbols2Json deSer = new OCLSymbols2Json();
    OCLMill.globalScope().addSubScope(deSer.load(filePath));
  }
}
