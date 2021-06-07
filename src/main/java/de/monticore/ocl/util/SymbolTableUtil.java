// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.util;

import de.monticore.class2mc.Class2MCResolver;
import de.monticore.class2mc.Java2MCResolver;
import de.monticore.io.paths.MCPath;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._symboltable.OCLDeSer;
import de.monticore.ocl.ocl._symboltable.OCLScopesGenitorDelegator;
import de.monticore.ocl.ocl._symboltable.OCLSymbolTableCompleter;
import de.monticore.ocl.ocl._symboltable.OCLSymbols2Json;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.oclexpressions._symboltable.OCLExpressionsSymbolTableCompleter;
import de.monticore.ocl.setexpressions._symboltable.SetExpressionsSymbolTableCompleter;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.ocl.util.library.CollectionType;
import de.monticore.ocl.util.library.GlobalQueries;
import de.monticore.ocl.util.library.ListType;
import de.monticore.ocl.util.library.SetType;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbolDeSer;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbolDeSer;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbolDeSer;
import de.monticore.symbols.oosymbols.OOSymbolsMill;
import de.se_rwth.commons.logging.Log;

import java.nio.file.Paths;

/**
 * Contains helpers that execute MontiCore API that are almost always called together
 *
 * @since 24.03.21
 */
public class SymbolTableUtil {
  static public void prepareMill() {
    OOSymbolsMill.reset();
    OOSymbolsMill.init();

    OCLMill.reset();
    OCLMill.init();
    OCLMill.globalScope().clear();
    BasicSymbolsMill.initializePrimitives();

    OOSymbolsMill.globalScope().setSymbolPath(new MCPath(Paths.get("")));
    Class2MCResolver resolver = new Class2MCResolver();
    OOSymbolsMill.globalScope().addAdaptedOOTypeSymbolResolver(resolver);
    OOSymbolsMill.globalScope().addAdaptedTypeSymbolResolver(resolver);
    OCLMill.globalScope().addAdaptedTypeSymbolResolver(resolver);

    addOclpLibrary();
  }

  protected static void addOclpLibrary() {
    CollectionType c = new CollectionType();
    ListType l = new ListType();
    SetType s = new SetType();
    GlobalQueries g = new GlobalQueries();
    c.addCollectionType();
    l.addListType();
    s.addSetType();
    c.addMethodsAndFields();
    l.addMethodsAndFields();
    s.addMethodsAndFields();
    g.addMethodsAndFields();
  }

  static public void runSymTabGenitor(ASTOCLCompilationUnit ast) {
    OCLScopesGenitorDelegator genitor = OCLMill.scopesGenitorDelegator();
    genitor.createFromAST(ast);
  }

  static public void runSymTabCompleter(ASTOCLCompilationUnit ast) {
    OCLSymbolTableCompleter stCompleter = new OCLSymbolTableCompleter(
      ast.getMCImportStatementList(), ast.getPackage()
    );
    stCompleter.setTypeVisitor(new DeriveSymTypeOfOCLCombineExpressions());
    OCLExpressionsSymbolTableCompleter stCompleter2 = new OCLExpressionsSymbolTableCompleter(
      ast.getMCImportStatementList(), ast.getPackage()
    );
    stCompleter2.setTypeVisitor(new DeriveSymTypeOfOCLCombineExpressions());
    SetExpressionsSymbolTableCompleter stCompleter3 = new SetExpressionsSymbolTableCompleter(
      ast.getMCImportStatementList(), ast.getPackage()
    );
    stCompleter3.setTypeVisitor(new DeriveSymTypeOfOCLCombineExpressions());

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
