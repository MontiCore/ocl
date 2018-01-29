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
package ocl.monticoreocl.ocl._visitors;


import de.monticore.prettyprint.IndentPrinter;
import de.monticore.umlcd4a.cd4analysis._ast.*;
import de.monticore.umlcd4a.cd4analysis._visitor.CD4AnalysisVisitor;


import java.util.Optional;


public class CD4A2PlantUMLVisitor implements CD4AnalysisVisitor {

    protected CD4A2PlantUMLVisitor realThis;
    protected IndentPrinter printer;

    public CD4A2PlantUMLVisitor(IndentPrinter printer) {
        this.printer=printer;
        realThis = this;
    }

    public IndentPrinter getPrinter() {
        return this.printer;
    }

    public String print2PlantUML(ASTCDCompilationUnit node) {
        getPrinter().clearBuffer();
        node.accept(getRealThis());
        return getPrinter().getContent();
    }

    @Override
    public CD4A2PlantUMLVisitor getRealThis() {
        return realThis;
    }

    @Override
    public void handle(ASTCDCompilationUnit node) {
        node.getCDDefinition().accept(getRealThis());
    }

    @Override
    public void handle(ASTCDDefinition node) {
        getPrinter().print("@startuml\n");
        node.getCDInterfaces().forEach(i -> i.accept(getRealThis()));
        node.getCDEnums().forEach(e -> e.accept(getRealThis()));
        node.getCDClasses().forEach(c -> c.accept(getRealThis()));
        node.getCDAssociations().forEach(a -> a.accept(getRealThis()));
        getPrinter().print("@enduml");
    }

    @Override
    public void handle(ASTCDInterface node) {
        getPrinter().print("interface " + node.getName() + "\n");
    }

    @Override
    public void handle(ASTCDEnum node) {
        getPrinter().print("enum " + node.getName() + "\n");
    }

    @Override
    public void handle(ASTCDClass node) {
        getPrinter().print("class " + node.getName() + "\n");
    }

    @Override
    public void handle(ASTCDAssociation node) {
        String leftClass = node.getLeftReferenceName().toString();
        String rightClass = node.getRightReferenceName().toString();
        String leftCardinality = printCardinality(node.getLeftCardinality());
        String rightCardinality = printCardinality((node.getRightCardinality()));
        String direction;
        if(node.isLeftToRight()) {
            direction = "-->";
        } else if (node.isRightToLeft()) {
            direction = "<--";
        } else if (node.isBidirectional()){
            direction = "<-->";
        } else {
            direction = "--";
        }
        getPrinter().print(leftClass + " " + leftCardinality + " " + direction + " " + rightCardinality + " " + rightClass + "\n");
    }

    public String printCardinality(Optional<ASTCardinality> cardinality) {
        if(!cardinality.isPresent())
            return "";

        if(cardinality.get().isMany()) {
            return "\"*\"";
        } else if (cardinality.get().isOne()) {
            return "\"1\"";
        } else if (cardinality.get().isOneToMany()) {
            return "\"1..*\"";
        } else if (cardinality.get().isOptional()) {
            return "\"0..1\"";
        }

        return "";
    }



}
