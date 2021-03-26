package de.monticore.ocl.ocl._cocos;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.ocl._ast.ASTOCLOperationConstraint;
import de.monticore.ocl.types.check.DeriveSymTypeOfOCLCombineExpressions;
import de.monticore.ocl.types.check.OCLTypeCheck;
import de.monticore.types.check.SymTypeExpression;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;

public class PreAndPostConditionsAreBooleanType implements OCLASTOCLOperationConstraintCoCo{

  private Optional<DeriveSymTypeOfOCLCombineExpressions> typeVisitor;

  public PreAndPostConditionsAreBooleanType(){ }

  public PreAndPostConditionsAreBooleanType(DeriveSymTypeOfOCLCombineExpressions typeVisitor){
    this.typeVisitor = Optional.of(typeVisitor);
  }

  @Override
  public void check(ASTOCLOperationConstraint node) {
    //if no typevisitor was set: use default type visitor
    if (!typeVisitor.isPresent()){
      typeVisitor = Optional.of(new DeriveSymTypeOfOCLCombineExpressions());
    }

    //check preconditions
    for (ASTExpression e : node.getPreConditionList()){
      e.accept(typeVisitor.get().getTraverser());
      Optional<SymTypeExpression> type = typeVisitor.get().getResult();
      if(!type.isPresent()){
        Log.error("type of precondition expression " + e + " could not be calculated.");
      }
      if(!OCLTypeCheck.isBoolean(type.get())){
        Log.error("type of precondition expression " + e +
                " has to be boolean, but is " + type.get().print());
      }
    }

    //check postconditions
    for (ASTExpression e : node.getPostConditionList()){
      e.accept(typeVisitor.get().getTraverser());
      Optional<SymTypeExpression> type = typeVisitor.get().getResult();
      if(!type.isPresent()){
        Log.error("type of postcondition expression " + e + " could not be calculated.");
      }
      if(!OCLTypeCheck.isBoolean(type.get())){
        Log.error("type of postcondition expression " + e +
                " has to be boolean, but is " + type.get().print());
      }
    }
  }

  public void setTypeVisitor(DeriveSymTypeOfOCLCombineExpressions typeVisitor){
    this.typeVisitor = Optional.of(typeVisitor);
  }
}
