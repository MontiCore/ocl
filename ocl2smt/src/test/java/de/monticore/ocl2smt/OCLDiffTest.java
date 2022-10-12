package de.monticore.ocl2smt;


import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Context;

import de.monticore.cd2smt.Helper.Identifiable;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.od4report.prettyprinter.OD4ReportFullPrettyPrinter;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odbasis._ast.ASTODNamedObject;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.io.FileUtils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class OCLDiffTest extends AbstractTest {
    protected static final String RELATIVE_MODEL_PATH = "src/test/resources/de/monticore/ocl2smt/OCLDiff";
    protected  static  final String RELATIVE_TARGET_PATH = "target/generated/sources/annotationProcessor/java/ocl2smttest";
   protected static Map<String, String> ctxParam = new HashMap<>();

    public void setUp(){
        Log.init();
        OCLMill.init();
        CD4CodeMill.init();
        ctxParam.put("model", "true");
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
    public void test_ocl_diff() throws IOException {
        ASTCDCompilationUnit ast = parseCD("Auction.cd");

        Set<ASTOCLCompilationUnit> pocl = new HashSet<>();
        pocl.add(parseOCl("Auction.cd", "PosConstraint1.ocl"));
        pocl.add(parseOCl("Auction.cd", "PosConstraint2.ocl"));
        Set<ASTOCLCompilationUnit> nocl = new HashSet<>();
        nocl.add(parseOCl("Auction.cd", "negConstraint2.ocl"));
        nocl.add(parseOCl("Auction.cd", "negConstraint1.ocl"));

        Set<ASTODArtifact> ods = OCLDiffGenerator.oclDiff(ast, pocl, nocl,new Context(ctxParam));
        ods.forEach(this::printOD);
        List<String> odNames = new ArrayList<>();
        ods.forEach(od -> odNames.add(od.getObjectDiagram().getName()));

        org.junit.jupiter.api.Assertions.assertEquals(8, odNames.size());
        org.junit.jupiter.api.Assertions.assertTrue(odNames.contains("inv_6"));
        org.junit.jupiter.api.Assertions.assertTrue(odNames.contains("inv_12"));
        org.junit.jupiter.api.Assertions.assertTrue(odNames.contains("inv_20"));
        org.junit.jupiter.api.Assertions.assertTrue(odNames.contains("inv_26"));

        org.junit.jupiter.api.Assertions.assertTrue(odNames.contains("UNSAT_CORE_5"));
        org.junit.jupiter.api.Assertions.assertTrue(odNames.contains("UNSAT_CORE_11"));
        org.junit.jupiter.api.Assertions.assertTrue(odNames.contains("UNSAT_CORE_15"));
        org.junit.jupiter.api.Assertions.assertTrue(odNames.contains("UNSAT_CORE_20"));


    }

    @Test
    public void testOdPartial() throws IOException {
        ASTCDCompilationUnit cdAST = parseCD("Partial/Partial.cd");
        Set<ASTOCLCompilationUnit> oclSet = new HashSet<>();
        oclSet.add(parseOCl("Partial/Partial.cd","Partial/partial.ocl"));

        List<Identifiable< BoolExpr>> constraintList = OCLDiffGenerator.getPositiveSolverConstraints(cdAST,oclSet, new Context(ctxParam));
        Optional<ASTODArtifact> od = OCLDiffGenerator.buildOd(OCLDiffGenerator.cdContext, "Partial", constraintList, cdAST.getCDDefinition(),true);
        assert od.isPresent();
        printOD(od.get());

        od.get().getObjectDiagram().getODElementList().forEach(p->{
            assert !(p instanceof ASTODNamedObject) || (((ASTODNamedObject) p).getODAttributeList().size() <= 3);
        });
    }
}
