/**
 * ******************************************************************************
 *  MontiCAR Modeling Family, www.se-rwth.de
 *  Copyright (c) 2017, Software Engineering Group at RWTH Aachen,
 *  All rights reserved.
 *
 *  This project is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version.
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * *******************************************************************************
 */
package de.monticore.numberunit._ast;

import static de.monticore.numberunit.PrintHelper.print;

import javax.measure.unit.NonSI;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import java.util.Optional;

import de.monticore.literals.literals._ast.ASTNumericLiteral;

/**
 * Created by MichaelvonWenckstern on 08.01.2018.
 */
public class ASTNumberWithUnit extends ASTNumberWithUnitTOP {

  public  ASTNumberWithUnit (
      de.monticore.numberunit._ast.ASTNumberWithInf number,
      de.monticore.numberunit._ast.ASTUnit unit,
      de.monticore.numberunit._ast.ASTComplexNumber complexNumber) {
    super(number, unit, complexNumber);
  }

  public ASTNumberWithUnit() {
    super();
  }

  public boolean isPlusInfinite() {
    return this.numIsPresent() && this.getNum().get().posInfIsPresent();
  }

  public boolean isMinusInfinite() {
    return this.numIsPresent() && this.getNum().get().negInfIsPresent();
  }

  public boolean isComplexNumber() {
    return this.cnIsPresent();
  }

  /**
   * returns Optional.empty() if the number is:
   *   a) a complex number
   *   b) plus or minus infinity
   * @return
   */
  public Optional<Double> getNumber() {
    if (this.numIsPresent() && this.getNum().get().numberIsPresent()) {
      ASTNumericLiteral number = this.getNum().get().getNumber().get();
      double d;
      if (this.getNum().get().negNumberIsPresent()) {
        d = -1*Double.parseDouble(print(number));
      }
      else {
        d = Double.parseDouble(print(number));
      }

      if (this.unIsPresent() && this.getUn().get().imperialUnitIsPresent() &&
          this.getUn().get().getImperialUnit().get().getName().equals("th")) {
        d *= 0.0254;
      }
      return Optional.of(d);
    }
    return Optional.empty();
  }

  public Optional<ASTComplexNumber> getComplexNumber() {
    return this.getCn();
  }

  public Unit getUnit() {
    if (this.unIsPresent()) {
      if (this.getUn().get().degCelsiusIsPresent()) {
        return SI.CELSIUS;
      }
      if (this.getUn().get().degFahrenheitIsPresent()) {
        return NonSI.FAHRENHEIT;
      }
      if (this.getUn().get().imperialUnitIsPresent()) {
        if (this.getUn().get().getImperialUnit().get().getName().equals("th")) {
          return Unit.valueOf("mm");
        }
        return Unit.valueOf(this.getUn().get().getImperialUnit().get().getName());
      }
      if (this.getUn().get().sIUnitIsPresent()) {
        return siUnit(this.getUn().get().getSIUnit().get());
      }
    }
    return Unit.ONE;
  }

  protected Unit siUnit(ASTSIUnit siUnit) {
    if (siUnit.siUnitDimensionlessIsPresent()) {
      return Unit.valueOf(siUnit.getSiUnitDimensionless().get().getName().replace("deg", "°"));
    }
    String s = toString(siUnit.getSIUnitBasics().get(0));
    for (int i = 1; i < siUnit.getSIUnitBasics().size(); i++) {
      s += toString(siUnit.getTimeDivs(i-1)) +  toString(siUnit.getSIUnitBasics().get(i));
    }
    return Unit.valueOf(s);
  }

  protected String toString(ASTSIUnitBasic sib) {
    String unit = "";
    if (sib.unitBaseDimWithPrefixIsPresent()) {
      unit =  sib.getUnitBaseDimWithPrefix().get().getName();
    }
    else if (sib.officallyAcceptedUnitIsPresent()) {
      unit = sib.getOfficallyAcceptedUnit().get().getName();
    }
    else if (sib.degIsPresent()) {
      unit = "°";
    }
    if (sib.signedIntLiteralIsPresent()) {
      unit = unit + "^" + print(sib.getSignedIntLiteral().get());
    }
    return unit;
  }

  protected String toString(ASTTimeDiv timeDivs) {
    return timeDivs.isDivIsPresent() ? "/" : "*";
  }
}
