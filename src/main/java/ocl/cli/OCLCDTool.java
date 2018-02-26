/**
 * ******************************************************************************
 *  MontiCAR Modeling Family, www.se-rwth.de
 *  Copyright (c) 2017, Software Engineering Group at RWTH Aachen,
 *  All rights reserved.
 *
 *  This project is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version.
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * *******************************************************************************
 */
package ocl.cli;

import java.io.*;
import java.nio.file.Paths;
import java.util.Optional;

import de.monticore.ModelingLanguageFamily;
import de.monticore.io.paths.ModelPath;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.symboltable.GlobalScope;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.ResolvingConfiguration;
import de.monticore.umlcd4a.CD4AnalysisLanguage;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDCompilationUnit;
import de.monticore.umlcd4a.cd4analysis._parser.CD4AnalysisParser;
import de.monticore.umlcd4a.symboltable.CD4AnalysisSymbolTableCreator;
import de.se_rwth.commons.logging.Log;
import ocl.LogConfig;
import ocl.monticoreocl.ocl._ast.ASTCompilationUnit;
import ocl.monticoreocl.ocl._cocos.OCLCoCoChecker;
import ocl.monticoreocl.ocl._cocos.OCLCoCos;
import ocl.monticoreocl.ocl._symboltable.OCLLanguage;
import ocl.monticoreocl.ocl._symboltable.OCLSymbolTableCreator;
import ocl.monticoreocl.ocl._visitors.CD4A2PlantUMLVisitor;
import org.antlr.v4.runtime.RecognitionException;
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

        Option path = new Option("path", "project-path", true, "absolute path to project, " +
                "required when ocl given as qualified name");
        options.addOption(path);

        Option cd = new Option("cd", "classdiagram", true, "input classdiagram as string");
        options.addOption(cd);

        Option ocl = new Option("ocl", "ocl-file", true, "input ocl as qualified name or string");
        options.addOption(ocl);

        Option printSrc = new Option("printSrc", "classdiagram", true, "input classdiagram as string");
        options.addOption(printSrc);
        Option printTgt = new Option("printTgt", "classdiagram", true, "output path for visualized classdiagram");
        options.addOption(printTgt);
        Option showAtt = new Option("showAttributes", "showAttributes", false, "show attributes when printing cd");
        options.addOption(showAtt);
        Option showAssoc = new Option("showAssociationNames", "showAssociationNames", false, "show association names when printing cd");
        options.addOption(showAssoc);
        Option showRoles = new Option("showRoleNames", "showRoleNames", false, "show role name when printing cd");
        options.addOption(showRoles);
        Option showCard = new Option("showNoCardinality", "showNoCardinality", false, "don't show cardinality when printing cd");
        options.addOption(showCard);



        Option verbose = new Option("verbose", "verbose", false, "sets verbose logging");
        options.addOption(verbose);

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
        Boolean verb = cmd.hasOption("verbose");

        if (cmd.hasOption("path") && cmd.hasOption("ocl") && isQualifiedName(oclModel)) {
            loadOclModel(parentDir, oclModel, verb);
            if (Log.getErrorCount() > 0) {
                System.out.println("There are errors!");
            } else {
                System.out.println("OCL Model loaded successfully!");
            }
        } else if (cmd.hasOption("ocl") && cmd.hasOption("cd") && !isQualifiedName(oclModel) && !isQualifiedName(cdModel)) {
            loadOclFromString(oclModel, cdModel, verb);
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


    }

    protected static ASTCompilationUnit loadOclFromString(String oclModel, String cdModel, Boolean verbose) {
        final OCLLanguage ocllang = new OCLLanguage();
        final CD4AnalysisLanguage cd4AnalysisLang = new CD4AnalysisLanguage();

        if (!verbose) {
            LogConfig.init();
        }

        try {
            ModelPath modelPath = new ModelPath();

            ModelingLanguageFamily modelingLanguageFamily = new ModelingLanguageFamily();
            modelingLanguageFamily.addModelingLanguage(ocllang);
            modelingLanguageFamily.addModelingLanguage(cd4AnalysisLang);
            GlobalScope globalScope = new GlobalScope(modelPath, modelingLanguageFamily);

            ResolvingConfiguration resolvingConfiguration = new ResolvingConfiguration();
            resolvingConfiguration.addDefaultFilters(ocllang.getResolvers());
            resolvingConfiguration.addDefaultFilters(cd4AnalysisLang.getResolvers());

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
        } catch (IOException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Error during parsing of ocl model.");
    }


    protected static ASTCompilationUnit loadOclModel(String parentDirectory, String modelFullQualifiedFilename, Boolean verbose) {
        final OCLLanguage ocllang = new OCLLanguage();
        final CD4AnalysisLanguage cd4AnalysisLang = new CD4AnalysisLanguage();

        if (!verbose) {
            LogConfig.init();
        }

        try {
            ModelPath modelPath = new ModelPath(Paths.get(parentDirectory));

            ModelingLanguageFamily modelingLanguageFamily = new ModelingLanguageFamily();
            modelingLanguageFamily.addModelingLanguage(ocllang);
            modelingLanguageFamily.addModelingLanguage(cd4AnalysisLang);
            GlobalScope globalScope = new GlobalScope(modelPath, modelingLanguageFamily);

            ResolvingConfiguration resolvingConfiguration = new ResolvingConfiguration();
            resolvingConfiguration.addDefaultFilters(ocllang.getResolvers());
            resolvingConfiguration.addDefaultFilters(cd4AnalysisLang.getResolvers());
            OCLSymbolTableCreator oclSymbolTableCreator = ocllang.getSymbolTableCreator(resolvingConfiguration, globalScope).get();
            Optional<ASTCompilationUnit> astOCLCompilationUnit = ocllang.getModelLoader().loadModel(modelFullQualifiedFilename, modelPath);

            if (astOCLCompilationUnit.isPresent()) {
                astOCLCompilationUnit.get().accept(oclSymbolTableCreator);
                OCLCoCoChecker checker = OCLCoCos.createChecker();
                checker.checkAll(astOCLCompilationUnit.get());
                return astOCLCompilationUnit.get();
            }
        } catch (RecognitionException e) {
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