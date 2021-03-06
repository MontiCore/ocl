// (c) https://github.com/MontiCore/monticore

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
import de.monticore.ocl.util.LogHelper;
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
import static de.monticore.types.check.SymTypePrimitive.unbox;

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
    Optional<SymTypeExpression> elemResult = acceptThisAndReturnSymTypeExpressionOrLogError(node.getElem(),
      "0xA0289");
    Optional<SymTypeExpression> setResult = acceptThisAndReturnSymTypeExpressionOrLogError(node.getSet(),
      "0xA0290");
    if (elemResult.isPresent() && setResult.isPresent()) {
      return calculateSetInExpressionHelper(elemResult.get(), setResult.get());
    } else {
      typeCheckResult.reset();
      return Optional.empty();
    }
  }

  protected Optional<SymTypeExpression> calculateSetInExpression(ASTSetInExpression node) {
    Optional<SymTypeExpression> elemResult = acceptThisAndReturnSymTypeExpressionOrLogError(node.getElem(),
      "0xA0289");
    Optional<SymTypeExpression> setResult = acceptThisAndReturnSymTypeExpressionOrLogError(node.getSet(),
      "0xA0290");
    if (elemResult.isPresent() && setResult.isPresent()) {
      return calculateSetInExpressionHelper(elemResult.get(), setResult.get());
    } else {
      return Optional.empty();
    }
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
        wholeResult = Optional.of(SymTypeExpressionFactory.createPrimitive("boolean"));
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
    Optional<SymTypeExpression> leftResult = acceptThisAndReturnSymTypeExpressionOrLogError(leftExpr,
      "0xA0294");
    Optional<SymTypeExpression> rightResult = acceptThisAndReturnSymTypeExpressionOrLogError(rightExpr,
      "0xA0295");
    if (!(leftResult.isPresent() && rightResult.isPresent())) {
      typeCheckResult.reset();
      return Optional.empty();
    }
    Optional<SymTypeExpression> wholeResult = Optional.empty();
    if (rightResult.get().isGenericType() && leftResult.get().isGenericType()) {
      SymTypeOfGenerics leftGeneric = (SymTypeOfGenerics) leftResult.get();
      SymTypeOfGenerics rightGeneric = (SymTypeOfGenerics) rightResult.get();
      String left = leftGeneric.getTypeInfo().getName();
      String right = rightGeneric.getTypeInfo().getName();
      if (collections.contains(left) && unbox(left).equals(unbox(right))) {
        if (compatible(leftGeneric.getArgument(0), rightGeneric.getArgument(0))) {
          wholeResult = Optional.of(SymTypeExpressionFactory
            .createGenerics(leftGeneric.getTypeInfo(), leftGeneric.getArgument(0).deepClone()));
        }
        else if (compatible(rightGeneric.getArgument(0), leftGeneric.getArgument(0))) {
          TypeSymbol loader = new TypeSymbolSurrogate(right);
          loader.setEnclosingScope(getScope(expr.getEnclosingScope()));
          wholeResult = Optional.of(SymTypeExpressionFactory.createPrimitive("boolean"));
        }
      }
    }
    return wholeResult;
  }

  @Override
  public void endVisit(ASTSetAndExpression node) {
    storeResultOrLogError(acceptThisAndReturnSymTypeExpressionOrLogError(
      node.getSet(), "0xA0296"), node, "0xA0297");
  }

  @Override
  public void endVisit(ASTSetOrExpression node) {
    storeResultOrLogError(acceptThisAndReturnSymTypeExpressionOrLogError(
      node.getSet(), "0xA0296"), node, "0xA0297");
  }

  @Override
  public void traverse(ASTSetComprehension node) {
    SymTypeExpression result = null;
      node.getMCType().accept(getTraverser());
      if (typeCheckResult.isPresentResult()) {
        boolean correct = false;
        for (String s : collections) {
          if (typeCheckResult.getResult().getTypeInfo().getName().equals(s)) {
            correct = true;
          }
        }
        if (!correct) {
          typeCheckResult.reset();
          Log
            .error("0xA0298 could not calculate type at " + node.get_SourcePositionStart());
        }
        else {
          result = SymTypeExpressionFactory.createGenerics(typeCheckResult.getResult().
            getTypeInfo());
          typeCheckResult.reset();

        }
      }
      else {
        typeCheckResult.reset();
        Log.error("0xA0299 could not determine type of " + node.getMCType().getClass().getName());
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
      if (!typeCheckResult.isPresentResult()) {
        OCLFullPrettyPrinter printer = new OCLFullPrettyPrinter();
        Log.error("0xA0309 " + node.getLeft().get_SourcePositionStart()
          + " Could not calculate type of expression \"" + printer
          .prettyprint(node.getLeft()) + "\" on the left side of SetComprehension ");
      }
      else {
        leftType = typeCheckResult.getResult();
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
          varNames.remove(item.getGeneratorDeclaration().getName());
        }
        else if (item.isPresentSetVariableDeclaration()) {
          varNames.remove(item.getSetVariableDeclaration().getName());
        }
      }
    }

    if (result == null) {
      result = SymTypeExpressionFactory
        .createGenerics("java.util.Set", getScope(node.getEnclosingScope()));
    }
    ((SymTypeOfGenerics) result).addArgument(leftType);
    typeCheckResult.setResult(result);
  }

  public void traverse(ASTSetEnumeration node) {
    SymTypeExpression result = null;
    SymTypeExpression innerResult = null;
      node.getMCType().accept(getTraverser());
      if (typeCheckResult.isPresentResult()) {
        boolean correct = false;
        for (String s : collections) {
          if (typeCheckResult.getResult().getTypeInfo().getName().equals(s)) {
            correct = true;
          }
        }
        if (!correct) {
          typeCheckResult.reset();
          Log
            .error("0xA0298 there must be a type for collection at" + node.get_SourcePositionStart());
        }
        else {
          result = SymTypeExpressionFactory.createGenerics(typeCheckResult.getResult().
            getTypeInfo());
          typeCheckResult.reset();
        }
      }
      else {
        typeCheckResult.reset();
        Log.error("0xA0299 could not determine type of " + node.getClass().getName());
      }

    if (result == null) {
      result = SymTypeExpressionFactory
        .createGenerics("java.util.Set", getScope(node.getEnclosingScope()));
    }

    //check type of elements in set
    for (ASTSetCollectionItem item : node.getSetCollectionItemList()) {
      if (item instanceof ASTSetValueItem) {
        ((ASTSetValueItem) item).getExpression().accept(getTraverser());
        if (typeCheckResult.isPresentResult()) {
          if (innerResult == null) {
            innerResult = typeCheckResult.getResult();
            typeCheckResult.reset();
          }
          else if (!compatible(innerResult, typeCheckResult.getResult())) {
            LogHelper.error(node, "0xA0333", "different types in SetEnumeration");
          }
        }
        else {
          LogHelper
            .error(node, "0xA0334", "Could not determine type of an expression in SetEnumeration");
        }
      }
      else {
        item.accept(getTraverser());
        if (typeCheckResult.isPresentResult()) {
          if (innerResult == null) {
            innerResult = typeCheckResult.getResult();
            typeCheckResult.reset();
          }
          else if (!compatible(innerResult, typeCheckResult.getResult())) {
            LogHelper.error(node, "0xA0335", "different types in SetEnumeration");
          }
        }
        else {
          LogHelper.error(node, "0xA0336",
            "Could not determine type of a SetValueRange in SetEnumeration");
        }
      }
    }

    ((SymTypeOfGenerics) result).addArgument(innerResult);
    typeCheckResult.setResult(result);
  }

  public void traverse(ASTSetValueRange node) {
    Optional<SymTypeExpression> left = acceptThisAndReturnSymTypeExpressionOrLogError(node.getLowerBound(),
      "0xA0311");
    Optional<SymTypeExpression> right = acceptThisAndReturnSymTypeExpressionOrLogError(node.getUpperBound(),
      "0xA0312");
    if (!(left.isPresent() && right.isPresent())) {
      typeCheckResult.reset();
      return;
    }

    if (!isIntegralType(left.get()) || !isIntegralType(right.get())) {
      LogHelper
        .error(node, "0xA0337", "bounds in SetValueRange are not integral types, but have to be");
    }
    typeCheckResult.setResult(left.get());
  }

  @Override public SetExpressionsTraverser getTraverser() {
    return traverser;
  }

  public void setTraverser(SetExpressionsTraverser traverser) {
    this.traverser = traverser;
  }
}
