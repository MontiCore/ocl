/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.types3.util;

import static org.assertj.core.api.Assertions.assertThat;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.util.SymbolTableUtil;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.SymTypeRelations;
import de.monticore.types3.TypeCheck3;
import java.util.Optional;
import java.util.stream.Collectors;

import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class OCLWithinScopeBasicSymbolsResolverTest extends AbstractTest {

  @BeforeEach
  void setUp() {
    initLogger();
    SymbolTableUtil.prepareMill();
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/AuctionCD.sym");
  }

  @Test
  void passesOptionalResultFromQualifiedNameVisitor() {
    Optional<ASTOCLCompilationUnit> result = parse(
        RELATIVE_MODEL_PATH + "/testinput/types3/util/QualifiedStaticAccess.ocl",
        false
    );
    assertThat(result).isPresent();
    ASTOCLCompilationUnit compilationUnit = result.get();
    SymbolTableUtil.runSymTabGenitor(compilationUnit);
    SymbolTableUtil.runSymTabCompleter(compilationUnit);

    ASTExpression expression = ((ASTOCLInvariant) compilationUnit
        .getOCLArtifact()
        .getOCLConstraint(0))
        .getExpression();
    SymTypeExpression type = TypeCheck3.typeOf(expression);

    assertThat(type.isObscureType()).isFalse();
    assertNoFindings();
  }

  @Test
  void resolvesStandaloneTypeNameAsAllInstances() {
    Optional<ASTOCLCompilationUnit> result = parse(
        RELATIVE_MODEL_PATH
            + "/testinput/parsable/symtab/coco/not_javagen/quantifiers2.ocl",
        false
    );
    assertThat(result).isPresent();
    ASTOCLCompilationUnit compilationUnit = result.get();
    SymbolTableUtil.runSymTabGenitor(compilationUnit);
    SymbolTableUtil.runSymTabCompleter(compilationUnit);

    ASTExpression expression = ((ASTOCLInvariant) compilationUnit
        .getOCLArtifact()
        .getOCLConstraint(0))
        .getExpression();
    SymTypeExpression type = TypeCheck3.typeOf(expression);

    assertThat(SymTypeRelations.isBoolean(type)).isTrue();
    assertNoFindings();
  }


  @Test
  void resolvesEnumValue() {
    Optional<ASTOCLCompilationUnit> result = parse(
        RELATIVE_MODEL_PATH
            + "/testinput/parsable/symtab/coco/not_javagen/enums1.ocl",
        false
    );
    assertThat(result).isPresent();
    ASTOCLCompilationUnit compilationUnit = result.get();
    SymbolTableUtil.runSymTabGenitor(compilationUnit);
    SymbolTableUtil.runSymTabCompleter(compilationUnit);

    ASTExpression expression = ((ASTOCLInvariant) compilationUnit
        .getOCLArtifact()
        .getOCLConstraint(0))
        .getExpression();
    SymTypeExpression type = TypeCheck3.typeOf(expression);

    assertThat(type.isObscureType())
        .withFailMessage(() -> Log.getFindings().stream().map(Finding::toString).collect(Collectors.joining("\n")))
        .isFalse();
    assertThat(SymTypeRelations.isBoolean(type)).isTrue();
    assertNoFindings();
  }
}
