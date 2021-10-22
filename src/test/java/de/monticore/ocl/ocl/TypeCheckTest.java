package de.monticore.ocl.ocl;

import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.ocl.types.check.FullSynthesizeSymTypeFromMCSimpleGenericTypes;
import de.monticore.ocl.util.SymbolTableUtil;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.TypeCheck;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeCheckTest extends AbstractTest {

  @Disabled
  @Test
  public void testTypCheckForGenericMethodCalls(String arg) {
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

    TypeCheck typeCheck = new TypeCheck(new FullSynthesizeSymTypeFromMCSimpleGenericTypes(),
        new DeriveSymTypeOfOCLCombineExpressions());

    // NullpointerException, weil TypeCheck eigentlich schon vorher mit Log.error abbrechen sollte
    try {
      SymTypeExpression t =
          typeCheck.typeOf(((ASTOCLInvariant) ast.get().getOCLArtifact().getOCLConstraint(0)).getExpression());
    } catch(NullPointerException e) {
      Log.error("This should not happen. The model was valid. All types were loaded. What "
          + "happened?");
    }

    // Additional check that nothing broke
    assertThat(Log.getErrorCount()).isEqualTo(0);
  }
}
