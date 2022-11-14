/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.ocl._symboltable;

import com.google.common.base.Preconditions;
import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLConstraint;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

public class OCLScopesGenitorTest extends AbstractTest {

  protected final String DEBUG_LOG_NAME = "OCLScopesGenitorTest";

  protected static Stream<String> invariantProvider() {
    return Stream.concat(invariantWithoutNameProvider(), invariantWithNameProvider());
  }

  protected static Stream<String> invariantWithoutNameProvider() {
    return Stream.of("inv: true;");
  }

  protected static Stream<String> invariantWithNameProvider() {
    return Stream.of(
      "inv A: true;",
      "inv B(): true;",
      "inv C(int a): true;",
      "inv D(int a, int b): true;"
    );
  }

  @Override
  @BeforeEach
  public void setUp() {
    super.setUp();
    OCLMill.globalScope().clear();
    OCLMill.init();
  }

  @ParameterizedTest
  @MethodSource("getModelsWithValidSymTab")
  public void shouldSetEnclosingScopeOfOCLConstraint(String filename) throws IOException {
    // Given
    String file = prefixValidModelsPath("/testinput/validGrammarModels/"+filename);
    Optional<ASTOCLCompilationUnit> ast = OCLMill.parser().parse(file);
    OCLScopesGenitorDelegator genitor = OCLMill.scopesGenitorDelegator();

    Preconditions.checkState(!OCLMill.parser().hasErrors(),
      String.format("There where errors parsing the input file `%s`. " +
        "The log lists the following findings: %s", file, Log.getFindings()));
    Preconditions.checkState(ast.isPresent(),
      String.format("The parser did not return an abstract syntax tree for input file '%s'. " +
        "The log lists the following findings: %s", file, Log.getFindings()));

    // When
    genitor.createFromAST(ast.get());

    // Then
    Assertions.assertAll(() -> {
      for (ASTOCLConstraint c : ast.get().getOCLArtifact().getOCLConstraintList()) {
        Assertions.assertNotNull(c.getEnclosingScope(),
          String.format("%s: The ast of the ocl constraint is missing its enclosing scope.", c.get_SourcePositionStart()));
      }
    });
  }

  @ParameterizedTest
  @MethodSource("invariantProvider")
  @Order(0)
  public void handleInvariant_shouldNotModifyScopeStack(String inv) {
    // Given
    ASTOCLInvariant ast = parseInvariant(inv);
    OCLScopesGenitorDelegator genitor = OCLMill.scopesGenitorDelegator();
    IOCLArtifactScope scope = OCLMill.artifactScope();
    genitor.scopeStack.addLast(scope);

    // When
    ast.accept(genitor.traverser);

    // Then
    Assertions.assertEquals(scope, genitor.scopeStack.getLast(), "The scope at the tail of the scope stack " +
      "should match the manually added scope but does not. Thus, the genitor either removes too many scopes or " +
      "does not remove all added scopes.");
  }

  /**
   * Asserts that the enclosing scope of the ast of the ocl invariant is present
   * and matches the expected scope.
   *
   * @param inv the String assumed to be an ocl invariant
   */
  @ParameterizedTest
  @MethodSource("invariantProvider")
  @Order(1)
  public void handleInvariant_shouldSetEnclosingScope(String inv) {
    // Given
    ASTOCLInvariant ast = parseInvariant(inv);
    OCLScopesGenitorDelegator genitor = OCLMill.scopesGenitorDelegator();
    genitor.scopeStack.addLast(OCLMill.artifactScope());
    IOCLScope enclosingScope = OCLMill.scope();
    genitor.scopeStack.addLast(enclosingScope);

    // When
    ast.accept(genitor.traverser);

    // Then
    Assertions.assertAll(() -> {
      Assertions.assertNotNull(ast.getEnclosingScope(),
        "The ast of the ocl invariant is missing its enclosing scope.");
      Assertions.assertEquals(enclosingScope, ast.getEnclosingScope(),
        "The enclosing scope of the ast of the ocl invariant does not match the expected scope.");
    });
  }

