package de.monticore.ocl2smt;


import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.od4report.prettyprinter.OD4ReportFullPrettyPrinter;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.io.FileUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OCLDiffTest extends AbstractTest {
    protected static final String RELATIVE_MODEL_PATH = "src/test/resources/de/monticore/ocl2smt/OCLDiff";
    protected  static  final String RELATIVE_TARGET_PATH = "target/generated/sources/annotationProcessor/java/ocl2smttest";
    public void setUp(){
        Log.init();
        OCLMill.init();
        CD4CodeMill.init();
    }
    protected ASTOCLCompilationUnit parseOCl(String cdFileName, String oclFileName) throws IOException {
        setUp();
        return OCL_Loader.loadAndCheckOCL(Paths.get(RELATIVE_MODEL_PATH, oclFileName).toFile(),Paths.get(RELATIVE_MODEL_PATH, cdFileName).toFile() );
    }
    protected ASTCDCompilationUnit parseCD(String cdFileName) throws IOException{
        setUp();
        return OCL_Loader.loadAndCheckCD(Paths.get(RELATIVE_MODEL_PATH, cdFileName).toFile());
    }

    public void printOD(ASTODArtifact od) {
        Path outputFile = Paths.get(RELATIVE_TARGET_PATH, od.getObjectDiagram().getName() + ".od");
        try {
            FileUtils.writeStringToFile(outputFile.toFile(), new OD4ReportFullPrettyPrinter().prettyprint(od), Charset.defaultCharset());
        } catch (Exception e) {
            e.printStackTrace();
            Assertions.fail("It Was Not Possible to Print the Object Diagram");
        }
    }
    @Test
    public void simpleTest() throws IOException {
        ASTCDCompilationUnit ast = parseCD("Auction.cd");

        Set<ASTOCLCompilationUnit> pocl = new HashSet<>();
        pocl.add(parseOCl("Auction.cd", "PosConstraint1.ocl"));
        pocl.add(parseOCl("Auction.cd", "PosConstraint2.ocl"));
        Set<ASTOCLCompilationUnit> nocl = new HashSet<>();
        nocl.add(parseOCl("Auction.cd", "negConstraint1.ocl"));
        nocl.add(parseOCl("Auction.cd", "negConstraint2.ocl"));

        OCLDiffGenerator oclDiffGenerator = new OCLDiffGenerator();
        Set<ASTODArtifact> ods = oclDiffGenerator.oclDiff(ast, pocl, nocl);

        for (ASTODArtifact od : ods) {
            printOD(od);
        }
        List<String> odNames = new ArrayList<>();
        ods.forEach(od -> odNames.add(od.getObjectDiagram().getName()));

        org.junit.jupiter.api.Assertions.assertTrue(odNames.contains("MinIdent_5"));
        org.junit.jupiter.api.Assertions.assertTrue(odNames.contains("Exists_ONLY_Facebook_AND_BMW4"));
        org.junit.jupiter.api.Assertions.assertTrue(odNames.contains("BMW_ident_is_3"));
        org.junit.jupiter.api.Assertions.assertTrue(odNames.contains("Exists_Exactly_2_Auction"));

        org.junit.jupiter.api.Assertions.assertFalse(odNames.contains("MaxIdent_9"));
        org.junit.jupiter.api.Assertions.assertFalse(odNames.contains("Exists_one_Auction"));


    }
}
