/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.types3.util;

import static de.monticore.ocl.types3.util.OCLCollectionSymTypeFactory.createOCLCollection;
import static de.monticore.types.check.SymTypeExpressionFactory.createIntersection;
import static de.monticore.types.check.SymTypeExpressionFactory.createTypeArray;
import static de.monticore.types.check.SymTypeExpressionFactory.createUnion;
import static de.monticore.types.mccollectiontypes.types3.util.MCCollectionSymTypeFactory.createList;
import static de.monticore.types.mccollectiontypes.types3.util.MCCollectionSymTypeFactory.createOptional;
import static de.monticore.types.mccollectiontypes.types3.util.MCCollectionSymTypeFactory.createSet;
import static de.monticore.types3.util.DefsTypesForTests._carSymType;
import static de.monticore.types3.util.DefsTypesForTests._childSymType;
import static de.monticore.types3.util.DefsTypesForTests._csStudentSymType;
import static de.monticore.types3.util.DefsTypesForTests._intSymType;
import static de.monticore.types3.util.DefsTypesForTests._personSymType;
import static de.monticore.types3.util.DefsTypesForTests._studentSymType;
import static de.monticore.types3.util.DefsTypesForTests.inScope;
import static de.monticore.types3.util.DefsTypesForTests.type;
import static de.monticore.types3.util.DefsTypesForTests.typeVariable;
import static org.junit.jupiter.api.Assertions.assertEquals;

import de.monticore.expressions.combineexpressionswithliterals._symboltable.ICombineExpressionsWithLiteralsScope;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.types3.OCLSymTypeRelations;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.symbols.basicsymbols._symboltable.IBasicSymbolsGlobalScope;
import de.monticore.symbols.basicsymbols._symboltable.IBasicSymbolsScope;
import de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types3.AbstractTypeTest;
import de.monticore.types3.ISymTypeRelations;
import de.monticore.types3.util.DefsTypesForTests;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class OCLSymTypeLeastUpperBoundTest extends AbstractTypeTest {

  protected ICombineExpressionsWithLiteralsScope scope;

  ISymTypeRelations tr;

  SymTypeOfGenerics _unboxedCollectionSymType;

  SymTypeOfGenerics _boxedCollectionSymType;

  @BeforeEach
  public void setup() {
    OCLMill.reset();
    OCLMill.init();
    DefsTypesForTests.setup();
    setupCollectionType();
    tr = new OCLSymTypeRelations();
  }

  protected void setupCollectionType() {
    IBasicSymbolsGlobalScope gs = BasicSymbolsMill.globalScope();
    TypeVarSymbol uCVar = typeVariable("T");
    _unboxedCollectionSymType =
        SymTypeExpressionFactory.createGenerics(
            inScope(gs, type("Collection", List.of(), List.of(uCVar))),
            SymTypeExpressionFactory.createTypeVariable(uCVar));
    IBasicSymbolsScope javaUtilScope = gs.resolveType("java.util.List").get().getEnclosingScope();
    TypeVarSymbol bCVar = typeVariable("T");
    _boxedCollectionSymType =
        SymTypeExpressionFactory.createGenerics(
            inScope(javaUtilScope, type("Collection", List.of(), List.of(uCVar))),
            SymTypeExpressionFactory.createTypeVariable(bCVar));
  }

  @Test
  public void leastUpperBound() {
    checkLub(_personSymType, "Person");
    checkLub(createUnion(_personSymType, _studentSymType), "Person");
    checkLub(createUnion(_childSymType, _studentSymType), "(Person & Teachable)");
    checkLub(createUnion(_childSymType, _csStudentSymType), "(Person & Teachable)");
    checkLub(createUnion(_childSymType, _carSymType), "Obscure");
    checkLub(
        createIntersection(_personSymType, createUnion(_childSymType, _studentSymType)),
        "(Person & Teachable)");
    checkLub(
        createUnion(createTypeArray(_childSymType, 2), createTypeArray(_csStudentSymType, 2)),
        "(Person[][] & Teachable[][])");
  }

  @Test
  public void OCLCollectionsleastUpperBound() {
    checkLub(createList(_intSymType), "List<int>");
    checkLub(createSet(_intSymType), "Set<int>");
    checkLub(createOptional(_intSymType), "Optional<int>");
    checkLub(createOCLCollection(_intSymType), "Collection<int>");
    // note: this could also be Set<(Person & Teachable)>,
    // there is (theoretically) no distinction (for OCL collection types)
    checkLub(
        createUnion(createSet(_childSymType), createSet(_csStudentSymType)),
        "(Set<Person> & Set<Teachable>)");
  }

  protected void checkLub(SymTypeExpression type, String expectedPrint) {
    Optional<SymTypeExpression> lubOpt = tr.leastUpperBound(type);
    String printed = lubOpt.map(SymTypeExpression::printFullName).orElse("");
    assertNoFindings();
    assertEquals(expectedPrint, printed);
  }
}
