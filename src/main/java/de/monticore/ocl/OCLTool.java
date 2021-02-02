// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl;

import com.google.common.base.Preconditions;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cd4code._cocos.CD4CodeCoCoChecker;
import de.monticore.cd4code._parser.CD4CodeParser;
import de.monticore.cd4code._symboltable.CD4CodeSymbolTableCreatorDelegator;
import de.monticore.cd4code._symboltable.ICD4CodeArtifactScope;
import de.monticore.cd4code._symboltable.ICD4CodeGlobalScope;
import de.monticore.cd4code._symboltable.ICD4CodeScope;
import de.monticore.cd4code.cocos.CD4CodeCoCos;
import de.monticore.cd4code.resolver.CD4CodeResolver;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDPackage;
import de.monticore.io.paths.ModelPath;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._cocos.OCLCoCoChecker;
import de.monticore.ocl.ocl._cocos.OCLCoCos;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.ocl.ocl._symboltable.IOCLArtifactScope;
import de.monticore.ocl.ocl._symboltable.IOCLGlobalScope;
import de.monticore.ocl.ocl._symboltable.OCLSymbolTableCreator;
import de.monticore.ocl.ocl._symboltable.OCLSymbolTableCreatorDelegator;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.ocl.util.ParserUtil;
import de.se_rwth.commons.logging.Log;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;

public class OCLTool {
  protected OCLCoCoChecker oclChecker;

  protected CD4CodeCoCoChecker cdChecker;

  protected boolean isSymTabInitialized;

  public static final String OCL_FILE_EXTENSION = "ocl";

  public static final String CD_FILE_EXTENSION = "cd";

  protected static final String TOOL_NAME = "OCLTool";

  public OCLTool() {
    this(OCLCoCos.createChecker(), new CD4CodeCoCos().createNewChecker());
  }

  public OCLTool(OCLCoCoChecker oclChecker,
    CD4CodeCoCoChecker cdChecker) {
    Preconditions.checkArgument(oclChecker != null);
    Preconditions.checkArgument(cdChecker != null);
    this.oclChecker = oclChecker;
    this.cdChecker = cdChecker;
    this.isSymTabInitialized = false;
  }

  protected OCLCoCoChecker getOCLChecker() {
    return this.oclChecker;
  }

  protected CD4CodeCoCoChecker getCdChecker() {
    return this.cdChecker;
  }

  public IOCLGlobalScope processModels(Path... modelPaths) {
    Preconditions.checkArgument(modelPaths != null);
    Preconditions.checkArgument(!Arrays.asList(modelPaths).contains(null));
    ModelPath mp = new ModelPath(Arrays.asList(modelPaths));
    ICD4CodeGlobalScope cdGlobalScope = CD4CodeMill.globalScope();
    cdGlobalScope.setModelPath(mp);
    cdGlobalScope.setFileExt(CD_FILE_EXTENSION);

    IOCLGlobalScope oclGlobalScope = OCLMill.globalScope();
    oclGlobalScope.setModelPath(mp);
    oclGlobalScope.setFileExt(OCL_FILE_EXTENSION);

    linkResolvingDelegates(oclGlobalScope, cdGlobalScope);

    this.processModels(cdGlobalScope);
    this.processModels(oclGlobalScope);
    return oclGlobalScope;
  }

  protected void linkResolvingDelegates(IOCLGlobalScope oclGlobalScope,
    ICD4CodeGlobalScope cdGlobalScope) {
    CD4CodeResolver cdTypeSymbolDelegate = new CD4CodeResolver(cdGlobalScope);
    oclGlobalScope.addAdaptedTypeSymbolResolver(cdTypeSymbolDelegate);
  }

  public void processModels(IOCLGlobalScope scope) {
    Preconditions.checkArgument(scope != null);
    for (IOCLArtifactScope as : this.createSymbolTable(scope)) {
      ASTOCLCompilationUnit a = (ASTOCLCompilationUnit) as.getAstNode();
      a.accept(this.getOCLChecker().getTraverser());
    }
  }

  public void processModels(ICD4CodeGlobalScope scope) {
    Preconditions.checkArgument(scope != null);
    for (ICD4CodeArtifactScope a : this.createSymbolTable(scope)) {
      for (ICD4CodeScope as : a.getSubScopes()) {
        ASTCDPackage astNode = (ASTCDPackage) as.getSpanningSymbol().getAstNode();
        astNode.accept(this.getCdChecker().getTraverser());
      }
    }
  }

  public Collection<IOCLArtifactScope> createSymbolTable(IOCLGlobalScope scope) {
    Preconditions.checkArgument(scope != null);
    Collection<IOCLArtifactScope> result = new HashSet<>();

    for (ASTOCLCompilationUnit ast : parseModels(scope)) {
      OCLSymbolTableCreatorDelegator symTab = new OCLSymbolTableCreatorDelegator(
        scope);

      if (!symTab.getOCLVisitor().isPresent()) {
        Log.error("0xOCL31 Symbol table is not linked to OCL visitor");
      } else {
        OCLSymbolTableCreator symTabCreator = (OCLSymbolTableCreator) symTab.getOCLVisitor().get();
        symTabCreator.setTypeVisitor(new DeriveSymTypeOfOCLCombineExpressions());
      }

      result.add(symTab.createFromAST(ast));
    }
    return result;
  }

  public Collection<ICD4CodeArtifactScope> createSymbolTable(ICD4CodeGlobalScope scope) {
    Preconditions.checkArgument(scope != null);
    Collection<ICD4CodeArtifactScope> result = new HashSet<>();
    for (ASTCDCompilationUnit ast : parseModels(scope)) {
      CD4CodeSymbolTableCreatorDelegator symTab = new CD4CodeSymbolTableCreatorDelegator(
        scope);
      result.add(symTab.createFromAST(ast));
    }
    return result;
  }

  public Collection<ASTOCLCompilationUnit> parseModels(IOCLGlobalScope scope) {
    return (Collection<ASTOCLCompilationUnit>) ParserUtil
      .parseModels(scope, OCL_FILE_EXTENSION, new OCLParser());
  }

  public Collection<ASTCDCompilationUnit> parseModels(ICD4CodeGlobalScope scope) {
    return (Collection<ASTCDCompilationUnit>) ParserUtil
      .parseModels(scope, CD_FILE_EXTENSION, new CD4CodeParser());
  }

  public Optional<ASTOCLCompilationUnit> parseOCL(String filename) {
    return (Optional<ASTOCLCompilationUnit>) ParserUtil.parse(filename, new OCLParser());
  }

  public Optional<ASTCDCompilationUnit> parseCD(String filename) {
    return (Optional<ASTCDCompilationUnit>) ParserUtil.parse(filename, new CD4CodeParser());
  }

  public Collection<ASTOCLCompilationUnit> parseOCL(Path path) {
    return (Collection<ASTOCLCompilationUnit>) ParserUtil
      .parse(path, OCL_FILE_EXTENSION, new OCLParser());
  }

  public Collection<ASTCDCompilationUnit> parseCD(Path path) {
    return (Collection<ASTCDCompilationUnit>) ParserUtil
      .parse(path, CD_FILE_EXTENSION, new CD4CodeParser());
  }
}
