package de.monticore.ocl2smt.trafo;

import de.monticore.cd.facade.CDAttributeFacade;
import de.monticore.cd.facade.CDModifier;
import de.monticore.cdbasis._ast.ASTCDAttribute;
import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._visitor.CDBasisHandler;
import de.monticore.cdbasis._visitor.CDBasisTraverser;
import de.monticore.cdbasis._visitor.CDBasisVisitor2;
import de.monticore.od4report.OD4ReportMill;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.umlmodifier._ast.ASTModifier;

public class AddPreAttributeTrafo implements CDBasisHandler, CDBasisVisitor2 {

  protected CDBasisTraverser traverser;

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
    // create for each attribute a pre-attribute
    node.getCDAttributeList().forEach(attr -> node.addCDMember(createPreAttribute(attr)));

    // create boolean attribute to identify pre objects
    ASTMCType type = OD4ReportMill.mCPrimitiveTypeBuilder().setPrimitive(1).build();
    CDAttributeFacade facade = CDAttributeFacade.getInstance();
    ASTModifier mod = CDModifier.PUBLIC.build();
    ASTCDAttribute attribute = facade.createAttribute(mod, type, "ispre");
    node.addCDMember(attribute);
  }

  private ASTCDAttribute createPreAttribute(ASTCDAttribute node) {
    ASTMCType type = node.getMCType();
    CDAttributeFacade facade = CDAttributeFacade.getInstance();
    ASTModifier mod = node.getModifier();
    return facade.createAttribute(mod, type, node.getName() + "__pre");
  }
}
