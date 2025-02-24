package de.monticore.expressions.commonexpressions.types3;

import static de.monticore.types.check.SymTypeExpressionFactory.createObscureType;

import de.monticore.expressions.commonexpressions._ast.ASTArrayAccessExpression;
import de.monticore.ocl.types3.OCLCollectionSymTypeRelations;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types3.SymTypeRelations;
import de.se_rwth.commons.logging.Log;

public class OCLCommonExpressionsTypeVisitor extends CommonExpressionsCTTIVisitor {

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
      } else {
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
    } else {
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
