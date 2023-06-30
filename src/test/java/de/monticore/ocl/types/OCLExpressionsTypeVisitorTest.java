package de.monticore.ocl.types;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLArtifact;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLConstraint;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.ocl.ocl._symboltable.IOCLArtifactScope;
import de.monticore.ocl.types.check.OCLTraverserProvider;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.Type4Ast;
import de.monticore.visitor.ITraverser;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

public class OCLExpressionsTypeVisitorTest extends AbstractTest {
  
  // we can use our own type4Ast instance to try to find occurrences of
  // Type Visitors using the map from the mill instead of the provided one
  protected Type4Ast type4Ast;
  
  protected ITraverser typeMapTraverser;
  
  protected ITraverser scopeGenitor;
  
  protected OCLParser parser;
  
  //@BeforeEach
  public void setupDefaultMill() {
    System.out.println("before each");
    OCLMill.reset();
    OCLMill.init();
    BasicSymbolsMill.initializePrimitives();
    parser = OCLMill.parser();
    type4Ast = new Type4Ast();
    OCLTraverserProvider typeTraverserProvider = new OCLTraverserProvider();
    typeTraverserProvider.setType4Ast(type4Ast);
    typeMapTraverser = typeTraverserProvider.init(OCLMill.traverser());
    scopeGenitor = OCLMill.traverser();
  }
  
  protected static Stream<Arguments> source() {
    return Stream.of(Arguments.of("(byte) 5", "byte"), Arguments.of("(int) 5", "int"));
  }
  
  @ParameterizedTest
  @MethodSource("source")
  protected void checkExpr(String exprStr, String expectedType) throws IOException {
    setupDefaultMill();
    ASTExpression expr = parseExpr(exprStr);
    generateScopes(expr);
    calculateTypes(expr);
    assertNoFindings();
    Assertions.assertTrue(getType4Ast().hasTypeOfExpression(expr),
        "No type calculated for expression " + exprStr);
    SymTypeExpression type = getType4Ast().getTypeOfExpression(expr);
    assertNoFindings();
    Assertions.assertEquals(expectedType, type.printFullName(),
        "Wrong type for expression " + exprStr);
  }
  
  protected ASTExpression parseExpr(String exprStr) throws IOException {
    Optional<ASTExpression> astExpression = parseStringExpr(exprStr);
    Assertions.assertTrue(astExpression.isPresent());
    return astExpression.get();
  }
  
  // Parse a String expression of the according language
  protected Optional<ASTExpression> parseStringExpr(String exprStr) throws IOException {
    return parser.parse_StringExpression(exprStr);
  }
  
  protected void generateScopes(ASTExpression expr) {
    // create a root
    ASTOCLConstraint oclConstraint = OCLMill.oCLInvariantBuilder().setExpression(expr).build();
    ASTOCLArtifact oclArtifact =
        OCLMill.oCLArtifactBuilder().setName("fooArtifact").addOCLConstraint(oclConstraint).build();
    ASTOCLCompilationUnit compilationUnit =
        OCLMill.oCLCompilationUnitBuilder().setOCLArtifact(oclArtifact).build();
    /*ASTTypeIfThenExpression rootNode = OCLMill.typeIfThenExpressionBuilder()
        .setExpression(expr)
        .build();*/
    IOCLArtifactScope rootScope = OCLMill.scopesGenitorDelegator().createFromAST(compilationUnit);
    rootScope.setName("fooRoot");
    // complete the symbol table
    expr.accept(getScopeGenitor());
  }
  
  protected void calculateTypes(ASTExpression expr) {
    expr.accept(typeMapTraverser);
  }
  
  protected Type4Ast getType4Ast() {
    return type4Ast;
  }
  
  protected ITraverser getScopeGenitor() {
    return scopeGenitor;
  }
  
}