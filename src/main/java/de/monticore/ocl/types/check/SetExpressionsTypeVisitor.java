package de.monticore.ocl.types.check;

import com.google.common.collect.Lists;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.ocl._visitor.NameExpressionsFromExpressionVisitor;
import de.monticore.ocl.setexpressions.SetExpressionsMill;
import de.monticore.ocl.setexpressions._ast.*;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsHandler;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsTraverser;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsVisitor2;
import de.monticore.ocl.util.LogHelper;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types3.AbstractTypeVisitor;
import de.monticore.types3.SymTypeRelations;
import de.se_rwth.commons.logging.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static de.monticore.ocl.types.check.OCLTypeCheck.compatible;
import static de.monticore.types.check.SymTypeExpressionFactory.createObscureType;

public class SetExpressionsTypeVisitor extends AbstractTypeVisitor
    implements SetExpressionsVisitor2, SetExpressionsHandler {
  
  // TODO MSm: better way to define them?
  protected final List<String> collections =
      Lists.newArrayList("List", "Set", "Collection", "java.util.List", "java.util.Set",
          "java.util.Collection");
  
  protected SetExpressionsTraverser traverser;
  protected SymTypeRelations typeRelations;
  
  public SetExpressionsTypeVisitor() {
    this(new SymTypeRelations());
  }
  
  protected SetExpressionsTypeVisitor(SymTypeRelations typeRelations) {
    this.typeRelations = typeRelations;
  }
  
  @Override
  public SetExpressionsTraverser getTraverser() {
    return traverser;
  }
  
  @Override
  public void setTraverser(SetExpressionsTraverser traverser) {
    this.traverser = traverser;
  }
  
  protected SymTypeRelations getTypeRel() {
    return typeRelations;
  }
  
  @Override
  public void endVisit(ASTSetInExpression expr) {
    var elemResult = getType4Ast().getPartialTypeOfExpr(expr.getElem());
    var setResult = getType4Ast().getPartialTypeOfExpr(expr.getSet());
    calculateSetInExpression(expr, elemResult, setResult);
  }
  
  @Override
  public void endVisit(ASTSetNotInExpression expr) {
    var elemResult = getType4Ast().getPartialTypeOfExpr(expr.getElem());
    var setResult = getType4Ast().getPartialTypeOfExpr(expr.getSet());
    calculateSetInExpression(expr, elemResult, setResult);
  }
  
  protected void calculateSetInExpression(ASTExpression expr, SymTypeExpression elemResult,
      SymTypeExpression setResult) {
    
    SymTypeExpression result;
    if (!elemResult.isObscureType() && !setResult.isObscureType() && setResult.isGenericType() &&
        collections.stream().anyMatch(c -> setResult.getTypeInfo().getName().equals(c)) &&
        compatible(((SymTypeOfGenerics) setResult).getArgument(0), elemResult)) {
      
      result = SymTypeExpressionFactory.createPrimitive("boolean");
    }
    else {
      result = createObscureType();
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTUnionExpression expr) {
    // union of two sets -> both sets need to have the same type or their types need to be sub/super
    // types
    var leftResult = getType4Ast().getPartialTypeOfExpr(expr.getLeft());
    var rightResult = getType4Ast().getPartialTypeOfExpr(expr.getRight());
    calculateSetOperation(expr, leftResult, rightResult);
  }
  
  @Override
  public void endVisit(ASTIntersectionExpression expr) {
    // union of two sets -> both sets need to have the same type or their types need to be sub/super
    // types
    var leftResult = getType4Ast().getPartialTypeOfExpr(expr.getLeft());
    var rightResult = getType4Ast().getPartialTypeOfExpr(expr.getRight());
    calculateSetOperation(expr, leftResult, rightResult);
  }
  
  @Override
  public void endVisit(ASTSetMinusExpression expr) {
    // union of two sets -> both sets need to have the same type or their types need to be sub/super
    // types
    var leftResult = getType4Ast().getPartialTypeOfExpr(expr.getLeft());
    var rightResult = getType4Ast().getPartialTypeOfExpr(expr.getRight());
    calculateSetOperation(expr, leftResult, rightResult);
  }
  
  public void calculateSetOperation(
      ASTExpression expr, SymTypeExpression leftResult, SymTypeExpression rightResult) {
    SymTypeExpression result = createObscureType();
    
    if (!leftResult.isObscureType() &&
        !rightResult.isObscureType() &&
        rightResult.isGenericType() &&
        leftResult.isGenericType()) {
      var leftGeneric = (SymTypeOfGenerics) leftResult;
      var rightGeneric = (SymTypeOfGenerics) rightResult;
      
      if (collections.contains(leftGeneric.getTypeInfo().getName()) &&
          getTypeRel().unbox(leftGeneric).equals(getTypeRel().unbox(rightGeneric))) {
        
        if (compatible(leftGeneric.getArgument(0), rightGeneric.getArgument(0))) {
          result = SymTypeExpressionFactory.createGenerics(leftGeneric.getTypeInfo(),
              leftGeneric.getArgument(0).deepClone());
        }
        else if (compatible(rightGeneric.getArgument(0), leftGeneric.getArgument(0))) {
          // TODO MSm: no effect
          // TypeSymbol loader = new TypeSymbolSurrogate(right);
          // loader.setEnclosingScope(getScope(expr.getEnclosingScope()));
          result = SymTypeExpressionFactory.createPrimitive("boolean");
        }
      }
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  @Override
  public void endVisit(ASTSetAndExpression expr) {
    SymTypeExpression setResult = getType4Ast().getPartialTypeOfExpr(expr.getSet());
    getType4Ast().setTypeOfExpression(expr, setResult);
  }
  
  @Override
  public void endVisit(ASTSetOrExpression expr) {
    SymTypeExpression setResult = getType4Ast().getPartialTypeOfExpr(expr.getSet());
    getType4Ast().setTypeOfExpression(expr, setResult);
  }
  
  @Override
  public void endVisit(ASTSetComprehension expr) {
    SymTypeExpression result = null;
    
    var typeResult = getType4Ast().getPartialTypeOfTypeId(expr.getMCType());
    
    if (typeResult.isObscureType()) {
      result = createObscureType();
    }
    else if (collections.stream().anyMatch(c -> typeResult.getTypeInfo().getName().equals(c))) {
      result = SymTypeExpressionFactory.createGenerics(typeResult.getTypeInfo());
    }
    else {
      Log.error("0xA0298 could not calculate type at " + expr.get_SourcePositionStart());
    }
    
    SymTypeExpression leftType = createObscureType();
    Set<String> varNames = new HashSet<>();
    if (expr.getLeft().isPresentExpression()) {
      SetExpressionsTraverser traverser = SetExpressionsMill.traverser();
      NameExpressionsFromExpressionVisitor nameVisitor = new NameExpressionsFromExpressionVisitor();
      traverser.add4ExpressionsBasis(nameVisitor);
      expr.getLeft().getExpression().accept(traverser);
      varNames = nameVisitor.getVarNames();
      // TODO MSm: Type result wrong?
      expr.getLeft().getExpression().accept(getTraverser());
      if (typeResult.isObscureType()) {
        result = createObscureType();
      }
      else {
        leftType = typeResult;
      }
    }
    else if (expr.getLeft().isPresentGeneratorDeclaration()) {
      leftType = expr.getLeft().getGeneratorDeclaration().getSymbol().getType();
    }
    else {
      leftType = expr.getLeft().getSetVariableDeclaration().getSymbol().getType();
    }
    
    if (!leftType.isObscureType()) {
      // check that all varNames are initialized on the right side
      while (!varNames.isEmpty()) {
        for (ASTSetComprehensionItem item : expr.getSetComprehensionItemList()) {
          if (item.isPresentGeneratorDeclaration()) {
            varNames.remove(item.getGeneratorDeclaration().getName());
          }
          else if (item.isPresentSetVariableDeclaration()) {
            varNames.remove(item.getSetVariableDeclaration().getName());
          }
        }
      }
      
      if (result == null) {
        result = SymTypeExpressionFactory.createGenerics("java.util.Set",
            getAsBasicSymbolsScope(expr.getEnclosingScope()));
      }
      if (result instanceof SymTypeOfGenerics) {
        ((SymTypeOfGenerics) result).setArgument(0, leftType);
      }
      
      getType4Ast().setTypeOfExpression(expr, result);
    }
  }
  
  public void endVisit(ASTSetEnumeration expr) {
    boolean obscure = false;
    SymTypeExpression result = null;
    SymTypeExpression innerResult = null;
    var typeResult = getType4Ast().getPartialTypeOfTypeId(expr.getMCType());
    
    boolean correct = false;
    for (String s : collections) {
      if (typeResult.getTypeInfo().getName().equals(s)) {
        correct = true;
      }
    }
    if (!correct) {
      Log.error("0xA0298 there must be a type for collection at" + expr.get_SourcePositionStart());
    }
    else {
      result = SymTypeExpressionFactory.createGenerics(typeResult.getTypeInfo());
    }
    
    if (result == null) {
      result =
          SymTypeExpressionFactory.createGenerics(
              "java.util.Set", getAsBasicSymbolsScope(expr.getEnclosingScope()));
    }
    
    // check type of elements in set
    for (ASTSetCollectionItem item : expr.getSetCollectionItemList()) {
      if (item instanceof ASTSetValueItem) {
        // TODO MSm: type result wrong?
        ((ASTSetValueItem) item).getExpression().accept(getTraverser());
        if (typeResult.isObscureType()) {
          obscure = true;
        }
        else if (innerResult == null) {
          innerResult = typeResult;
        }
        else if (!compatible(innerResult, typeResult)) {
          LogHelper.error(expr, "0xA0333", "different types in SetEnumeration");
        }
      }
      else {
        // TODO MSm: item type result?
        item.accept(getTraverser());
        if (typeResult.isObscureType()) {
          obscure = true;
        }
        else if (innerResult == null) {
          innerResult = typeResult;
        }
        else if (!compatible(innerResult, typeResult)) {
          LogHelper.error(expr, "0xA0335", "different types in SetEnumeration");
        }
      }
    }
    
    if (!obscure) {
      ((SymTypeOfGenerics) result).setArgument(0, innerResult);
    }
    else {
      result = SymTypeExpressionFactory.createObscureType();
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }
  
  public void traverse(ASTSetValueRange expr) {
    var leftResult = getType4Ast().getPartialTypeOfExpr(expr.getLowerBound());
    var rightResult = getType4Ast().getPartialTypeOfExpr(expr.getUpperBound());
    
    SymTypeExpression result;
    if (leftResult.isObscureType() || rightResult.isObscureType()) {
      result = createObscureType();
    }
    else {
      if (!getTypeRel().isIntegralType(leftResult) || !getTypeRel().isIntegralType(rightResult)) {
        LogHelper.error(expr, "0xA0337",
            "bounds in SetValueRange are not integral types, but have to be");
      }
      result = leftResult;
    }
    // TODO MSm: How? getType4Ast().setTypeOfTypeIdentifier(expr, result);
  }
}