/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen;

import com.google.common.base.Preconditions;
import de.monticore.expressions.expressionsbasis._prettyprint.ExpressionsBasisPrettyPrinter;
import de.monticore.literals.mccommonliterals._prettyprint.MCCommonLiteralsPrettyPrinter;
import de.monticore.mcbasics._prettyprint.MCBasicsPrettyPrinter;
import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.ocl.codegen.visitors.CommonExpressionsPrinter;
import de.monticore.ocl.codegen.visitors.OCLExpressionsPrinter;
import de.monticore.ocl.codegen.visitors.OCLPrinter;
import de.monticore.ocl.codegen.visitors.OptionalOperatorsPrinter;
import de.monticore.ocl.codegen.visitors.SetExpressionsPrinter;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.check.IDerive;
import de.monticore.types.check.ISynthesize;
import de.monticore.types.mcbasictypes._prettyprint.MCBasicTypesPrettyPrinter;
import de.monticore.types.mccollectiontypes._prettyprint.MCCollectionTypesPrettyPrinter;
import de.monticore.types.mcsimplegenerictypes._prettyprint.MCSimpleGenericTypesPrettyPrinter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;

public class OCL2JavaGenerator {

  public static void generate(ASTOCLCompilationUnit ast, String outputFile) throws IOException {
    Preconditions.checkNotNull(ast);
    Preconditions.checkNotNull(ast.getEnclosingScope());
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
    return generate(ast, new IndentPrinter());
  }

  protected static String generate(ASTOCLCompilationUnit ast, IndentPrinter printer) {
    Preconditions.checkNotNull(ast);
    Preconditions.checkNotNull(printer);

    printer.println("/* (c) https://github.com/MontiCore/monticore */");
    ast.accept(new OCL2JavaGenerator(printer).getTraverser());
    return printer.getContent();
  }

  protected OCLTraverser traverser;

  protected IndentPrinter printer;

  protected OCLTraverser getTraverser() {
    return this.traverser;
  }

  protected OCL2JavaGenerator(IndentPrinter printer) {
    this(printer, new VariableNaming());
  }

  /** @deprecated use {@link #OCL2JavaGenerator(IndentPrinter, VariableNaming)} */
  @Deprecated
  protected OCL2JavaGenerator(
      IndentPrinter printer, VariableNaming naming, IDerive deriver, ISynthesize syntheziser) {
    Preconditions.checkNotNull(printer);
    Preconditions.checkNotNull(naming);

    this.traverser = OCLMill.traverser();

    // Expressions
    CommonExpressionsPrinter comExprPrinter =
        new CommonExpressionsPrinter(printer, naming, deriver, syntheziser);
    this.traverser.setCommonExpressionsHandler(comExprPrinter);
    this.traverser.add4CommonExpressions(comExprPrinter);
    ExpressionsBasisPrettyPrinter exprBasPrinter = new ExpressionsBasisPrettyPrinter(printer, true);
    this.traverser.setExpressionsBasisHandler(exprBasPrinter);
    this.traverser.add4ExpressionsBasis(exprBasPrinter);
    OCLExpressionsPrinter oclExprPrinter =
        new OCLExpressionsPrinter(printer, naming, deriver, syntheziser);
    this.traverser.setOCLExpressionsHandler(oclExprPrinter);
    this.traverser.add4OCLExpressions(oclExprPrinter);
    SetExpressionsPrinter setExprPrinter =
        new SetExpressionsPrinter(printer, naming, deriver, syntheziser);
    this.traverser.setSetExpressionsHandler(setExprPrinter);
    this.traverser.add4SetExpressions(setExprPrinter);
    OptionalOperatorsPrinter optExprPrinter =
        new OptionalOperatorsPrinter(printer, naming, deriver, syntheziser);
    this.traverser.setOptionalOperatorsHandler(optExprPrinter);
    this.traverser.add4OptionalOperators(optExprPrinter);

    // Types
    MCSimpleGenericTypesPrettyPrinter simpleGenericTypes =
        new MCSimpleGenericTypesPrettyPrinter(printer, true);
    traverser.setMCSimpleGenericTypesHandler(simpleGenericTypes);
    traverser.add4MCSimpleGenericTypes(simpleGenericTypes);
    MCCollectionTypesPrettyPrinter collectionTypes =
        new MCCollectionTypesPrettyPrinter(printer, true);
    traverser.setMCCollectionTypesHandler(collectionTypes);
    traverser.add4MCCollectionTypes(collectionTypes);
    MCBasicTypesPrettyPrinter basicTypes = new MCBasicTypesPrettyPrinter(printer, true);
    traverser.setMCBasicTypesHandler(basicTypes);
    traverser.add4MCBasicTypes(basicTypes);
    MCBasicsPrettyPrinter basics = new MCBasicsPrettyPrinter(printer, true);
    traverser.add4MCBasics(basics);

    MCCommonLiteralsPrettyPrinter comLitPrinter = new MCCommonLiteralsPrettyPrinter(printer, true);
    this.traverser.setMCCommonLiteralsHandler(comLitPrinter);
    this.traverser.add4MCCommonLiterals(comLitPrinter);

    // OCL
    OCLPrinter oclPrinter = new OCLPrinter(printer, naming, deriver, syntheziser);
    this.traverser.setOCLHandler(oclPrinter);
    this.traverser.add4OCL(oclPrinter);
  }

