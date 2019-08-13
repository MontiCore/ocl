/* (c) https://github.com/MontiCore/monticore */
package ocl.cli;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import de.monticore.ModelingLanguageFamily;
import de.monticore.io.paths.ModelPath;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.ResolvingConfiguration;
import de.monticore.types.types._ast.ASTImportStatement;
import de.monticore.umlcd4a.CD4AnalysisLanguage;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDCompilationUnit;
import de.monticore.umlcd4a.cd4analysis._parser.CD4AnalysisParser;
import de.monticore.umlcd4a.symboltable.CD4AnalysisSymbolTableCreator;
import de.se_rwth.commons.SourcePosition;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
import ocl.LogConfig;
import ocl.monticoreocl.ocl._ast.ASTCompilationUnit;
import ocl.monticoreocl.ocl._cocos.OCLCoCoChecker;
import ocl.monticoreocl.ocl._cocos.OCLCoCos;
import ocl.monticoreocl.ocl._parser.OCLParser;
import ocl.monticoreocl.ocl._symboltable.OCLLanguage;
import ocl.monticoreocl.ocl._symboltable.OCLSymbolTableCreator;
import ocl.monticoreocl.ocl._visitors.CD4A2PlantUMLVisitor;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class OCLCDTool {

    public static void main(String[] args) throws Exception {

        Log.enableFailQuick(false);

        Options options = new Options();

        Option preload = new Option("preloadCD", "preloadCD", false, "preload classdiagrams in browser");
        options.addOption(preload);
        Option path = new Option("path", "project-path", true, "absolute path to project, " +
                "required when ocl given as qualified name");
        options.addOption(path);
        Option cd = new Option("cd", "classdiagram", true, "input classdiagram as string");
        options.addOption(cd);
        Option ocl = new Option("ocl", "ocl-file", true, "input ocl as qualified name or string");
        options.addOption(ocl);
        Option parseOpt = new Option("parseOnly", "parseOnly", false, "only parse the cd model, don't check cocos");
        options.addOption(parseOpt);
        Option logErrOpt = new Option("logErrTo", "logErrTo", true, "Log errors to this file");
        options.addOption(logErrOpt);

        Option printSrc = new Option("printSrc", "printCDSrc", true, "input classdiagram as string");
        options.addOption(printSrc);
        Option printTgt = new Option("printTgt", "printCDTgt", true, "output path for visualized classdiagram");
        options.addOption(printTgt);
        Option showAtt = new Option("showAttributes", "showAttributes", false, "show attributes when printing cd");
        options.addOption(showAtt);
        Option showAssoc = new Option("showAssociationNames", "showAssociationNames", false, "show association names when printing cd");
        options.addOption(showAssoc);
        Option showRoles = new Option("showRoleNames", "showRoleNames", false, "show role name when printing cd");
        options.addOption(showRoles);
        Option showCard = new Option("showNoCardinality", "showNoCardinality", false, "don't show cardinality when printing cd");
        options.addOption(showCard);



        Option verboseOpt = new Option("verbose", "verbose", false, "sets verbose logging");
        options.addOption(verboseOpt);

        CommandLineParser parser = new BasicParser();
        CommandLine cmd;

        try {
            cmd = parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printHelp(options);
            return;
        }

        String parentDir = cmd.getOptionValue("path");
        String oclModel = cmd.getOptionValue("ocl");
        String cdModel = cmd.getOptionValue("cd");
        String cdString = cmd.getOptionValue("printSrc");
        String cdPath = cmd.getOptionValue("printTgt");
        Boolean verbose = cmd.hasOption("verbose");
        Boolean parse = cmd.hasOption("parseOnly");
        Boolean preloadCD = cmd.hasOption("preloadCD");

        // Disable verbose logging
        if (!verbose) {
            LogConfig.init();
        }

        if (cmd.hasOption("path") && cmd.hasOption("ocl") && isQualifiedName(oclModel)) {
            if(!parse) {
                loadOclModel(parentDir, oclModel,preloadCD);
            } else {
                parseOcl(parentDir, oclModel);
            }
            if (Log.getErrorCount() > 0) {
                System.out.println("There are errors!");
            } else {
                System.out.println("OCL Model loaded successfully!");
            }
        } else if (cmd.hasOption("ocl") && !isQualifiedName(oclModel)) {
            if(!parse) {
                loadOclFromString(oclModel, cdModel);
            } else {
                parseOclFromString(oclModel);
            }
            if (Log.getErrorCount() > 0) {
                System.out.println("There are errors!");
            } else {
                System.out.println("OCL Model loaded successfully!");
            }
        } else if (cmd.hasOption("printSrc") && cmd.hasOption("printTgt")) {
            printCD2PlantUML(cdString, cdPath, cmd.hasOption("showAttributes"), cmd.hasOption("showAssociationNames"),
                    cmd.hasOption("showRoleNames"), !cmd.hasOption("showNoCardinality"));
        } else {
            printHelp(options);
        }
        if(cmd.hasOption("logErrTo")) {
            findingsToJSON(cmd.getOptionValue("logErrTo"));
        }
    }

    public static void findingsToJSON(String logErrPath) {
        try {
            String findings = findingsToJSON();
            File newTextFile = new File(logErrPath);
            FileWriter fw = new FileWriter(newTextFile);
            fw.write(findings);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String findingsToJSON() {
        List<Finding> findings = Log.getFindings();
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        for (Finding f:findings) {
            int sl = -1, sc = -1, el = -1, ec = -1;
            if (f.getSourcePosition().isPresent()) {
                sl = f.getSourcePosition().get().getLine();
                sc = f.getSourcePosition().get().getColumn();
            }
            if (f.getSourcePositionEnd() != null && f.getSourcePositionEnd().isPresent()) {
                el = f.getSourcePositionEnd().get().getLine();
                ec = f.getSourcePositionEnd().get().getColumn();
            }
            sb.append("{\n");
            String positions = String.format("    \"pos\": { \"sl\": %d, \"el\": %d, \"sc\": %d, \"ec\": %d },\n", sl, el, sc, ec);
            sb.append(positions);
            sb.append("    \"type\": \"").append(f.getType()).append("\",\n");
            sb.append("    \"message\": \"").append(f.getMsg()).append("\"\n");
            sb.append("},\n");
        }
        sb.deleteCharAt(sb.length()-1);
        sb.deleteCharAt(sb.length()-1);
        sb.append("\n]\n");
        return sb.toString();

    }

    private static void parseOcl(String parent, String oclModel) {
        try {
            Path oclModelPath = Paths.get(parent + "\\" + oclModel.replaceAll("\\.", "/") + ".ocl");
            OCLParser parser = new OCLParser();
            Optional<ASTCompilationUnit> oclAST = parser.parse(oclModelPath.toString());
            if (!oclAST.isPresent())
                Log.error("Could not parse OCL Model!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void parseOclFromString(String oclModel) {
        try {
            OCLParser oclParser = new OCLParser();
            Optional<ASTCompilationUnit> oclAST = oclParser.parse_String(oclModel);
            if (!oclAST.isPresent())
                Log.error("Could not parse OCL Model!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static ASTCompilationUnit loadOclFromString(String oclModel, String cdModel) {
        final OCLLanguage ocllang = new OCLLanguage();
        final CD4AnalysisLanguage cd4AnalysisLang = new CD4AnalysisLanguage();

        try {
            ModelPath modelPath = new ModelPath();

            ModelingLanguageFamily modelingLanguageFamily = new ModelingLanguageFamily();
            modelingLanguageFamily.addModelingLanguage(ocllang);
            modelingLanguageFamily.addModelingLanguage(cd4AnalysisLang);
            GlobalScope globalScope = new GlobalScope(modelPath, modelingLanguageFamily);

            ResolvingConfiguration resolvingConfiguration = new ResolvingConfiguration();
            resolvingConfiguration.addDefaultFilters(ocllang.getResolvingFilters());
            resolvingConfiguration.addDefaultFilters(cd4AnalysisLang.getResolvingFilters());

            CD4AnalysisSymbolTableCreator cd4AnalysisSymbolTableCreator = cd4AnalysisLang.getSymbolTableCreator(resolvingConfiguration, globalScope).get();
            Optional<ASTCDCompilationUnit> astcdCompilationUnit = cd4AnalysisLang.getParser().parse_String(cdModel);

            if (!astcdCompilationUnit.isPresent()) {
                Log.error("Could not load CD Model!");
                return null;
            }

            MutableScope scope = cd4AnalysisSymbolTableCreator.createFromAST(astcdCompilationUnit.get()).getSubScopes().get(0).getAsMutableScope();
            OCLSymbolTableCreator oclSymbolTableCreator = ocllang.getSymbolTableCreator(resolvingConfiguration, scope).get();
            Optional<ASTCompilationUnit> astOCLCompilationUnit = ocllang.getParser().parse_String(oclModel);

            if (!astOCLCompilationUnit.isPresent()) {
                Log.error("Could not load OCL Model!");
                return null;
            }

            oclSymbolTableCreator.createFromAST(astOCLCompilationUnit.get());
            OCLCoCoChecker checker2 = OCLCoCos.createChecker();
            checker2.checkAll(astOCLCompilationUnit.get());
            return astOCLCompilationUnit.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Error during parsing of ocl model.");
    }


    protected static ASTCompilationUnit loadOclModel(String parentDirectory, String modelFullQualifiedFilename, boolean preloadCD) {
        final OCLLanguage ocllang = new OCLLanguage();
        final CD4AnalysisLanguage cd4AnalysisLang = new CD4AnalysisLanguage();

        try {
            ModelPath modelPath = new ModelPath(Paths.get(parentDirectory));

            ModelingLanguageFamily modelingLanguageFamily = new ModelingLanguageFamily();
            modelingLanguageFamily.addModelingLanguage(ocllang);
            modelingLanguageFamily.addModelingLanguage(cd4AnalysisLang);
            GlobalScope globalScope = new GlobalScope(modelPath, modelingLanguageFamily);

            ResolvingConfiguration resolvingConfiguration = new ResolvingConfiguration();
            resolvingConfiguration.addDefaultFilters(ocllang.getResolvingFilters());
            resolvingConfiguration.addDefaultFilters(cd4AnalysisLang.getResolvingFilters());
            OCLSymbolTableCreator oclSymbolTableCreator = ocllang.getSymbolTableCreator(resolvingConfiguration, globalScope).get();
            CD4AnalysisSymbolTableCreator cdSymbolTableCreator = cd4AnalysisLang.getSymbolTableCreator(resolvingConfiguration, globalScope).get();

            Optional<ASTCompilationUnit> astOCLCompilationUnit;

            if (preloadCD) {
                String path = parentDirectory + "/" + modelFullQualifiedFilename.replace(".", "/") + ".ocl";
                if (path.startsWith("/"))
                    path = path.substring(1);

                //System.out.println("path: " + path);
                OCLParser parser = new OCLParser();
                astOCLCompilationUnit = parser.parse(path);
            }
            else {
                astOCLCompilationUnit = ocllang.getModelLoader().loadModel(modelFullQualifiedFilename, modelPath);
            }
            //System.out.println("astOCLCompilationUnit.isPresent(): " + astOCLCompilationUnit.isPresent());
            if (astOCLCompilationUnit.isPresent()) {
                if (preloadCD) {
                    for (ASTImportStatement importS : astOCLCompilationUnit.get().getImportStatementList()) {
                        // need to reload all cd4 files and add symbols, b/c the model loader does not work in JS file system (anything is wrong with path calculation)
                        // ignore here .* imports -> this is only for the fiddle
                        String cdPath = parentDirectory + "/" + importS.getImportList().stream().collect(Collectors.joining("/")) + ".cd";
                        if (cdPath.startsWith("/"))
                            cdPath = cdPath.substring(1);
                        //System.out.println("cdPath: " + cdPath);
                        CD4AnalysisParser cdParser = new CD4AnalysisParser();
                        Optional<ASTCDCompilationUnit> astcdCompilationUnit = cdParser.parse(cdPath);
                        //System.out.println("astcdCompilationUnit.isPresent(): " + astcdCompilationUnit.isPresent());
                        if (astcdCompilationUnit.isPresent()) {
                            astcdCompilationUnit.get().accept(cdSymbolTableCreator);
                        }
                    }
                }

                astOCLCompilationUnit.get().accept(oclSymbolTableCreator);
                OCLCoCoChecker checker = OCLCoCos.createChecker();
                checker.checkAll(astOCLCompilationUnit.get());
                return astOCLCompilationUnit.get();
            } else {
              Log.error("Could not create AST (probably model could not be loaded).");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Error during loading of model " + modelFullQualifiedFilename + ".");
    }


    private static void printHelp(Options options) {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp("java -jar OCLCDTool", options);
        System.out.println("\nExample with qualified names:");
        System.out.println("java -jar OCLCDTool -path C.\\path\\to\\project -ocl de.monticore.myConstraint");
        System.out.println("Or with data as string:");
        System.out.println("java -jar OCLCDTool -ocl \"package xyz;\\nocl {\\nconstraint ...\\n}\" " +
                "-cd \"package xyz;\\nclassdiagram ABC {\\n...\\n}\"");

        System.exit(1);
        return;
    }

    /**
     * @return if name is of pattern abc.ab.c
     */
    private static boolean isQualifiedName(String name) {
        return name.matches("^(\\w+\\.)*\\w+$");
    }

    protected static void printCD2PlantUML(String cdString, String cdPath, Boolean showAtt, Boolean showAssoc,
                                           Boolean showRoles, Boolean showCard) {
        // Log.debug("OCLCDTool","Printing plantuml cd to: " + Paths.get(cdPath).toAbsolutePath());

        String plantUMLString = printCD2PlantUML(cdString, showAtt, showAssoc, showRoles, showCard);

        try {
            File newTextFile = new File(cdPath);
            FileWriter fw = new FileWriter(newTextFile);
            fw.write(plantUMLString);
            fw.close();
        } catch (IOException e) {
            Log.error(e.getMessage());
        }
    }

    protected static String printCD2PlantUML(String cdString) {
        return printCD2PlantUML(cdString, false, false, false, true);
    }

    protected static String printCD2PlantUML(String cdString, Boolean showAtt, Boolean showAssoc,
                                             Boolean showRoles, Boolean showCard) {
        IndentPrinter printer = new IndentPrinter();
        CD4A2PlantUMLVisitor cdVisitor = new CD4A2PlantUMLVisitor(printer, showAtt, showAssoc, showRoles, showCard);
        CD4AnalysisParser parser = new CD4AnalysisParser();
        String plantUMLString = "@startuml\n@enduml";

        try {
            Optional<ASTCDCompilationUnit> astCD = parser.parse_String(cdString);
            if (astCD.isPresent()) {
                cdVisitor.print2PlantUML(astCD.get());
                plantUMLString = printer.getContent();
            }
        } catch (IOException e) {
            Log.error("Cannot display CD since it contains errors!");
        }

        return plantUMLString;
    }
}
