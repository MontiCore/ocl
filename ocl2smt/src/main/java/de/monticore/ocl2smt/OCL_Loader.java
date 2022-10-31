/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt;

import de.monticore.cd._symboltable.BuiltInTypes;
import de.monticore.cd4analysis.CD4AnalysisMill;
import de.monticore.cd4analysis._cocos.CD4AnalysisCoCoChecker;
import de.monticore.cd4analysis._parser.CD4AnalysisParser;
import de.monticore.cd4analysis._symboltable.CD4AnalysisSymbolTableCompleter;
import de.monticore.cd4analysis._symboltable.ICD4AnalysisArtifactScope;
import de.monticore.cd4analysis._visitor.CD4AnalysisTraverser;
import de.monticore.cd4analysis.cocos.CD4AnalysisCoCosDelegator;
import de.monticore.cd4analysis.trafo.CDAssociationCreateFieldsFromAllRoles;
import de.monticore.cd4code._symboltable.CD4CodeSymbols2Json;
import de.monticore.cd4code._symboltable.ICD4CodeScope;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._cocos.OCLCoCoChecker;
import de.monticore.ocl.ocl._cocos.OCLCoCos;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.ocl.ocl._symboltable.IOCLArtifactScope;
import de.monticore.ocl.ocl._symboltable.OCLSymbolTableCompleter;
import de.monticore.ocl.ocl._symboltable.OCLSymbols2Json;
import de.monticore.ocl.util.SymbolTableUtil;
import de.monticore.types.mcbasictypes.MCBasicTypesMill;

import java.io.File;
import java.io.IOException;
import java.util.Optional;


public class OCL_Loader {

  public static ASTCDCompilationUnit loadAndCheckCD(File cdFile) throws IOException {
    assert cdFile.getName().endsWith(".cd");
    ASTCDCompilationUnit cdAST = parseCDModel(cdFile.getAbsolutePath());
    cdAST.setEnclosingScope(createCDSymTab(cdAST));
    checkCDCoCos(cdAST);

    return cdAST;
  }
  protected  static void transformAllRoles(ASTCDCompilationUnit cdAST){
    final CDAssociationCreateFieldsFromAllRoles cdAssociationCreateFieldsFromAllRoles =
            new CDAssociationCreateFieldsFromAllRoles();
    final CD4AnalysisTraverser traverser = CD4AnalysisMill.traverser();
    traverser.add4CDAssociation(cdAssociationCreateFieldsFromAllRoles);
    traverser.setCDAssociationHandler(cdAssociationCreateFieldsFromAllRoles);
    cdAssociationCreateFieldsFromAllRoles.transform(cdAST);
  }
  public static ASTOCLCompilationUnit loadAndCheckOCL(File oclFile, File cdFile) throws IOException {
    ASTCDCompilationUnit cdAST = loadAndCheckCD(cdFile);
    transformAllRoles(cdAST);

    assert oclFile.getName().endsWith(".ocl");
    ASTOCLCompilationUnit oclAST = parseOCLModel(oclFile.getAbsolutePath());

    oclAST.setEnclosingScope(createOCLSymTab(oclAST));

  //  loadCDModel(oclAST, cdAST);

   // checkOCLCoCos(oclAST);
    return oclAST;
  }

  protected static ASTCDCompilationUnit parseCDModel(String cdFilePath) throws IOException {
    CD4AnalysisParser cdParser = new CD4AnalysisParser();
    final Optional<ASTCDCompilationUnit> optCdAST = cdParser.parse(cdFilePath);
    assert (optCdAST.isPresent());
    return optCdAST.get();
  }

  protected static ASTOCLCompilationUnit parseOCLModel(String oclFilePath) throws IOException {
    OCLParser oclParser = new OCLParser();
    final Optional<ASTOCLCompilationUnit> optOclAST = oclParser.parse(oclFilePath);
    assert (optOclAST.isPresent());
    return optOclAST.get();
  }

  protected static ICD4AnalysisArtifactScope createCDSymTab(ASTCDCompilationUnit ast) {
    ICD4AnalysisArtifactScope as = CD4AnalysisMill.scopesGenitorDelegator().createFromAST(ast);
    BuiltInTypes.addBuiltInTypes(CD4AnalysisMill.globalScope());
    CD4AnalysisSymbolTableCompleter c = new CD4AnalysisSymbolTableCompleter(
        ast.getMCImportStatementList(), MCBasicTypesMill.mCQualifiedNameBuilder().build());
    ast.accept(c.getTraverser());
    return as;
  }

  protected static IOCLArtifactScope createOCLSymTab(ASTOCLCompilationUnit ast) {
    IOCLArtifactScope as = OCLMill.scopesGenitorDelegator().createFromAST(ast);
    OCLSymbolTableCompleter c = new OCLSymbolTableCompleter(
        ast.getMCImportStatementList(), MCBasicTypesMill.mCQualifiedNameBuilder().build().getQName());
    c.setTraverser(OCLMill.traverser());
    ast.accept(c.getTraverser());
    return as;
  }

  protected static void checkCDCoCos(ASTCDCompilationUnit cdAST) {
    CD4AnalysisCoCoChecker cdChecker = new CD4AnalysisCoCosDelegator().getCheckerForAllCoCos();
    cdChecker.checkAll(cdAST);
  }

  protected static void checkOCLCoCos(ASTOCLCompilationUnit oclAST) {
    OCLCoCoChecker oclChecker = OCLCoCos.createChecker();
    oclChecker.checkAll(oclAST);
  }

  protected static void loadCDModel(ASTOCLCompilationUnit oclAST, ASTCDCompilationUnit cdAST) {
    String serialized = new CD4CodeSymbols2Json().serialize((ICD4CodeScope) cdAST.getEnclosingScope());
    SymbolTableUtil.prepareMill();
    SymbolTableUtil.addCd4cSymbols();
    OCLMill.globalScope().addSubScope(new OCLSymbols2Json().deserialize(serialized));
    SymbolTableUtil.runSymTabGenitor(oclAST);
    SymbolTableUtil.runSymTabCompleter(oclAST);
  }
}
