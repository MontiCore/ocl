package de.monticore.types.mccollectiontypes;

import de.monticore.refadaptation.IAdaptationContext;
import de.monticore.types.mcbasictypes.MCBasicTypesAdaptationContext;

public interface MCCollectionTypesAdaptationContext extends IAdaptationContext, MCBasicTypesAdaptationContext {

  MCCollectionTypesAdaptationVariant createVariant();
  MCCollectionTypesAdaptationContext fork();
  void addBindings(MCCollectionTypesAdaptationVariant variant);
}
