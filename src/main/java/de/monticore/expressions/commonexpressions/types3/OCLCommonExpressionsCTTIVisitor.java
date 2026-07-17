package de.monticore.expressions.commonexpressions.types3;

import de.monticore.expressions.commonexpressions._ast.ASTArrayAccessExpression;
import de.monticore.expressions.commonexpressions._ast.ASTFieldAccessExpression;
import de.monticore.ocl.types3.OCLCollectionSymTypeRelations;
import de.monticore.symboltable.modifiers.AccessModifier;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types3.SymTypeRelations;
import de.monticore.types3.util.TypeContextCalculator;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;

import static de.monticore.types.check.SymTypeExpressionFactory.createObscureType;
import static de.monticore.types.mccollectiontypes.types3.util.MCCollectionSymTypeFactory.createSet;

public class OCLCommonExpressionsCTTIVisitor extends CommonExpressionsCTTIVisitor {

  @Override
  protected Optional<SymTypeExpression> calculateTypeIdFieldAccess(
      ASTFieldAccessExpression expr,
      boolean resultsAreOptional
  ) {
    Optional<SymTypeExpression> type =
        super.calculateTypeIdFieldAccess(expr, resultsAreOptional);
    if (type.isEmpty() && !resultsAreOptional) {
      // todo technically incorrect,
      //  as this does not store the inner type as the set it is,
      //  but doing it here would not be proper either.
      //  This requires a OCL specific rewrite for FieldAccessExpressions.
      SymTypeExpression innerAsTypeIdType =
          getType4Ast().getPartialTypeOfTypeIdForName(expr.getExpression());
      SymTypeOfGenerics innerAsSetOfInstances = createSet(innerAsTypeIdType);
      // hacky, will break in some cases:
      innerAsSetOfInstances.getSourceInfo().setSourceSymbol(innerAsTypeIdType.getTypeInfo());
      AccessModifier accessModifier = TypeContextCalculator.getAccessModifier(
          innerAsSetOfInstances.getTypeInfo(), expr.getEnclosingScope()
      );
      type = resolveVariablesAndFunctionsWithinType(
          innerAsSetOfInstances,
          expr.getName(),
          accessModifier,
          v -> true,
          f -> true
      );
      if (type.isPresent()) {
        getType4Ast().internal_setTypeOfTypeIdentifier2(
            expr.getExpression(), null
        );
        getType4Ast().setTypeOfExpression(
            expr.getExpression(), innerAsSetOfInstances
        );
      }
    }
    return type;
  }

  @Override
  protected SymTypeExpression calculateArrayAccess(
      ASTArrayAccessExpression expr, SymTypeExpression toBeAccessed, SymTypeExpression indexType) {
    SymTypeExpression result;
    if (toBeAccessed.isArrayType()) {
      result = super.calculateArrayAccess(expr, toBeAccessed, indexType);
    }
    // add special access for collection types
    else if (!OCLCollectionSymTypeRelations.isOCLCollection(toBeAccessed)
        && !OCLCollectionSymTypeRelations.isOptional(toBeAccessed)
        && !OCLCollectionSymTypeRelations.isMap(toBeAccessed)) {
      Log.error(
          "0xFD3D6 trying a qualified access on "
              + toBeAccessed.printFullName()
              + " which is not a type "
              + "applicable to qualified accesses",
          expr.get_SourcePositionStart(),
          expr.get_SourcePositionEnd());
      result = createObscureType();
    }
    // case qualified access based on order: List
    else if (SymTypeRelations.isIntegralType(indexType)
        && OCLCollectionSymTypeRelations.isList(toBeAccessed)) {
      result = OCLCollectionSymTypeRelations.getCollectionElementType(toBeAccessed);
    }
    // case qualified access on OCLCollection
    // container.role[qualifier] == {elem.role[qualifier] | elem in container}
    else if (OCLCollectionSymTypeRelations.isOCLCollection(toBeAccessed)
        || OCLCollectionSymTypeRelations.isOptional(toBeAccessed)) {
      SymTypeExpression preResultInnerType =
          calculateArrayAccess(
              expr,
              OCLCollectionSymTypeRelations.getCollectionElementType(toBeAccessed),
              indexType);
      // wrap in same kind of collection
      SymTypeOfGenerics wrappedPreResult = (SymTypeOfGenerics) toBeAccessed.deepClone();
      wrappedPreResult.setArgument(0, preResultInnerType);
      result = wrappedPreResult;
    }
    // case map access
    else if (OCLCollectionSymTypeRelations.isMap(toBeAccessed)) {
      if (SymTypeRelations.isCompatible(
          OCLCollectionSymTypeRelations.getMapKeyType(toBeAccessed), indexType)) {
        result = OCLCollectionSymTypeRelations.getCollectionElementType(toBeAccessed);
      }
      else {
        Log.error(
            "0xFDC85 trying to access a map of type "
                + toBeAccessed.printFullName()
                + " with a key of type "
                + indexType.printFullName()
                + ", which is not applicable",
            expr.get_SourcePositionStart(),
            expr.get_SourcePositionEnd());
        result = createObscureType();
      }
    }
    else {
      Log.error(
          "0xFDC86 trying to access expression of type "
              + toBeAccessed.printFullName()
              + " (collections may have been unwrapped) "
              + "with qualifier of type "
              + indexType.printFullName()
              + " which is not applicable",
          expr.get_SourcePositionStart(),
          expr.get_SourcePositionEnd());
      result = createObscureType();
    }
    return result;
  }
}
