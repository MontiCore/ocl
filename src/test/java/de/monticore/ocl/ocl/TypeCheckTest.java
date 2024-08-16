/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.ocl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.util.SymbolTableUtil;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class TypeCheckTest extends AbstractTest {

  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
  }

  @Test
  public void testTypCheckForGenericMethodCalls() {
    String filename = "list_typeinferringModels.ocl";

    Log.enableFailQuick(false);

    // given
    final Optional<ASTOCLCompilationUnit> ast =
        parse(
            RELATIVE_MODEL_PATH + "/testinput/parsable/symtab/coco/not_javagen/" + filename, false);
    assertThat(ast).isPresent();

    SymbolTableUtil.prepareMill();
    SymbolTableUtil.addCd4cSymbols();
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/AuctionCD.sym");
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/DefaultTypes.sym");

    // when / then
    SymbolTableUtil.runSymTabGenitor(ast.get());
    SymbolTableUtil.runSymTabCompleter(ast.get());

    ASTExpression expr =
        ((ASTOCLInvariant) ast.get().getOCLArtifact().getOCLConstraint(0)).getExpression();
    SymTypeExpression type = TypeCheck3.typeOf(expr);

    // Additional check that nothing broke
    assertNoFindings();
    assertFalse(type.isObscureType());
  }
}
