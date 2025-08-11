package de.monticore.types.mccollectiontypes;

import de.monticore.refadaptation.IAdaptationContext;
import de.monticore.types.mcbasictypes.IMCBasicTypesAdaptationContext;

public interface IMCCollectionTypesAdaptationContext extends IAdaptationContext, IMCBasicTypesAdaptationContext {

  IMCCollectionTypesAdaptationVariant createVariant();
  IMCCollectionTypesAdaptationContext fork();
}
