package de.monticore.oclrefadaptation;

import de.monticore.cd._symboltable.BuiltInTypes;
import de.monticore.cd4analysis._visitor.CD4AnalysisTraverser;
import de.monticore.cd4analysis.trafo.CDAssociationCreateFieldsFromAllRoles;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cd4code._symboltable.CD4CodeSymbolTableCompleter;
import de.monticore.cd4code._symboltable.CD4CodeSymbols2Json;
import de.monticore.cd4code._symboltable.ICD4CodeArtifactScope;
import de.monticore.cd4code._symboltable.ICD4CodeScope;
import de.monticore.cdassociation._visitor.CDAssociationTraverser;
import de.monticore.cdassociation.trafo.CDAssociationRoleNameTrafo;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconcretization.UnderspecifiedPlaceholderType;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._symboltable.IOCLArtifactScope;
import de.monticore.ocl.ocl._symboltable.OCLSymbolTableCompleter;
import de.monticore.ocl.ocl._symboltable.OCLSymbols2Json;
import de.monticore.ocl.ocl.types3.OCLTypeCheck3;
import de.monticore.ocl.util.SymbolTableUtil;
import de.monticore.symboltable.ImportStatement;
import de.monticore.types.mcbasictypes.MCBasicTypesMill;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.BeforeEach;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.monticore.cdconformance.CDConfParameter.*;
import static org.junit.jupiter.api.Assertions.*;

public abstract class AbstractOCLAdapterTest extends AbstractTest {

  protected static final String TEST_RES_DIR = "src/test/resources/de/monticore/oclrefadaptation/";

  /**
   * The default conformance parameters that are used for each test case if not specified otherwise.
   */
  protected static final Set<CDConfParameter> DEFAULT_CONFORMANCE_PARAMS = Set.of(
          STEREOTYPE_MAPPING, NAME_MAPPING, SRC_TARGET_ASSOC_MAPPING, INHERITANCE,
          ALLOW_CARD_RESTRICTION, METHOD_OVERLOADING);

  protected ASTCDCompilationUnit refCD;
  protected ASTCDCompilationUnit conCD;

  protected ASTOCLCompilationUnit refOCL;

  protected ASTOCLCompilationUnit expectedAdaptedOCL;

  protected Set<CDConfParameter> confParameters;

  @BeforeEach
  @Override
  protected void initLogger() {
    Log.init();
    Log.enableFailQuick(false);
  }

  @BeforeEach
  public void setupEach() {
    Log.clearFindings();
    initMills();
    confParameters = new HashSet<>(DEFAULT_CONFORMANCE_PARAMS);
  }

  @Override
  protected void initMills() {
    initOCLMill();
    //OCLMill.reset();
    //OCLMill.init();
    //OCLMill.globalScope().clear();

    initCD4CodeMill();

    // init OCL TypeCheck again after other mills
    OCLTypeCheck3.init();

  }

  protected void initCD4CodeMill() {
    CD4CodeMill.reset();
    CD4CodeMill.init();
    CD4CodeMill.globalScope().clear();
    BuiltInTypes.addBuiltInTypes(CD4CodeMill.globalScope());
    UnderspecifiedPlaceholderType.addPlaceholderType(CD4CodeMill.globalScope(), OCLAdapter.DEFAULT_UNDERSPECIFIED_TYPE_NAME);
  }

  protected void initOCLMill() {
    SymbolTableUtil.prepareMill();
    SymbolTableUtil.addCd4cSymbols();
    UnderspecifiedPlaceholderType.addPlaceholderType(OCLMill.globalScope(), OCLAdapter.DEFAULT_UNDERSPECIFIED_TYPE_NAME);
  }

  /**
   * Tests the adaptation of a single OCL artifact against a concrete and a reference class diagram.
   * The test passes if the adapted OCL artifact equals the expected one (ignoring same order in AST)
   *
   * @param conCDFile the concrete CD
   * @param refCDFile the reference CD
   * @param refOCLFile the reference OCL artifact
   * @param expectedOCLFile the expected adapted OCL artifact
   */
  protected ASTOCLCompilationUnit testAdaptedEqualsExpected(
      String conCDFile, String refCDFile, String refOCLFile, String expectedOCLFile) {
    parseModels(conCDFile, refCDFile, refOCLFile, expectedOCLFile);
    OCLAdapter oclAdapter = new OCLAdapter(confParameters);
    List<ASTOCLCompilationUnit> adaptedOCLList = oclAdapter.adapt(conCD, refCD, "ref", List.of(refOCL));
    assertEquals(1, adaptedOCLList.size(),
        "Expected exactly one adapted OCL artifact for a single reference artifact");
    ASTOCLCompilationUnit adaptedOCL = adaptedOCLList.get(0);

    System.out.println("Adapted OCL: \n");
    System.out.println(OCLMill.prettyPrint(adaptedOCL, true));

    assertTrue(expectedAdaptedOCL.deepEquals(adaptedOCL, false),
            "Expected adapted OCL does not match the actual one");
    return adaptedOCL;
  }

