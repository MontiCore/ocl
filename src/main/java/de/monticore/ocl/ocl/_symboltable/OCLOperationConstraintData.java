// (c) https://github.com/MontiCore/monticore

package de.monticore.ocl.ocl._symboltable;

import de.monticore.types.check.SymTypeExpression;

import java.util.List;

public class OCLOperationConstraintData {
  
  protected SymTypeExpression returnType;
  protected String fullyQualifiedName;
  protected List<SymTypeExpression> parameters;
  protected boolean hasPre;
  protected boolean hasPost;
  
  public OCLOperationConstraintData(SymTypeExpression returnType, String fullyQualifiedName,
      List<SymTypeExpression> parameters) {
    this.returnType = returnType;
    this.fullyQualifiedName = fullyQualifiedName;
    this.parameters = parameters;
    this.hasPre = false;
    this.hasPost = false;
  }
  
  public SymTypeExpression getReturnType() {
    return returnType;
  }
  
  public String getFullyQualifiedName() {
    return fullyQualifiedName;
  }
  
  public List<SymTypeExpression> getParameters() {
    return parameters;
  }
  
  public void setHasPre(boolean pre) {
    hasPre = pre;
  }
  
  public void setHasPost(boolean post) {
    hasPost = post;
  }
  
  public boolean hasPre() {
    return hasPre;
  }
  
  public boolean hasPost() {
    return hasPost;
  }
}
