package de.monticore.ocl;

import de.monticore.ocl.ocl.OCLMill;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.types.mcbasictypes.refadaptation.MCTypeFactory;
import de.monticore.types.mcbasictypes.refadaptation.MCBasicTypesTypeFactory;

import java.io.IOException;

// TODO Discuss again this implementation vs. handwritten mapping as in MCBasicTypesTypeFactory
/**
 * {@link MCTypeFactory} implementation based on the parser and {@link SymTypeExpression#print()}
 * functionality. In order to create an {@link ASTMCType} from a {@link SymTypeExpression},
 * we print the type expression and parse it back into an AST node.<br>
 * <br>
 * WARN: This only work as long as the {@link de.monticore.types3.util.SymTypePrintFullNameVisitor}
 * supports all the types we want to create!
 */
public class OCLMCTypeFactory extends MCBasicTypesTypeFactory {

  @Override
  public ASTMCType createMCType(SymTypeExpression symTypeExpression) {
    try {
      return OCLMill.parser().parse_StringMCType(symTypeExpression.printFullName())
              .orElseThrow(() -> new RuntimeException("Could not parse MCType from " +
                      "SymTypeExpression: " + symTypeExpression.print()));
    } catch (IOException e) {
      throw new RuntimeException("Unexpected IOException while parsing string", e);
    }
  }
}
