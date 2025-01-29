package de.monticore.ocl.ocl;

import de.monticore.ocl.ocl.types3.OCLTypeCheck3;

public class OCLMill extends OCLMillTOP {

  /** additionally inits the OCL TypeCheck */
  public static void init() {
    OCLMillTOP.init();
    OCLTypeCheck3.init();
  }
}
