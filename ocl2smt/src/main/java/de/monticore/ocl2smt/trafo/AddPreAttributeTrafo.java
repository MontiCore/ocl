package de.monticore.ocl2smt.trafo;

import de.monticore.cd.facade.CDAttributeFacade;
import de.monticore.cd.facade.CDModifier;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDType;
import de.monticore.cdbasis._visitor.CDBasisHandler;
import de.monticore.cdbasis._visitor.CDBasisTraverser;
import de.monticore.cdbasis._visitor.CDBasisVisitor2;
import de.monticore.od4report.OD4ReportMill;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.umlmodifier._ast.ASTModifier;
import java.util.ArrayList;
import java.util.List;

public class AddPreAttributeTrafo implements CDBasisHandler, CDBasisVisitor2 {

  protected CDBasisTraverser traverser;

  List<ASTCDAttribute> attributes = new ArrayList<>();

  @Override
  public CDBasisTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(CDBasisTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void handle(ASTCDClass node) {
    node.getCDAttributeList().forEach(attribute -> attribute.accept(traverser));
    node.addAllCDMembers(attributes);

    ASTMCType type = OD4ReportMill.mCPrimitiveTypeBuilder().setPrimitive(1).build();
    CDAttributeFacade facade = CDAttributeFacade.getInstance();
    ASTModifier mod = CDModifier.PUBLIC.build();
    ASTCDAttribute attribute = facade.createAttribute(mod, type, "ispre");

    attributes.add(attribute);
  }

  @Override
  public void handle(ASTCDAttribute node) {
   node.setName(node.getName() +"__pre");
  }

}
