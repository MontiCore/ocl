/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import de.monticore.generating.templateengine.reporting.commons.ASTNodeIdentHelper;
import de.monticore.ocl.ocl._od.OCL2OD;
import de.monticore.ocl.ocl._symboltable.OCLSymbolTableCreator;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.ocl.ocl._symboltable.IOCLArtifactScope;
import de.monticore.ocl.ocl._symboltable.IOCLGlobalScope;
import de.monticore.ocl.ocl._symboltable.OCLSymbolTableCreatorDelegator;
import de.monticore.ocl.ocl.prettyprint.OCLFullPrettyPrinter;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import de.monticore.generating.templateengine.reporting.commons.ReportingRepository;
import de.monticore.io.paths.ModelPath;
import de.monticore.prettyprint.IndentPrinter;
import de.se_rwth.commons.logging.Log;

/**
 * Command line interface for the OCL language and corresponding tooling.
 * Defines, handles, and executes the corresponding command line options and
 * arguments, such as --help
 */
public class OCLCLI {

  /*=================================================================*/
  /* Part 1: Handling the arguments and options
  /*=================================================================*/

  /**
   * Main method that is called from command line and runs the OCL tool.
   *
   * @param args The input parameters for configuring the OCL tool.
   */
  public static void main(String[] args) {
    OCLCLI cli = new OCLCLI();
    cli.run(args);
  }

  /**
   * Processes user input from command line and delegates to the corresponding
   * tools.
   *
   * @param args The input parameters for configuring the OCL tool.
   */
  public void run(String[] args) {

    Options options = initOptions();

    try {
      // create CLI parser and parse input options from command line
      CommandLineParser cliparser = new DefaultParser();
      CommandLine cmd = cliparser.parse(options, args);

      // help: when --help
      if (cmd.hasOption("h")) {
        printHelp(options);
        // do not continue, when help is printed
        return;
      }

      // if -i input is missing: also print help and stop
      if (!cmd.hasOption("i")) {
        printHelp(options);
        // do not continue, when help is printed
        return;
      }

      // -option developer logging
      if (cmd.hasOption("d")) {
        Log.initDEBUG();
      } else {
        Log.init();
      }

      // parse input file, which is now available
      // (only returns if successful)
      ASTOCLCompilationUnit oCLCompilationUnit = parseFile(cmd.getOptionValue("i"));

      // create the symbol table
      createSymbolTable(oCLCompilationUnit);

      // -option pretty print
      if (cmd.hasOption("pp")) {
        String path = cmd.getOptionValue("pp");
        prettyPrint(oCLCompilationUnit, path);
      }

      // -option syntax objects
      if (cmd.hasOption("so")) {
        String path = cmd.getOptionValue("so");
        ocl2od(oCLCompilationUnit, getModelNameFromFile(cmd.getOptionValue("i")), path);
      }

    } catch (ParseException e) {
      // ann unexpected error from the apache CLI parser:
      Log.error("0xA7101 Could not process CLI parameters: " + e.getMessage());
    }

  }

  /**
   * Processes user input from command line and delegates to the corresponding
   * tools.
   *
   * @param options The input parameters and options.
   */
  public void printHelp(Options options) {
    HelpFormatter formatter = new HelpFormatter();
    formatter.setWidth(80);
    formatter.printHelp("OCLCLI", options);
  }

  /*=================================================================*/
  /* Part 2: Executing arguments
  /*=================================================================*/

  /**
   * Parses the contents of a given file as OCL.
   *
   * @param path The path to the OCL-file as String
   */
  public ASTOCLCompilationUnit parseFile(String path) {
    Optional<ASTOCLCompilationUnit> OCLCompilationUnit = Optional.empty();

    // disable fail-quick to find all parsing errors
    Log.enableFailQuick(false);
    try {
      Path model = Paths.get(path);
      OCLParser parser = new OCLParser();
      OCLCompilationUnit = parser.parse(model.toString());
    }
    catch (IOException | NullPointerException e) {
      Log.error("0xA7102 Input file '" + path + "' not found.");
    }

    // re-enable fail-quick to print potential errors
    Log.enableFailQuick(true);
    return OCLCompilationUnit.get();
  }

  /**
   * Prints the contents of the OCL-AST to stdout or a specified file.
   *
   * @param oCLCompilationUnit The OCL-AST to be pretty printed
   * @param file The target file name for printing the OCL artifact. If empty,
   *          the content is printed to stdout instead
   */
  public void prettyPrint(ASTOCLCompilationUnit oCLCompilationUnit, String file) {
    // pretty print AST
    OCLFullPrettyPrinter pp = new OCLFullPrettyPrinter(new IndentPrinter());
    String OCL = pp.prettyprint(oCLCompilationUnit);
    print(OCL, file);
  }

