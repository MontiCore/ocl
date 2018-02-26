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
    protected Boolean showAtt, showAssoc, showRoles, showCard;

    public CD4A2PlantUMLVisitor(IndentPrinter printer) {
        this.showAtt = false;
        this.showAssoc = false;
        this.showRoles = false;
        this.showCard = true;
        this.printer=printer;
        realThis = this;
    }

    public CD4A2PlantUMLVisitor(IndentPrinter printer, Boolean showAtt, Boolean showAssoc,
                                Boolean showRoles, Boolean showCard) {
        this.showAtt = showAtt;
        this.showAssoc = showAssoc;
        this.showRoles = showRoles;
        this.showCard = showCard;
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
        getPrinter().print("class " + node.getName());
        if (showAtt && node.getCDAttributes().size() > 0) {
            getPrinter().print(" {\n");
            getPrinter().indent();
            node.getCDAttributes().forEach(a -> a.accept(getRealThis()));
            getPrinter().unindent();
            getPrinter().print("}\n");
        } else {
            getPrinter().print("\n");
        }
    }

    @Override
    public void handle(ASTCDAttribute node) {
        if(node.modifierIsPresent())
            node.getModifier().get().accept(getRealThis());
        getPrinter().print(node.printType() + " " + node.getName() + "\n");
    }

    @Override
    public void handle(ASTModifier node) {
        if(node.isPrivate())
            getPrinter().print("-");
        else if(node.isProtected())
            getPrinter().print("#");
        else if(node.isPublic())
            getPrinter().print("+");
    }

    @Override
    public void handle(ASTCDAssociation node) {
        getPrinter().print(node.getLeftReferenceName().toString() + " ");

        if(node.leftCardinalityIsPresent() || node.leftRoleIsPresent()) {
            getPrinter().print("\"");
            if(showRoles && node.leftRoleIsPresent())
                getPrinter().print("(" + node.getLeftRole().get() + ") ");
            if(node.leftCardinalityIsPresent())
                node.getLeftCardinality().get().accept(getRealThis());
            getPrinter().print("\" ");
        }

        if(node.isLeftToRight()) {
            getPrinter().print("-->");
        } else if (node.isRightToLeft()) {
            getPrinter().print("<--");
        } else if (node.isBidirectional()){
            getPrinter().print("<-->");
        } else {
            getPrinter().print("--");
        }

        if(node.rightCardinalityIsPresent() || node.rightRoleIsPresent()) {
            getPrinter().print(" \"");
            if(showRoles && node.rightRoleIsPresent())
                getPrinter().print("(" + node.getRightRole().get() + ") ");
            if(node.rightCardinalityIsPresent())
                node.getRightCardinality().get().accept(getRealThis());
            getPrinter().print("\"");
        }

        getPrinter().print(" " + node.getRightReferenceName().toString());

        if(showAssoc && node.nameIsPresent())
            getPrinter().print(" : " + node.getName().get());

        getPrinter().print("\n");
    }

    @Override
    public void handle(ASTCardinality cardinality) {
        if(showCard) {
            if (cardinality.isMany()) {
                getPrinter().print("*");
            } else if (cardinality.isOne()) {
                getPrinter().print("1");
            } else if (cardinality.isOneToMany()) {
                getPrinter().print("1..*");
            } else if (cardinality.isOptional()) {
                getPrinter().print("0..1");
            }
        }
    }



}
