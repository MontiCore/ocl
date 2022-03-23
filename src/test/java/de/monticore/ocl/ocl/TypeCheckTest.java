package de.monticore.ocl.ocl;

import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.types.check.OCLTypeCalculator;
import de.monticore.ocl.util.SymbolTableUtil;
import de.monticore.types.check.TypeCheckResult;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeCheckTest extends AbstractTest {

  @Test
  public void testTypCheckForGenericMethodCalls() {
    String filename = "list.ocl";

    Log.enableFailQuick(false);

    // given
    final Optional<ASTOCLCompilationUnit> ast =
        parse(RELATIVE_MODEL_PATH + "/testinput"+"/typeInferringModels/" +filename,
        false);
    assertThat(ast).isPresent();

    SymbolTableUtil.prepareMill();
    SymbolTableUtil.addCd4cSymbols();
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/AuctionCD.sym");
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/DefaultTypes.sym");

    // when / then
    SymbolTableUtil.runSymTabGenitor(ast.get());
    SymbolTableUtil.runSymTabCompleter(ast.get());

    OCLTypeCalculator typeCalculator = new OCLTypeCalculator();

    TypeCheckResult t = typeCalculator.deriveType(((ASTOCLInvariant) ast.get().getOCLArtifact().getOCLConstraint(0)).getExpression());

    // Additional check that nothing broke
    assertThat(Log.getErrorCount()).isEqualTo(0);
    assertThat(t.isPresentCurrentResult()).isTrue();
  }
}
