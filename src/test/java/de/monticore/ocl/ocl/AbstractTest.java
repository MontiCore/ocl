// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl;

import com.google.common.collect.Sets;
import de.monticore.cd4analysis.CD4AnalysisMill;
import de.monticore.cd4analysis._parser.CD4AnalysisParser;
import de.monticore.cd4analysis._symboltable.CD4AnalysisGlobalScope;
import de.monticore.cd4analysis._symboltable.CD4AnalysisSymbolTableCreatorDelegator;
import de.monticore.cd4analysis.resolver.CD4AnalysisResolver;
import de.monticore.cd4analysis.trafo.CD4AnalysisTrafo4DefaultsDelegator;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.io.paths.ModelPath;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.ocl.ocl._symboltable.IOCLGlobalScope;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

public abstract class AbstractTest {
  protected static final String RELATIVE_MODEL_PATH = "src/test/resources";

  protected IOCLGlobalScope globalScope;

  public static String[] getValidModels() {
    File f = new File(RELATIVE_MODEL_PATH + "/testinput/validGrammarModels");
    String[] filenames = f.list();
    assertThat(filenames).isNotNull();
    filenames = Arrays.stream(filenames)
      .sorted()
      .collect(Collectors.toList())
      .toArray(filenames);

    return filenames;
  }

  public static String prefixValidModelsPath(String fileName) {
    return RELATIVE_MODEL_PATH + "/testinput/validGrammarModels/" + fileName;
  }

  public Optional<ASTOCLCompilationUnit> parse(String relativeFilePath,
    boolean expParserErrors) {
    OCLParser parser = new OCLParser();
    Optional<ASTOCLCompilationUnit> optAst;
    try {
      optAst = parser.parse(relativeFilePath);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    if (expParserErrors) {
      assertThat(parser.hasErrors()).isTrue();
      assertThat(optAst).isNotPresent();
    } else {
      assertThat(parser.hasErrors()).isFalse();
      assertThat(optAst).isPresent();
    }
    return optAst;
  }

  public void setupGlobalScope() {
    this.globalScope = OCLMill.globalScope();
    this.globalScope.setModelPath(new ModelPath(Paths.get(RELATIVE_MODEL_PATH)));
    this.globalScope.setFileExt("ocl");
  }

  public void initSymbolTable(String model, String modelPath) throws IOException {
    initSymbolTable(model, Paths.get(modelPath).toFile());
  }


  public void initSymbolTable(String model, File... modelPaths) throws IOException {
    Set<Path> p = Sets.newHashSet();
    for (File mP : modelPaths) {
      p.add(Paths.get(mP.getAbsolutePath()));
    }

    final ModelPath mp = new ModelPath(p);

    CD4AnalysisGlobalScope cd4AGlobalScope = (CD4AnalysisGlobalScope) CD4AnalysisMill.globalScope();
    cd4AGlobalScope.setModelPath(mp);
    cd4AGlobalScope.setFileExt("cd");

    CD4AnalysisSymbolTableCreatorDelegator symbolTableCreatorDelegator = new CD4AnalysisSymbolTableCreatorDelegator(cd4AGlobalScope);
    Optional<ASTCDCompilationUnit> ast = new CD4AnalysisParser().parse(Paths.get(RELATIVE_MODEL_PATH + model).toString());
    CD4AnalysisTrafo4DefaultsDelegator a = new CD4AnalysisTrafo4DefaultsDelegator();
    a.transform(ast.get());
    symbolTableCreatorDelegator.createFromAST(ast.get());

    CD4AnalysisResolver cdResolver = new CD4AnalysisResolver(cd4AGlobalScope);

    setupGlobalScope();
    globalScope.addAdaptedTypeSymbolResolver(cdResolver);
  }
}
