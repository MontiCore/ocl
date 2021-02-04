/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cd4code._symboltable.ICD4CodeGlobalScope;
import de.monticore.generating.templateengine.reporting.commons.ASTNodeIdentHelper;
import de.monticore.generating.templateengine.reporting.commons.ReportingRepository;
import de.monticore.io.FileReaderWriter;
import de.monticore.io.paths.ModelPath;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._cocos.*;
import de.monticore.ocl.ocl._od.OCL2OD;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.ocl.ocl._symboltable.*;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.ocl.prettyprint.OCLFullPrettyPrinter;
import de.monticore.ocl.oclexpressions._symboltable.OCLExpressionsSymbolTableCompleter;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.prettyprint.IndentPrinter;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

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

    OCLTool tool = new OCLTool();
    Options options = initOptions();

    try {
      // create CLI parser and parse input options from command line
      CommandLineParser cliparser = new DefaultParser();
      CommandLine cmd = cliparser.parse(options, args);

      // help: when --help
      if (cmd.hasOption("h") || !cmd.hasOption("i")) {
        printHelp(options);
        // do not continue, when help is printed
        return;
      }

      // -option developer logging
      if (cmd.hasOption("d")) {
        Log.initDEBUG();
      }
      else {
        Log.init();
      }

      // parse input file, which is now available
      // (only returns if successful)
      List<ASTOCLCompilationUnit> inputOCLs = new ArrayList<>();
      for (String inputFileName : cmd.getOptionValues("i")) {
        Optional<ASTOCLCompilationUnit> ast = tool.parseOCL(inputFileName);
        if (ast.isPresent()) {
          inputOCLs.add(ast.get());
        }
        else {
          Log.error("0xOCL30 File '" + inputFileName + "' cannot be parsed");
        }
      }

      // -option pretty print
      if (cmd.hasOption("pp")) {
        String path = cmd.getOptionValue("pp");
        for (ASTOCLCompilationUnit compUnit : inputOCLs) {
          prettyprint(compUnit, path);
        }
      }

      // we need the global scope for symbols and cocos
      ModelPath modelPath = new ModelPath(Paths.get(""));
      if (cmd.hasOption("mp")) {
        modelPath = new ModelPath(Arrays.stream(cmd.getOptionValues("mp"))
          .map(x -> Paths.get(x))
          .collect(Collectors.toList())
        );
      }

      IOCLGlobalScope globalScope = OCLMill.globalScope();
      globalScope.setModelPath(modelPath);

      //
      // Parsing and pretty printing can be done without a symbol table
      // but executing the following options requires a symbol table
      //

      //IOCLGlobalScope globalScope = tool.processModels(Paths.get(cmd.getOptionValue("mp")));
      //inputOCLs = IOCLGlobalScopeHelper.getCompilationUnits(globalScope);


      Set<String> cocoOptionValues = new HashSet<>();
      if (cmd.hasOption("c") && cmd.getOptionValues("c") != null) {
        cocoOptionValues.addAll(Arrays.asList(cmd.getOptionValues("c")));
      }
      if (cmd.hasOption("c") || cmd.hasOption("s")) {
        for (ASTOCLCompilationUnit ocl : inputOCLs) {
          deriveSymbolSkeleton(ocl);
        }
        if (cocoOptionValues.isEmpty() || cocoOptionValues.contains("type") || cmd.hasOption("s")) {
          loadSymbols("src/test/resources/docs/Bookshop.cdsym");
          for (ASTOCLCompilationUnit ocl : inputOCLs) {
            OCLSymbolTableCompleter stCompleter = new OCLSymbolTableCompleter(
              ocl.getMCImportStatementList(), ocl.getPackage()
            );
            OCLTraverser t = OCLMill.traverser();
            t.add4BasicSymbols(stCompleter);
            t.setOCLHandler(stCompleter);
            stCompleter.setTraverser(t);
            globalScope.accept(t);
          }
        }
      }

      // create the symbol table and check cocos
      if (cmd.hasOption("c") || cmd.hasOption("s")) {
        if (cmd.hasOption("s") || cocoOptionValues.isEmpty() || cocoOptionValues.contains("type")) {
          for (ASTOCLCompilationUnit sd : inputOCLs) {
            checkAllCoCos(sd);
          }
        }
        else if (cocoOptionValues.contains("inter")) {
          for (ASTOCLCompilationUnit sd : inputOCLs) {
            checkAllExceptTypeCoCos(sd);
          }
        }
        else if (cocoOptionValues.contains("intra")) {
          for (ASTOCLCompilationUnit sd : inputOCLs) {
            checkIntraModelCoCos(sd);
          }
        }
        else {
          Log.error(String.format("Received unexpected arguments '%s' for option 'coco'. "
            + "Possible arguments are 'type', 'inter', and 'intra'.", cocoOptionValues.toString()));
        }
      }

      // -option syntax objects
      if (cmd.hasOption("so")) {
        String path = cmd.getOptionValue("so");
        for (ASTOCLCompilationUnit compUnit : inputOCLs) {
          ocl2od(compUnit, getModelNameFromFile(cmd.getOptionValue("i")), path);
        }
      }

      // store symbols
      if (cmd.hasOption("s")) {
        if (cmd.getOptionValues("s") == null || cmd.getOptionValues("s").length == 0) {
          for (int i = 0; i < inputOCLs.size(); i++) {
            ASTOCLCompilationUnit ocl = inputOCLs.get(i);
            OCLDeSer deSer = new OCLDeSer();
            String serialized = deSer.serialize((OCLArtifactScope) ocl.getEnclosingScope());

            String fileName = cmd.getOptionValues("i")[i];
            String symbolFile = FilenameUtils.getName(fileName) + "sym";
            String symbol_out = "target/symbols";
            String packagePath = ocl.isPresentPackage() ? ocl.getPackage().replace('.', '/') : "";
            Path filePath = Paths.get(symbol_out, packagePath, symbolFile);
            FileReaderWriter.storeInFile(filePath, serialized);
          }
        }
        else if (cmd.getOptionValues("s").length != inputOCLs.size()) {
          Log.error(String.format("Received '%s' output files for the storesymbols option. "
              + "Expected that '%s' many output files are specified. "
              + "If output files for the storesymbols option are specified, then the number "
              + " of specified output files must be equal to the number of specified input files.",
            cmd.getOptionValues("s").length, inputOCLs.size()));
        }
        else {
          for (int i = 0; i < inputOCLs.size(); i++) {
            ASTOCLCompilationUnit ocl_i = inputOCLs.get(i);
            storeSymbols(ocl_i, cmd.getOptionValues("s")[i]);
          }
        }
      }

    }
    catch (ParseException e) {
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
   * @param file               The target file name for printing the OCL artifact. If empty,
   *                           the content is printed to stdout instead
   */
  public void prettyprint(ASTOCLCompilationUnit oCLCompilationUnit, String file) {
    // pretty print AST
    OCLFullPrettyPrinter pp = new OCLFullPrettyPrinter(new IndentPrinter());
    String OCL = pp.prettyprint(oCLCompilationUnit);
    print(OCL, file);
  }

  /**
   * Derives symbols for ast and adds them to the globalScope.
   */
  public void deriveSymbolSkeleton(ASTOCLCompilationUnit ast) {
    OCLScopesGenitorDelegator genitor = new OCLScopesGenitorDelegator();
    genitor.createFromAST(ast);
  }

  /**
   * Checks whether ast satisfies the intra-model CoCos.
   */
  public void checkIntraModelCoCos(ASTOCLCompilationUnit ast) {
    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new MethSignatureStartsWithLowerCaseLetter());
    checker.addCoCo(new ConstructorNameStartsWithCapitalLetter());
    checker.addCoCo(new InvariantNameStartsWithCapitalLetter());
    checker.addCoCo(new ParameterNamesUnique());
    checker.addCoCo(new IterateExpressionVariableUsageIsCorrect());
    checker.addCoCo(new ExpressionHasNoSideEffect());
    checker.addCoCo(new ContextVariableNamesAreUnique());
    checker.addCoCo(new ContextHasOnlyOneType());
    checker.addCoCo(new SetComprehensionHasGenerator());
    checker.addCoCo(new UnnamedInvariantDoesNotHaveParameters());
    checker.checkAll(ast);
  }

  /**
   * Checks whether ast satisfies the CoCos not targeting type correctness.
   * This method checks all CoCos except the CoCos, which check that used types
   * (for objects and variables) are defined.
   */
  public void checkAllExceptTypeCoCos(ASTOCLCompilationUnit ast) {
    checkIntraModelCoCos(ast);
    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new ConstructorNameReferencesType());
    checker.checkAll(ast);
  }

  /**
   * Checks whether ast satisfies all CoCos.
   */
  public void checkAllCoCos(ASTOCLCompilationUnit ast) {
    checkAllExceptTypeCoCos(ast);
    DeriveSymTypeOfOCLCombineExpressions typeChecker = new DeriveSymTypeOfOCLCombineExpressions();
    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new ValidTypes(typeChecker));
    checker.addCoCo(new PreAndPostConditionsAreBooleanType(typeChecker));
    checker.checkAll(ast);
  }

  /**
   * Loads the symbols from the symbol file filename and returns the symbol table.
   *
   * @param filename Name of the symbol file to load.
   */
  public IOCLArtifactScope loadSymbols(String filename) {
    OCLSymbols2Json deSer = new OCLSymbols2Json();
    return deSer.load(filename);
  }

  /*=================================================================*/

  /**
   * Creates the symbol table from the parsed AST.
   *
   * @param ast The top OCL model element.
   * @return The artifact scope derived from the parsed AST
   */
  public IOCLArtifactScope createSymbolTable(ASTOCLCompilationUnit ast, ModelPath mp) {
    IOCLGlobalScope globalScope = OCLMill.globalScope();
    globalScope.setFileExt(".ocl");
    globalScope.setModelPath(mp);

    ICD4CodeGlobalScope cdGlobalScope = CD4CodeMill.globalScope();
    cdGlobalScope.setModelPath(mp);
    cdGlobalScope.setFileExt(".cd");

    OCLSymbolTableCreatorDelegator symbolTable = OCLMill.oCLSymbolTableCreatorDelegator();
    ((OCLSymbolTableCreator) symbolTable.getOCLVisitor().get())
      .setTypeVisitor(new DeriveSymTypeOfOCLCombineExpressions());

    return symbolTable.createFromAST(ast);
  }

  /**
   * Stores the symbols for ast in the symbol file filename.
   * For example, if filename = "target/symbolfiles/file.sdsym", then the symbol file corresponding to
   * ast is stored in the file "target/symbolfiles/file.sdsym".
   *
   * @param ast      The ast of the SD.
   * @param filename The name of the produced symbol file.
   */
  public void storeSymbols(ASTOCLCompilationUnit ast, String filename) {
    OCLDeSer deSer = new OCLDeSer();
    String serialized = deSer.serialize((OCLArtifactScope) ast.getEnclosingScope());
    FileReaderWriter.storeInFile(Paths.get(filename), serialized);
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
   * @param modelName          The derived model name for the OCL-AST
   * @param file               The target file name for printing the object diagram. If empty,
   *                           the content is printed to stdout instead
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
   * @param path    The target path to the file for printing the content. If empty,
   *                the content is printed to stdout instead
   */
  public void print(String content, String path) {
    // print to stdout or file
    if (path == null || path.isEmpty()) {
      System.out.println(content);
      System.out.println();
    }
    else {
      File f = new File(path);
      // create directories (logs error otherwise)
      f.getAbsoluteFile().getParentFile().mkdirs();

      FileWriter writer;
      try {
        writer = new FileWriter(f);
        writer.write(content);
        writer.close();
      }
      catch (IOException e) {
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
    Option dev = new Option("d",
      "Specifies whether developer level logging should be used (default is false)");
    dev.setLongOpt("dev");
    options.addOption(dev);

    // parse input file
    Option parse = Option.builder("i")
      .longOpt("input")
      .argName("file")
      .hasArgs()
      .desc("Processes the list of OCL input artifacts. " +
        "Argument list is space separated. CoCos are not checked automatically (see -c).")
      .build();
    options.addOption(parse);

    // model paths
    Option path = new Option("mp", "Sets the artifact path for imported symbols, space separated.");
    path.setLongOpt("modelpath");
    path.setArgName("directory");
    path.setOptionalArg(true);
    path.setArgs(1);
    options.addOption(path);

    // pretty print OCL
    Option prettyprint = new Option("pp",
      "Prints the OCL-AST to stdout or the specified file (optional)");
    prettyprint.setLongOpt("prettyprint");
    prettyprint.setArgName("file");
    prettyprint.setOptionalArg(true);
    prettyprint.setArgs(1);
    options.addOption(prettyprint);

    // check CoCos
    Option cocos = Option.builder("c").
      longOpt("coco").
      optionalArg(true).
      numberOfArgs(3).
      desc("Checks the CoCos for the input. Optional arguments are:\n"
        + "-c intra to check only the intra-model CoCos,\n"
        + "-c inter checks also inter-model CoCos,\n"
        + "-c type (default) checks all CoCos.")
      .build();
    options.addOption(cocos);

    // create and store symboltable
    Option symboltable = Option.builder("s")
      .longOpt("symboltable")
      .optionalArg(true)
      .argName("file")
      .hasArgs()
      .desc("Stores the symbol tables of the input OCL artifacts in the specified files. " +
        "The n-th input OCL (-i option) is stored in the file as specified by the n-th argument " +
        "of this option. Default is 'target/symbols/{packageName}/{artifactName}.sdsym'.")
      .build();
    options.addOption(symboltable);

    // print object diagram
    Option syntaxobjects = new Option("so",
      "Prints an object diagram of the OCL-AST to stdout or the specified file (optional)");
    syntaxobjects.setLongOpt("syntaxobjects");
    syntaxobjects.setArgName("file");
    syntaxobjects.setOptionalArg(true);
    syntaxobjects.setArgs(1);
    options.addOption(syntaxobjects);

    return options;
  }
}
