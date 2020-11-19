package de.monticore.types.check;

import com.google.common.collect.Lists;
import de.monticore.expressions.commonexpressions._ast.ASTCallExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.expressions.expressionsbasis._symboltable.ExpressionsBasisSymTabMill;
import de.monticore.expressions.expressionsbasis._symboltable.IExpressionsBasisScope;
import de.monticore.expressions.oclexpressions._ast.*;
import de.monticore.expressions.testoclexpressions._ast.TestOCLExpressionsMill;
import de.monticore.expressions.testoclexpressions._parser.TestOCLExpressionsParser;
import de.monticore.expressions.testoclexpressions._symboltable.*;
import de.monticore.io.paths.ModelPath;
import de.monticore.literals.mccommonliterals._ast.MCCommonLiteralsMill;
import de.monticore.symboltable.resolving.ResolvedSeveralEntriesForSymbolException;
import de.monticore.types.typesymbols._symboltable.FieldSymbol;
import de.monticore.types.typesymbols._symboltable.MethodSymbol;
import de.monticore.types.typesymbols._symboltable.TypeSymbol;
import de.monticore.types.typesymbols._symboltable.TypeSymbolLoader;
import de.se_rwth.commons.logging.Log;
import de.se_rwth.commons.logging.LogStub;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static de.monticore.types.check.DefsTypeBasic.*;
import static org.junit.Assert.*;

// TODO
// - add as much information to the log as possible (specific types, ...)
// - setScope should be called in endVisit of DeriveSymTypeOfOCLExpressions
// - write CoCos (typeif => instanceof)
// - combine forall and exists expression
// - add ternary expression
// - add a name to each expression-part
// - add coco for simpledeclaration, that either exttype or value has to be present
// - change method
/*
```
// at least one of MCReturnType or value has to be present
OCLMethodDeclaration implements OCLDeclaration <30> =
  MCReturnType? name:Name "(" param:(OCLParamDeclaration || ",")* ")"
    ("=" value:Expression)?;
```
 */
