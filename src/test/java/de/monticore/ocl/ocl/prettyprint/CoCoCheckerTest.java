/*
 *  * (c) https://github.com/MontiCore/monticore
 *  *
 *  * The license generally applicable for this project
 *  * can be found under https://github.com/MontiCore/monticore.
 */

package de.monticore.ocl.ocl.prettyprint;

import com.google.common.collect.Lists;
import de.monticore.cd.cd4analysis._symboltable.CD4AnalysisGlobalScope;
import de.monticore.cd.cd4analysis._symboltable.CD4AnalysisSymTabMill;
import de.monticore.expressions.expressionsbasis._symboltable.ExpressionsBasisSymTabMill;
import de.monticore.expressions.expressionsbasis._symboltable.IExpressionsBasisScope;
import de.monticore.io.paths.ModelPath;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._cocos.OCLCoCos;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.ocl.ocl._symboltable.*;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.check.*;
import de.monticore.types.typesymbols._symboltable.TypeSymbol;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

import static de.monticore.types.check.DefsTypeBasic.add2scope;
import static de.monticore.types.check.DefsTypeBasic.type;
import static org.junit.Assert.fail;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class CoCoCheckerTest {
  private OCLSymbolTableCreatorDelegator symbolTableCreator;

  // This is an auxiliary
  DeriveSymTypeOfOCLCombineExpressions derLit = new DeriveSymTypeOfOCLCombineExpressions(ExpressionsBasisSymTabMill
      .expressionsBasisScopeBuilder()
      .build());

  // This is the TypeChecker under Test:
  TypeCheck tc = new TypeCheck(new SynthesizeSymTypeFromMCCollectionTypes(), derLit);

  @BeforeClass
  public static void init() {
    LogStub.init();
    Log.enableFailQuick(false);
  }

  @Before
  public void setUp() {
    Log.getFindings().clear();
  }

  @Before
  public void doBefore() {
    LogStub.init();
    DefsTypeBasic.setup();

    final OCLLanguage language = OCLSymTabMill.oCLLanguageBuilder().build();
    final OCLGlobalScope globalScope = OCLSymTabMill
        .oCLGlobalScopeBuilder()
        .setModelPath(new ModelPath(Paths.get("src/test/resources")))
        .setOCLLanguage(language)
        .build();

    final CD4AnalysisGlobalScope cd4AnalysisGlobalScope =
        CD4AnalysisSymTabMill.cD4AnalysisGlobalScopeBuilder()
            .setModelPath(globalScope.getModelPath())
            .setCD4AnalysisLanguage(CD4AnalysisSymTabMill.cD4AnalysisLanguageBuilder().build())
            .build();
    globalScope.addAdaptedTypeSymbolResolvingDelegate(new CDTypeSymbolDelegate(cd4AnalysisGlobalScope));

    symbolTableCreator = OCLSymTabMill
        .oCLSymbolTableCreatorDelegatorBuilder()
        .setGlobalScope(globalScope)
        .build();
  }

  private void createPolicy(IExpressionsBasisScope scope) {
    // person
    SymTypeExpression supclass = SymTypeExpressionFactory.createTypeObject("Policy", scope);
    TypeSymbol superclass = type("Policy", Collections.emptyList(), Collections.emptyList(),
        Lists.newArrayList(), Lists.newArrayList()
    );
    add2scope(scope, superclass);
  }

  @Test
  public void validConstructorName0() throws IOException {
    final OCLParser parser = new OCLParser();
    final Optional<ASTOCLCompilationUnit> astOCLCompilationUnit = parser.parseOCLCompilationUnit("src/test/resources/example/cocos/valid/validConstructorName0.ocl");
    System.out.println(parser.hasErrors());
    System.out.println(Log.getFindings());
    final ASTOCLCompilationUnit ast = astOCLCompilationUnit.get();
    final OCLScope st = createSTFromAST(ast, true);
    OCLCoCos.createChecker(derLit).checkAll(ast);

    final OCLCombinePrettyPrinter printer = new OCLCombinePrettyPrinter(new IndentPrinter());
    String output = printer.prettyprint(ast);
    System.out.println(output);
  }

  @Test
  public void validConstructorName() throws IOException {
    final OCLParser parser = new OCLParser();
    final ASTOCLCompilationUnit ast = parser.parseOCLCompilationUnit("src/test/resources/example/cocos/valid/validConstructorName.ocl").get();
    createSTFromAST(ast, true);
    OCLCoCos.createChecker(derLit).checkAll(ast);

    final OCLCombinePrettyPrinter printer = new OCLCombinePrettyPrinter(new IndentPrinter());
    String output = printer.prettyprint(ast);
    System.out.println(output);
  }

  private OCLScope createSTFromAST(ASTOCLCompilationUnit astex, boolean failOnError) {
    symbolTableCreator.setTypeVisitor(derLit);

    final OCLGlobalScope globalScope = (OCLGlobalScope) symbolTableCreator.getGlobalScope();

    //createPolicy(scope);

    final OCLArtifactScope fromAST = symbolTableCreator.createFromAST(astex);
/*
    final ASTOCLConstructorSignature oclOperationSignature = (ASTOCLConstructorSignature) ((ASTOCLOperationConstraint) astex.getOCLFile().getOCLConstraint(0)).getOCLOperationSignature();
    final IOCLScope enclosingScope = oclOperationSignature.getEnclosingScope();
    final String referenceType = oclOperationSignature.getReferenceType();
    final Optional<TypeSymbol> typeSymbol = enclosingScope.resolveType(referenceType);
*/
    derLit.setScope(fromAST.getEnclosingScope());

    if (failOnError) {
      printFindingsAndFail();
    }

    return fromAST;
  }

  private void printFindingsAndFail() {
    if (!Log.getFindings().isEmpty()) {
      Log.getFindings().forEach(System.out::println);
      fail();
    }
  }
}
