/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen.visitors;

import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.ocl.types.check.OCLTypeCalculator;

public abstract class AbstractPrinter {

  protected static String NO_TYPE_DERIVED_ERROR = "0xAB846 Could not calculate type of expression";

  protected VariableNaming naming;

  protected VariableNaming getNaming() {
    return this.naming;
  }

  protected OCLTypeCalculator typeCalculator;

  protected OCLTypeCalculator getTypeCalculator() {
    return this.typeCalculator;
  }

}
