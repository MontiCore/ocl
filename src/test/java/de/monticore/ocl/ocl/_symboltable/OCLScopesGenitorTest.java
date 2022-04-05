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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Optional;

public class OCLScopesGenitorTest extends AbstractTest {

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
    String file = prefixValidModelsPath(filename);
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
      for (ASTOCLConstraint c: ast.get().getOCLArtifact().getOCLConstraintList()) {
      Assertions.assertNotNull(c.getEnclosingScope(),
        String.format("%s: The ast of the ocl constraint is missing its enclosing scope.", c.get_SourcePositionStart()));
      }
    });
  }

  @ParameterizedTest
  @ValueSource(strings = {
    "inv A : true;",
    "inv B() : true;",
    "inv C(int a) : true;",
    "inv : true;"
  })
  public void handleInvariant_shouldNotModifyScopeStack(String inv) throws IOException {
    // Given
    Optional<ASTOCLInvariant> ast = OCLMill.parser().parse_StringOCLInvariant(inv);
    OCLScopesGenitorDelegator genitor = OCLMill.scopesGenitorDelegator();
    IOCLArtifactScope scope = OCLMill.artifactScope();
    genitor.scopeStack.add(scope);

    Preconditions.checkState(!OCLMill.parser().hasErrors(),
      String.format("There where errors parsing the invariant `%s`. " +
        "The log lists the following findings: %s", inv, Log.getFindings()));
    Preconditions.checkState(ast.isPresent(),
      String.format("The parser did not return an abstract syntax tree for invariant '%s'. " +
        "The log lists the following findings: %s", inv, Log.getFindings()));
    Preconditions.checkState(genitor.scopeStack.getLast().equals(scope),
      "The added scope should be the scope at the tail of the scope stack but is not.");

    // When
    ast.get().accept(genitor.traverser);

    // Then
    Assertions.assertEquals(scope, genitor.scopeStack.getLast(), "The scope at the tail of the scope stack " +
      "should match the manually added scope but does not. Thus, the genitor either removes too many scopes or " +
      "does not remove all added scopes.");
  }
}
