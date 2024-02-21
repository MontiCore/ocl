package de.monticore.od2smt;

import static de.monticore.od2smt.OD2SMTUtils.getAttributeValue;

import com.microsoft.z3.*;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cd2smt.Helper.SMTHelper;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTMill;
import de.monticore.cd2smt.cd2smtGenerator.assocStrategies.AssociationStrategy;
import de.monticore.cd2smt.cd2smtGenerator.classStrategies.ClassStrategy;
import de.monticore.cd2smt.cd2smtGenerator.inhrStrategies.InheritanceStrategy;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl2smt.helpers.IOHelper;
import de.monticore.ocl2smt.ocldiff.OCLDiffAbstractTest;
import de.monticore.ocl2smt.util.OCL_Loader;
import de.monticore.od4report.OD4ReportTool;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odbasis._ast.ASTODNamedObject;
import de.monticore.odbasis._ast.ASTODObject;
import de.se_rwth.commons.logging.Log;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class OD2SMTGeneratorTest extends OCLDiffAbstractTest {
  protected ASTODArtifact od;
  protected ASTODArtifact witness;
  protected OD2SMTGenerator od2SMTGenerator;
  protected Model model;
  protected String MODEL_PATH = "src/test/resources/de/monticore/od2smt/";

  @BeforeEach
  public void setup() throws IOException {
    Log.init();
    initMills();
  }

  @ParameterizedTest
  @MethodSource("cd2smtStrategies")
  public void testObject2smt(
      ClassStrategy.Strategy cs, InheritanceStrategy.Strategy is, AssociationStrategy.Strategy as) {
    CD2SMTMill.init(cs, is, as);
    computeWitness();
    ASTODNamedObject person_0 = (ASTODNamedObject) convertObject("person_0", "Person");

    Assertions.assertEquals("10", getAttributeValue(person_0, "age"));
    Assertions.assertEquals("\"Naruto\"", getAttributeValue(person_0, "name"));
    Assertions.assertEquals("true", getAttributeValue(person_0, "isAdult"));
    Assertions.assertEquals("\"2023-01-05 15:30:00\"", getAttributeValue(person_0, "birthDate"));
    Assertions.assertEquals("DE", getAttributeValue(person_0, "country"));
  }

  @ParameterizedTest
  @MethodSource("cd2smtStrategies")
  public void testLink2smt(
      ClassStrategy.Strategy cs, InheritanceStrategy.Strategy is, AssociationStrategy.Strategy as) {
    CD2SMTMill.init(cs, is, as);
    computeWitness();
    ASTODNamedObject person_0 = (ASTODNamedObject) convertObject("person_0", "Person");
    ASTODNamedObject auction_0 = (ASTODNamedObject) convertObject("auction_0", "Auction");
    Assertions.assertTrue(checkLink(auction_0.getName(), person_0.getName(), witness));
  }

  public void computeWitness() {
    ASTCDCompilationUnit cd = null;
    try {
      cd = OCL_Loader.loadAndCheckCD(new File(MODEL_PATH + "Auction.cd"));
    } catch (IOException e) {
      Log.error("Unable to parse Auction.cd");
    }

    od = new OD4ReportTool().parse(MODEL_PATH + "Auction.od");
    Context ctx = buildContext();
    od2SMTGenerator = new OD2SMTGenerator();

    od2SMTGenerator.od2smt(od, cd, ctx);

    // build ConstraintsList
    List<IdentifiableBoolExpr> solverConstraints =
        new ArrayList<>(od2SMTGenerator.getAllODConstraints());

    // check Sat
    Solver solver = od2SMTGenerator.cd2SMTGenerator.makeSolver(solverConstraints);
    Assertions.assertEquals(solver.check(), Status.SATISFIABLE);
    model = solver.getModel();

    // build od
    Optional<ASTODArtifact> newOd = od2SMTGenerator.cd2SMTGenerator.smt2od(model, false, "Auction");
    Assertions.assertTrue(newOd.isPresent());
    witness = newOd.get();

    // print result
    IOHelper.printOD(witness, Path.of("target/generated/od2smt"));
  }

  public ASTODObject convertObject(String oldObjectName, String objType) {
    Expr<? extends Sort> oldObj =
        od2SMTGenerator.getObject(OD2SMTUtils.getObject(oldObjectName, od));
    String newObjName = SMTHelper.buildObjectName(model.evaluate(oldObj, true), objType);

    return OD2SMTUtils.getObject(newObjName, witness);
  }
}
