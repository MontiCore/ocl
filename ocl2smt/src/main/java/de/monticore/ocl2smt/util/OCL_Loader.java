/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.util;

import de.monticore.cd._symboltable.BuiltInTypes;
import de.monticore.cd4analysis.CD4AnalysisMill;
import de.monticore.cd4analysis._visitor.CD4AnalysisTraverser;
import de.monticore.cd4analysis.trafo.CDAssociationCreateFieldsFromAllRoles;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cd4code._cocos.CD4CodeCoCoChecker;
import de.monticore.cd4code._parser.CD4CodeParser;
import de.monticore.cd4code._symboltable.CD4CodeSymbolTableCompleter;
import de.monticore.cd4code._symboltable.CD4CodeSymbols2Json;
import de.monticore.cd4code._symboltable.ICD4CodeArtifactScope;
import de.monticore.cd4code._symboltable.ICD4CodeScope;
import de.monticore.cd4code._visitor.CD4CodeTraverser;
import de.monticore.cd4code.cocos.CD4CodeCoCosDelegator;
import de.monticore.cdassociation._visitor.CDAssociationTraverser;
import de.monticore.cdassociation.trafo.CDAssociationRoleNameTrafo;
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
import de.monticore.od4report.OD4ReportTool;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.symboltable.ImportStatement;
import de.monticore.types.mcbasictypes.MCBasicTypesMill;
import de.se_rwth.commons.logging.Log;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

public class OCL_Loader {

  public static ASTCDCompilationUnit loadAndCheckCD(File cdFile) throws IOException {
    assert cdFile.getName().endsWith(".cd");
    ASTCDCompilationUnit cdAST = parseCDModel(cdFile.getAbsolutePath());

    // apply role name trafo
    CD4CodeTraverser t = CD4CodeMill.traverser();
    t.add4CDAssociation(new CDAssociationRoleNameTrafo());
    cdAST.accept(t);

    cdAST.setEnclosingScope(createCDSymTab(cdAST));
    setAssociationsRoles(cdAST);
    checkCDCoCos(cdAST);

    return cdAST;
  }

  public static ASTODArtifact loadAndCheckOD(File odFile) {
    assert odFile.getName().endsWith(".od");
    return new OD4ReportTool().parse(odFile.getAbsolutePath());
  }

  public static void setAssociationsRoles(ASTCDCompilationUnit ast) {
    // transformations that need an already created symbol table
    createCDSymTab(ast);
    final CDAssociationRoleNameTrafo cdAssociationRoleNameTrafo = new CDAssociationRoleNameTrafo();
    final CDAssociationTraverser traverser = CD4AnalysisMill.traverser();
    traverser.add4CDAssociation(cdAssociationRoleNameTrafo);
    ast.accept(traverser);
  }

  protected static void transformAllRoles(ASTCDCompilationUnit cdAST) {
    final CDAssociationCreateFieldsFromAllRoles cdAssociationCreateFieldsFromAllRoles =
        new CDAssociationCreateFieldsFromAllRoles();
    final CD4AnalysisTraverser traverser = CD4AnalysisMill.traverser();
    traverser.add4CDAssociation(cdAssociationCreateFieldsFromAllRoles);
    traverser.setCDAssociationHandler(cdAssociationCreateFieldsFromAllRoles);
    cdAssociationCreateFieldsFromAllRoles.transform(cdAST);
  }

  public static ASTOCLCompilationUnit loadAndCheckOCL(File oclFile, File cdFile)
      throws IOException {
    ASTCDCompilationUnit cdAST = loadAndCheckCD(cdFile);
    transformAllRoles(cdAST);
    assert oclFile.getName().endsWith(".ocl");
    ASTOCLCompilationUnit oclAST = parseOCLModel(oclFile.getAbsolutePath());

    oclAST.setEnclosingScope(createOCLSymTab(oclAST));

    createCDSymTab(cdAST);
    loadCDModel(oclAST, cdAST);
    checkOCLCoCos(oclAST);
    return oclAST;
  }

  protected static ASTCDCompilationUnit parseCDModel(String cdFilePath) throws IOException {
    CD4CodeParser cdParser = new CD4CodeParser();
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

  protected static ICD4CodeArtifactScope createCDSymTab(ASTCDCompilationUnit ast) {
    BuiltInTypes.addBuiltInTypes(CD4CodeMill.globalScope());
    ICD4CodeArtifactScope as = CD4CodeMill.scopesGenitorDelegator().createFromAST(ast);
    as.addImports(new ImportStatement("java.lang", true));
    as.addImports(new ImportStatement("java.util", true));
    CD4CodeSymbolTableCompleter c =
        new CD4CodeSymbolTableCompleter(
            ast.getMCImportStatementList(), MCBasicTypesMill.mCQualifiedNameBuilder().build());
    ast.accept(c.getTraverser());
    return as;
  }

  protected static IOCLArtifactScope createOCLSymTab(ASTOCLCompilationUnit ast) {
    IOCLArtifactScope as = OCLMill.scopesGenitorDelegator().createFromAST(ast);
    as.addImports(new ImportStatement("java.lang", true));
    as.addImports(new ImportStatement("java.util", true));
    OCLSymbolTableCompleter c =
        new OCLSymbolTableCompleter(
            ast.getMCImportStatementList(),
            MCBasicTypesMill.mCQualifiedNameBuilder().build().getQName());
    c.setTraverser(OCLMill.traverser());
    ast.accept(c.getTraverser());
    return as;
  }

  protected static void checkCDCoCos(ASTCDCompilationUnit cdAST) {
    CD4CodeCoCoChecker cdChecker = new CD4CodeCoCosDelegator().getCheckerForAllCoCos();
    cdChecker.checkAll(cdAST);
  }

  protected static void checkOCLCoCos(ASTOCLCompilationUnit oclAST) {
    OCLCoCoChecker oclChecker = OCLCoCos.createChecker();
    oclChecker.checkAll(oclAST);
  }

  protected static void loadCDModel(ASTOCLCompilationUnit oclAST, ASTCDCompilationUnit cdAST) {
    String serialized =
        new CD4CodeSymbols2Json().serialize((ICD4CodeScope) cdAST.getEnclosingScope());
    //  serialized = serialized.replaceAll("Optional", "java.util.Optional");
    Log.trace(serialized, OCL_Loader.class.getName());
    SymbolTableUtil.prepareMill();
    SymbolTableUtil.addCd4cSymbols();
    OCLMill.globalScope().addSubScope(new OCLSymbols2Json().deserialize(serialized));
    SymbolTableUtil.runSymTabGenitor(oclAST);
    SymbolTableUtil.runSymTabCompleter(oclAST);
  }
}
