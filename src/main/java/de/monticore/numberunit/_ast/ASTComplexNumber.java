/* (c) https://github.com/MontiCore/monticore */
package de.monticore.numberunit._ast;

import java.util.Optional;

import static de.monticore.numberunit.PrintHelper.print;

/**
 * Created by MichaelvonWenckstern on 08.01.2018.
 */
public class ASTComplexNumber extends ASTComplexNumberTOP {

  public  ASTComplexNumber (
          de.monticore.literals.literals._ast.ASTNumericLiteral real, de.monticore.literals.literals._ast.ASTNumericLiteral im,
          de.monticore.numberunit._ast.ASTI i, Optional<String> negRe, Optional<String> negIm) {
   super(real, im, i, negRe, negIm);
  }

  public ASTComplexNumber() {
    super();
  }

  public double getRealNumber() {
    if (this.isPresentNegRe()) {
      return -1*Double.parseDouble(print(this.getReal()));
    }
    else {
      return Double.parseDouble(print(this.getReal()));
    }
  }

  public double getImagineNumber() {
    if (this.isPresentNegIm()) {
      return -1*Double.parseDouble(print(this.getIm()));
    }
    else {
      return Double.parseDouble(print(this.getIm()));
    }
  }
}
