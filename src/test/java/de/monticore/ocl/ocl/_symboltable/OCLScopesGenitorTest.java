/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.ocl._symboltable;

import com.google.common.base.Preconditions;
import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLConstraint;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

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
  void shouldSetScopeOfOCLConstraint(String filename) throws IOException {
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
}
