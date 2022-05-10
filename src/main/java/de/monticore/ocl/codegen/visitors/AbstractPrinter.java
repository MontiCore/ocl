/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen.visitors;

import de.monticore.ocl.codegen.util.VariableNaming;
import de.monticore.ocl.types.check.OCLTypeCalculator;

public abstract class AbstractPrinter {

  protected static final String NO_TYPE_DERIVED_ERROR =
      "0xAB846 Could not calculate type of expression";

  protected static final String INNER_TYPE_NOT_DERIVED_ERROR =
      "0xFC921 could not derive inner type (container expected)";

  protected VariableNaming naming;

  protected VariableNaming getNaming() {
    return this.naming;
  }

  protected OCLTypeCalculator typeCalculator;

  protected OCLTypeCalculator getTypeCalculator() {
    return this.typeCalculator;
  }

}
