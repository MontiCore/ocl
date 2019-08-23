/* (c) https://github.com/MontiCore/monticore */
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
        if (node.isPresentDegCelsius()) {
            getPrinter().print("°C");
        }
        else if (node.isPresentDegFahrenheit()) {
            getPrinter().print("°F");
        }
        else if (node.isPresentImperialUnit()) {
            node.getImperialUnit().accept(getRealThis());
        }
        else if (node.isPresentSIUnit()) {
            node.getSIUnit().accept(getRealThis());
        }
    }

    @Override
    public void handle(ASTImperialUnit node) {
        getPrinter().print(node.getName());
    }

    @Override
    public void handle(ASTSIUnit node) {
        if (node.isPresentSiUnitDimensionless()) {
            node.getSiUnitDimensionless().accept(getRealThis());
        } else {
            List<ASTTimeDiv> timeDivs = node.getTimeDivList();
            List<ASTSIUnitBasic> astsiUnitBasics = node.getSIUnitBasicList();
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
        if (node.isPresentIsDiv()) {
            getPrinter().print(node.getIsDiv());
        } else if (node.isPresentIsTime()) {
            getPrinter().print(node.getIsTime());
        }
    }

    @Override
    public void handle(ASTSIUnitBasic node) {
        if (node.isPresentUnitBaseDimWithPrefix()) {
            node.getUnitBaseDimWithPrefix().accept(getRealThis());
        }
        else if (node.isPresentOfficallyAcceptedUnit()) {
            node.getOfficallyAcceptedUnit().accept(getRealThis());
        }
        else if (node.isPresentDeg()) {
            getPrinter().print("°");
        }

        if(node.isPresentSignedIntLiteral()) {
            getPrinter().print("^");
            if (node.getSignedIntLiteral().isNegative() )
                getPrinter().print("-");
            getPrinter().print(node.getSignedIntLiteral().getSource());
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