  protected OCL2JavaGenerator(IndentPrinter printer, VariableNaming naming) {
    Preconditions.checkNotNull(printer);
    Preconditions.checkNotNull(naming);

    this.traverser = OCLMill.traverser();

    // Expressions
    CommonExpressionsPrinter comExprPrinter = new CommonExpressionsPrinter(printer, naming);
    this.traverser.setCommonExpressionsHandler(comExprPrinter);
    this.traverser.add4CommonExpressions(comExprPrinter);
    ExpressionsBasisPrettyPrinter exprBasPrinter = new ExpressionsBasisPrettyPrinter(printer, true);
    this.traverser.setExpressionsBasisHandler(exprBasPrinter);
    this.traverser.add4ExpressionsBasis(exprBasPrinter);
    OCLExpressionsPrinter oclExprPrinter = new OCLExpressionsPrinter(printer, naming);
    this.traverser.setOCLExpressionsHandler(oclExprPrinter);
    this.traverser.add4OCLExpressions(oclExprPrinter);
    SetExpressionsPrinter setExprPrinter = new SetExpressionsPrinter(printer, naming);
    this.traverser.setSetExpressionsHandler(setExprPrinter);
    this.traverser.add4SetExpressions(setExprPrinter);
    OptionalOperatorsPrinter optExprPrinter = new OptionalOperatorsPrinter(printer, naming);
    this.traverser.setOptionalOperatorsHandler(optExprPrinter);
    this.traverser.add4OptionalOperators(optExprPrinter);

    // Types
    MCSimpleGenericTypesPrettyPrinter simpleGenericTypes =
        new MCSimpleGenericTypesPrettyPrinter(printer, true);
    traverser.setMCSimpleGenericTypesHandler(simpleGenericTypes);
    traverser.add4MCSimpleGenericTypes(simpleGenericTypes);
    MCCollectionTypesPrettyPrinter collectionTypes =
        new MCCollectionTypesPrettyPrinter(printer, true);
    traverser.setMCCollectionTypesHandler(collectionTypes);
    traverser.add4MCCollectionTypes(collectionTypes);
    MCBasicTypesPrettyPrinter basicTypes = new MCBasicTypesPrettyPrinter(printer, true);
    traverser.setMCBasicTypesHandler(basicTypes);
    traverser.add4MCBasicTypes(basicTypes);
    MCBasicsPrettyPrinter basics = new MCBasicsPrettyPrinter(printer, true);
    traverser.add4MCBasics(basics);

    MCCommonLiteralsPrettyPrinter comLitPrinter = new MCCommonLiteralsPrettyPrinter(printer, true);
    this.traverser.setMCCommonLiteralsHandler(comLitPrinter);
    this.traverser.add4MCCommonLiterals(comLitPrinter);

    // OCL
    OCLPrinter oclPrinter = new OCLPrinter(printer, naming);
    this.traverser.setOCLHandler(oclPrinter);
    this.traverser.add4OCL(oclPrinter);
  }
}
