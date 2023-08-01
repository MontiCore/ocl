package de.monticore.ocl.types;

import static de.monticore.types3.util.DefsTypesForTests._booleanSymType;
import static de.monticore.types3.util.DefsTypesForTests._intSymType;
import static de.monticore.types3.util.DefsTypesForTests._personSymType;
import static de.monticore.types3.util.DefsTypesForTests._studentSymType;
import static de.monticore.types3.util.DefsTypesForTests.function;
import static de.monticore.types3.util.DefsTypesForTests.inScope;
import static de.monticore.types3.util.DefsTypesForTests.variable;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLArtifact;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLConstraint;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.ocl.ocl.types3.OCLTypeTraverserFactory;
import de.monticore.ocl.types3.IOCLSymTypeRelations;
import de.monticore.ocl.types3.OCLSymTypeRelations;
import de.monticore.ocl.types3.util.OCLCollectionSymTypeFactory;
import de.monticore.ocl.util.SymbolTableUtil;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.symbols.basicsymbols._symboltable.IBasicSymbolsScope;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types3.Type4Ast;
import de.monticore.types3.util.DefsTypesForTests;
import de.monticore.types3.util.DefsVariablesForTests;
import de.monticore.visitor.ITraverser;
import de.se_rwth.commons.logging.Finding;
import de.se_rwth.commons.logging.Log;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class OCLExpressionsTypeVisitorTest extends AbstractTest {

  // we can use our own type4Ast instance to try to find occurrences of
  // Type Visitors using the map from the mill instead of the provided one
  protected Type4Ast type4Ast;

  protected IOCLSymTypeRelations symTypeRelations;

  protected ITraverser typeMapTraverser;

  protected ITraverser scopeGenitor;

  protected OCLParser parser;

  @BeforeEach
  public void setup() {
    initMills();
    BasicSymbolsMill.initializePrimitives();
    SymbolTableUtil.addOclpLibrary();
    DefsTypesForTests.set_thePrimitives();
    DefsTypesForTests.set_boxedPrimitives();
    DefsTypesForTests.set_unboxedMapSymType();
    DefsTypesForTests.set_unboxedObjects();
    DefsTypesForTests.set_objectTypes();
    DefsTypesForTests.set_specialSymTypes();

    setupValues();

    parser = OCLMill.parser();
    type4Ast = new Type4Ast();
    symTypeRelations = new OCLSymTypeRelations();

    typeMapTraverser = new OCLTypeTraverserFactory().createTraverser(type4Ast);

    scopeGenitor = OCLMill.traverser();
    assertNoFindings();
  }

  protected void setupValues() {
    IBasicSymbolsScope gs = BasicSymbolsMill.globalScope();
    DefsVariablesForTests.set_thePrimitives(gs);
    DefsVariablesForTests.set_boxedPrimitives(gs);
    DefsVariablesForTests.set_unboxedObjects(gs);
    DefsVariablesForTests.set_objectTypes(gs);
    inScope(gs, variable("intArray1", SymTypeExpressionFactory.createTypeArray(_intSymType, 1)));
    inScope(gs, variable("intArray2", SymTypeExpressionFactory.createTypeArray(_intSymType, 2)));
    inScope(gs, variable("intArray3", SymTypeExpressionFactory.createTypeArray(_intSymType, 3)));
  }

  protected static Stream<Arguments> castExpressions() {
    return Stream.of(
        Arguments.of("(boolean) false", "boolean"),
        Arguments.of("(int) 5", "int"),
        Arguments.of("(long) 5", "long"),
        Arguments.of("(long) 5l", "long"),
        Arguments.of("(long) 5L", "long"),
        Arguments.of("(double) 5.0", "double"));
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
        Arguments.of("(float) 5.0", "float"));
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
        Arguments.of("(double) 5.0f", "double"));
  }

  @ParameterizedTest
  @MethodSource("upcastExpressions")
  protected void checkUpcast(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }

  protected static Stream<Arguments> typeIfExpressions() { // TODO FDr UnionType ready machen
    return Stream.of(
        Arguments.of("typeif vardouble instanceof double then 5.0 else 2*2", "double"),
        Arguments.of("typeif vardouble instanceof int then 5 else 5.0", "double"));
  }

  @ParameterizedTest
  @MethodSource("typeIfExpressions")
  protected void checkTypeIfExpressions(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }

  @Test
  protected void checkTypeIfExpressionCorrectTypeInThen() throws IOException {
    // add Student::getSemester
    BasicSymbolsMill.globalScope()
        .resolveType("Student")
        .get()
        .addFunctionSymbol(function("getSemester", _intSymType));
    // varPerson.getSemester() allowed iff varPerson is a Student
    checkExpr("typeif varPerson instanceof Student then varPerson.getSemester() else -1", "int");
  }

  @Test
  protected void checkTypeIfExpressionRedundant() throws IOException {
    checkErrorExpr("typeif varStudent instanceof Person then varStudent else varPerson", "0xFD290");
  }

  protected static Stream<Arguments> ifThenElseExpressions() {
    return Stream.of(
        Arguments.of("if true then 5.0 else 2*2", "double"),
        Arguments.of("if true then 5 else 5.0", "double"),
        Arguments.of("if true then varPerson else varStudent", "Person"));
  }

  @ParameterizedTest
  @MethodSource("ifThenElseExpressions")
  protected void checkIfThenElseExpressions(String exprStr, String expectedType)
      throws IOException {
    checkExpr(exprStr, expectedType);
  }

  protected static Stream<Arguments> impliesExpressions() {
    return Stream.of(Arguments.of("true implies false", "boolean"));
  }

  @ParameterizedTest
  @MethodSource("impliesExpressions")
  protected void checkImpliesExpressions(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }

  protected static Stream<Arguments> equivalentExpressions() {
    return Stream.of(Arguments.of("true <=> false", "boolean"));
  }

  @ParameterizedTest
  @MethodSource("equivalentExpressions")
  protected void checkEquivalentExpressions(String exprStr, String expectedType)
      throws IOException {
    checkExpr(exprStr, expectedType);
  }

  protected static Stream<Arguments> forallExpressions() {
    return Stream.of(Arguments.of("forall int num in {1,2,3} : (num + 1) > 1", "boolean"));
  }

  @ParameterizedTest
  @MethodSource("forallExpressions")
  protected void checkForallExpressions(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }

  protected static Stream<Arguments> existsExpressions() {
    return Stream.of(Arguments.of("exists int num in {1,2,3} : (num + 1) < 3", "boolean"));
  }

  @ParameterizedTest
  @MethodSource("existsExpressions")
  protected void checkExistsExpressions(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }

  protected static Stream<Arguments> anyExpressions() {
    return Stream.of(
        Arguments.of("any {1}", "int") // TODO MSm create valid expression
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
        Arguments.of("let double a = 5.0; int b = 5 in a*b", "double"));
  }

  @ParameterizedTest
  @MethodSource("letInExpressions")
  protected void checkLetInExpression(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }

  protected static Stream<Arguments> iterateExpressions() {
    return Stream.of(
        Arguments.of("iterate { int a in {1,2,3}; int count = 0 : count=count+1 }", "int"),
        Arguments.of("iterate { a in {1,2,3} ; count = 0 : count=count+1 }", "int"),
        Arguments.of("iterate { int a in {1,2,3}; List<int> l = [0] : l = l.add(a)}", "List<int>"));
  }

  @ParameterizedTest
  @MethodSource("iterateExpressions")
  protected void checkIterateExpression(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }

  @Test
  protected void checkIterateExpressionDomainType() throws IOException {
    // add Person::getAge
    BasicSymbolsMill.globalScope()
        .resolveType("Person")
        .get()
        .addFunctionSymbol(function("getAge", _intSymType));
    checkExpr("iterate { Person p; int totalAge = 0 : totalAge = totalAge+p.getAge()}", "int");
    checkExpr(
        "iterate { Person p in Person; int totalAge = 0 : totalAge = totalAge+p.getAge()}", "int");
  }

  protected static Stream<Arguments> instanceofExpressions() {
    return Stream.of(
        Arguments.of("true instanceof boolean", "boolean"),
        Arguments.of("null instanceof boolean", "boolean"),
        Arguments.of("null instanceof int", "boolean"));
  }

  @ParameterizedTest
  @MethodSource("instanceofExpressions")
  protected void checkInstanceofExpression(String exprStr, String expectedType) throws IOException {
    checkExpr(exprStr, expectedType);
  }

  protected static Stream<Arguments> arrayQualificationExpressions() {
    return Stream.of(
        Arguments.of("intArray1[0]", "int"),
        Arguments.of("intArray2[0]", "int[]"),
        Arguments.of("intArray2[0][0]", "int"));
  }

  @ParameterizedTest
  @MethodSource("arrayQualificationExpressions")
  protected void checkArrayQualificationExpression(String exprStr, String expectedType)
      throws IOException {
    checkExpr(exprStr, expectedType);
  }

  @Test
  protected void complexArrayQualificationExpressionTest1() throws IOException {
    IBasicSymbolsScope gs = BasicSymbolsMill.globalScope();
    // Map<Person, List<Student>[]>[][] c;
    gs.add(
        variable(
            "c",
            SymTypeExpressionFactory.createTypeArray(
                OCLCollectionSymTypeFactory.createMap(
                    _personSymType,
                    SymTypeExpressionFactory.createTypeArray(
                        OCLCollectionSymTypeFactory.createList(_studentSymType), 1)),
                2)));
    checkExpr("c[0][0][varPerson][0][0]", "Student");
  }

  @Test
  protected void complexArrayQualificationExpressionTest2() throws IOException {
    IBasicSymbolsScope gs = BasicSymbolsMill.globalScope();
    // Optional<Map<Student, List<Map<Student, boolean[][]>>>>[] c;
    gs.add(
        variable(
            "c",
            SymTypeExpressionFactory.createTypeArray(
                OCLCollectionSymTypeFactory.createOptional(
                    OCLCollectionSymTypeFactory.createMap(
                        _studentSymType,
                        OCLCollectionSymTypeFactory.createList(
                            OCLCollectionSymTypeFactory.createMap(
                                _studentSymType,
                                SymTypeExpressionFactory.createTypeArray(_booleanSymType, 2))))),
                1)));
    // due to OCL rules,
    // listOfMaps[notIntegral] == [m[notIntegral] | m in listOfMaps],
    // so (in specific cases) we can switch the order of arguments
    checkExpr("c[0][varStudent]", "Optional<List<Map<Student,boolean[][]>>>");
    checkExpr("c[0][varStudent][0]", "Optional<Map<Student,boolean[][]>>");
    checkExpr("c[0][varStudent][varStudent]", "Optional<List<boolean[][]>>");
    checkExpr("c[0][varStudent][varStudent][0][0]", "Optional<boolean[]>");
    checkExpr("c[0][varStudent][0][varStudent][0]", "Optional<boolean[]>");
  }

  protected static Stream<Arguments> atPreQualificationExpressions() {
    return Stream.of(Arguments.of("varboolean@pre", "boolean"), Arguments.of("varint@pre", "int"));
  }

  @ParameterizedTest
  @MethodSource("atPreQualificationExpressions")
  protected void checkAtPreQualificationExpression(String exprStr, String expectedType)
      throws IOException {
    checkExpr(exprStr, expectedType);
  }

  protected static Stream<Arguments> transitiveQualificationExpressions() {
    return Stream.of(Arguments.of("varPerson**", "Person"));
  }

  @ParameterizedTest
  @MethodSource("transitiveQualificationExpressions")
  protected void checkTransitiveQualificationExpression(String exprStr, String expectedType)
      throws IOException {
    checkExpr(exprStr, expectedType);
  }

  protected void checkExpr(String exprStr, String expectedType) throws IOException {
    assertNoFindings();
    Optional<ASTExpression> astExpression = parser.parse_StringExpression(exprStr);
    assertTrue(astExpression.isPresent(), "Cannot parse expression '" + exprStr + '"');
    ASTExpression expr = astExpression.get();
    assertNoFindings();

    generateScopes(expr);
    assertNoFindings();
    expr.accept(typeMapTraverser);
    assertNoFindings();

    assertTrue(
        getType4Ast().hasTypeOfExpression(expr), "No type calculated for expression " + exprStr);
    SymTypeExpression type = getType4Ast().getTypeOfExpression(expr);
    SymTypeExpression typeNormalized = getTypeRel().normalize(type);
    assertNoFindings();
    Assertions.assertEquals(
        expectedType, typeNormalized.printFullName(), "Wrong type for expression " + exprStr);
  }

  protected void checkErrorExpr(String exprStr, String expectedError) throws IOException {
    assertNoFindings();
    Optional<ASTExpression> astExpression = parser.parse_StringExpression(exprStr);
    assertTrue(astExpression.isPresent(), "Cannot parse expression '" + exprStr + '"');
    ASTExpression expr = astExpression.get();
    assertNoFindings();

    generateScopes(expr);
    assertNoFindings();
    expr.accept(typeMapTraverser);
    SymTypeExpression type = getType4Ast().getTypeOfExpression(astExpression.get());
    assertTrue(
        type.isObscureType(),
        "expected Obscure for expression \"" + exprStr + "\" but got " + type.printFullName());
    assertHasErrorCode(expectedError);
    Log.getFindings().clear();
  }

  protected List<String> getAllErrorCodes() {
    return Log.getFindings().stream()
        .filter(Finding::isError)
        .map(err -> err.getMsg().split(" ")[0])
        .collect(Collectors.toList());
  }

  protected void assertHasErrorCode(String code) {
    assertTrue(
        getAllErrorCodes().stream().anyMatch(code::equals),
        "Error \""
            + code
            + "\" expected, "
            + "but instead the errors are:"
            + System.lineSeparator()
            + Log.getFindings().stream()
                .map(Finding::buildMsg)
                .collect(Collectors.joining(System.lineSeparator()))
            + System.lineSeparator());
  }

  protected void generateScopes(ASTExpression expr) {
    // create a root
    ASTOCLConstraint oclConstraint = OCLMill.oCLInvariantBuilder().setExpression(expr).build();
    ASTOCLArtifact oclArtifact =
        OCLMill.oCLArtifactBuilder().setName("fooArtifact").addOCLConstraint(oclConstraint).build();
    ASTOCLCompilationUnit compilationUnit =
        OCLMill.oCLCompilationUnitBuilder().setOCLArtifact(oclArtifact).build();

    // complete the symbol table
    SymbolTableUtil.runSymTabGenitor(compilationUnit);
    SymbolTableUtil.runSymTabCompleter(compilationUnit);
  }

  protected Type4Ast getType4Ast() {
    return type4Ast;
  }

  protected IOCLSymTypeRelations getTypeRel() {
    return symTypeRelations;
  }

  protected ITraverser getScopeGenitor() {
    return scopeGenitor;
  }

  // TODO Randbedingungen wann TODOs abgearbeitet werden

}
