package de.monticore.ocl.ocl;

import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.ocl.types.check.FullSynthesizeSymTypeFromMCSimpleGenericTypes;
import de.monticore.ocl.util.SymbolTableUtil;
import de.monticore.types.check.TypeCheck;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class TypeCheckTest extends AbstractTest {

  @Disabled
  @Test
  public void testTypCheckForGenericMethodCalls() throws IOException {
    String filename = "list.ocl";

    // given
    final Optional<ASTOCLCompilationUnit> ast = parse(prefixValidModelsPath(filename), false);
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

    assertThat(typeCheck.typeOf(((ASTOCLInvariant) ast.get().getOCLArtifact()
        .getOCLConstraint(0)).getExpression()).print()).isEqualTo("boolean");
  }
}
