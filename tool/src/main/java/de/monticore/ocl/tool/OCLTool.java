/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.tool;

import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.io.FileReaderWriter;
import de.monticore.io.paths.MCPath;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._cocos.*;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.ocl.ocl._symboltable.IOCLArtifactScope;
import de.monticore.ocl.ocl._symboltable.IOCLGlobalScope;
import de.monticore.ocl.ocl._symboltable.OCLArtifactScope;
import de.monticore.ocl.ocl._symboltable.OCLSymbols2Json;
import de.monticore.ocl.oclexpressions.cocos.IterateExpressionVariableUsageIsCorrect;
import de.monticore.ocl.setexpressions.cocos.SetComprehensionHasGenerator;
import de.monticore.ocl.util.SymbolTableUtil;
import de.monticore.ocl2smt.ocldiff.OCLDiffGenerator;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;

public class OCLTool extends de.monticore.ocl.ocl.OCLTool {
  /*=================================================================*/
  /* Part 1: Handling the arguments and options
  /*=================================================================*/

  /**
   * Processes user input from command line and delegates to the corresponding tools.
   *
   * @param args The input parameters for configuring the OCL tool.
   */
  @Override
  public void run(String[] args) {
    init();
    Options options = initOptions();

    SymbolTableUtil.prepareMill();

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
      if (cmd.hasOption("i")) {
        // -option developer logging
        if (cmd.hasOption("d")) {
          Log.initDEBUG();
        } else {
          Log.init();
        }

        // parse input file, which is now available
        // (only returns if successful)
        List<ASTOCLCompilationUnit> inputOCLs = new ArrayList<>();
        for (String inputFileName : cmd.getOptionValues("i")) {
          ASTOCLCompilationUnit ast = parse(inputFileName);
          inputOCLs.add(ast);
        }

        // -option pretty print
        if (cmd.hasOption("pp")) {
          int ppArgs = cmd.getOptionValues("pp") == null ? 0 : cmd.getOptionValues("pp").length;
          int iArgs = cmd.getOptionValues("i") == null ? 0 : cmd.getOptionValues("i").length;
          if (ppArgs != 0 && ppArgs != iArgs) {
            Log.error(
                "0xOCL31 Number of arguments of -pp (which is "
                    + ppArgs
                    + ") must match number of arguments of -i (which is "
                    + iArgs
                    + "). "
                    + "Or provide no arguments to print to stdout.");
          }

          String[] paths = cmd.getOptionValues("pp");
          int i = 0;
          for (ASTOCLCompilationUnit compUnit : inputOCLs) {
            String currentPath = "";
            if (cmd.getOptionValues("pp") != null && cmd.getOptionValues("pp").length != 0) {
              currentPath = paths[i];
              i++;
            }
            prettyPrint(compUnit, currentPath);
          }
        }

        // we need the global scope for symbols and cocos
        MCPath symbolPath = new MCPath(Paths.get(""));
        if (cmd.hasOption("p")) {
          symbolPath =
              new MCPath(
                  Arrays.stream(cmd.getOptionValues("p"))
                      .map(x -> Paths.get(x))
                      .collect(Collectors.toList()));
        }

        //
        // Parsing and pretty printing can be done without a symbol table
        // but executing the following options requires a symbol table
        //

        IOCLGlobalScope globalScope = OCLMill.globalScope();
        globalScope.setSymbolPath(symbolPath);

        // Add custom symbols to deserialize
        if (cmd.hasOption("ts")) {
          for (String symbol : cmd.getOptionValues("ts")) {
            SymbolTableUtil.addTypeSymbol(symbol);
          }
        }

        if (cmd.hasOption("vs")) {
          for (String symbol : cmd.getOptionValues("vs")) {
            SymbolTableUtil.addVariableSymbol(symbol);
          }
        }

        if (cmd.hasOption("fs")) {
          for (String symbol : cmd.getOptionValues("fs")) {
            SymbolTableUtil.addFunctionSymbol(symbol);
          }
        }

        if (cmd.hasOption("is")) {
          for (String symbol : cmd.getOptionValues("is")) {
            SymbolTableUtil.ignoreSymbolKind(symbol);
          }
        }

        if (cmd.hasOption("cd4c")) {
          SymbolTableUtil.addCd4cSymbols();
        }

        Set<String> cocoOptionValues = new HashSet<>();
        if (cmd.hasOption("c") && cmd.getOptionValues("c") != null) {
          cocoOptionValues.addAll(Arrays.asList(cmd.getOptionValues("c")));
        }
        if (cmd.hasOption("c") || cmd.hasOption("s")) {
          for (ASTOCLCompilationUnit ocl : inputOCLs) {
            createSymbolTable(ocl);
          }
          if (cocoOptionValues.isEmpty()
              || cocoOptionValues.contains("type")
              || cmd.hasOption("s")) {

            // Deserialize *.sym files
            for (Path path : symbolPath.getEntries()) {
              try {
                Files.walk(path)
                    .filter(file -> file.toString().toLowerCase().matches(".*\\.[a-z]*sym$"))
                    .forEach(file -> SymbolTableUtil.loadSymbolFile(file.toString()));
              } catch (IOException e) {
                e.printStackTrace();
                Log.error("0xA7106 Could not deserialize symbol files");
              }
            }

            // Complete symbol table
            for (ASTOCLCompilationUnit ocl : inputOCLs) {
              SymbolTableUtil.runSymTabCompleter(ocl);
            }
          }
        }

        // create the symbol table and check cocos
        if (cmd.hasOption("c") || cmd.hasOption("s")) {
          if (cmd.hasOption("s")
              || cocoOptionValues.isEmpty()
              || cocoOptionValues.contains("type")) {
            for (ASTOCLCompilationUnit ocl : inputOCLs) {
              checkAllCoCos(ocl);
            }
          } else if (cocoOptionValues.contains("inter")) {
            for (ASTOCLCompilationUnit sd : inputOCLs) {
              checkAllExceptTypeCoCos(sd);
            }
          } else if (cocoOptionValues.contains("intra")) {
            for (ASTOCLCompilationUnit sd : inputOCLs) {
              checkIntraModelCoCos(sd);
            }
          } else {
            Log.error(
                String.format(
                    "Received unexpected arguments '%s' for option 'coco'. "
                        + "Possible arguments are 'type', 'inter', and 'intra'.",
                    cocoOptionValues.toString()));
          }
        }

        // store symbols
        if (cmd.hasOption("s")) {
          if (cmd.getOptionValues("s") == null || cmd.getOptionValues("s").length == 0) {
            for (int i = 0; i < inputOCLs.size(); i++) {
              ASTOCLCompilationUnit ocl = inputOCLs.get(i);
              OCLSymbols2Json symbols2Json = new OCLSymbols2Json();
              String serialized =
                  symbols2Json.serialize((OCLArtifactScope) ocl.getEnclosingScope());

              String fileName = cmd.getOptionValues("i")[i];
              String symbolFile = FilenameUtils.getName(fileName) + "sym";
              String symbol_out = "target/symbols";
              String packagePath = ocl.isPresentPackage() ? ocl.getPackage().replace('.', '/') : "";
              Path filePath = Paths.get(symbol_out, packagePath, symbolFile);
              FileReaderWriter.storeInFile(filePath, serialized);
            }
          } else if (cmd.getOptionValues("s").length != inputOCLs.size()) {
            Log.error(
                String.format(
                    "Received '%s' output files for the storesymbols option. "
                        + "Expected that '%s' many output files are specified. "
                        + "If output files for the storesymbols option are specified, then the number "
                        + " of specified output files must be equal to the number of specified input files.",
                    cmd.getOptionValues("s").length, inputOCLs.size()));
          } else {
            for (int i = 0; i < inputOCLs.size(); i++) {
              ASTOCLCompilationUnit ocl_i = inputOCLs.get(i);
              storeSymbols(ocl_i, cmd.getOptionValues("s")[i]);
            }
          }
        }
      }
      if (cmd.hasOption("w")
          || cmd.hasOption("diff")
          || cmd.hasOption("odiff")
          || cmd.hasOption("ow")) {

        CD4CodeMill.init();
        CD2SMTMill.initDefault();
        // check if there are a class diagram
        if (!cmd.hasOption("cd")) {
          Log.error(
              "the option -w, -diff , -odiff and -ow  required a class diagram this can be introduce with the option -cd");
        }

        // check if a new version of ocl is present for the diff
        if ((cmd.hasOption("diff") || cmd.hasOption("odiff"))
            && (!cmd.hasOption("nocl") || !cmd.hasOption("ocl"))) {
          Log.error(
              "The option -diff and -odiff required, two version of the ocl specification. the older version can be added with the option -ocl. And the newest one with the option -nocl");
        }

        // build the output path
        Path output = Path.of("target/output");
        if (cmd.hasOption("o")) {
          output = Path.of(cmd.getOptionValue("o"));
        }

        // check if the computation must be partial
        boolean partial = cmd.hasOption("pt");

        // declared artifacts
        File cd = new File(cmd.getOptionValue("cd"));
        File newCd;
        Set<File> oclFiles = new HashSet<>();
        Set<File> newOClFiles = new HashSet<>();
        Set<File> posOdExamples = new HashSet<>();
        Set<File> negOdExample = new HashSet<>();
        String methodName;

        // get ocl files
        if (cmd.hasOption("ocl") && cmd.getOptionValues("ocl") != null) {
          for (String file : cmd.getOptionValues("ocl")) {
            oclFiles.add(new File(file));
          }
        }

        if (cmd.hasOption("nocl") && cmd.getOptionValues("nocl") != null) {
          for (String file : cmd.getOptionValues("nocl")) {
            newOClFiles.add(new File(file));
          }
        }

        // get pos and negative od Examples
        if (cmd.hasOption("od") && cmd.getOptionValues("od") != null) {
          for (String file : cmd.getOptionValues("od")) {
            posOdExamples.add(new File(file));
          }
        }

        if (cmd.hasOption("nod") && cmd.getOptionValues("nod") != null) {
          for (String file : cmd.getOptionValues("nod")) {
            negOdExample.add(new File(file));
          }
        }

        // compute ocl witness
        if (cmd.hasOption("w")) {
          OCLDiffGenerator.oclWitness(cd, oclFiles, posOdExamples, negOdExample, partial, output);
        }

        // compute ocl operations witness
        if (cmd.hasOption("ow")) {
          if (cmd.hasOption("mn")) {
            methodName = cmd.getOptionValue("mn");
            OCLDiffGenerator.oclOPWitness(cd, oclFiles, methodName, partial, output);
          } else {
            OCLDiffGenerator.oclOPWitness(cd, oclFiles, partial, output);
          }
        }

        if (cmd.hasOption("diff")) {
          if (cmd.hasOption("ncd") && cmd.getOptionValue("ncd") != null) {
            newCd = new File(cmd.getOptionValue("ncd"));
            OCLDiffGenerator.oclDiff(
                cd, newCd, oclFiles, newOClFiles, posOdExamples, negOdExample, partial, output);
          } else {
            OCLDiffGenerator.oclDiff(
                cd, oclFiles, newOClFiles, posOdExamples, negOdExample, partial, output);
          }
        }

        if (cmd.hasOption("odiff")) {
          if (cmd.hasOption("mn")) {
            methodName = cmd.getOptionValue("mn");
            OCLDiffGenerator.oclOPDiffV1(cd, oclFiles, newOClFiles, methodName, partial, output);
          } else {
            Log.error(
                "The option --odiff need a Method name. this can be added with the option -mn");
            // TODO: inplement OCLDiffGenerator.oclDiff(cd, oclFiles, newOClFiles, partial, output);
          }
        }
      }

    } catch (ParseException e) {
      // ann unexpected error from the apache CLI parser:
      Log.error("0xA7101 Could not process CLI parameters: " + e.getMessage());
    }
  }

  /*=================================================================*/
  /* Part 2: Executing arguments
  /*=================================================================*/

  /**
   * Parses the contents of a given file as OCL.
   *
   * @param path The path to the OCL-file as String
   * @return parsed AST
   */
  @Override
  public ASTOCLCompilationUnit parse(String path) {
    Optional<ASTOCLCompilationUnit> OCLCompilationUnit = Optional.empty();

    // disable fail-quick to find all parsing errors
    Log.enableFailQuick(false);
    try {
      Path model = Paths.get(path);
      OCLParser parser = new OCLParser();
      OCLCompilationUnit = parser.parse(model.toString());
    } catch (IOException | NullPointerException e) {
      Log.error("0xA7102 Input file '" + path + "' not found.");
    }

    // re-enable fail-quick to print potential errors
    Log.enableFailQuick(true);
    return OCLCompilationUnit.get();
  }

  /**
   * Stores the symbols for ast in the symbol file filename. For example, if filename =
   * "target/symbolfiles/file.oclsym", then the symbol file corresponding to ast is stored in the
   * file "target/symbolfiles/file.oclsym".
   *
   * @param ast The ast of the SD.
   * @param filename The name of the produced symbol file.
   */
  public void storeSymbols(ASTOCLCompilationUnit ast, String filename) {
    OCLSymbols2Json symbols2Json = new OCLSymbols2Json();
    String serialized = symbols2Json.serialize((OCLArtifactScope) ast.getEnclosingScope());
    FileReaderWriter.storeInFile(Paths.get(filename), serialized);
  }

  /**
   * Prints the contents of the OCL-AST to stdout or a specified file.
   *
   * @param oCLCompilationUnit The OCL-AST to be pretty printed
   * @param file The target file name for printing the OCL artifact. If empty, the content is
   *     printed to stdout instead
   */
  @Override
  public void prettyPrint(ASTOCLCompilationUnit oCLCompilationUnit, String file) {
    // pretty print AST
    print(OCLMill.prettyPrint(oCLCompilationUnit, true), file);
  }

  /**
   * Checks whether ast satisfies the intra-model CoCos.
   *
   * @param ast AST to check intra-model cocos for
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
    checker.addCoCo(new VariableDeclarationOfCorrectType());
    checker.checkAll(ast);
  }

  /**
   * Checks whether ast satisfies the CoCos not targeting type correctness. This method checks all
   * CoCos except the CoCos, which check that used types (for objects and variables) are defined.
   *
   * @param ast AST to check cocos for
   */
  public void checkAllExceptTypeCoCos(ASTOCLCompilationUnit ast) {
    checkIntraModelCoCos(ast);
    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new ConstructorNameReferencesType());
    checker.checkAll(ast);
  }

  /**
   * Checks whether ast satisfies all CoCos.
   *
   * @param ast AST to check cocos for
   */
  public void checkAllCoCos(ASTOCLCompilationUnit ast) {
    checkAllExceptTypeCoCos(ast);
    OCLCoCoChecker checker = new OCLCoCoChecker();
    checker.addCoCo(new ExpressionValidCoCo());
    checker.addCoCo(new PreAndPostConditionsAreBooleanType());
    checker.checkAll(ast);
  }

  /**
   * Loads the symbols from the symbol file filename and returns the symbol table.
   *
   * @param filename Name of the symbol file to load.
   * @return the symbol table
   */
  public IOCLArtifactScope loadSymbols(String filename) {
    OCLSymbols2Json symbols2Json = new OCLSymbols2Json();
    return symbols2Json.load(filename);
  }

  /*=================================================================*/

  /*=================================================================*/
  /* Part 3: Defining the options incl. help-texts
  /*=================================================================*/

  /**
   * Initializes the standard options for the OCL tool.
   *
   * @return The CLI options with arguments.
   */
  @Override
  public Options addStandardOptions(Options options) {

    // help dialog
    Option help = new Option("h", "Prints this help dialog");
    help.setLongOpt("help");
    options.addOption(help);

    // parse input file
    Option parse =
        Option.builder("i")
            .longOpt("input")
            .argName("files")
            .hasArgs()
            .desc(
                "Processes the list of OCL input artifacts. "
                    + "Argument list is space separated. CoCos are not checked automatically (see -c).")
            .build();
    options.addOption(parse);

    // model paths
    Option path =
        new Option(
            "p",
            "Sets the artifact path for imported symbols. "
                + "Directory will be searched recursively for files with the ending "
                + "\".*sym\" (for example \".cdsym\" or \".sym\"). Defaults to the current folder.");
    path.setLongOpt("path");
    path.setArgName("directory");
    path.setOptionalArg(true);
    path.setArgs(1);
    options.addOption(path);

    // pretty print OCL
    Option prettyprint =
        new Option(
            "pp",
            "Prints the OCL model to stdout or the specified file(s) (optional). "
                + "Multiple files should be separated by spaces and will be used in the same order "
                + "in which the input files (-i option) are provided.");
    prettyprint.setLongOpt("prettyprint");
    prettyprint.setArgName("files");
    prettyprint.setOptionalArg(true);
    prettyprint.setArgs(Option.UNLIMITED_VALUES);
    options.addOption(prettyprint);

    // create and store symboltable
    Option symboltable =
        Option.builder("s")
            .longOpt("symboltable")
            .optionalArg(true)
            .argName("files")
            .hasArgs()
            .desc(
                "Stores the symbol tables of the input OCL artifacts in the specified files. "
                    + "For each input OCL artifact (-i option) please provide one output symbol file "
                    + "(using same order in which the input artifacts are provided) to store its symbols in. "
                    + "For example, -i x.ocl y.ocl -s a.oclsym b.oclsym will store the symbols of x.ocl to "
                    + "a.oclsym and the symbols of y.ocl to b.oclsym. "
                    + "Arguments are separated by spaces. "
                    + "If no arguments are given, output is stored to "
                    + "'target/symbols/{packageName}/{artifactName}.oclsym'.")
            .build();
    options.addOption(symboltable);
    return options;
  }

  /**
   * Initializes the additional options for the OCL tool.
   *
   * @return The CLI options with arguments.
   */
  @Override
  public Options addAdditionalOptions(Options options) {

    // accept TypeSymbols
    Option typeSymbols =
        Option.builder("ts")
            .longOpt("typeSymbol")
            .optionalArg(true)
            .argName("fqns")
            .hasArgs()
            .desc(
                "Takes the fully qualified name of one or more symbol kind(s) that should be "
                    + "treated as TypeSymbol when deserializing symbol files. Multiple symbol kinds "
                    + "should be separated by spaces.")
            .build();
    options.addOption(typeSymbols);

    // accept VariableSymbols
    Option varSymbols =
        Option.builder("vs")
            .longOpt("variableSymbol")
            .optionalArg(true)
            .argName("fqns")
            .hasArgs()
            .desc(
                "Takes the fully qualified name of one or more symbol kind(s) that should be "
                    + "treated as VariableSymbol when deserializing symbol files. Multiple symbol kinds "
                    + "should be separated by spaces.")
            .build();
    options.addOption(varSymbols);

    // accept FunctionSymbols
    Option funcSymbols =
        Option.builder("fs")
            .longOpt("functionSymbol")
            .optionalArg(true)
            .argName("fqns")
            .hasArgs()
            .desc(
                "Takes the fully qualified name of one or more symbol kind(s) that should be "
                    + "treated as FunctionSymbol when deserializing symbol files. Multiple symbol kinds "
                    + "should be separated by spaces.")
            .build();
    options.addOption(funcSymbols);

    // accept FunctionSymbols
    Option ignoreSymbols =
        Option.builder("is")
            .longOpt("ignoreSymKind")
            .optionalArg(true)
            .argName("fqns")
            .hasArgs()
            .desc(
                "Takes the fully qualified name of one or more symbol kind(s) for which no warnings "
                    + "about not being able to deserialize them shall be printed. Allows cleaner outputs. "
                    + "Multiple symbol kinds should be separated by spaces. ")
            .build();
    options.addOption(ignoreSymbols);

    // developer level logging
    Option cd4c =
        new Option(
            "cd4c",
            "Load symbol kinds from CD4C. Shortcut for loading CDTypeSymbol as TypeSymbol, "
                + "CDMethodSignatureSymbol as FunctionSymbol, and FieldSymbol as VariableSymbol. "
                + "Furthermore, warnings about not deserializing CDAssociationSymbol and CDRoleSymbol "
                + "will be ignored.");
    cd4c.setLongOpt("cd4code");
    options.addOption(cd4c);

    // check CoCos
    Option cocos =
        Option.builder("c")
            .longOpt("coco")
            .optionalArg(true)
            .numberOfArgs(1)
            .desc(
                "Checks the CoCos for the input. Optional arguments are:\n"
                    + "-c intra to check only the intra-model CoCos,\n"
                    + "-c inter checks also inter-model CoCos,\n"
                    + "-c type (default) checks all CoCos.")
            .build();
    options.addOption(cocos);

    // developer level logging
    Option dev =
        new Option(
            "d", "Specifies whether developer level logging should be used (default is false)");
    dev.setLongOpt("dev");
    options.addOption(dev);

    // *************************************************OCLDiff-Options****************************************************/
    // ocl invariant witness
    Option witness =
        Option.builder("w")
            .longOpt("witness")
            .desc(
                "compute a witness object diagram, which is a valid instance of the class diagram and satisfies the OCL constraints")
            .numberOfArgs(0)
            .build();
    options.addOption(witness);

    // oclDiff
    Option diff =
        Option.builder("diff")
            .longOpt("diff")
            .desc(
                "compute diff between  an old version and the new version of two ocl model and print the the diff."
                    + "the diff is a set of witness object diagram and a tracing of invariant of the new model in the older one")
            .numberOfArgs(0)
            .build();
    options.addOption(diff);

    // ocl operations  diff
    Option opDiff =
        Option.builder("odiff")
            .longOpt("odiff")
            .desc(
                "Converts CD + OCL-Model with Operation constraints in SMT and produces a set of witness Object"
                    + "Diagrams as witness for the operation.")
            .numberOfArgs(0)
            .build();
    options.addOption(opDiff);

    // ocl operations  diff
    Option opWitness =
        Option.builder("ow")
            .longOpt("ow")
            .desc(
                "Computes the semantic difference(of an operation constraint) between a new OCL-Model and an old"
                    + " one. The result is a set of witness object diagrams and specifications tracing.")
            .numberOfArgs(0)
            .build();
    options.addOption(opWitness);

    // introduces the ocl constraints
    Option oOcl =
        Option.builder("ocl")
            .longOpt("OCL-constraint")
            .desc("introduces the set of OCl constraints")
            .argName("oclFiles")
            .numberOfArgs(Option.UNLIMITED_VALUES)
            .build();
    options.addOption(oOcl);

    // introduces the new version of the oclConstraint
    Option nOcl =
        Option.builder("nocl")
            .longOpt("newOcl")
            .desc("introduces the new version of the  ocl constraints")
            .argName("oclFiles")
            .numberOfArgs(Option.UNLIMITED_VALUES)
            .build();
    options.addOption(nOcl);

    // introduces the ocl constraints
    Option posOD =
        Option.builder("od")
            .longOpt("OD-Example")
            .desc("introduces system state that muss always appear with od")
            .argName("odFiles")
            .numberOfArgs(Option.UNLIMITED_VALUES)
            .build();
    options.addOption(posOD);

    // introduces the new version of the oclConstraint
    Option negOD =
        Option.builder("nod")
            .longOpt("negative-od")
            .desc("introduces system state that muss not  appear appear with od")
            .argName("odFileFiles")
            .numberOfArgs(Option.UNLIMITED_VALUES)
            .build();
    options.addOption(negOD);

    // introduces the class diagram
    Option cd =
        Option.builder("cd")
            .longOpt("class-diagram")
            .desc("introduce the old class diagram")
            .argName("class-diagram")
            .numberOfArgs(1)
            .build();
    options.addOption(cd);

    // introduces the new version of the class diagram
    Option newCd =
        Option.builder("ncd")
            .longOpt("new class diagram")
            .desc("introduce a new class diagram")
            .argName("new-class-diagram")
            .numberOfArgs(1)
            .build();
    options.addOption(newCd);

    // option for partial models
    Option partial =
        Option.builder("pt")
            .longOpt("new class diagram")
            .desc("When set, the witness object diagram produce is partial in term of attributes")
            .numberOfArgs(0)
            .build();
    options.addOption(partial);

    // option for operation constraint
    Option method =
        Option.builder("mn")
            .longOpt("Method-Name")
            .desc("introduce the the Method name , the diff or witness has to be computed ")
            .numberOfArgs(1)
            .build();
    options.addOption(method);

    Option output =
        Option.builder("o")
            .longOpt("output")
            .desc("introduce the output directory to print the results")
            .numberOfArgs(1)
            .build();
    options.addOption(output);

    return options;
  }
}
