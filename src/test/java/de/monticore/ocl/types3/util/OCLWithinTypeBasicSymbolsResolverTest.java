package de.monticore.ocl.types3.util;

import de.monticore.expressions.commonexpressions._ast.ASTFieldAccessExpression;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsVisitor2;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisVisitor2;
import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.util.SymbolTableUtil;
import de.monticore.symboltable.ISymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.SymTypeRelations;
import de.monticore.types3.TypeCheck3;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// todo https://git.rwth-aachen.de/monticore/monticore/-/work_items/5099
@Disabled
public class OCLWithinTypeBasicSymbolsResolverTest extends AbstractTest {

  @BeforeEach
  public void setUp() {
    super.initLogger();
    super.initMills();
  }

  @Test
  void flattenedSymTypeHasSourceInfoOfOriginalVariable() {
    final Optional<ASTOCLCompilationUnit> optAST = parse(RELATIVE_MODEL_PATH
            + "/testinput/parsable/symtab/coco/not_javagen/chainedAssocs.ocl", false);
    assertTrue(optAST.isPresent());
    final ASTOCLCompilationUnit ast = optAST.get();

    SymbolTableUtil.prepareMill();
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/AuctionCD.sym");
    SymbolTableUtil.loadSymbolFile("src/test/resources/testinput/CDs/DefaultTypes.sym");

    SymbolTableUtil.runSymTabGenitor(ast);
    SymbolTableUtil.runSymTabCompleter(ast);

    new AssertSourceSymbolPresentVisitor(
            "message",
            "AuctionCD.Person.message"
    ).traverseAndCheck(ast);
    assertNoFindings();
  }

  @Test
  void typeChecksStaticMethodQualifierAfterParentExpression() {
    final Optional<ASTOCLCompilationUnit> optAST = parse(RELATIVE_MODEL_PATH
        + "/testinput/types3/util/ExplicitTypeImportStaticAccess.ocl", false);
    assertTrue(optAST.isPresent());
    final ASTOCLCompilationUnit ast = optAST.get();

    SymbolTableUtil.prepareMill();
    OCLMill.globalScope().getSymbolPath().addEntry(Paths.get("target/classes/java/test"));

    SymbolTableUtil.runSymTabGenitor(ast);
    SymbolTableUtil.runSymTabCompleter(ast);

    ASTExpression expression = ((ASTOCLInvariant) ast
        .getOCLArtifact()
        .getOCLConstraint(0))
        .getExpression();
    SymTypeExpression type = TypeCheck3.typeOf(expression);

    assertTrue(SymTypeRelations.isBoolean(type));
    new AssertSourceSymbolPresentVisitor("MyFunctionalModule", "class2mc_examples.MyFunctionalModule")
        .traverseAndCheck(ast);
    assertNoFindings();
  }

  /**
   * Makes sure that when type checking a NameExpression or FieldAccessExpression referencing the
   * given name, a source symbol is present and the full name of the symbol matches the
   * expectedFullSymbolName.
   */
  private static class AssertSourceSymbolPresentVisitor
          implements ExpressionsBasisVisitor2, CommonExpressionsVisitor2 {

    private String variableName;
    private String expectedFullSymbolName;
    private boolean foundMatch = false;

    public AssertSourceSymbolPresentVisitor(String variableName, String expectedFullSymbolName) {
      this.variableName = variableName;
      this.expectedFullSymbolName = expectedFullSymbolName;
    }

    public void traverseAndCheck(ASTOCLCompilationUnit ast) {
      OCLTraverser traverser = OCLMill.traverser();
      traverser.add4ExpressionsBasis(this);
      traverser.add4CommonExpressions(this);
      ast.accept(traverser);
      assertTrue(foundMatch, "Found no match for variable: " + variableName);
    }

    @Override
    public void visit(ASTNameExpression expr) {
      if (expr.getName().equals(variableName)) {
        assertSourceSymbolPresent(expr);
      }
    }

    @Override
    public void visit(ASTFieldAccessExpression expr) {
      if (expr.getName().equals(variableName)) {
        assertSourceSymbolPresent(expr);
      }
    }

    protected void assertSourceSymbolPresent(ASTExpression expression) {
      SymTypeExpression symType = TypeCheck3.typeOf(expression);
      Optional<ISymbol> srcSymbol = symType.getSourceInfo().getSourceSymbol();
      assertTrue(srcSymbol.isPresent());
      assertEquals(expectedFullSymbolName, srcSymbol.get().getFullName());
      foundMatch = true;
    }
  }
}