  /*=================================================================*/

  /**
   * Creates the symbol table from the parsed AST.
   *
   * @param ast The top OCL model element.
   * @return The artifact scope derived from the parsed AST
   */
  public IOCLArtifactScope createSymbolTable(ASTOCLCompilationUnit ast) {
    IOCLGlobalScope globalScope = OCLMill.globalScope();
    globalScope.setFileExt(".ocl");

    OCLSymbolTableCreatorDelegator symbolTable = OCLMill.oCLSymbolTableCreatorDelegator();
    ((OCLSymbolTableCreator)symbolTable.getOCLVisitor().get()).setTypeVisitor(new DeriveSymTypeOfOCLCombineExpressions());


    return symbolTable.createFromAST(ast);
  }

  /**
   * Extracts the model name from a given file name. The model name corresponds
   * to the unqualified file name without file extension.
   *
   * @param file The path to the input file
   * @return The extracted model name
   */
  public String getModelNameFromFile(String file) {
    String modelName = new File(file).getName();
    // cut file extension if present
    if (modelName.length() > 0) {
      int lastIndex = modelName.lastIndexOf(".");
      if (lastIndex != -1) {
        modelName = modelName.substring(0, lastIndex);
      }
    }
    return modelName;
  }

  /**
   * Creates an object diagram for the OCL-AST to stdout or a specified file.
   *
   * @param oCLCompilationUnit The OCL-AST for which the object diagram is created
   * @param modelName The derived model name for the OCL-AST
   * @param file The target file name for printing the object diagram. If empty,
   *          the content is printed to stdout instead
   */
  public void ocl2od(ASTOCLCompilationUnit oCLCompilationUnit, String modelName, String file) {
    // initialize OCL2od printer
    IndentPrinter printer = new IndentPrinter();
    ASTNodeIdentHelper identifierHelper = new ASTNodeIdentHelper();
    ReportingRepository repository = new ReportingRepository(identifierHelper);
    OCL2OD OCL2od = new OCL2OD(printer, repository);

    // print object diagram
    String od = OCL2od.printObjectDiagram((new File(modelName)).getName(), oCLCompilationUnit);
    print(od, file);
  }

  /**
   * Prints the given content to a target file (if specified) or to stdout (if
   * the file is Optional.empty()).
   *
   * @param content The String to be printed
   * @param path The target path to the file for printing the content. If empty,
   *          the content is printed to stdout instead
   */
  public void print(String content, String path) {
    // print to stdout or file
    if (path == null || path.isEmpty()) {
      System.out.println(content);
    } else {
      File f = new File(path);
      // create directories (logs error otherwise)
      f.getAbsoluteFile().getParentFile().mkdirs();

      FileWriter writer;
      try {
        writer = new FileWriter(f);
        writer.write(content);
        writer.close();
      } catch (IOException e) {
        Log.error("0xA7105 Could not write to file " + f.getAbsolutePath());
      }
    }
  }

  /*=================================================================*/
  /* Part 3: Defining the options incl. help-texts
  /*=================================================================*/

  /**
   * Initializes the available CLI options for the OCL tool.
   *
   * @return The CLI options with arguments.
   */
  protected Options initOptions() {
    Options options = new Options();

    // help dialog
    Option help = new Option("h", "Prints this help dialog");
    help.setLongOpt("help");
    options.addOption(help);

    // developer level logging
    Option dev = new Option("d", "Specifies whether developer level logging should be used (default is false)");
    dev.setLongOpt("dev");
    options.addOption(dev);

    // parse input file
    Option parse = new Option("i", "Reads the source file (mandatory) and parses the contents as OCL");
    parse.setLongOpt("input");
    parse.setArgName("file");
    parse.setOptionalArg(true);
    parse.setArgs(1);
    options.addOption(parse);

    // pretty print OCL
    Option prettyprint = new Option("pp", "Prints the OCL-AST to stdout or the specified file (optional)");
    prettyprint.setLongOpt("prettyprint");
    prettyprint.setArgName("file");
    prettyprint.setOptionalArg(true);
    prettyprint.setArgs(1);
    options.addOption(prettyprint);

    // print object diagram
    Option syntaxobjects = new Option("so", "Prints an object diagram of the OCL-AST to stdout or the specified file (optional)");
    syntaxobjects.setLongOpt("syntaxobjects");
    syntaxobjects.setArgName("file");
    syntaxobjects.setOptionalArg(true);
    syntaxobjects.setArgs(1);
    options.addOption(syntaxobjects);

    return options;
  }
}