  /**
   * Asserts that the spanned scope of the ast of the ocl invariant is present
   * and linked with the enclosing scope.
   *
   * @param inv the String assumed to be an ocl invariant
   */
  @ParameterizedTest
  @MethodSource("invariantProvider")
  @Order(2)
  public void handleInvariant_shouldCreateSpannedScope(String inv) {
    // Given
    ASTOCLInvariant ast = parseInvariant(inv);
    OCLScopesGenitorDelegator genitor = OCLMill.scopesGenitorDelegator();
    genitor.scopeStack.addLast(OCLMill.artifactScope());
    genitor.scopeStack.addLast(OCLMill.scope());

    // When
    ast.accept(genitor.traverser);

    // Then
    Assertions.assertAll(() -> {
      Assertions.assertNotNull(ast.getSpannedScope(),
        "The ast of the ocl invariant is missing its spanned scope.");
      Assertions.assertTrue(ast.getSpannedScope().isPresentAstNode(),
        "The spanned scope is not linked with the ast of the ocl invariant.");
      Assertions.assertEquals(ast, ast.getSpannedScope().getAstNode(),
        "The ast of the spanned scope does not match the ast of the ocl invariant.");
      Assertions.assertNotNull(ast.getSpannedScope().getEnclosingScope(),
        "The enclosing scope of the spanned scope of the ocl invariant is missing.");
      Assertions.assertEquals(ast.getEnclosingScope(), ast.getSpannedScope().getEnclosingScope(),
        "The enclosing scope of the spanned scope of the ocl invariant does not match the expected scope.");
      Assertions.assertTrue(ast.getEnclosingScope().getSubScopes().contains(ast.getSpannedScope()),
        "The enclosing scope of the ocl invariant does not contain the spanned scope as sub-scope.");
    });
  }

  /**
   * Asserts that the symbol of the ocl invariant is present and linked with
   * its ast, spanned scope, and enclosing scope.
   *
   * @param inv the String assumed to be an ocl invariant with name
   */
  @ParameterizedTest
  @MethodSource("invariantWithNameProvider")
  @Order(3)
  public void handleInvariant_shouldCreateSymbol(String inv) {
    // Given
    ASTOCLInvariant ast = parseInvariant(inv);
    Preconditions.checkState(ast.isPresentName());
    OCLScopesGenitorDelegator genitor = OCLMill.scopesGenitorDelegator();
    genitor.scopeStack.addLast(OCLMill.artifactScope());
    IOCLScope enclosingScope = OCLMill.scope();
    genitor.scopeStack.addLast(enclosingScope);

    // When
    ast.accept(genitor.traverser);

    // Then
    Assertions.assertAll(() -> {
      Assertions.assertTrue(ast.isPresentSymbol(),
        "The ast of the ocl invariant is not linked with its symbol.");
      Assertions.assertTrue(ast.getSpannedScope().isPresentSpanningSymbol(),
        "The spanning symbol of the scope spanned by the ocl invariant is missing.");
      Assertions.assertTrue(ast.getSpannedScope().getSpanningSymbol().isPresentAstNode(),
        "The symbol of the ocl invariant is not linked with its ast.");
      Assertions.assertNotNull(ast.getSpannedScope().getSpanningSymbol().getEnclosingScope(),
        "The symbol of the ocl invariant is missing its enclosing scope.");
      Assertions.assertEquals(ast.getSymbol(), ast.getSpannedScope().getSpanningSymbol(),
        "The symbol of the ast and the spanning symbol of the scope spanned by the ocl invariant do not match.");
      Assertions.assertEquals(ast.getSpannedScope(), ast.getSpannedScope().getSpanningSymbol().getSpannedScope(),
        "The scope spanned by the ocl invariant does not match the scope of its spanning symbol.");
      Assertions.assertEquals(ast, ast.getSpannedScope().getSpanningSymbol().getAstNode(),
        "The ast of the ocl invariant and the ast of its symbol do not match.");
      Assertions.assertEquals(ast.getEnclosingScope(), ast.getSpannedScope().getSpanningSymbol().getEnclosingScope(),
        "The enclosing scope of the symbol of the ocl invariant does not match the enclosing scope of its ast.");
      Assertions.assertTrue(enclosingScope.getLocalOCLInvariantSymbols().contains(ast.getSymbol()),
        "The enclosing scope of the ocl invariant does not contain the spanning symbol as local symbol.");
    });
  }

  /**
   * Parsers the provided string as ocl invariant, catching exceptions to the
   * debug log and throwing runtime errors in case of parser errors.
   *
   * @param inv the String to parse, assumed to be an ocl invariant,
   *            e.g., of the form `inv Name? ( Parameters*)? : Expression;`
   * @return the ast of the given String
   */
  protected ASTOCLInvariant parseInvariant(String inv) {
    Optional<ASTOCLInvariant> ast;
    try {
      ast = OCLMill.parser().parse_StringOCLInvariant(inv);
    } catch (IOException e) {
      Log.debug("An I/O Exception occurred parsing the invariant `%s`. ", DEBUG_LOG_NAME);
      Log.debug("Error thrown: " + e, DEBUG_LOG_NAME);
      throw new IllegalStateException(" An internal error occurred. See the debug log.");
    }
    Preconditions.checkState(!OCLMill.parser().hasErrors(),
      String.format("There where errors parsing the invariant `%s`. " +
        "The log lists the following findings: %s", inv, Log.getFindings()));
    Preconditions.checkState(ast.isPresent(),
      String.format("The parser did not return an abstract syntax tree for invariant '%s'. " +
        "The log lists the following findings: %s", inv, Log.getFindings()));
    return ast.get();
  }
}
