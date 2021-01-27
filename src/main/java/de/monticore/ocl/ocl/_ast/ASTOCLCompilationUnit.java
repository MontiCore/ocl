// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._ast;

public class ASTOCLCompilationUnit extends ASTOCLCompilationUnitTOP {
  public String getPackage() {
    return String.join(".", this.getPackageList());
  }

  public boolean isPresentPackage() {
    return !getPackageList().isEmpty();
  }
}
