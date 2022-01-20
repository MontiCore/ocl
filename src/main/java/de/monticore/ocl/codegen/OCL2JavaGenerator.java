/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen;

import com.google.common.base.Preconditions;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._visitor.OCLTraverser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class OCL2JavaGenerator {

  public static void generate(ASTOCLCompilationUnit ast, String outputFile) throws IOException {
    Preconditions.checkNotNull(ast);
    Preconditions.checkNotNull(outputFile);
    Preconditions.checkArgument(!outputFile.isEmpty());
    StringBuilder sb = new StringBuilder();
    OCL2JavaGenerator generator = new OCL2JavaGenerator(sb);
    ast.accept(generator.getTraverser());

    Files.write(Paths.get(outputFile), sb.toString().getBytes());
  }

  protected OCLTraverser traverser;

  protected OCLTraverser getTraverser() {
    return this.traverser;
  }

  protected OCL2JavaGenerator(StringBuilder sb) {
    this(sb, new OCLVariableNaming());
  }
  protected OCL2JavaGenerator(StringBuilder sb, OCLVariableNaming naming) {
    Preconditions.checkNotNull(sb);
    Preconditions.checkNotNull(naming);

    this.traverser = OCLMill.traverser();

    this.traverser.setCommonExpressionsHandler(new OCL2JavaCommonExpressions(sb, naming));
  }
}
