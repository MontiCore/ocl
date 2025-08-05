package de.monticore.expressions.setexpressions.types3;

import de.monticore.ocl.setexpressions.types3.SetExpressionsCTTIVisitor;
import de.monticore.ocl.types3.OCLCollectionSymTypeRelations;
import de.monticore.types.check.SymTypeExpression;

public class OCLSetExpressionsTypeVisitor extends SetExpressionsCTTIVisitor {

  @Override
  protected boolean isSetOrListCollection(SymTypeExpression type) {
    // Adds support for 'Collection' as a collection type.
    // This is required when association roles are chained.
    // see OCLWithinTypeBasicSymbolsResolver
    return OCLCollectionSymTypeRelations.isOCLCollection(type);
  }
}
