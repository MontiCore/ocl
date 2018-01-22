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
package de.monticore.numberunit.prettyprint;

import de.monticore.numberunit._ast.*;
import de.monticore.numberunit._visitor.NumberUnitVisitor;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.types.TypesPrinter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Ferdinand Mehlan on 12.01.2018.
 */
public class NumberUnitPrettyPrinter implements NumberUnitVisitor{
    protected NumberUnitVisitor realThis;

    protected IndentPrinter printer;

    public NumberUnitPrettyPrinter(IndentPrinter printer) {
        this.printer=printer;
        realThis = this;
    }

    public IndentPrinter getPrinter() {
        return this.printer;
    }

    public String prettyprint(ASTUnit node) {
        getPrinter().clearBuffer();
        node.accept(getRealThis());
        return getPrinter().getContent();
    }

    @Override
    public void setRealThis(NumberUnitVisitor realThis) {
        this.realThis = realThis;
    }

    @Override
    public NumberUnitVisitor getRealThis() {
        return realThis;
    }

    //Todo: only prints Units as of now, add number part
    @Override
    public void handle(ASTUnit node) {
        if (node.degCelsiusIsPresent()) {
            getPrinter().print("°C");
        }
        else if (node.degFahrenheitIsPresent()) {
            getPrinter().print("°F");
        }
        else if (node.imperialUnitIsPresent()) {
            node.getImperialUnit().get().accept(getRealThis());
        }
        else if (node.sIUnitIsPresent()) {
            node.getSIUnit().get().accept(getRealThis());
        }
    }

    @Override
    public void handle(ASTImperialUnit node) {
        getPrinter().print(node.getName());
    }

    @Override
    public void handle(ASTSIUnit node) {
        if (node.siUnitDimensionlessIsPresent()) {
            node.getSiUnitDimensionless().get().accept(getRealThis());
        } else {
            List<ASTTimeDiv> timeDivs = node.getTimeDivs();
            List<ASTSIUnitBasic> astsiUnitBasics = node.getSIUnitBasics();
            int i;
            for(i = 0; i < timeDivs.size(); i++) {
                astsiUnitBasics.get(i).accept(getRealThis());
                timeDivs.get(i).accept(getRealThis());
            }
            astsiUnitBasics.get(i).accept(getRealThis());
        }
    }

    @Override
    public void handle(ASTSiUnitDimensionless node) {
        getPrinter().print(node.getName());
    }

    @Override
    public void handle(ASTTimeDiv node) {
        if (node.isDivIsPresent()) {
            getPrinter().print(node.getIsDiv());
        } else if (node.isTimeIsPresent()) {
            getPrinter().print(node.getIsTime());
        }
    }

    @Override
    public void handle(ASTSIUnitBasic node) {
        if (node.unitBaseDimWithPrefixIsPresent()) {
            node.getUnitBaseDimWithPrefix().get().accept(getRealThis());
        }
        else if (node.officallyAcceptedUnitIsPresent()) {
            node.getOfficallyAcceptedUnit().get().accept(getRealThis());
        }
        else if (node.degIsPresent()) {
            getPrinter().print("°");
        }

        if(node.signedIntLiteralIsPresent()) {
            getPrinter().print("^");
            if (node.getSignedIntLiteral().get().isNegative() )
                getPrinter().print("-");
            getPrinter().print(node.getSignedIntLiteral().get().getSource());
        }
    }

    @Override
    public void handle(ASTUnitBaseDimWithPrefix node) {
        getPrinter().print(node.getName());
    }

    @Override
    public void handle(ASTOfficallyAcceptedUnit node) {
        getPrinter().print(node.getName());
    }
}