  public static void createRoleNamesIfAbsent(ASTCDCompilationUnit ast) {
    final CDAssociationTraverser traverser = CD4CodeMill.inheritanceTraverser();
    /*
     * NOTE: Although in ocl2smt there is a comment this Trafo needs to be applied after the
     * symbol table was created, this is wrong! Looking at CDAssociationDirectCompositionTrafo
     * the docs state that the Trafo should be applied before the symbol table is created!
     */
    traverser.add4CDAssociation(new CDAssociationRoleNameTrafo());
    ast.accept(traverser);
  }

  protected static void transformAllRoles(ASTCDCompilationUnit cdAST) {
    final CDAssociationCreateFieldsFromAllRoles cdAssociationCreateFieldsFromAllRoles =
            new CDAssociationCreateFieldsFromAllRoles();
    final CD4AnalysisTraverser traverser = CD4CodeMill.inheritanceTraverser();
    traverser.add4CDAssociation(cdAssociationCreateFieldsFromAllRoles);
    traverser.setCDAssociationHandler(cdAssociationCreateFieldsFromAllRoles);
    cdAssociationCreateFieldsFromAllRoles.transform(cdAST);
  }

  protected void parseModels(String concreteCDFile, String refCDDFile, String refOCLFile, String expectedOCLFile) {
    conCD = loadCD(concreteCDFile);
    transformAllRoles(conCD);

    refCD = loadCD(refCDDFile);
    transformAllRoles(refCD);

    /*
     * TODO Cleanup & make sure everything is working fine. if we cannot compare symbols in CD4CodeMill and OCLMill global scopes we have issues...
     */


    refOCL = parseOCL(refOCLFile);
    createOCLSymTab(refOCL);
    loadCDModel(refOCL, refCD);

    expectedAdaptedOCL = parseOCL(expectedOCLFile);
    createOCLSymTab(expectedAdaptedOCL);
    loadCDModel(expectedAdaptedOCL, conCD);


    //refOCL = loadOCL(refOCLFile);
    //expectedAdaptedOCL = loadOCL(expectedOCLFile);
  }

  protected void loadCDModel(ASTOCLCompilationUnit oclAST, ASTCDCompilationUnit cdAST) {
    String serialized =
            new CD4CodeSymbols2Json().serialize((ICD4CodeScope) cdAST.getEnclosingScope());
    /*
     TODO is there a cleaner way to load the models ?
     * We need to reset the Mill here to avoid that symbols form CD4A are resolved form the global scope
     */
    initOCLMill();

    OCLMill.globalScope().addSubScope(new OCLSymbols2Json().deserialize(serialized));
    SymbolTableUtil.runSymTabGenitor(oclAST);
    SymbolTableUtil.runSymTabCompleter(oclAST);
  }

  protected static void createOCLSymTab(ASTOCLCompilationUnit ast) {
    IOCLArtifactScope as = OCLMill.scopesGenitorDelegator().createFromAST(ast);
    as.addImports(new ImportStatement("java.lang.String", true));
    as.addImports(new ImportStatement("java.util.Date", true));
    OCLSymbolTableCompleter c =
            new OCLSymbolTableCompleter(
                    ast.getMCImportStatementList(),
                    MCBasicTypesMill.mCQualifiedNameBuilder().build().getQName());
    c.setTraverser(OCLMill.inheritanceTraverser());
    ast.accept(c.getTraverser());
    ast.setEnclosingScope(as);
  }

  protected static ICD4CodeArtifactScope createCDSymTab(ASTCDCompilationUnit ast) {
    BuiltInTypes.addBuiltInTypes(CD4CodeMill.globalScope());
    ICD4CodeArtifactScope as = CD4CodeMill.scopesGenitorDelegator().createFromAST(ast);
    ast.accept(new CD4CodeSymbolTableCompleter(ast).getTraverser());
    return as;
  }

  public static ASTCDCompilationUnit loadCD(String filePath) {
    ASTCDCompilationUnit cd;
    // 1. parse CD
    try {
      cd = CD4CodeMill.parser().parseCDCompilationUnit(TEST_RES_DIR + filePath).orElseThrow(
              () -> new RuntimeException("Could not parse CD: " + filePath));
    }
    catch (IOException e) {
      throw new RuntimeException("Failed to load CD: " + filePath, e);
    }
    // 2. AST trafo adding all implicit role names
    createRoleNamesIfAbsent(cd);

    // 3. create symbol table
    cd.setEnclosingScope(createCDSymTab(cd));

    assertNoFindings();
    return cd;
  }

  public ASTOCLCompilationUnit parseOCL(String filePath) {
    ASTOCLCompilationUnit ocl = parse(TEST_RES_DIR + filePath, false)
            .orElseThrow(() -> new RuntimeException("Could not parse OCL: " + filePath));
    assertNoFindings();
    return ocl;
  }
}
