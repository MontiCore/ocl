// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.util;

import de.monticore.class2mc.Java2MCResolver;
import de.monticore.io.paths.ModelPath;
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
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.symbols.basicsymbols._symboltable.*;
import de.monticore.symbols.oosymbols.OOSymbolsMill;
import de.monticore.symbols.oosymbols._symboltable.OOTypeSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.se_rwth.commons.logging.Log;

import java.nio.file.Paths;
import java.util.Optional;

/**
 * Contains helpers that execute MontiCore API that are almost always called together
 *
 * @since 24.03.21
 */
public class SymbolTableUtil {
  static public void prepareMill() {
    OCLMill.reset();
    OCLMill.init();
    OCLMill.globalScope().clear();
    BasicSymbolsMill.initializePrimitives();

    OOSymbolsMill.globalScope().setModelPath(new ModelPath(Paths.get("")));
    Java2MCResolver resolver = new Java2MCResolver(OOSymbolsMill.globalScope());
    OCLMill.globalScope().addAdaptedTypeSymbolResolver(resolver);
    OOSymbolsMill.globalScope().addAdaptedTypeSymbolResolver(resolver);

    addMethodsToList();
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
    ((OCLDeSer)OCLMill.globalScope().getDeSer()).ignoreSymbolKind(symbolFqn);
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

  protected static void addMethodsToList(){
    //resolve for List
    Optional<TypeSymbol> optList = OCLMill.globalScope().resolveType("java.util.List");
    if(optList.isPresent()){
      OOTypeSymbol list = (OOTypeSymbol) optList.get();
      //remove list from its enclosing scope, otherwise the changes would not be committed to the symbol table
      IBasicSymbolsScope enclosingScope = list.getEnclosingScope();
      enclosingScope.remove((OOTypeSymbol) list);
      //method prepend
      FunctionSymbol prepend = OCLMill.functionSymbolBuilder()
        .setName("prepend")
        .setEnclosingScope(list.getSpannedScope())
        .setSpannedScope(OCLMill.scope())
        .build();

      //type variable of List is E
      Optional<TypeVarSymbol> optE = list.getSpannedScope().resolveTypeVar("E");
      if(optE.isPresent()){
        //parameter o of type E
        VariableSymbol o = OCLMill.variableSymbolBuilder()
          .setName("o")
          .setEnclosingScope(prepend.getSpannedScope())
          //the type of the parameter is E
          .setType(SymTypeExpressionFactory.createTypeVariable(optE.get()))
          .build();

        //add parameter o to method prepend
        prepend.getSpannedScope().add(o);

        //create and set return type of the method
        SymTypeExpression returnType = SymTypeExpressionFactory
          .createGenerics(list, SymTypeExpressionFactory.createTypeVariable(optE.get()));
        prepend.setReturnType(returnType);
      }
      //add method prepend to List
      list.getSpannedScope().add(prepend);


      //method asSet
      FunctionSymbol asSet = OCLMill.functionSymbolBuilder()
        .setName("asSet")
        .setEnclosingScope(list.getSpannedScope())
        .setSpannedScope(OCLMill.scope())
        .build();

      //resolve for Set as its type symbol is needed for the return type
      Optional<TypeSymbol> optSet = OCLMill.globalScope().resolveType("java.util.Set");
      if(optSet.isPresent() && optE.isPresent()){
        TypeSymbol set = optSet.get();
        //create and set return type Set<E>
        SymTypeExpression returnType = SymTypeExpressionFactory
          .createGenerics(set, SymTypeExpressionFactory.createTypeVariable(optE.get()));
        asSet.setReturnType(returnType);
      }

      //add method asSet to List
      list.getSpannedScope().add(asSet);
      //add list again to its enclosing scope
      enclosingScope.add(list);
    }
  }
}
