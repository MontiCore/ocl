package de.monticore.ocl2smt;


import com.microsoft.z3.*;
import de.monticore.cd2smt.context.CDContext;
import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;

import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.types.check.OCLDeriver;
import de.monticore.ocl.util.SymbolTableUtil;
import de.monticore.types.check.TypeCheckResult;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;


public class Expr2SmtTest extends AbstractTest {
    protected static final String RELATIVE_MODEL_PATH = "src/ocl2smttest/resources/de.monticore.ocl2smt";
    protected List<BoolExpr> res;
    protected CDContext cdContext = new CDContext(new Context());

    Optional<ASTOCLCompilationUnit> oclopt;

    @BeforeEach
    public void setup() {
        Log.init();
        Log.enableFailQuick(false);
        SymbolTableUtil.prepareMill();
        SymbolTableUtil.addCd4cSymbols();
        SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/AuctionCD.sym");
        SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/DefaultTypes.sym");
        oclopt = this.parse(Paths.get(RELATIVE_MODEL_PATH, "Test01.ocl").toString(), false);
        OCL2SMTGenerator ocl2SMTGenerator = new OCL2SMTGenerator(cdContext);
        Assertions.assertTrue(oclopt.isPresent());
        res = ocl2SMTGenerator.ocl2smt(oclopt.get().getOCLArtifact());
    }

    @Test
    public void testTypCheck() {


        // when / then
        SymbolTableUtil.runSymTabGenitor(oclopt.get());
        SymbolTableUtil.runSymTabCompleter(oclopt.get());

        OCLDeriver deriver = new OCLDeriver();

        TypeCheckResult t = deriver.deriveType(((ASTOCLInvariant) oclopt.get().
                getOCLArtifact().getOCLConstraint(0)).getExpression());

        // Additional check that nothing broke
        assertThat(Log.getErrorCount()).isEqualTo(0);
        assertThat(t.isPresentResult()).isTrue();
    }

    @Test
    public void testComparisonConverter() {
        Assertions.assertEquals(res.get(12).getSExpr(), "(< 10 3)");
        Assertions.assertEquals(res.get(13).getSExpr(), "(> 10 4)");
        Assertions.assertEquals(res.get(14).getSExpr(), "(<= 10 4)");
        Assertions.assertEquals(res.get(15).getSExpr(), "(>= 10 4)");
        Assertions.assertEquals(res.get(16).getSExpr(), "(= 10 4)");
        Assertions.assertEquals(res.get(17).getSExpr(), "(not (= 10 4))");
    }

    @Test
    public void testArithmeticExpressionConverter() {
        Assertions.assertEquals(res.get(8).getSExpr(), "(= (+ 10 12) 22)");
        Assertions.assertEquals(res.get(9).getSExpr(), "(= (div 10 5) 2)");
        Assertions.assertEquals(res.get(10).getSExpr(), "(= (* 10 5) 50)");
        Assertions.assertEquals(res.get(11).getSExpr(), "(= (mod 10 2) 0)");
        Assertions.assertEquals(res.get(18).getSExpr(), "(= (- 10 12) (* (- 1) 2))");
    }

    @Test
    public void testLogicExpressionConverter() {
        Assertions.assertEquals(res.get(0), cdContext.getContext().mkBool(true));
        Assertions.assertEquals(res.get(1), cdContext.getContext().mkFalse());
        Assertions.assertEquals(res.get(2).getSExpr(), "(not true)");
        Assertions.assertEquals(res.get(3).getSExpr(), "(not false)");
        Assertions.assertEquals(res.get(4).getSExpr(), "(and false false)");
        Assertions.assertEquals(res.get(5).getSExpr(), "(and false true)");
        Assertions.assertEquals(res.get(6).getSExpr(), "(or true false)");
        Assertions.assertEquals(res.get(7).getSExpr(), "(or true true)");
    }


}



