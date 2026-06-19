package de.monticore.ocl.codegen;

import de.monticore.class2mc.Class2MCResolver;
import de.monticore.io.paths.MCPath;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._symboltable.IOCLGlobalScope;
import de.monticore.ocl.ocl._symboltable.OCLScopesGenitorDelegator;
import de.monticore.ocl.util.SymbolTableUtil;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.symbols.basicsymbols._symboltable.IBasicSymbolsScope;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbolDeSer;
import de.monticore.symboltable.serialization.json.JsonObject;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.cli.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class OCL2JavaGeneratorTool {

  public static void gradleMain(String[] args){
    main(args);
  }

  public static void main(String[] args) {
    Options options = new Options();

    Option oclOption = Option.builder("ocl")
        .required(true)
        .hasArgs()
        .valueSeparator(',')
        .desc("Comma-separated list of OCL files")
        .build();

    Option symbolsOption = Option.builder("symbols")
        .required(true)
        .hasArgs()
        .valueSeparator(',')
        .desc("Comma-separated list of symbol paths")
        .build();

    Option outputOption = Option.builder("out")
        .required(true)
        .hasArg()
        .desc("Output directory")
        .build();

    Option domainOption = Option.builder("domain")
        .required(false)
        .hasArgs()
        .valueSeparator(',')
        .desc("Comma-separated list of domain models")
        .build();

    Option prefixPackageOption = Option.builder("prefixPackage")
        .required(false)
        .hasArg()
        .desc("Prefix package for the generated code")
        .build();

    options.addOption(oclOption);
    options.addOption(symbolsOption);
    options.addOption(outputOption);
    options.addOption(domainOption);
    options.addOption(prefixPackageOption);

    CommandLineParser parser = new DefaultParser();
    HelpFormatter formatter = new HelpFormatter();

    CommandLine cmd = null;
    try {
      cmd = parser.parse(options, args);
    } catch (ParseException e) {
      System.err.println(e.getMessage());
      formatter.printHelp("GeneratorApp", options);
      System.exit(1);
    }

    List<Path> oclFiles = Arrays.stream(cmd.getOptionValues("ocl"))
        .map(Paths::get)
        .collect(Collectors.toList());

    List<Path> symbolPaths = Arrays.stream(cmd.getOptionValues("symbols"))
        .map(Paths::get)
        .collect(Collectors.toList());

    Path outputDir = Paths.get(cmd.getOptionValue("out"));

    List<String> domainModels = cmd.hasOption("domain")
        ? List.of(cmd.getOptionValues("domain"))
        : List.of();

    String prefixPackage = cmd.getOptionValue("prefixPackage");

    OCL2JavaGeneratorTool tool = new OCL2JavaGeneratorTool();
    tool.generate(oclFiles, symbolPaths, outputDir, domainModels, prefixPackage);
  }

  public void generate(List<Path> oclFiles, List<Path> symbolPaths, Path outputDir, List<String> domainModels, String prefixPackage) {
    initSymtab(symbolPaths, domainModels);
    for (Path oclFile : oclFiles) {
      Log.info("Processing " + oclFile, "ocl2java");
      try {
        Optional<ASTOCLCompilationUnit> astOpt = OCLMill.parser().parse(oclFile.toString());
        if (astOpt.isPresent()) {
          ASTOCLCompilationUnit ast = astOpt.get();

          String fullPackage = (prefixPackage != null && !prefixPackage.isEmpty())
              ? (ast.isPresentPackage() && !ast.getPackage().isEmpty() ? prefixPackage + "." + ast.getPackage() : prefixPackage)
              : ast.getPackage();

          Path outputPath = outputDir.resolve(fullPackage.replace(".", "/")).resolve(ast.getOCLArtifact().getName() + ".java");
          createSymtab(ast);
          outputPath.toFile().getParentFile().mkdirs();
          OCL2JavaGenerator.generate(ast, fullPackage, outputPath.toString());
        } else {
          Log.error("Can not parse from ocl " + oclFile);
        }
      } catch (IOException | NullPointerException e) {
        Log.error("Can not generate from ocl " + oclFile, e);
      }
    }
  }

  @SuppressWarnings("removal")
  public static void createSymtab(ASTOCLCompilationUnit ocl) {
    OCLScopesGenitorDelegator genitor = OCLMill.scopesGenitorDelegator();
    SymbolTableUtil.addDefaultImports(genitor.createFromAST(ocl));
    
    SymbolTableUtil.runSymTabCompleter(ocl);
  }

  public static void initSymtab(List<Path> symbolPaths, List<String> domainModels) {
    OCLMill.reset();
    OCLMill.init();
    OCLMill.globalScope().clear();
    BasicSymbolsMill.initializePrimitives();

    Class2MCResolver resolver = new Class2MCResolver();
    OCLMill.globalScope().addAdaptedTypeSymbolResolver(resolver);
    SymbolTableUtil.addOclpLibrary();

    NoRegexOCLTypeCheck3.init();

    DomainTypeUtil.getInstance().init(domainModels);

    IOCLGlobalScope gs = OCLMill.globalScope();

    gs.putTypeSymbolDeSer("de.monticore.cdbasis._symboltable.CDTypeSymbol");
    gs.putFunctionSymbolDeSer("de.monticore.cd4codebasis._symboltable.CDMethodSignatureSymbol");
    gs.putVariableSymbolDeSer("de.monticore.symbols.oosymbols._symboltable.FieldSymbol");
    gs.putSymbolDeSer("de.monticore.cdassociation._symboltable.CDRoleSymbol", new VariableSymbolDeSer() {
      @Override
      public SymTypeExpression deserializeType(IBasicSymbolsScope scope, JsonObject symbolJson) {
        if (symbolJson.getStringMember("cardinality").equals("[*]")) {
          return SymTypeExpressionFactory.createGenerics(OCLMill.globalScope().resolveType("List").get(), List.of(super.deserializeType(scope, symbolJson)));
        } else {
          return super.deserializeType(scope, symbolJson);
        }
      }
    });

    gs.setSymbolPath(new MCPath(symbolPaths));
  }
}
