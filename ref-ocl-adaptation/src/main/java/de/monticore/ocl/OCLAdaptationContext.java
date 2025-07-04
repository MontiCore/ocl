package de.monticore.ocl;

import de.monticore.expressions.commonexpressions.CommonExpressionsAdaptationContext;
import de.monticore.refadaptation.IAdaptationContext;
import de.monticore.types.mccollectiontypes.MCCollectionTypesAdaptationContext;

public interface OCLAdaptationContext extends
        /*
        de.monticore.umlstereotype._symboltable.IUMLStereotypeScope,
        de.monticore.types.mcsimplegenerictypes._symboltable.IMCSimpleGenericTypesScope,
        de.monticore.ocl.setexpressions._symboltable.ISetExpressionsScope,
        de.monticore.ocl.oclexpressions._symboltable.IOCLExpressionsScope,
        de.monticore.ocl.optionaloperators._symboltable.IOptionalOperatorsScope,
        de.monticore.expressions.bitexpressions._symboltable.IBitExpressionsScope*/
  // TODO extend from all sub languages
        IAdaptationContext,
        MCCollectionTypesAdaptationContext,
        CommonExpressionsAdaptationContext {

  OCLAdaptationVariant createVariant();
  OCLAdaptationContext fork();
  void addBindings(OCLAdaptationVariant variant);
}
