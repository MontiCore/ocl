package de.monticore.ocl.oclexpressions;

import de.monticore.ocl.oclexpressions.types3.OCLExpressionsTypeVisitor;
import de.monticore.ocl.types3.OCLCollectionSymTypeRelations;
import de.monticore.types.check.SymTypeExpression;

public class OCLOCLExpressionsTypeVisitor extends OCLExpressionsTypeVisitor {

  @Override
  protected boolean isSetOrList(SymTypeExpression type) {
    // Adds support for 'Collection' as a collection type.
    // This is required when association roles are chained.
    // see OCLWithinTypeBasicSymbolsResolver
    return OCLCollectionSymTypeRelations.isOCLCollection(type);
  }
}
