// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3.util;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.symbols.basicsymbols._symboltable.IBasicSymbolsGlobalScope;
import de.monticore.symbols.basicsymbols._symboltable.IBasicSymbolsScope;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types3.AbstractTypeTest;
import de.monticore.types3.util.DefsTypesForTests;
import de.se_rwth.commons.logging.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static de.monticore.types.mccollectiontypes.types3.util.MCCollectionSymTypeFactory.createList;
import static de.monticore.types.mccollectiontypes.types3.util.MCCollectionSymTypeFactory.createMap;
import static de.monticore.types.mccollectiontypes.types3.util.MCCollectionSymTypeFactory.createOptional;
import static de.monticore.types.mccollectiontypes.types3.util.MCCollectionSymTypeFactory.createSet;
import static de.monticore.types3.util.DefsTypesForTests._boxedListSymType;
import static de.monticore.types3.util.DefsTypesForTests._boxedMapSymType;
import static de.monticore.types3.util.DefsTypesForTests._boxedOptionalSymType;
import static de.monticore.types3.util.DefsTypesForTests._boxedSetSymType;
import static de.monticore.types3.util.DefsTypesForTests._intSymType;
import static de.monticore.types3.util.DefsTypesForTests._personSymType;
import static de.monticore.types3.util.DefsTypesForTests._unboxedListSymType;
import static de.monticore.types3.util.DefsTypesForTests._unboxedMapSymType;
import static de.monticore.types3.util.DefsTypesForTests._unboxedOptionalSymType;
import static de.monticore.types3.util.DefsTypesForTests._unboxedSetSymType;
import static de.monticore.types3.util.DefsTypesForTests._unboxedString;
import static de.monticore.types3.util.DefsTypesForTests.inScope;
import static de.monticore.types3.util.DefsTypesForTests.type;
import static de.monticore.types3.util.DefsTypesForTests.typeVariable;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OCLCollectionTypeRelationsTest extends AbstractTypeTest {

  protected SymTypeOfGenerics _unboxedCollectionSymType;

  protected SymTypeOfGenerics _boxedCollectionSymType;

  @BeforeEach
  public void init() {
    OCLMill.reset();
    OCLMill.init();
    BasicSymbolsMill.initializePrimitives();
    DefsTypesForTests.setup();
    setupCollectionType();
  }

  protected void setupCollectionType() {
    IBasicSymbolsGlobalScope gs = BasicSymbolsMill.globalScope();
    TypeVarSymbol uCVar = typeVariable("T");
    _unboxedCollectionSymType = SymTypeExpressionFactory.createGenerics(
        inScope(gs, type("Collection", List.of(), List.of(uCVar))),
        SymTypeExpressionFactory.createTypeVariable(uCVar)
    );
    IBasicSymbolsScope javaUtilScope =
        gs.resolveType("java.util.List").get().getEnclosingScope();
    TypeVarSymbol bCVar = typeVariable("T");
    _boxedCollectionSymType = SymTypeExpressionFactory.createGenerics(
        inScope(javaUtilScope, type("Collection", List.of(), List.of(uCVar))),
        SymTypeExpressionFactory.createTypeVariable(bCVar)
    );
  }

  @Test
  public void recognizeUnboxedOCLCollectionTypes() {
    assertTrue(getRel().isOCLCollection(_unboxedCollectionSymType));
    assertTrue(getRel().isOCLCollection(_unboxedListSymType));
    assertTrue(getRel().isOCLCollection(_unboxedSetSymType));

  }

  @Test
  public void recognizeBoxedCollectionTypes() {
    assertTrue(getRel().isOCLCollection(_boxedCollectionSymType));
    assertTrue(getRel().isOCLCollection(_boxedListSymType));
    assertTrue(getRel().isOCLCollection(_boxedSetSymType));
  }

  @Test
  public void recognizeNonCollectionTypes() {
    assertFalse(getRel().isOCLCollection(_unboxedOptionalSymType));
    assertFalse(getRel().isOCLCollection(_unboxedMapSymType));
    assertFalse(getRel().isOCLCollection(_boxedOptionalSymType));
    assertFalse(getRel().isOCLCollection(_boxedMapSymType));
    assertFalse(getRel().isCollection(_intSymType));
    assertFalse(getRel().isCollection(_personSymType));
    assertFalse(getRel().isCollection(_unboxedString));
    assertFalse(getRel().isCollection(SymTypeExpressionFactory.createGenerics(
        "noList", BasicSymbolsMill.scope(), _intSymType
    )));

    // incorrect number of arguments
    _unboxedCollectionSymType.setArgumentList(Collections.emptyList());
    _boxedCollectionSymType.setArgumentList(Collections.emptyList());
    assertFalse(getRel().isList(_unboxedCollectionSymType));
    assertFalse(getRel().isList(_boxedCollectionSymType));
    _unboxedCollectionSymType.setArgumentList(List.of(_intSymType, _intSymType));
    _boxedCollectionSymType.setArgumentList(List.of(_intSymType, _intSymType));
    assertFalse(getRel().isList(_unboxedCollectionSymType));
    assertFalse(getRel().isList(_boxedCollectionSymType));
  }

  @Test
  public void getCollectionElementTypeTest() {
    assertSame(_unboxedCollectionSymType.getArgument(0),
        getRel().getCollectionElementType(_unboxedCollectionSymType));
    assertSame(_boxedCollectionSymType.getArgument(0),
        getRel().getCollectionElementType(_boxedCollectionSymType));
  }

  @Test
  public void flattenTest() {
    flattenTestUsingDefinition(_intSymType);
    // other Collections
    flattenTestUsingDefinition(createOptional(_intSymType));
    flattenTestUsingDefinition(createMap(_intSymType, _intSymType));
    // test that we don't break dependent on the structure
    flattenTestUsingDefinition(createCollection(_intSymType));
    flattenTestUsingDefinition(createList(_intSymType));
    flattenTestUsingDefinition(createSet(_intSymType));
    flattenTestUsingDefinition(createSet(_intSymType));
    flattenTestUsingDefinition(createSet(createSet(_intSymType)));
    flattenTestUsingDefinition(createSet(createList(_intSymType)));
    flattenTestUsingDefinition(createSet(createCollection(_intSymType)));
    flattenTestUsingDefinition(createList(createSet(_intSymType)));
    flattenTestUsingDefinition(createList(createList(_intSymType)));
    flattenTestUsingDefinition(createList(createCollection(_intSymType)));
    flattenTestUsingDefinition(createCollection(createSet(_intSymType)));
    flattenTestUsingDefinition(createCollection(createList(_intSymType)));
    flattenTestUsingDefinition(createCollection(createCollection(_intSymType)));
  }

  protected void flattenTestUsingDefinition(SymTypeExpression innerType) {
    // Tests according to Modellierung mit UML 3.3.6
    assertTrue(createSet(innerType)
        .deepEquals(getRel().flatten(createSet(createSet(innerType)))));
    assertTrue(createList(innerType)
        .deepEquals(getRel().flatten(createSet(createList(innerType)))));
    assertTrue(createCollection(innerType)
        .deepEquals(getRel().flatten(createSet(createCollection(innerType)))));
    assertTrue(createList(innerType)
        .deepEquals(getRel().flatten(createList(createSet(innerType)))));
    assertTrue(createList(innerType)
        .deepEquals(getRel().flatten(createList(createList(innerType)))));
    assertTrue(createList(innerType)
        .deepEquals(getRel().flatten(createList(createCollection(innerType)))));
    assertTrue(createCollection(innerType)
        .deepEquals(getRel().flatten(createCollection(createSet(innerType)))));
    assertTrue(createList(innerType)
        .deepEquals(getRel().flatten(createCollection(createList(innerType)))));
    assertTrue(createCollection(innerType)
        .deepEquals(getRel().flatten(createCollection(createCollection(innerType)))));
  }

  @Test
  public void flattenIdTest() {
    // do not change other types
    testFlattenAsId(_intSymType);
    testFlattenAsId(createCollection(_intSymType));
    testFlattenAsId(createList(_intSymType));
    testFlattenAsId(createSet(_intSymType));
    testFlattenAsId(createOptional(_intSymType));
    testFlattenAsId(createMap(_intSymType, _intSymType));
    // "flatable" type within other type
    testFlattenAsId(createOptional(createList(createList(_intSymType))));
  }

  protected void testFlattenAsId(SymTypeExpression toNotFlatten) {
    assertTrue(toNotFlatten.deepEquals(getRel().flatten(toNotFlatten)));
  }

  // Helper

  protected OCLCollectionTypeRelations getRel() {
    return new OCLCollectionTypeRelations();
  }

  protected SymTypeOfGenerics createCollection(SymTypeExpression elementType) {
    IBasicSymbolsGlobalScope gs = BasicSymbolsMill.globalScope();
    Optional<TypeSymbol> typeSymbolOpt = gs.resolveType("Collection")
        .or(() -> gs.resolveType("java.util.Collection"));
    if (typeSymbolOpt.isPresent()) {
      return SymTypeExpressionFactory.createGenerics(
          typeSymbolOpt.get(), elementType);
    }
    else {
      Log.error("0xFD327 could not find symbol \"Collection\" "
          + "to create type Collection<" + elementType.printFullName() + ">");
      return null;
    }
  }

}
