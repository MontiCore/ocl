/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen;

import com.google.common.base.Preconditions;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._visitor.OCLTraverser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class OCL2JavaGenerator {

  public static void generate(ASTOCLCompilationUnit ast, String outputFile) throws IOException {
    Preconditions.checkNotNull(ast);
    Preconditions.checkNotNull(outputFile);
    Preconditions.checkArgument(!outputFile.isEmpty());
    File output = Paths.get(outputFile).toFile();
    output.getParentFile().mkdirs();

    FileOutputStream fos = new FileOutputStream(output, false);
    fos.write(generate(ast).getBytes());
    fos.close();
  }

  public static String generate(ASTOCLCompilationUnit ast) {
    Preconditions.checkNotNull(ast);
    return generate(ast, new StringBuilder());
  }

  protected static String generate(ASTOCLCompilationUnit ast, StringBuilder sb) {
    Preconditions.checkNotNull(ast);
    Preconditions.checkNotNull(sb);
    sb.append("/* (c) https://github.com/MontiCore/monticore */");
    sb.append(System.lineSeparator());
    ast.accept(new OCL2JavaGenerator(sb).getTraverser());
    return sb.toString();
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

    // Expressions
    CommonExpressionsPrinter comExprPrinter = new CommonExpressionsPrinter(sb, naming);
    this.traverser.setCommonExpressionsHandler(comExprPrinter);
    this.traverser.add4CommonExpressions(comExprPrinter);

    // OCL
    OCLPrinter oclPrinter = new OCLPrinter(sb, naming);
    this.traverser.setOCLHandler(oclPrinter);
    this.traverser.add4OCL(oclPrinter);
  }
}
