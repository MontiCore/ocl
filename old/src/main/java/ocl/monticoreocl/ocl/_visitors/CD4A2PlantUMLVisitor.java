/**
 *
 * (c) https://github.com/MontiCore/monticore
 *
 * The license generally applicable for this project
 * can be found under https://github.com/MontiCore/monticore.
 */
/**
 *
 * /* (c) https://github.com/MontiCore/monticore */
 *
 * The license generally applicable for this project
 * can be found under https://github.com/MontiCore/monticore.
 */
/* (c) https://github.com/MontiCore/monticore */
package ocl.monticoreocl.ocl._visitors;


import de.monticore.prettyprint.IndentPrinter;
import de.monticore.umlcd4a.cd4analysis._ast.*;
import de.monticore.umlcd4a.cd4analysis._visitor.CD4AnalysisVisitor;



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
        node.getCDInterfaceList().forEach(i -> i.accept(getRealThis()));
        node.getCDEnumList().forEach(e -> e.accept(getRealThis()));
        node.getCDClassList().forEach(c -> c.accept(getRealThis()));
        node.getCDAssociationList().forEach(a -> a.accept(getRealThis()));
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

        if (node.isPresentSuperclass()) {
            getPrinter().print(" extends " + node.printSuperClass());
        }

        if (!node.getInterfaceList().isEmpty()) {
            getPrinter().print(" implements " + node.printInterfaces());
        }

        if (showAtt && node.getCDAttributeList().size() > 0) {
            getPrinter().print(" {\n");
            getPrinter().indent();
            node.getCDAttributeList().forEach(a -> a.accept(getRealThis()));
            getPrinter().unindent();
            getPrinter().print("}\n");
        } else {
            getPrinter().print("\n");
        }
    }

    @Override
    public void handle(ASTCDAttribute node) {
        if(node.isPresentModifier())
            node.getModifier().accept(getRealThis());
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

        if((showRoles && node.isPresentLeftRole()) || (showCard && node.isPresentLeftCardinality())) {
            getPrinter().print("\"");
            if(showRoles && node.isPresentLeftRole())
                getPrinter().print("(" + node.getLeftRole() + ") ");
            if(node.isPresentLeftCardinality())
                node.getLeftCardinality().accept(getRealThis());
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

        if((showRoles && node.isPresentRightRole()) || (showCard && node.isPresentRightCardinality())) {
            getPrinter().print(" \"");
            if(showRoles && node.isPresentRightRole())
                getPrinter().print("(" + node.getRightRole() + ") ");
            if(node.isPresentRightCardinality())
                node.getRightCardinality().accept(getRealThis());
            getPrinter().print("\"");
        }

        getPrinter().print(" " + node.getRightReferenceName().toString());

        if(showAssoc && node.isPresentName())
            getPrinter().print(" : " + node.getName());

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
