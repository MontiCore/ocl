package de.monticore.ocl.types;

import static de.monticore.types3.util.DefsTypesForTests.*;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.ocl.AbstractTest;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLArtifact;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLConstraint;
import de.monticore.ocl.ocl._parser.OCLParser;
import de.monticore.ocl.types3.OCLSymTypeRelations;
import de.monticore.ocl.types3.util.OCLCollectionSymTypeFactory;
import de.monticore.ocl.util.SymbolTableUtil;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.symbols.basicsymbols._symboltable.IBasicSymbolsScope;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types3.TypeCheck3;
import de.monticore.types3.util.DefsTypesForTests;
import de.monticore.types3.util.DefsVariablesForTests;
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
    return Stream.of(
        Arguments.of("varboolean@pre", "boolean"),
        Arguments.of("varint@pre", "int"),
        Arguments.of("varPerson@pre", "Person"));
  }

  @ParameterizedTest
  @MethodSource("atPreQualificationExpressions")
  protected void checkAtPreQualificationExpression(String exprStr, String expectedType)
      throws IOException {
    checkExpr(exprStr, expectedType);
  }

  @Test
  protected void complexAtPreTest1() throws IOException {
    TypeSymbol person = BasicSymbolsMill.globalScope().resolveType("Person").get();
    person.addFunctionSymbol(function("getAge", _intSymType));
    person.addVariableSymbol(variable("age", _intSymType));
    // todo enable test after type dispatcher fix
    org.junit.jupiter.api.Assumptions.assumeFalse(true);
    checkExpr("varPerson@pre.getAge()", "int");
    checkExpr("(varPerson@pre).getAge()", "int");
    checkExpr("varPerson@pre.age", "int");
    checkExpr("(varPerson@pre).age", "int");
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
    SymTypeExpression type = TypeCheck3.typeOf(expr);
    assertNoFindings();

    assertFalse(type.isObscureType(), "No type calculated for expression " + exprStr);
    SymTypeExpression typeNormalized = OCLSymTypeRelations.normalize(type);
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
    SymTypeExpression type = TypeCheck3.typeOf(expr);
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

  // TODO Randbedingungen wann TODOs abgearbeitet werden

}
