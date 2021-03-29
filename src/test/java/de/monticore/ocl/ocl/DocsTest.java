// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl;

import de.monticore.ocl.OCLCLI;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._symboltable.OCLArtifactScope;
import de.monticore.ocl.ocl._symboltable.OCLDeSer;
import de.monticore.ocl.ocl.prettyprint.OCLFullPrettyPrinter;
import de.monticore.ocl.util.SymbolTableUtil;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks that the example used in the tutorial is correct
 */
public class DocsTest extends AbstractTest {

  @ParameterizedTest
  @CsvSource({
    "src/test/resources/docs/Bookshop/Bookshop.ocl, src/test/resources/docs/Bookshop/Bookshop.sym",
    "src/test/resources/docs/Banking/Banking.ocl, src/test/resources/docs/Banking/Banking.cdsym",
  })
  public void shouldProcessExampleFromDocs(final String oclFile, final String cdFile) {
    // given

    // when (parse)
    final Optional<ASTOCLCompilationUnit> ast = parse(oclFile, false);
    assertThat(ast).isPresent();

    // when (prettyprint)
    new OCLFullPrettyPrinter().prettyprint(ast.get());

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
