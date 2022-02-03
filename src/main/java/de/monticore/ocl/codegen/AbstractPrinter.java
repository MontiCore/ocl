/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen;

public abstract class AbstractPrinter {

  protected StringBuilder stringBuilder;

  protected StringBuilder getStringBuilder() {
    return this.stringBuilder;
  }

  protected OCLVariableNaming naming;

  protected OCLVariableNaming getNaming() {
    return this.naming;
  }
}
