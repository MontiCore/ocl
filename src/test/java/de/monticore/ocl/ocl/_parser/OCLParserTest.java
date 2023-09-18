// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._parser;

import de.monticore.ocl.ocl.AbstractTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class OCLParserTest extends AbstractTest {

  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
  }

  protected static final String RELATIVE_MODEL_PATH = "src/test/resources";

  @ParameterizedTest
  @MethodSource("getParsableModels")
  public void parseParsableModels(String fileName) {
    System.out.println(fileName);
    this.parse(fileName, false);
  }

  @ParameterizedTest
  @MethodSource("getNotParsableModels")
  public void parseNotParsableModels(String fileName) {
    System.out.println(fileName);
    this.parse(fileName, true);
  }
}
