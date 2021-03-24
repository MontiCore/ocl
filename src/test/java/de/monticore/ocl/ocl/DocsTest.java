// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl;

import de.monticore.ocl.OCLCLI;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._symboltable.OCLArtifactScope;
import de.monticore.ocl.ocl._symboltable.OCLDeSer;
import de.monticore.ocl.util.SymbolTableUtil;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that the example used in the tutorial is correct
 */
public class DocsTest extends AbstractTest {

  @Test
  public void shouldProcessBookshopExample() throws IOException {
    // given
    final String oclFile = "src/test/resources/docs/Bookshop/Bookshop.ocl";
    final String cdFile = "src/test/resources/docs/Bookshop/Bookshop.sym";

    // when (parse)
    final Optional<ASTOCLCompilationUnit> ast = parse(oclFile, false);
    assertThat(ast).isPresent();

    // when (create symbol table)
    SymbolTableUtil.prepareMill();
    SymbolTableUtil.addCd4cSymbols();
    SymbolTableUtil.loadSymbolFile(cdFile);
    SymbolTableUtil.runSymTabGenitor(ast.get());
    SymbolTableUtil.runSymTabCompleter(ast.get());

    // when (cocos)
    OCLCLI cli = new OCLCLI();
    cli.checkAllCoCos(ast.get());

    // when (serialize)
    OCLDeSer deSer = new OCLDeSer();
    String serialized = deSer.serialize((OCLArtifactScope) ast.get().getEnclosingScope());

    // when (deserialize)
    deSer.deserialize(serialized);
  }
}
