/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.ocl.prettyprint;

import de.monticore.ocl.ocl._ast.ASTCompilationUnit;
import de.monticore.ocl.ocl._visitor.OCLVisitor;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;
import de.se_rwth.commons.Names;

public class OCLPrettyPrinter implements OCLVisitor {

  protected OCLVisitor realThis;
  protected IndentPrinter printer;

  public OCLPrettyPrinter(IndentPrinter printer) {
    this.printer = printer;
    realThis = this;
  }

  @Override
  public OCLVisitor getRealThis() {
    return realThis;
  }

  public IndentPrinter getPrinter() {
    return printer;
  }

  @Override
  public void handle(ASTCompilationUnit unit) {
    if (unit.getPackageList() != null && !unit.getPackageList().isEmpty()) {
      printer
          .println("package " + Names.getQualifiedName(unit.getPackageList()) + ";\n");
    }
    if (unit.getMCImportStatementList() != null && !unit.getMCImportStatementList().isEmpty()) {
      for (ASTMCImportStatement s : unit.getMCImportStatementList()) {
        getPrinter().print("import " + Names.getQualifiedName(s.getMCQualifiedName().getPartList()));
        if (s.isStar()) {
          getPrinter().println(".*;");
        }
        else {
          getPrinter().println(";");
        }
      }
      getPrinter().println();
    }
    unit.getOCLFile().accept(getRealThis());
  }
}
