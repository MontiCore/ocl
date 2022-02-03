/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen.visitors;

import de.monticore.ocl.codegen.util.VariableNaming;

public abstract class AbstractPrinter {

  protected StringBuilder stringBuilder;

  protected StringBuilder getStringBuilder() {
    return this.stringBuilder;
  }

  protected VariableNaming naming;

  protected VariableNaming getNaming() {
    return this.naming;
  }
}