// - does OCLVariableDeclaration always need a value?

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class DeriveSymTypeOfOCLExpressionTest {
  private TestOCLExpressionsSymbolTableCreatorDelegator symbolTableCreator;

  /**
   * Focus: Deriving Type of Literals, here:
   * literals/MCLiteralsBasis.mc4
   */

  @BeforeClass
  public static void setup() {
    LogStub.init();
    LogStub.enableFailQuick(false);
  }

  @Before
  public void doBefore() {
    LogStub.init();
    DefsTypeBasic.setup();

    // TODO SVa: exchange with TestOCLExpressionsSymTabMill
    final TestOCLExpressionsLanguage language = new TestOCLExpressionsLanguage("TestOCL", "ocl") {
    };
    final TestOCLExpressionsGlobalScope globalScope = TestOCLExpressionsSymTabMill
        .testOCLExpressionsGlobalScopeBuilder()
        .setModelPath(new ModelPath())
        .setTestOCLExpressionsLanguage(language)
        .build();
    symbolTableCreator = TestOCLExpressionsSymTabMill
        .testOCLExpressionsSymbolTableCreatorDelegatorBuilder()
        .setGlobalScope(globalScope)
        .build();
  }

  private void createBasicTypes(IExpressionsBasisScope scope) {
    // we add a variety of TypeSymbols to the same scope (which in reality doesn't happen)
    add2scope(scope, DefsTypeBasic._int);
    add2scope(scope, DefsTypeBasic._char);
    add2scope(scope, DefsTypeBasic._boolean);
    add2scope(scope, DefsTypeBasic._double);
    add2scope(scope, DefsTypeBasic._float);
    add2scope(scope, DefsTypeBasic._long);

    add2scope(scope, DefsTypeBasic._array);
    add2scope(scope, DefsTypeBasic._Object);
    add2scope(scope, DefsTypeBasic._String);

    add2scope(scope, field("x", _intSymType));
    add2scope(scope, field("y", SymTypeExpressionFactory.createGenerics(new TypeSymbolLoader("List", scope), _intSymType)));
  }

  private void createPersonAndStudent(IExpressionsBasisScope scope) {
    // person
    SymTypeExpression supclass = SymTypeExpressionFactory.createTypeObject("Person", scope);
    MethodSymbol getName = method("getName", SymTypeExpressionFactory.createTypeObject("String", scope));
    FieldSymbol allInstances = field("allInstances", supclass);
    FieldSymbol field = field("name", SymTypeExpressionFactory.createTypeObject("String", scope));
    TypeSymbol superclass = type("Person", Lists.newArrayList(getName), Lists.newArrayList(field, allInstances),
        Lists.newArrayList(), Lists.newArrayList()
    );
    add2scope(scope, superclass);

    // student
    MethodSymbol getStudentId = method("getStudentId", SymTypeExpressionFactory.createTypeObject("String", scope));
    TypeSymbol subclass = type("Student", Lists.newArrayList(getStudentId), Lists.newArrayList(),
        Lists.newArrayList(supclass), Lists.newArrayList()
    );
    add2scope(scope, subclass);

    SymTypeExpression sub = SymTypeExpressionFactory.createTypeObject("Student", scope);

    add2scope(scope, field("p1", sub));
    add2scope(scope, field("p2", supclass));
  }

  private void createComplexType(IExpressionsBasisScope scope) {
    // person
    MethodSymbol getB = method("getB", SymTypeExpressionFactory.createGenerics(new TypeSymbolLoader("List", scope), _intSymType));
    FieldSymbol field = field("b", SymTypeExpressionFactory.createGenerics(new TypeSymbolLoader("List", scope), _intSymType));
    FieldSymbol assoc = field("a", SymTypeExpressionFactory.createGenerics(new TypeSymbolLoader("List", scope), SymTypeExpressionFactory.createTypeObject("A", scope)));
    TypeSymbol superclass = type("A", Lists.newArrayList(getB), Lists.newArrayList(field, assoc),
        Lists.newArrayList(), Lists.newArrayList()
    );
    add2scope(scope, superclass);
    SymTypeExpression supclass = SymTypeExpressionFactory.createTypeObject("A", scope);

    // student
    TypeSymbol subclass = type("B", Lists.newArrayList(), Lists.newArrayList(),
        Lists.newArrayList(supclass), Lists.newArrayList()
    );
    add2scope(scope, subclass);

    SymTypeExpression sub = SymTypeExpressionFactory.createTypeObject("B", scope);

    add2scope(scope, field("b", sub));
    add2scope(scope, field("a", supclass));
  }

  public SymTypeExpression typeOf(ASTOCLExpressionsNode node) {
    tc.iTypesCalculator.init();
    Optional<SymTypeExpression> result =
        ((DeriveSymTypeOfOCLCombineExpressions) tc.iTypesCalculator).calculateType(node);
    if (!result.isPresent()) {
      Log.error("0xED780 Internal Error: No Type for Expression " + node
          + " Probably TypeCheck mis-configured.");
    }
    return result.get();
  }

  // Parser used for convenience:
  // (may be any other Parser that understands CommonExpressions)
  TestOCLExpressionsParser p = new TestOCLExpressionsParser();

  // This is an auxiliary
  DeriveSymTypeOfOCLCombineExpressions derLit = new DeriveSymTypeOfOCLCombineExpressions(ExpressionsBasisSymTabMill
      .expressionsBasisScopeBuilder()
      .build());

  // other arguments not used (and therefore deliberately null)

  // This is the TypeChecker under Test:
  TypeCheck tc = new TypeCheck(new SynthesizeSymTypeFromMCCollectionTypes(), derLit);

  private void printFindingsAndFail() {
    if (!Log.getFindings().isEmpty()) {
      Log.getFindings().forEach(System.out::println);
      fail();
    }
  }
  /*--------------------------------------------------- TESTS ---------------------------------------------------------*/

  @Test
  public void deriveFromOCLInstanceOfExpression0() throws IOException {
    String s = "5 instanceof int";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLInstanceOfExpression);
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLInstanceOfExpression1() throws IOException {
    String s = "5.5 instanceof double";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLInstanceOfExpression);
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();

  }

  @Test
  public void deriveFromOCLInstanceOfExpression2() throws IOException {
    String s = "5.5 instanceof int";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLInstanceOfExpression);
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();

  }

  @Test
  public void deriveFromOCLInstanceOfExpression3() throws IOException {
    String s = "p1 instanceof int";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLInstanceOfExpression);
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLInstanceOfExpression4() throws IOException {
    String s = "p1 instanceof Person";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLInstanceOfExpression);
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  /**
   * test a unresolvable wrong type in instanceof
   */
  @Test
  public void testInvalidOCLInstanceOfExpression() throws IOException {
    String s;
    ASTExpression astex;

    // `not_present` is an external type here
/*
    s = "5.5 instanceof not_present";
    astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLInstanceOfExpression);
    tc.typeOf(astex);
    assertEquals("0x" + "A32FF The type of OCLExtType could not be found", Log.getFindings().get(0).getMsg());
*/
    s = "not_present instanceof int";
    astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLInstanceOfExpression);
    createSTFromAST(astex);

    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A3000 The type of the left expression of the OCLInstanceOfExpression could not be calculated", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void deriveFromOCLTypeIfExpression0() throws IOException {
    String s = "typeif x instanceof int then 1 else 2";
    ASTOCLTypeIfExpression astex = p.parse_StringOCLTypeIfExpression(s).get();
    createSTFromAST(astex);
    assertEquals("int", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Ignore
  @Test
  public void deriveFromOCLTypeIfExpression1() throws IOException {
    String s = "typeif (typeif x instanceof int then 1 else 2) instanceof int then 3 else 4";
    ASTOCLTypeIfExpression astex = p.parse_StringOCLTypeIfExpression(s).get();
    createSTFromAST(astex);
    assertEquals("int", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLTypeIfExpression2() throws IOException {
    String s = "typeif x instanceof int then x else 1";
    ASTOCLTypeIfExpression astex = p.parse_StringOCLTypeIfExpression(s).get();
    createSTFromAST(astex);
    assertEquals("int", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLTypeIfExpression3() throws IOException {
    String s = "typeif p1 instanceof Person then p1.getName() else \"No Name\"";
    ASTOCLTypeIfExpression astex = p.parse_StringOCLTypeIfExpression(s).get();
    createSTFromAST(astex);
    assertEquals("String", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLTypeIfExpression4() throws IOException {
    String s = "typeif p2 instanceof Student then p1.getStudentId() else \"No ID\"";
    ASTOCLTypeIfExpression astex = p.parse_StringOCLTypeIfExpression(s).get();
    createSTFromAST(astex);
    assertEquals("String", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLTypeIfExpression5() throws IOException {
    String s = "typeif x instanceof Person then x.getName() else \"Nothing\"";
    ASTOCLTypeIfExpression astex = p.parse_StringOCLTypeIfExpression(s).get();
    createSTFromAST(astex);
    assertEquals("String", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void testInvalidOCLTypeIfExpression() throws IOException {
    String s = "typeif x instanceof Person then x else 1";
    ASTOCLTypeIfExpression astex = p.parse_StringOCLTypeIfExpression(s).get();
    createSTFromAST(astex);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A3015 The type of the else expression of the OCLTypeIfExpr doesn't match the then expression", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void deriveFromOCLIfThenElseExpression0() throws IOException {
    String s = "if true then true else false";
    ASTOCLIfThenElseExpression astex = p.parse_StringOCLIfThenElseExpression(s).get();
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLIfThenElseExpression1() throws IOException {
    String s = "if 5==5 then 7 else 5";
    ASTOCLIfThenElseExpression astex = p.parse_StringOCLIfThenElseExpression(s).get();
    createSTFromAST(astex);
    assertEquals("int", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLIfThenElseExpression2() throws IOException {
    String s = "if 42/2+21 == 42 then x else 7.2";
    ASTOCLIfThenElseExpression astex = p.parse_StringOCLIfThenElseExpression(s).get();
    createSTFromAST(astex);
    assertEquals("double", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void testInvalidOCLIfThenElseExpression() throws IOException {
    String s = "if true then 1 else false";
    ASTOCLIfThenElseExpression astex = p.parse_StringOCLIfThenElseExpression(s).get();
    createSTFromAST(astex);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A3044 The type of the else expression of the OCLIfThenElseExpr doesn't match the then expression", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void deriveFromOCLImpliesExpression0() throws IOException {
    String s = "true implies true";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLImpliesExpression);
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLImpliesExpression1() throws IOException {
    String s = "false implies true";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLImpliesExpression);
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLImpliesExpression2() throws IOException {
    String s = "5<10 implies 4<10";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLImpliesExpression);
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLImpliesExpression3() throws IOException {
    String s = "(exists u in List{1,5}: u==5) implies x<10";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLImpliesExpression);
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void testInvalidOCLImpliesExpression0() throws IOException {
    String s = "43 implies true";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLImpliesExpression);
    createSTFromAST(astex, false);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A3201 The type of the left condition of the OCLImpliesExpression has to be boolean", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void testInvalidOCLImpliesExpression1() throws IOException {
    String s = "true implies 43";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLImpliesExpression);
    createSTFromAST(astex, false);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A3203 The type of the left condition of the OCLImpliesExpression has to be boolean", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void deriveFromOCLForallExpression0() throws IOException {
    String s = "forall z in {2,2,2,2}: z==2";
    ASTOCLForallExpression astex = p.parse_StringOCLForallExpression(s).get();
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLForallExpression1() throws IOException {
    String s = "forall y in List{2,2,2,1}: y==2";
    ASTOCLForallExpression astex = p.parse_StringOCLForallExpression(s).get();
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLForallExpression2() throws IOException {
    String s = "forall u in {1..10}, v in List{1..10}: u+v<21";
    ASTOCLForallExpression astex = p.parse_StringOCLForallExpression(s).get();
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLForallExpression3() throws IOException {
    String s = "forall z in A: true";
    ASTOCLForallExpression astex = p.parse_StringOCLForallExpression(s).get();
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLForallExpression4() throws IOException {
    String s = "forall z in a.b: z > 5";
    ASTOCLForallExpression astex = p.parse_StringOCLForallExpression(s).get();
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void testInvalidOCLForallExpression() throws IOException {
    String s = "forall y in List{2,2}: y == false";
    ASTOCLForallExpression astex = p.parse_StringOCLForallExpression(s).get();
    createSTFromAST(astex, false);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A0197 The resulting type of the EqualsExpression (==) cannot be calculated", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void deriveFromOCLOCLExistsExpression0() throws IOException {
    String s = "exists x in List{2,2,2,2}: x==2";
    ASTOCLExistsExpression astex = p.parse_StringOCLExistsExpression(s).get();
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLOCLExistsExpression1() throws IOException {
    String s = "exists x in List{1,1,1,1}: x==2";
    ASTOCLExistsExpression astex = p.parse_StringOCLExistsExpression(s).get();
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLOCLExistsExpression2() throws IOException {
    String s = "exists x in List{1..10}, y in List{1..10}: x+y<21";
    ASTOCLExistsExpression astex = p.parse_StringOCLExistsExpression(s).get();
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLOCLExistsExpression3() throws IOException {
    String s = "exists z in A: true";
    ASTOCLExistsExpression astex = p.parse_StringOCLExistsExpression(s).get();
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLOCLExistsExpression4() throws IOException {
    String s = "exists z in a.b: true";
    ASTOCLExistsExpression astex = p.parse_StringOCLExistsExpression(s).get();
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void testInvalidOCLExistsExpression() throws IOException {
    String s = "exists x in {2..4}: x==false";
    ASTOCLExistsExpression astex = p.parse_StringOCLExistsExpression(s).get();
    createSTFromAST(astex, false);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A0197 The resulting type of the EqualsExpression (==) cannot be calculated", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void deriveFromOCLAnyExpression0() throws IOException {
    String s = "any List{1.1, 1.1, 1.1, 1.1}";
    ASTOCLAnyExpression astex = p.parse_StringOCLAnyExpression(s).get();
    createSTFromAST(astex);
    assertEquals("double", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLAnyExpression1() throws IOException {
    String s = "any Person";
    ASTOCLAnyExpression astex = p.parse_StringOCLAnyExpression(s).get();
    createSTFromAST(astex);
    assertEquals("Person", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLAnyExpression2() throws IOException {
    String s = "any a.a";
    ASTOCLAnyExpression astex = p.parse_StringOCLAnyExpression(s).get();
    createSTFromAST(astex);
    assertEquals("A", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Ignore
  @Test
  public void testInvalidOCLAnyExpression() throws IOException {
    String s = "any 1";
    ASTOCLAnyExpression astex = p.parse_StringOCLAnyExpression(s).get();
    createSTFromAST(astex, false);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A3052 The type of the expression of the OCLAnyExpr should be a generic", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void deriveFromOCLLetinExpression0() throws IOException {
    String s = "let x=5 in x";
    ASTOCLLetinExpression astex = p.parse_StringOCLLetinExpression(s).get();
    createSTFromAST(astex);
    assertEquals("int", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLLetinExpression1() throws IOException {
    String s = "let x=5;y=6 in x+y";
    ASTOCLLetinExpression astex = p.parse_StringOCLLetinExpression(s).get();
    createSTFromAST(astex);
    assertEquals("int", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLLetinExpression2() throws IOException {
    String s = "let x=5.5;y=6 in x+y";
    ASTOCLLetinExpression astex = p.parse_StringOCLLetinExpression(s).get();
    createSTFromAST(astex);
    assertEquals("double", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLLetinExpression3() throws IOException {
    String s = "let p = any Person in p";
    ASTOCLLetinExpression astex = p.parse_StringOCLLetinExpression(s).get();
    createSTFromAST(astex);
    assertEquals("Person", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLLetinExpression4() throws IOException {
    String s = "let p = any Person; n = p.getName() in n";
    ASTOCLLetinExpression astex = p.parse_StringOCLLetinExpression(s).get();
    createSTFromAST(astex);
    assertEquals("String", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLLetinExpression5() throws IOException {
    String s = "let plus1(int a) = a + 1 in plus1(x)";
    ASTOCLLetinExpression astex = p.parse_StringOCLLetinExpression(s).get();
    createSTFromAST(astex);
    assertEquals("int", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void testInvalidOCLLetinExpression0() throws IOException {
    String s;
    ASTOCLLetinExpression astex;

    s = "let z=5 in z && true";
    astex = p.parse_StringOCLLetinExpression(s).get();
    createSTFromAST(astex, false);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A0199 The resulting type of the BooleanAndOpExpression (&&) cannot be calculated", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void testInvalidOCLLetinExpression1() throws IOException {
    String s = "let u = 1..5; v = g in v";
    ASTOCLLetinExpression astex = p.parse_StringOCLLetinExpression(s).get();
    createSTFromAST(astex, false);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A3240 The type of the OCLDeclaration of the OCLDeclaration could not be calculated", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void testInvalidOCLLetinExpression2() throws IOException {
    String s = "let plus1(int a) = a + 1 in plus1(p1)";
    ASTOCLLetinExpression astex = p.parse_StringOCLLetinExpression(s).get();
    createSTFromAST(astex, false);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A0217 The resulting type of the CallExpression cannot be calculated", Log.getFindings().get(0).getMsg());
    }
  }

  {
    Person = {
        name = "Sblksfj",
        int age = 4,
    }
  }

  @Test
  public void deriveFromOCLVariableDeclarationExpression0() throws IOException {
    String s = "int u = 4";
    ASTOCLVariableDeclaration astex = p.parse_StringOCLVariableDeclaration(s).get();
    createSTFromAST(astex);
    assertEquals("void", typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void testInvalidOCLVariableDeclarationExpression0()
      throws IOException {
    String s = "int u = 4.5";
    ASTOCLVariableDeclaration astex = p.parse_StringOCLVariableDeclaration(s).get();
    createSTFromAST(astex, false);
    try {
      typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A3067 The type of the value of the OCLVariableDeclaration (double) has to be compatible to the ExtType (int)", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void deriveFromOCLMethodDeclarationExpression0() throws IOException {
    String s = "abc(int u) = 4";
    ASTOCLMethodDeclaration astex = p.parse_StringOCLMethodDeclaration(s).get();
    createSTFromAST(astex);
    assertEquals("void", typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void testInvalidOCLMethodDeclarationExpression0() throws IOException {
    String s = "abc(int u) = undef";
    ASTOCLMethodDeclaration astex = p.parse_StringOCLMethodDeclaration(s).get();
    createSTFromAST(astex, false);
    try {
      typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A3251 The type of the value of the OCLMethodDeclaration could not be calculated", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void testInvalidOCLMethodDeclarationExpression1() throws IOException {
    // TODO SVa: CoCo should forbid that
    String s = "abc(int u, int u) = u";
    ASTOCLMethodDeclaration astex = p.parse_StringOCLMethodDeclaration(s).get();
    try {
      createSTFromAST(astex, false);
    }
    catch (ResolvedSeveralEntriesForSymbolException e) {
      assertEquals("0x" + "A4095 Found 2 symbols: u", e.getMessage());
    }
  }

  @Test
  public void deriveFromOCLIterateExpression0() throws IOException {
    String s = "iterate{x in {1,2,3,4}; int acc=0: acc = acc+x}";
    ASTOCLIterateExpression astex = p.parse_StringOCLIterateExpression(s).get();
    createSTFromAST(astex);
    assertEquals("int", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLIterateExpression1() throws IOException {
    String s = "iterate{ b in List{false, false, true}; boolean acc=false : acc = acc || b }";
    ASTOCLIterateExpression astex = p.parse_StringOCLIterateExpression(s).get();
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLIterateExpression2() throws IOException {
    String s = "iterate{x in List{1.1, 2.2, 3.3, 4.4}, y in List{4,3,2,1}; double acc=0: acc = acc+x+y}";
    ASTOCLIterateExpression astex = p.parse_StringOCLIterateExpression(s).get();
    createSTFromAST(astex);
    assertEquals("double", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLIterateExpression3() throws IOException {
    String s = "iterate{x in {{1..3},{5..9}}, y in x; double acc=0: acc = acc+y}";
    ASTOCLIterateExpression astex = p.parse_StringOCLIterateExpression(s).get();
    createSTFromAST(astex);
    assertEquals("double", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void testInvalidOCLIterateExpression() throws IOException {
    String s = "iterate{x in {1..5}; boolean y=false: y = x||y}";
    ASTOCLIterateExpression astex = p.parse_StringOCLIterateExpression(s).get();
    createSTFromAST(astex, false);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A0200 The resulting type of the BooleanOrOpExpression (||) cannot be calculated", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void deriveFromOCLTypeCastExpression0() throws IOException {
    String s = "(int) 4";
    ASTOCLTypeCastExpression astex = p.parse_StringOCLTypeCastExpression(s).get();
    createSTFromAST(astex);
    assertEquals("int", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLTypeCastExpression1() throws IOException {
    String s = "(double) 4";
    ASTOCLTypeCastExpression astex = p.parse_StringOCLTypeCastExpression(s).get();
    createSTFromAST(astex);
    assertEquals("double", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Ignore
  @Test
  public void deriveFromOCLTypeCastExpression2() throws IOException {
    // TODO: should this work?
    String s = "(int) 4.4";
    ASTOCLTypeCastExpression astex = p.parse_StringOCLTypeCastExpression(s).get();
    createSTFromAST(astex);
    assertEquals("int", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLTypeCastExpression3() throws IOException {
    String s = "(Person) p1";
    ASTOCLTypeCastExpression astex = p.parse_StringOCLTypeCastExpression(s).get();
    createSTFromAST(astex);
    assertEquals("Person", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLTypeCastExpression4() throws IOException {
    String s = "(double) 4 * 1.3";
    ASTOCLTypeCastExpression astex = p.parse_StringOCLTypeCastExpression(s).get();
    createSTFromAST(astex);
    assertEquals("double", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void testInvalidOCLTypeCastExpression0() throws IOException {
    String s = "(not_present) 52";
    ASTOCLTypeCastExpression astex = p.parse_StringOCLTypeCastExpression(s).get();
    createSTFromAST(astex, false);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A3082 The type of the expression of the OCLTypeCastExpression can't be cast to given type", Log.getFindings().get(0).getMsg());
      Log.getFindings().clear();
    }
  }

  @Test
  public void testInvalidOCLTypeCastExpression1() throws IOException {
    String s = "(int) not_present";
    ASTOCLTypeCastExpression astex = p.parse_StringOCLTypeCastExpression(s).get();
    createSTFromAST(astex, false);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A3080 The type of the expression of the OCLTypeCastExpression could not be calculated", Log.getFindings().get(0).getMsg());
      Log.getFindings().clear();
    }
  }

  @Test
  public void testInvalidOCLTypeCastExpression2() throws IOException {
    String s = "(string) 4";
    ASTOCLTypeCastExpression astex = p.parse_StringOCLTypeCastExpression(s).get();
    createSTFromAST(astex, false);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A3082 The type of the expression of the OCLTypeCastExpression can't be cast to given type", Log.getFindings().get(0).getMsg());
      Log.getFindings().clear();
    }
  }

  @Test
  public void deriveFromOCLParenthizedExpression0() throws IOException {
    String s = "(5)";
    ASTOCLParenthizedExpression astex = p.parse_StringOCLParenthizedExpression(s).get();
    createSTFromAST(astex);
    assertEquals("int", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLParenthizedExpression1() throws IOException {
    String s = "(5==5)";
    ASTOCLParenthizedExpression astex = p.parse_StringOCLParenthizedExpression(s).get();
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLParenthizedExpression2() throws IOException {
    String s = "((5==5))";
    ASTOCLParenthizedExpression astex = p.parse_StringOCLParenthizedExpression(s).get();
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLParenthizedExpression3() throws IOException {
    String s = "((1+2.2)*3)";
    ASTOCLParenthizedExpression astex = p.parse_StringOCLParenthizedExpression(s).get();
    createSTFromAST(astex);
    assertEquals("double", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLParenthizedExpression4() throws IOException {
    String s = "((1<(1+1)) && (1+(2+(3+(4+5)))<20))";
    ASTOCLParenthizedExpression astex = p.parse_StringOCLParenthizedExpression(s).get();
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLParenthizedExpression5() throws IOException {
    String s = "(typeif p1 instanceof Person then p1 else any Person)";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLParenthizedExpression);
    createSTFromAST(astex);
    assertEquals("Person", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLParenthizedExpression6() throws IOException {
    String s = "(p1).getName()";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTCallExpression);
    createSTFromAST(astex);
    assertEquals("String", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLParenthizedExpression7() throws IOException {
    String s = "(any Person).getName()";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTCallExpression);
    createSTFromAST(astex);
    assertEquals("String", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLParenthizedExpression8() throws IOException {
    String s = "(typeif p1 instanceof int then p1 else 5)";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLParenthizedExpression);
    createSTFromAST(astex);
    assertEquals("int", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLParenthizedExpression9() throws IOException {
    String s = "(typeif p1 instanceof Person then p1 else any Person).getName()";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTCallExpression);
    createSTFromAST(astex);
    assertEquals("String", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLInExpression0() throws IOException {
    String s = "x in List{1,2,3}";
    ASTOCLInExpression astex = p.parse_StringOCLInExpression(s).get();
    createSTFromAST(astex);
    assertEquals("int", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLInExpression1() throws IOException {
    String s = "x in a.b";
    ASTOCLInExpression astex = p.parse_StringOCLInExpression(s).get();
    createSTFromAST(astex);
    assertEquals("int", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLInExpression2() throws IOException {
    String s = "float x in a.b";
    ASTOCLInExpression astex = p.parse_StringOCLInExpression(s).get();
    createSTFromAST(astex);
    assertEquals("float", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLInExpression3() throws IOException {
    String s = "Student p in Student";
    ASTOCLInExpression astex = p.parse_StringOCLInExpression(s).get();
    createSTFromAST(astex);
    assertEquals("Student", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLInExpression4() throws IOException {
    String s = "int y, z in a.b";
    ASTOCLInExpression astex = p.parse_StringOCLInExpression(s).get();
    createSTFromAST(astex);
    assertEquals("int", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLInExpression5() throws IOException {
    String s = "Person p in Student.allInstances";
    ASTOCLInExpression astex = p.parse_StringOCLInExpression(s).get();
    createSTFromAST(astex);
    assertEquals("Person", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLInExpression6() throws IOException {
    String s = "x in {List{1,2,3}, {7..11}}";
    ASTOCLInExpression astex = p.parse_StringOCLInExpression(s).get();
    createSTFromAST(astex);
    assertEquals("List<int>", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void testInvalidOCLInExpression() throws IOException {
    String s;
    ASTOCLInExpression astex;

    // TODO SVa: forbid in CoCos
    s = "x in 2";
    astex = p.parse_StringOCLInExpression(s).get();
    createSTFromAST(astex, false);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A30A1 The expression of the OCLInExpr can be no primitive type", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void deriveFromOCLIsNewPrimary() throws IOException {
    String s = "isnew(p1)";
    ASTOCLIsNewPrimary astex = p.parse_StringOCLIsNewPrimary(s).get();
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Ignore
  @Test
  public void testInvalidOCLInExprOCLIsNewPrimary() throws IOException {
    //todo: was wäre hier Typsemantisch falsch? isnew(1) zB sollte ja schon woanders abgefangen werden
  }

  @Test
  public void deriveFromOCLDefinedPrimary0() throws IOException {
    String s = "defined(x)";
    ASTOCLDefinedPrimary astex = p.parse_StringOCLDefinedPrimary(s).get();
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLDefinedPrimary1() throws IOException {
    String s = "defined(y)";
    ASTOCLDefinedPrimary astex = p.parse_StringOCLDefinedPrimary(s).get();
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLDefinedPrimary2() throws IOException {
    String s = "defined(p1)";
    ASTOCLDefinedPrimary astex = p.parse_StringOCLDefinedPrimary(s).get();
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLDefinedPrimary3() throws IOException {
    String s = "defined(5)";
    ASTOCLDefinedPrimary astex = p.parse_StringOCLDefinedPrimary(s).get();
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void testInvalidOCLDefinedPrimary() throws IOException {
    String s = "defined(not_present)";
    ASTOCLDefinedPrimary astex = p.parse_StringOCLDefinedPrimary(s).get();
    createSTFromAST(astex, false);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A3220 The type of the expression of the OCLDefinedPrimary could not be calculated", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void deriveFromOCLQualifiedPrimary0() throws IOException {
    String s = "List{1,2,3,4}[2]";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLQualifiedPrimary);
    createSTFromAST(astex);
    assertEquals("int", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLQualifiedPrimary1() throws IOException {
    String s = "{1..4}[2]";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLQualifiedPrimary);
    createSTFromAST(astex);
    assertEquals("int", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLQualifiedPrimary2() throws IOException {
    String s = "{1..4}[2 + 2]";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLQualifiedPrimary);
    createSTFromAST(astex);
    assertEquals("int", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLQualifiedPrimary3() throws IOException {
    String s = "p1@pre";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLQualifiedPrimary);
    createSTFromAST(astex);
    assertEquals("Student", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLQualifiedPrimary4() throws IOException {
    String s = "p1@pre.getName()";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTCallExpression);
    createSTFromAST(astex);
    assertEquals("String", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLQualifiedPrimary5() throws IOException {
    String s = "a.a**";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLQualifiedPrimary);
    createSTFromAST(astex);
    assertEquals("List<A>", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLQualifiedPrimary6() throws IOException {
    String s = "p1.getName()";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTCallExpression);
    createSTFromAST(astex);
    assertEquals("String", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void testInvalidOCLQualifiedPrimary() throws IOException {
    String s;
    ASTExpression astex;

    s = "a[0]";
    astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLQualifiedPrimary);
    createSTFromAST(astex, false);

    try {
      if (Log.getErrorCount() > 0) {
        throw new RuntimeException();
      }
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A30C0 The type of the OCLArrayQualification needs a list to operate on", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void deriveFromOCLComprehensionPrimary0() throws IOException {
    String s = "List<int>{1}";
    ASTOCLComprehensionPrimary astex = p.parse_StringOCLComprehensionPrimary(s).get();
    createSTFromAST(astex);
    assertEquals("List<int>", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLComprehensionPrimary1() throws IOException {
    String s = "List<int>{1+1}";
    ASTOCLComprehensionPrimary astex = p.parse_StringOCLComprehensionPrimary(s).get();
    createSTFromAST(astex);
    assertEquals("List<int>", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLComprehensionPrimary2() throws IOException {
    String s = "List{1+1}";
    ASTOCLComprehensionPrimary astex = p.parse_StringOCLComprehensionPrimary(s).get();
    createSTFromAST(astex);
    assertEquals("List<int>", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLComprehensionPrimary3() throws IOException {
    String s = "List<int>{1..5}";
    ASTOCLComprehensionPrimary astex = p.parse_StringOCLComprehensionPrimary(s).get();
    createSTFromAST(astex);
    assertEquals("List<int>", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLComprehensionPrimary4() throws IOException {
    String s = "List{1..5}";
    ASTOCLComprehensionPrimary astex = p.parse_StringOCLComprehensionPrimary(s).get();
    createSTFromAST(astex);
    assertEquals("List<int>", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLComprehensionPrimary5() throws IOException {
    String s = "Collection{1..5}";
    ASTOCLComprehensionPrimary astex = p.parse_StringOCLComprehensionPrimary(s).get();
    createSTFromAST(astex);
    assertEquals("Collection<int>", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLComprehensionPrimary6() throws IOException {
    String s = "Set{x | x in 1..5}";
    ASTOCLComprehensionPrimary astex = p.parse_StringOCLComprehensionPrimary(s).get();
    createSTFromAST(astex);
    assertEquals("Set<int>", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLComprehensionPrimary7() throws IOException {
    String s = "List{x * x | x in 1..5}";
    ASTOCLComprehensionPrimary astex = p.parse_StringOCLComprehensionPrimary(s).get();
    createSTFromAST(astex);
    assertEquals("List<int>", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLComprehensionPrimary8() throws IOException {
    String s = "List{1..2, 4..6}";
    ASTOCLComprehensionPrimary astex = p.parse_StringOCLComprehensionPrimary(s).get();
    createSTFromAST(astex);
    assertEquals("List<int>", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLComprehensionPrimary9() throws IOException {
    String s = "List{1..2, 5, 7..9}";
    ASTOCLComprehensionPrimary astex = p.parse_StringOCLComprehensionPrimary(s).get();
    assertEquals("List<int>", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLComprehensionPrimary10() throws IOException {
    String s = "List{Set{x * x | x in y}}";
    ASTOCLComprehensionPrimary astex = p.parse_StringOCLComprehensionPrimary(s).get();
    createSTFromAST(astex);
    assertEquals("List<Set<int>>", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLComprehensionPrimary11() throws IOException {
    String s = "{List{1,2,3}, {7..11}}";
    ASTOCLComprehensionPrimary astex = p.parse_StringOCLComprehensionPrimary(s).get();
    createSTFromAST(astex);
    assertEquals("List<List<int>>", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLComprehensionPrimary12() throws IOException {
    String s = "{u | u in y}";
    ASTOCLComprehensionPrimary astex = p.parse_StringOCLComprehensionPrimary(s).get();
    createSTFromAST(astex);
    assertEquals("int", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void testInvalidOCLComprehensionPrimary0() throws IOException {
    String s = "not_present{1..5}";
    ASTOCLComprehensionPrimary astex = p.parse_StringOCLComprehensionPrimary(s).get();
    createSTFromAST(astex, false);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A3101 The type of the OCLExtType of the OCLComprehensionPrimary should be a collection type", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void testInvalidOCLComprehensionPrimary1() throws IOException {
    String s = "List{1..2, p1, 4..6}";
    ASTOCLComprehensionPrimary astex = p.parse_StringOCLComprehensionPrimary(s).get();
    createSTFromAST(astex, false);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A3131 The type of the Expression of the OCLComprehensionEnumerationStyle was Student but should be of any relation to int", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void deriveFromOCLComprehensionExpressionStyle0() throws IOException {
    String s = "1.5 + 2.8 | x in {1..5}";
    ASTOCLComprehensionExpressionStyle astex = p.parse_StringOCLComprehensionExpressionStyle(s).get();
    createSTFromAST(astex);
    assertEquals("double", typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLComprehensionExpressionStyle1() throws IOException {
    String s = "x * x | x in {1..5}";
    ASTOCLComprehensionExpressionStyle astex = p.parse_StringOCLComprehensionExpressionStyle(s).get();
    createSTFromAST(astex);
    assertEquals("int", typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLComprehensionExpressionStyle2() throws IOException {
    String s = "z * z | z in 1..5";
    ASTOCLComprehensionExpressionStyle astex = p.parse_StringOCLComprehensionExpressionStyle(s).get();
    createSTFromAST(astex);
    assertEquals("int", typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLComprehensionExpressionStyle3() throws IOException {
    String s = "x * x | x in {1..5}, x != 4";
    ASTOCLComprehensionExpressionStyle astex = p.parse_StringOCLComprehensionExpressionStyle(s).get();
    createSTFromAST(astex);
    assertEquals("int", typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLComprehensionExpressionStyle4() throws IOException {
    String s = "z * z | z in y";
    ASTOCLComprehensionExpressionStyle astex = p.parse_StringOCLComprehensionExpressionStyle(s).get();
    createSTFromAST(astex);
    assertEquals("int", typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLComprehensionExpressionStyle5() throws IOException {
    String s = "a.b[0] * b.b[0] | a in A, b in B";
    ASTOCLComprehensionExpressionStyle astex = p.parse_StringOCLComprehensionExpressionStyle(s).get();
    createSTFromAST(astex);
    assertEquals("int", typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void testInvalidOCLComprehensionExpressionStyle() throws IOException {
    String s = "z * z | x in 1";
    ASTOCLComprehensionExpressionStyle astex = p.parse_StringOCLComprehensionExpressionStyle(s).get();
    createSTFromAST(astex, false);
    try {
      typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A30A1 The expression of the OCLInExpr can be no primitive type", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void deriveFromOCLComprehensionEnumerationStyle0() throws IOException {
    String s = "1..5";
    ASTOCLComprehensionEnumerationStyle astex = p.parse_StringOCLComprehensionEnumerationStyle(s).get();
    createSTFromAST(astex);
    assertEquals("List<int>", typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLComprehensionEnumerationStyle1() throws IOException {
    String s = "1..2, 3, 4..6";
    ASTOCLComprehensionEnumerationStyle astex = p.parse_StringOCLComprehensionEnumerationStyle(s).get();
    createSTFromAST(astex);
    assertEquals("List<int>", typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLComprehensionEnumerationStyle2() throws IOException {
    String s = "1..2, 3.5f, 4..6";
    ASTOCLComprehensionEnumerationStyle astex = p.parse_StringOCLComprehensionEnumerationStyle(s).get();
    createSTFromAST(astex);
    assertEquals("List<float>", typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLComprehensionEnumerationStyle3() throws IOException {
    String s = "5..1";
    ASTOCLComprehensionEnumerationStyle astex = p.parse_StringOCLComprehensionEnumerationStyle(s).get();
    createSTFromAST(astex);
    assertEquals("List<int>", typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void testInvalidOCLComprehensionEnumerationStyle() throws IOException {
    String s;
    ASTOCLComprehensionEnumerationStyle astex;

    s = "1..5, true";
    astex = p.parse_StringOCLComprehensionEnumerationStyle(s).get();
    createSTFromAST(astex, false);
    try {
      typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A3131 The type of the Expression of the OCLComprehensionEnumerationStyle was boolean but should be of any relation to int", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void deriveFromOCLCollectionItem0() throws IOException {
    String s = "1..5";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLCollectionItem);
    createSTFromAST(astex);
    assertEquals("List<int>", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLCollectionItem1() throws IOException {
    String s = "1..5+8";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLCollectionItem);
    createSTFromAST(astex);
    assertEquals("List<int>", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void deriveFromOCLCollectionItem2() throws IOException {
    String s = "1..(7..100)[3]";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLCollectionItem);
    createSTFromAST(astex);
    assertEquals("List<int>", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void testInvalidOCLCollectionItem0() throws IOException {
    String s = "p1..5";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLCollectionItem);
    createSTFromAST(astex, false);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A3142 The type of the left expression of the OCLCollectionItem has to be an integral", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void testInvalidOCLCollectionItem1() throws IOException {
    String s = "1..p1";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLCollectionItem);
    createSTFromAST(astex, false);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A3143 The type of the right expression of the OCLCollectionItem has to be an integral", Log.getFindings().get(0).getMsg());
    }
  }

  @Test
  public void testInvalidOCLCollectionItem2() throws IOException {
    String s = "p1..(any Person)";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLCollectionItem);
    createSTFromAST(astex, false);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A3142 The type of the left expression of the OCLCollectionItem has to be an integral", Log.getFindings().get(0).getMsg());
      Log.getFindings().clear();
    }
  }

  @Test
  public void deriveFromOCLEquivalentExpression() throws IOException {
    String s = "x == 5 <=> true";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLEquivalentExpression);
    createSTFromAST(astex);
    assertEquals("boolean", tc.typeOf(astex).print());
    printFindingsAndFail();
  }

  @Test
  public void testInvalidOCLEquivalentExpression() throws IOException {
    String s = "x <=> true";
    ASTExpression astex = p.parse_StringExpression(s).get();
    assertTrue(astex instanceof ASTOCLEquivalentExpression);
    createSTFromAST(astex, false);
    try {
      tc.typeOf(astex);
      fail();
    }
    catch (RuntimeException e) {
      assertEquals("0x" + "A3150 The type of the left expression of the OCLEquivalentExpression has to be boolean", Log.getFindings().get(0).getMsg());
    }
  }

  private TestOCLExpressionsScope createSTFromAST(ASTExpression astex) {
    return createSTFromAST(astex, true);
  }

  private TestOCLExpressionsScope createSTFromAST(ASTExpression astex, boolean failOnError) {
    symbolTableCreator.setTypeVisitor(derLit);

    final IExpressionsBasisScope scope = symbolTableCreator.getGlobalScope();
    createBasicTypes(scope);
    createPersonAndStudent(scope);
    createComplexType(scope);

    final TestOCLExpressionsArtifactScope fromAST = symbolTableCreator.createFromAST(astex);

    derLit.setScope(fromAST.getEnclosingScope());

    if (failOnError) {
      printFindingsAndFail();
    }

    return fromAST;
  }

  private TestOCLExpressionsScope createSTFromAST(ASTOCLComprehensionExpression astex) {
    return createSTFromAST(astex, true);
  }

  private TestOCLExpressionsScope createSTFromAST(ASTOCLComprehensionExpression astex, boolean failOnError) {
    return createSTFromAST(OCLExpressionsMill.oCLComprehensionPrimaryBuilder().setExpression(astex).build(), failOnError);
  }

  private TestOCLExpressionsScope createSTFromAST(ASTOCLDeclaration astex) {
    return createSTFromAST(astex, true);
  }

  private TestOCLExpressionsScope createSTFromAST(ASTOCLDeclaration astex, boolean failOnError) {
    return createSTFromAST(OCLExpressionsMill.oCLLetinExpressionBuilder().setOCLDeclarationList(Collections.singletonList(astex))
        .setExpression(ExpressionsBasisMillForOCLExpressions.literalExpressionBuilder()
            .setLiteral(MCCommonLiteralsMill.stringLiteralBuilder().setSource("literal").build()).build()).build(), failOnError);
  }

}
