// (c) https://github.com/MontiCore/monticore

package de.monticore.ocl.types.check;

import static de.monticore.ocl.types.check.OCLTypeCheck.compatible;
import static de.monticore.types.check.SymTypePrimitive.unbox;

import com.google.common.collect.Lists;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._visitor.NameExpressionsFromExpressionVisitor;
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
import java.util.Set;

/**
 * @deprecated This class is no longer acceptable since we use <b>Type Check 3</b> to calculate the
 *     type of expressions and literals related to OCL. Use {@link SetExpressionsTypeVisitor}
 *     instead.
 */
@Deprecated(forRemoval = true)
public class DeriveSymTypeOfSetExpressions extends AbstractDeriveFromExpression
    implements SetExpressionsVisitor2, SetExpressionsHandler {

  protected SetExpressionsTraverser traverser;

  protected final List<String> collections =
      Lists.newArrayList(
          "List", "Set", "Collection", "java.util.List", "java.util.Set", "java.util.Collection");

  @Override
  public void traverse(ASTSetInExpression node) {
    SymTypeExpression elemResult = acceptThisAndReturnSymTypeExpression(node.getElem());
    SymTypeExpression setResult = acceptThisAndReturnSymTypeExpression(node.getSet());
    if (!elemResult.isObscureType() && !setResult.isObscureType()) {
      SymTypeExpression wholeResult = calculateSetInExpression(elemResult, setResult);
      storeResultOrLogError(wholeResult, node, "0xA0291");
    } else {
      getTypeCheckResult().reset();
      getTypeCheckResult().setResult(SymTypeExpressionFactory.createObscureType());
    }
  }

  @Override
  public void traverse(ASTSetNotInExpression node) {
    SymTypeExpression elemResult = acceptThisAndReturnSymTypeExpression(node.getElem());
    SymTypeExpression setResult = acceptThisAndReturnSymTypeExpression(node.getSet());
    if (!elemResult.isObscureType() && !setResult.isObscureType()) {
      SymTypeExpression wholeResult = calculateSetInExpression(elemResult, setResult);
      storeResultOrLogError(wholeResult, node, "0xA0291");
    } else {
      getTypeCheckResult().reset();
      getTypeCheckResult().setResult(SymTypeExpressionFactory.createObscureType());
    }
  }

  protected SymTypeExpression calculateSetInExpression(
      SymTypeExpression elemResult, SymTypeExpression setResult) {
    SymTypeExpression wholeResult = SymTypeExpressionFactory.createObscureType();

    boolean correct = false;
    for (String s : collections) {
      if (setResult.isGenericType() && setResult.getTypeInfo().getName().equals(s)) {
        correct = true;
      }
    }
    if (correct) {
      SymTypeOfGenerics genericResult = (SymTypeOfGenerics) setResult;
      if (compatible(genericResult.getArgument(0), elemResult)) {
        wholeResult = SymTypeExpressionFactory.createPrimitive("boolean");
      }
    }
    return wholeResult;
  }

  @Override
  public void traverse(ASTUnionExpression node) {
    // union of two sets -> both sets need to have the same type or their types need to be sub/super
    // types
    SymTypeExpression leftResult = acceptThisAndReturnSymTypeExpression(node.getLeft());
    SymTypeExpression rightResult = acceptThisAndReturnSymTypeExpression(node.getRight());
    if (!leftResult.isObscureType() && !rightResult.isObscureType()) {
      SymTypeExpression wholeResult =
          calculateUnionAndIntersectionInfix(node, leftResult, rightResult);
      storeResultOrLogError(wholeResult, node, "0xA0292");
    } else {
      getTypeCheckResult().reset();
      getTypeCheckResult().setResult(SymTypeExpressionFactory.createObscureType());
    }
  }

  @Override
  public void traverse(ASTIntersectionExpression node) {
    // intersection of two sets -> both sets need to have the same type or their types need to be
    // sub/super types
    SymTypeExpression leftResult = acceptThisAndReturnSymTypeExpression(node.getLeft());
    SymTypeExpression rightResult = acceptThisAndReturnSymTypeExpression(node.getRight());
    if (!leftResult.isObscureType() && !rightResult.isObscureType()) {
      SymTypeExpression wholeResult =
          calculateUnionAndIntersectionInfix(node, leftResult, rightResult);
      storeResultOrLogError(wholeResult, node, "0xA0293");
    } else {
      getTypeCheckResult().reset();
      getTypeCheckResult().setResult(SymTypeExpressionFactory.createObscureType());
    }
  }

  @Override
  public void traverse(ASTSetMinusExpression node) {
    // set minus of two sets -> both sets need to have the same type or their types need to be
    // sub/super types
    SymTypeExpression leftResult = acceptThisAndReturnSymTypeExpression(node.getLeft());
    SymTypeExpression rightResult = acceptThisAndReturnSymTypeExpression(node.getRight());
    if (!leftResult.isObscureType() && !rightResult.isObscureType()) {
      SymTypeExpression wholeResult =
          calculateUnionAndIntersectionInfix(node, leftResult, rightResult);
      storeResultOrLogError(wholeResult, node, "0xA0293");
    } else {
      getTypeCheckResult().reset();
      getTypeCheckResult().setResult(SymTypeExpressionFactory.createObscureType());
    }
  }

  public SymTypeExpression calculateUnionAndIntersectionInfix(
      ASTExpression expr, SymTypeExpression leftResult, SymTypeExpression rightResult) {
    SymTypeExpression wholeResult = SymTypeExpressionFactory.createObscureType();
    if (rightResult.isGenericType() && leftResult.isGenericType()) {
      SymTypeOfGenerics leftGeneric = (SymTypeOfGenerics) leftResult;
      SymTypeOfGenerics rightGeneric = (SymTypeOfGenerics) rightResult;
      String left = leftGeneric.getTypeInfo().getName();
      String right = rightGeneric.getTypeInfo().getName();
      if (collections.contains(left) && unbox(left).equals(unbox(right))) {
        if (compatible(leftGeneric.getArgument(0), rightGeneric.getArgument(0))) {
          wholeResult =
              SymTypeExpressionFactory.createGenerics(
                  leftGeneric.getTypeInfo(), leftGeneric.getArgument(0).deepClone());
        } else if (compatible(rightGeneric.getArgument(0), leftGeneric.getArgument(0))) {
          TypeSymbol loader = new TypeSymbolSurrogate(right);
          loader.setEnclosingScope(getScope(expr.getEnclosingScope()));
          wholeResult = SymTypeExpressionFactory.createPrimitive("boolean");
        }
      }
    }
    return wholeResult;
  }

  @Override
  public void endVisit(ASTSetAndExpression node) {
    SymTypeExpression setResult = acceptThisAndReturnSymTypeExpression(node.getSet());
    if (!setResult.isObscureType()) {
      storeResultOrLogError(setResult, node, "0xA0297");
    }
  }

  @Override
  public void endVisit(ASTSetOrExpression node) {
    SymTypeExpression setResult = acceptThisAndReturnSymTypeExpression(node.getSet());
    if (!setResult.isObscureType()) {
      storeResultOrLogError(setResult, node, "0xA0297");
    }
  }

  @Override
  public void traverse(ASTSetComprehension node) {
    boolean obscure = false;
    SymTypeExpression result = null;
    node.getMCType().accept(getTraverser());
    if (getTypeCheckResult().isPresentResult()) {
      if (getTypeCheckResult().getResult().isObscureType()) {
        obscure = true;
      } else {
        boolean correct = false;
        for (String s : collections) {
          if (getTypeCheckResult().getResult().getTypeInfo().getName().equals(s)) {
            correct = true;
          }
        }
        if (!correct) {
          getTypeCheckResult().reset();
          Log.error("0xA0298 could not calculate type at " + node.get_SourcePositionStart());
        } else {
          result =
              SymTypeExpressionFactory.createGenerics(
                  getTypeCheckResult().getResult().getTypeInfo());
          getTypeCheckResult().reset();
        }
      }
    } else {
      getTypeCheckResult().reset();
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
      if (!getTypeCheckResult().isPresentResult()) {
        Log.error(
            "0xA0309 "
                + node.getLeft().get_SourcePositionStart()
                + " Could not calculate type of expression \""
                + OCLMill.prettyPrint(node.getLeft(), false)
                + "\" on the left side of SetComprehension ");
      } else {
        if (getTypeCheckResult().getResult().isObscureType()) {
          obscure = true;
        } else {
          leftType = getTypeCheckResult().getResult();
        }
      }
    } else if (node.getLeft().isPresentGeneratorDeclaration()) {
      leftType = node.getLeft().getGeneratorDeclaration().getSymbol().getType();
    } else {
      leftType = node.getLeft().getSetVariableDeclaration().getSymbol().getType();
    }

    if (!obscure && !leftType.isObscureType()) {

      // check that all varNames are initialized on the right side
      while (!varNames.isEmpty()) {
        for (ASTSetComprehensionItem item : node.getSetComprehensionItemList()) {
          if (item.isPresentGeneratorDeclaration()) {
            varNames.remove(item.getGeneratorDeclaration().getName());
          } else if (item.isPresentSetVariableDeclaration()) {
            varNames.remove(item.getSetVariableDeclaration().getName());
          }
        }
      }

      if (result == null) {
        result =
            SymTypeExpressionFactory.createGenerics(
                "java.util.Set", getScope(node.getEnclosingScope()));
      }
      ((SymTypeOfGenerics) result).setArgument(0, leftType);
      getTypeCheckResult().setResult(result);
    } else {
      getTypeCheckResult().reset();
      getTypeCheckResult().setResult(SymTypeExpressionFactory.createObscureType());
    }
  }

  public void traverse(ASTSetEnumeration node) {
    boolean obscure = false;
    SymTypeExpression result = null;
    SymTypeExpression innerResult = null;
    node.getMCType().accept(getTraverser());
    if (getTypeCheckResult().isPresentResult()) {
      boolean correct = false;
      for (String s : collections) {
        if (getTypeCheckResult().getResult().getTypeInfo().getName().equals(s)) {
          correct = true;
        }
      }
      if (!correct) {
        getTypeCheckResult().reset();
        Log.error(
            "0xA0298 there must be a type for collection at" + node.get_SourcePositionStart());
      } else {
        result =
            SymTypeExpressionFactory.createGenerics(getTypeCheckResult().getResult().getTypeInfo());
        getTypeCheckResult().reset();
      }
    } else {
      getTypeCheckResult().reset();
      Log.error("0xA0299 could not determine type of " + node.getClass().getName());
    }

    if (result == null) {
      result =
          SymTypeExpressionFactory.createGenerics(
              "java.util.Set", getScope(node.getEnclosingScope()));
    }

    // check type of elements in set
    for (ASTSetCollectionItem item : node.getSetCollectionItemList()) {
      if (item instanceof ASTSetValueItem) {
        ((ASTSetValueItem) item).getExpression().accept(getTraverser());
        if (getTypeCheckResult().isPresentResult()) {
          if (getTypeCheckResult().getResult().isObscureType()) {
            obscure = true;
          } else {
            if (innerResult == null) {
              innerResult = getTypeCheckResult().getResult();
              getTypeCheckResult().reset();
            } else if (!compatible(innerResult, getTypeCheckResult().getResult())) {
              LogHelper.error(node, "0xA0333", "different types in SetEnumeration");
            }
          }
        } else {
          LogHelper.error(
              node, "0xA0334", "Could not determine type of an expression in SetEnumeration");
        }
      } else {
        item.accept(getTraverser());
        if (getTypeCheckResult().isPresentResult()) {
          if (getTypeCheckResult().getResult().isObscureType()) {
            obscure = true;
          } else {
            if (innerResult == null) {
              innerResult = getTypeCheckResult().getResult();
              getTypeCheckResult().reset();
            } else if (!compatible(innerResult, getTypeCheckResult().getResult())) {
              LogHelper.error(node, "0xA0335", "different types in SetEnumeration");
            }
          }
        } else {
          LogHelper.error(
              node, "0xA0336", "Could not determine type of a SetValueRange in SetEnumeration");
        }
      }
    }

    if (!obscure) {
      ((SymTypeOfGenerics) result).setArgument(0, innerResult);
      getTypeCheckResult().setResult(result);
    } else {
      getTypeCheckResult().reset();
      getTypeCheckResult().setResult(SymTypeExpressionFactory.createObscureType());
    }
  }

  public void traverse(ASTSetValueRange node) {
    SymTypeExpression left = acceptThisAndReturnSymTypeExpression(node.getLowerBound());
    SymTypeExpression right = acceptThisAndReturnSymTypeExpression(node.getUpperBound());
    if (!left.isObscureType() && !right.isObscureType()) {
      if (!isIntegralType(left) || !isIntegralType(right)) {
        LogHelper.error(
            node, "0xA0337", "bounds in SetValueRange are not integral types, but have to be");
      }
      getTypeCheckResult().setResult(left);
    } else {
      getTypeCheckResult().reset();
      getTypeCheckResult().setResult(SymTypeExpressionFactory.createObscureType());
    }
  }

  @Override
  public SetExpressionsTraverser getTraverser() {
    return traverser;
  }

  public void setTraverser(SetExpressionsTraverser traverser) {
    this.traverser = traverser;
  }
}
