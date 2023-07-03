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
import de.se_rwth.commons.logging.Log;
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
  
  @BeforeEach
  public void setup() {
    Log.getFindings().clear();
    
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
  
  protected static Stream<Arguments> castExpressions() {
    return Stream.of(
        Arguments.of("(boolean) false", "boolean"),
        Arguments.of("(byte) 0xA", "byte"), // TODO MSm cannot parse hex literals
        Arguments.of("(byte) 0xb", "byte"), // TODO MSm cannot parse hex literals
        Arguments.of("(int) 5", "int"),
        Arguments.of("(long) 5", "long"),
        Arguments.of("(long) 5l", "long"),
        Arguments.of("(long) 5L", "long"),
        Arguments.of("(double) 5.0", "double")
    );
  }
  
  // TODO cannot downcast
  @ParameterizedTest
  @MethodSource("castExpressions")
  protected void checkCast(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }
  
  protected static Stream<Arguments> downcastExpressions() {
    return Stream.of(
        Arguments.of("(byte) 5", "byte"),
        Arguments.of("(short) 5", "short"),
        Arguments.of("(char) 5", "char"),
        Arguments.of("(int) 5.0", "int"),
        Arguments.of("(float) 5.0", "float")
    );
  }
  
  @ParameterizedTest
  @MethodSource("downcastExpressions")
  protected void checkDowncast(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }
  
  protected static Stream<Arguments> upcastExpressions() {
    return Stream.of(
        Arguments.of("(float) 5", "float"),
        Arguments.of("(long) 5", "long"),
        Arguments.of("(double) 5", "double"),
        Arguments.of("(double) 5.0f", "double")
    );
  }
  
  @ParameterizedTest
  @MethodSource("upcastExpressions")
  protected void checkUpcast(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }
  
  protected static Stream<Arguments> typeIfExpressions() {
    return Stream.of(
        Arguments.of("let double a = 5.0 in (typeif a instanceof double then 5.0 else 2*2)", "double"), // then case
        Arguments.of("let double a = 5.0 in (typeif a instanceof int then 5 else 5.0)", "double") // else case
    );
  }
  
  @ParameterizedTest
  @MethodSource("typeIfExpressions")
  protected void checkTypeIfExpressions(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }
  
  protected static Stream<Arguments> ifThenElseExpressions() {
    return Stream.of(
        Arguments.of("if true then 5.0 else 2*2", "double"), // then case
        Arguments.of("if true then 5 else 5.0", "double") // else case
    );
  }
  
  @ParameterizedTest
  @MethodSource("ifThenElseExpressions")
  protected void checkIfThenElseExpressions(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }
  
  protected static Stream<Arguments> impliesExpressions() {
    return Stream.of(
        Arguments.of("true implies false", "boolean")
    );
  }
  
  @ParameterizedTest
  @MethodSource("impliesExpressions")
  protected void checkImpliesExpressions(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }
  
  protected static Stream<Arguments> equivalentExpressions() {
    return Stream.of(
        Arguments.of("true <=> false", "boolean")
    );
  }
  
  @ParameterizedTest
  @MethodSource("equivalentExpressions")
  protected void checkEquivalentExpressions(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }
  
  protected static Stream<Arguments> forallExpressions() {
    return Stream.of(
        Arguments.of("forall int num in {1,2,3} : (num + 1)", "int") // TODO MSm create valid set expression
    );
  }
  
  @ParameterizedTest
  @MethodSource("forallExpressions")
  protected void checkForallExpressions(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }
  
  protected static Stream<Arguments> existsExpressions() {
    return Stream.of(
        Arguments.of("exists int num in {1,2,3} : (num + 1)", "int") // TODO MSm create valid set expression
    );
  }
  
  @ParameterizedTest
  @MethodSource("existsExpressions")
  protected void checkExistsExpressions(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }
  
  protected static Stream<Arguments> anyExpressions() {
    return Stream.of(
        Arguments.of("any x in {1,2,3}", "int") // TODO MSm create valid expression
    );
  }
  
  @ParameterizedTest
  @MethodSource("anyExpressions")
  protected void checkAnyExpressions(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }
  
  protected static Stream<Arguments> letInExpressions() {
    return Stream.of(
        Arguments.of("let double a = 5.0 in 2*2", "int"),
        Arguments.of("let double a = 5.0 in a", "double"), // TODO MSm type check?
        Arguments.of("let double a = 5.0; int b = 5 in a*b", "double")
    );
  }
  
  @ParameterizedTest
  @MethodSource("letInExpressions")
  protected void checkTypeIfExpression(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }
  
  protected static Stream<Arguments> iterateExpressions() {
    return Stream.of(
        Arguments.of("iterate { int a ; int count = 0 : count=count+1 }", "int"),
        Arguments.of("iterate { int a in {1,2,3} ; int count : count=count+1 }", "int"),
        Arguments.of("iterate { a in {1,2,3} ; count = 0 : count=count+1 }", "int")
    );
  }
  
  @ParameterizedTest
  @MethodSource("iterateExpressions")
  protected void checkIterateExpression(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }
  
  protected static Stream<Arguments> instanceofExpressions() {
    return Stream.of(
        Arguments.of("true instanceof boolean", "boolean"),
        Arguments.of("null instanceof boolean", "boolean"),
        Arguments.of("null instanceof int", "boolean")
    );
  }
  
  @ParameterizedTest
  @MethodSource("instanceofExpressions")
  protected void checkInstanceofExpression(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }
  
  protected static Stream<Arguments> arrayQualificationExpressions() {
    return Stream.of(
        // TODO MSm how to define arrays?
    );
  }
  
  @ParameterizedTest
  @MethodSource("arrayQualificationExpressions")
  protected void checkArrayQualificationExpression(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }
  
  protected static Stream<Arguments> atPreQualificationExpressions() {
    return Stream.of(
        Arguments.of("true@pre", "boolean"),
        Arguments.of("(1+1)@pre", "int")
    );
  }
  
  @ParameterizedTest
  @MethodSource("atPreQualificationExpressions")
  protected void checkAtPreQualificationExpression(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }
  
  protected static Stream<Arguments> transitiveQualificationExpressions() {
    return Stream.of(
        Arguments.of("true**", "boolean")
    );
  }
  
  @ParameterizedTest
  @MethodSource("transitiveQualificationExpressions")
  protected void checkTransitiveQualificationExpression(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }
  
  protected void checkExpr(String exprStr, String expectedType) throws IOException {
    Optional<ASTExpression> astExpression = parser.parse_StringExpression(exprStr);
    Assertions.assertTrue(astExpression.isPresent(), "Cannot parse expression '" + exprStr + '"');
    ASTExpression expr = astExpression.get();
    
    generateScopes(expr);
    expr.accept(typeMapTraverser);
    
    assertNoFindings();
    Assertions.assertTrue(getType4Ast().hasTypeOfExpression(expr),
        "No type calculated for expression " + exprStr);
    SymTypeExpression type = getType4Ast().getTypeOfExpression(expr);
    assertNoFindings();
    Assertions.assertEquals(expectedType, type.printFullName(),
        "Wrong type for expression " + exprStr);
  }
  
  protected void generateScopes(ASTExpression expr) {
    // create a root
    ASTOCLConstraint oclConstraint = OCLMill.oCLInvariantBuilder().setExpression(expr).build();
    ASTOCLArtifact oclArtifact =
        OCLMill.oCLArtifactBuilder().setName("fooArtifact").addOCLConstraint(oclConstraint).build();
    ASTOCLCompilationUnit compilationUnit =
        OCLMill.oCLCompilationUnitBuilder().setOCLArtifact(oclArtifact).build();
    IOCLArtifactScope rootScope = OCLMill.scopesGenitorDelegator().createFromAST(compilationUnit);
    rootScope.setName("fooRoot");
    
    // complete the symbol table
    expr.accept(getScopeGenitor());
  }
  
  protected Type4Ast getType4Ast() {
    return type4Ast;
  }
  
  protected ITraverser getScopeGenitor() {
    return scopeGenitor;
  }
  
}