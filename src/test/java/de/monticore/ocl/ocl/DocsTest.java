// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl;

import static org.assertj.core.api.Assertions.assertThat;

import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._prettyprint.OCLFullPrettyPrinter;
import de.monticore.ocl.ocl._symboltable.OCLArtifactScope;
import de.monticore.ocl.ocl._symboltable.OCLSymbols2Json;
import de.monticore.ocl.util.SymbolTableUtil;
import de.monticore.prettyprint.IndentPrinter;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Checks that the example used in the tutorial is correct */
public class DocsTest extends AbstractTest {

  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
  }

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
    new OCLFullPrettyPrinter(new IndentPrinter(), true).prettyprint(ast.get());

    // when (create symbol table)
    SymbolTableUtil.prepareMill();
    SymbolTableUtil.addCd4cSymbols();
    SymbolTableUtil.loadSymbolFile(cdFile);
    SymbolTableUtil.runSymTabGenitor(ast.get());
    SymbolTableUtil.runSymTabCompleter(ast.get());

    // when (cocos)
    OCLTool tool = new OCLTool();
    tool.checkAllCoCos(ast.get());

    // when (serialize)
    OCLSymbols2Json symbols2Json = new OCLSymbols2Json();
    String serialized = symbols2Json.serialize((OCLArtifactScope) ast.get().getEnclosingScope());

    // when (deserialize)
    symbols2Json.deserialize(serialized);
  }
}
