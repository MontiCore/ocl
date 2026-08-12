package de.monticore.ocl.codegen.javagen;

import de.monticore.codegen.CodeGenPrintAction;
import de.monticore.codegen.javagen.SymTypeExpression2JavaConverter;
import de.monticore.codegen.javagen.operationprinter.JavaEqualityOperationHandler;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mccollectiontypes.types3.MCCollectionSymTypeRelations;

public class OCL2JavaEqualityOperationHandler extends JavaEqualityOperationHandler {
  @Override
  public void printEquals(IndentPrinter printer, SymTypeExpression leftType, SymTypeExpression rightType, CodeGenPrintAction leftExprPrintAction, CodeGenPrintAction rightExprPrintAction) {
    SymTypeExpression leftJavaType = SymTypeExpression2JavaConverter.getAsJavaType(leftType);
    SymTypeExpression rightJavaType = SymTypeExpression2JavaConverter.getAsJavaType(rightType);

    // note:
    // convert to same type beforehand to assure implicit conversion happens
    // (e.g., SI Units)

    // slight optimization for numbers:
    if (isJavaPrimitive(leftJavaType) && isJavaPrimitive(rightJavaType)) {
      printWithEqualsOperator(
          printer, leftType, rightType,
          leftExprPrintAction, rightExprPrintAction
      );
    }

    // Object identity for normal objects
    else if ((leftType.isObjectType() || isGenericButNotCollection(leftType))
        && (rightType.isObjectType() || isGenericButNotCollection(rightType))) {
      printWithEqualsOperator(
          printer, leftType, rightType,
          leftExprPrintAction, rightExprPrintAction
      );
    }
    // In OCL: Collections
    // Everywhere: tuples, arrays, etc.
    else {
      printer.print("java.util.Objects.equals(");
      printLeftConverted(printer, leftType, rightType, leftExprPrintAction);
      printer.print(", ");
      printRightConverted(printer, leftType, rightType, rightExprPrintAction);
      printer.print(")");
    }
  }

  protected boolean isGenericButNotCollection(SymTypeExpression t){
    return t.isGenericType() && !(MCCollectionSymTypeRelations.isMCCollection(t) && !MCCollectionSymTypeRelations.isOptional(t));
  }
}
