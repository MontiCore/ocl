/* (c) https://github.com/MontiCore/monticore */

package de.monticore.ocl.types.check;

import com.google.common.collect.Lists;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.ocl._visitor.NameExpressionsFromExpressionVisitor;
import de.monticore.ocl.ocl.prettyprint.OCLFullPrettyPrinter;
import de.monticore.ocl.setexpressions.SetExpressionsMill;
import de.monticore.ocl.setexpressions._ast.*;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsHandler;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsTraverser;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsVisitor2;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbolSurrogate;
import de.monticore.types.check.AbstractDeriveFromExpression;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.check.SymTypeOfGenerics;
import de.se_rwth.commons.logging.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static de.monticore.ocl.types.check.OCLTypeCheck.compatible;
import static de.monticore.types.check.SymTypeConstant.unbox;

public class DeriveSymTypeOfSetExpressions
  extends AbstractDeriveFromExpression
  implements SetExpressionsVisitor2, SetExpressionsHandler {

  protected SetExpressionsTraverser traverser;

  protected final List<String> collections = Lists
    .newArrayList("List", "Set", "Collection", "java.util.List", "java.util.Set",
      "java.util.Collection");

  @Override
  public void traverse(ASTSetInExpression node) {
    Optional<SymTypeExpression> wholeResult = calculateSetInExpression(node);
    storeResultOrLogError(wholeResult, node, "0xA0291");
  }

  @Override
  public void traverse(ASTSetNotInExpression node) {
    Optional<SymTypeExpression> wholeResult = calculateSetNotInExpression(node);
    storeResultOrLogError(wholeResult, node, "0xA0291");
  }

  protected Optional<SymTypeExpression> calculateSetNotInExpression(ASTSetNotInExpression node) {
    SymTypeExpression elemResult = acceptThisAndReturnSymTypeExpressionOrLogError(node.getElem(),
      "0xA0289");
    SymTypeExpression setResult = acceptThisAndReturnSymTypeExpressionOrLogError(node.getSet(),
      "0xA0290");
    return calculateSetInExpressionHelper(elemResult, setResult);
  }

  protected Optional<SymTypeExpression> calculateSetInExpression(ASTSetInExpression node) {
    SymTypeExpression elemResult = acceptThisAndReturnSymTypeExpressionOrLogError(node.getElem(),
      "0xA0289");
    SymTypeExpression setResult = acceptThisAndReturnSymTypeExpressionOrLogError(node.getSet(),
      "0xA0290");
    return calculateSetInExpressionHelper(elemResult, setResult);
  }

  protected Optional<SymTypeExpression> calculateSetInExpressionHelper(SymTypeExpression elemResult,
    SymTypeExpression setResult) {
    Optional<SymTypeExpression> wholeResult = Optional.empty();

    boolean correct = false;
    for (String s : collections) {
      if (setResult.isGenericType() && setResult.getTypeInfo().getName().equals(s)) {
        correct = true;
      }
    }
    if (correct) {
      SymTypeOfGenerics genericResult = (SymTypeOfGenerics) setResult;
      if (compatible(genericResult.getArgument(0), elemResult)) {
        wholeResult = Optional.of(SymTypeExpressionFactory.createTypeConstant("boolean"));
      }
    }
    return wholeResult;
  }

  @Override
  public void traverse(ASTUnionExpression node) {
    //union of two sets -> both sets need to have the same type or their types need to be sub/super types
    Optional<SymTypeExpression> wholeResult = calculateUnionExpressionInfix(node);
    storeResultOrLogError(wholeResult, node, "0xA0292");
  }

  protected Optional<SymTypeExpression> calculateUnionExpressionInfix(ASTUnionExpression node) {
    return calculateUnionAndIntersectionInfix(node, node.getLeft(), node.getRight());
  }

  @Override
  public void traverse(ASTIntersectionExpression node) {
    //intersection of two sets -> both sets need to have the same type or their types need to be sub/super types
    Optional<SymTypeExpression> wholeResult =
      calculateUnionAndIntersectionInfix(node, node.getLeft(), node.getRight());
    storeResultOrLogError(wholeResult, node, "0xA0293");
  }

  @Override
  public void traverse(ASTSetMinusExpression node) {
    //set minus of two sets -> both sets need to have the same type or their types need to be sub/super types
    Optional<SymTypeExpression> wholeResult =
      calculateUnionAndIntersectionInfix(node, node.getLeft(), node.getRight());
    storeResultOrLogError(wholeResult, node, "0xA0293");
  }

  public Optional<SymTypeExpression> calculateUnionAndIntersectionInfix(ASTExpression expr,
    ASTExpression leftExpr, ASTExpression rightExpr) {
    SymTypeExpression leftResult = acceptThisAndReturnSymTypeExpressionOrLogError(leftExpr,
      "0xA0294");
    SymTypeExpression rightResult = acceptThisAndReturnSymTypeExpressionOrLogError(rightExpr,
      "0xA0295");
    Optional<SymTypeExpression> wholeResult = Optional.empty();

    if (rightResult.isGenericType() && leftResult.isGenericType()) {
      SymTypeOfGenerics leftGeneric = (SymTypeOfGenerics) leftResult;
      SymTypeOfGenerics rightGeneric = (SymTypeOfGenerics) rightResult;
      String left = leftGeneric.getTypeInfo().getName();
      String right = rightGeneric.getTypeInfo().getName();
      if (collections.contains(left) && unbox(left).equals(unbox(right))) {
        if (compatible(leftGeneric.getArgument(0), rightGeneric.getArgument(0))) {
          TypeSymbol loader = new TypeSymbolSurrogate(left);
          loader.setEnclosingScope(getScope(expr.getEnclosingScope()));
          wholeResult = Optional.of(SymTypeExpressionFactory
            .createGenerics(loader, leftGeneric.getArgument(0).deepClone()));
        }
        else if (compatible(rightGeneric.getArgument(0), leftGeneric.getArgument(0))) {
          TypeSymbol loader = new TypeSymbolSurrogate(right);
          loader.setEnclosingScope(getScope(expr.getEnclosingScope()));
          wholeResult = Optional.of(SymTypeExpressionFactory.createTypeConstant("boolean"));
        }
      }
    }
    return wholeResult;
  }

  @Override
  public void endVisit(ASTSetAndExpression node) {
    storeResultOrLogError(Optional.of(acceptThisAndReturnSymTypeExpressionOrLogError(
      node.getSet(), "0xA0296")), node, "0xA0297");
  }

  @Override
  public void endVisit(ASTSetOrExpression node) {
    storeResultOrLogError(Optional.of(acceptThisAndReturnSymTypeExpressionOrLogError(
      node.getSet(), "0xA0296")), node, "0xA0297");
  }

  @Override
  public void traverse(ASTSetComprehension node) {
    SymTypeExpression result = null;
    if (node.isPresentMCType()) {
      node.getMCType().accept(getTraverser());
      if (typeCheckResult.isPresentCurrentResult()) {
        boolean correct = false;
        for (String s : collections) {
          if (typeCheckResult.getCurrentResult().getTypeInfo().getName().equals(s)) {
            correct = true;
          }
        }
        if (!correct) {
          typeCheckResult.reset();
          Log
            .error("0xA0298 there must be a type at " + node.getMCType().get_SourcePositionStart());
        }
        else {
          result = SymTypeExpressionFactory.createGenerics(typeCheckResult.getCurrentResult().
            getTypeInfo().getName(), getScope(node.getEnclosingScope()));
          typeCheckResult.reset();

        }
      }
      else {
        typeCheckResult.reset();
        Log.error("0xA0299 could not determine type of " + node.getMCType().getClass().getName());
      }
    }

    SymTypeExpression leftType = null;
    Set<String> varNames = new HashSet<>();
    if (node.getLeft().isPresentExpression()) {
      SetExpressionsTraverser traverser = SetExpressionsMill.traverser();
      NameExpressionsFromExpressionVisitor nameVisitor = new NameExpressionsFromExpressionVisitor();
      traverser.add4ExpressionsBasis(nameVisitor);
      node.getLeft().getExpression().accept(traverser);
      varNames = nameVisitor.getVarNames();
      node.getLeft().getExpression().accept(getTraverser());
      if (!typeCheckResult.isPresentCurrentResult()) {
        OCLFullPrettyPrinter printer = new OCLFullPrettyPrinter();
        Log.error("0xA0309 " + node.getLeft().get_SourcePositionStart()
          + " Could not calculate type of expression \"" + printer
          .prettyprint(node.getLeft()) + "\" on the left side of SetComprehension ");
      }
      else {
        leftType = typeCheckResult.getCurrentResult();
      }
    }
    else if (node.getLeft().isPresentGeneratorDeclaration()) {
      leftType = node.getLeft().getGeneratorDeclaration().getSymbol().getType();
    }
    else {
      leftType = node.getLeft().getSetVariableDeclaration().getSymbol().getType();
    }

    //check that all varNames are initialized on the right side
    while (!varNames.isEmpty()) {
      for (ASTSetComprehensionItem item : node.getSetComprehensionItemList()) {
        if (item.isPresentGeneratorDeclaration()) {
          if (varNames.contains(item.getGeneratorDeclaration().getName())) {
            varNames.remove(item.getGeneratorDeclaration().getName());
          }
        }
        else if (item.isPresentSetVariableDeclaration()) {
          if (varNames.contains(item.getSetVariableDeclaration().getName())) {
            varNames.remove(item.getSetVariableDeclaration().getName());
          }
        }
      }
    }

    if (result == null) {
      result = SymTypeExpressionFactory
        .createGenerics("java.util.Set", getScope(node.getEnclosingScope()));
    }
    ((SymTypeOfGenerics) result).addArgument(leftType);
    typeCheckResult.setCurrentResult(result);
  }

  public void traverse(ASTSetEnumeration node) {
    SymTypeExpression result = null;
    SymTypeExpression innerResult = null;
    if (node.isPresentMCType()) {
      node.getMCType().accept(getTraverser());
      if (typeCheckResult.isPresentCurrentResult()) {
        boolean correct = false;
        for (String s : collections) {
          if (typeCheckResult.getCurrentResult().getTypeInfo().getName().equals(s)) {
            correct = true;
          }
        }
        if (!correct) {
          typeCheckResult.reset();
          Log
            .error("0xA0298 there must be a type at " + node.getMCType().get_SourcePositionStart());
        }
        else {
          result = SymTypeExpressionFactory.createGenerics(typeCheckResult.getCurrentResult().
            getTypeInfo().getName(), getScope(node.getEnclosingScope()));
          typeCheckResult.reset();
        }
      }
      else {
        typeCheckResult.reset();
        Log.error("0xA0299 could not determine type of " + node.getMCType().getClass().getName());
      }
    }

    if (result == null) {
      result = SymTypeExpressionFactory
        .createGenerics("java.util.Set", getScope(node.getEnclosingScope()));
    }

    //check type of elements in set
    for (ASTSetCollectionItem item : node.getSetCollectionItemList()) {
      if (item instanceof ASTSetValueItem) {
        for (ASTExpression e : ((ASTSetValueItem) item).getExpressionList()) {
          e.accept(getTraverser());
        }
        if (typeCheckResult.isPresentCurrentResult()) {
          if (innerResult == null) {
            innerResult = typeCheckResult.getCurrentResult();
            typeCheckResult.reset();
          }
          else if (!compatible(innerResult, typeCheckResult.getCurrentResult())) {
            Log.error("different types in SetEnumeration");
          }
        }
        else {
          Log.error("Could not determine type of an expression in SetEnumeration");
        }
      }
      else {
        item.accept(getTraverser());
        if (typeCheckResult.isPresentCurrentResult()) {
          if (innerResult == null) {
            innerResult = typeCheckResult.getCurrentResult();
            typeCheckResult.reset();
          }
          else if (!compatible(innerResult, typeCheckResult.getCurrentResult())) {
            Log.error("different types in SetEnumeration");
          }
        }
        else {
          Log.error("Could not determine type of a SetValueRange in SetEnumeration");
        }
      }
    }

    ((SymTypeOfGenerics) result).addArgument(innerResult);
    typeCheckResult.setCurrentResult(result);
  }

  public void traverse(ASTSetValueRange node) {
    SymTypeExpression left = acceptThisAndReturnSymTypeExpressionOrLogError(node.getLowerBound(),
      "0xA0311");
    SymTypeExpression right = acceptThisAndReturnSymTypeExpressionOrLogError(node.getUpperBound(),
      "0xA0312");
    if (!isIntegralType(left) || !isIntegralType(right)) {
      Log.error("bounds in SetValueRange are not integral types, but have to be");
    }
    typeCheckResult.setCurrentResult(left);
  }

  @Override public SetExpressionsTraverser getTraverser() {
    return traverser;
  }

  public void setTraverser(SetExpressionsTraverser traverser) {
    this.traverser = traverser;
  }
}
