package de.monticore.ocl.codegen.javagen;

import de.monticore.codegen.CodeGenOperationPrinter;
import de.monticore.codegen.javagen.operationprinter.JavaAssignmentOperationHandler;
import de.monticore.codegen.javagen.operationprinter.JavaNumericOperationHandler;
import de.monticore.codegen.javagen.operationprinter.JavaStringConcatenationOperationHandler;
import de.se_rwth.commons.logging.Log;

import java.util.List;

public class OCL2JavaOperationPrinter extends CodeGenOperationPrinter {

  protected OCL2JavaOperationPrinter() {
    setOperatorHandlers(List.of(
        new OCL2JavaEqualityOperationHandler(),
        new JavaAssignmentOperationHandler(),
        new JavaNumericOperationHandler(),
        new JavaStringConcatenationOperationHandler()
    ));
  }

  // static delegate
  public static void init() {
    Log.trace("init JavaOperationPrinter", "CodeGen setup");
    OCL2JavaOperationPrinter converter = new OCL2JavaOperationPrinter();
    CodeGenOperationPrinter.setDelegate(converter);
  }
}
