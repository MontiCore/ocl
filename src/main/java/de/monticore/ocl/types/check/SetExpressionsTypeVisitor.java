package de.monticore.ocl.types.check;

import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.setexpressions.SetExpressionsMill;
import de.monticore.ocl.setexpressions._ast.ASTGeneratorDeclaration;
import de.monticore.ocl.setexpressions._ast.ASTIntersectionExpression;
import de.monticore.ocl.setexpressions._ast.ASTSetAndExpression;
import de.monticore.ocl.setexpressions._ast.ASTSetCollectionItem;
import de.monticore.ocl.setexpressions._ast.ASTSetComprehension;
import de.monticore.ocl.setexpressions._ast.ASTSetEnumeration;
import de.monticore.ocl.setexpressions._ast.ASTSetInExpression;
import de.monticore.ocl.setexpressions._ast.ASTSetMinusExpression;
import de.monticore.ocl.setexpressions._ast.ASTSetNotInExpression;
import de.monticore.ocl.setexpressions._ast.ASTSetOrExpression;
import de.monticore.ocl.setexpressions._ast.ASTSetValueRange;
import de.monticore.ocl.setexpressions._ast.ASTSetVariableDeclaration;
import de.monticore.ocl.setexpressions._ast.ASTUnionExpression;
import de.monticore.ocl.setexpressions._visitor.SetExpressionsVisitor2;
import de.monticore.ocl.types3.IOCLSymTypeRelations;
import de.monticore.ocl.types3.OCLSymTypeRelations;
import de.monticore.ocl.types3.util.OCLCollectionSymTypeFactory;
import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types3.AbstractTypeVisitor;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static de.monticore.types.check.SymTypeExpressionFactory.createObscureType;
import static de.monticore.types.check.SymTypeExpressionFactory.createPrimitive;

public class SetExpressionsTypeVisitor extends AbstractTypeVisitor
    implements SetExpressionsVisitor2 {

  protected IOCLSymTypeRelations typeRelations;

  public SetExpressionsTypeVisitor() {
    this(new OCLSymTypeRelations());
  }

  protected SetExpressionsTypeVisitor(IOCLSymTypeRelations typeRelations) {
    this.typeRelations = typeRelations;
  }

  protected IOCLSymTypeRelations getTypeRel() {
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

  protected void calculateSetInExpression(
      ASTExpression expr,
      SymTypeExpression elemResult,
      SymTypeExpression setResult) {
    SymTypeExpression result;

    if (elemResult.isObscureType() || setResult.isObscureType()) {
      // error already logged
      result = SymTypeExpressionFactory.createObscureType();
    }
    else if (getTypeRel().isOCLCollection(setResult)) {
      SymTypeExpression setElemType = getTypeRel().getCollectionElementType(setResult);
      // it does not make any sense to ask if it is in the set
      // if it cannot be in the set
      if (getTypeRel().isSubTypeOf(elemResult, setElemType) ||
          getTypeRel().isSubTypeOf(setElemType, elemResult)) {
        result = SymTypeExpressionFactory.createPrimitive(BasicSymbolsMill.BOOLEAN);
      }
      else {
        Log.error("0xFD541 tried to check whether a "
                + elemResult.printFullName() + " is in the collection of "
                + setElemType.printFullName() + ", which is impossible",
            expr.get_SourcePositionStart(),
            expr.get_SourcePositionEnd()
        );
        result = createObscureType();
      }
    }
    else {
      Log.error("0xFD542 tried to check whether a "
              + elemResult.printFullName() + " is in "
              + setResult.printFullName() + ", which is not a collection",
          expr.get_SourcePositionStart(),
          expr.get_SourcePositionEnd()
      );
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
      ASTExpression expr,
      SymTypeExpression leftResult,
      SymTypeExpression rightResult
  ) {
    SymTypeExpression result;

    if (leftResult.isObscureType() || rightResult.isObscureType()) {
      result = createObscureType();
    }
    else if (getTypeRel().isOCLCollection(leftResult) &&
        getTypeRel().isOCLCollection(rightResult)) {
      Optional<SymTypeExpression> lub =
          getTypeRel().leastUpperBound(leftResult, rightResult);
      if (lub.isPresent()) {
        result = lub.get();
      }
      else {
        Log.error("0xFD543 could not calculate a least upper bound of "
                + leftResult.printFullName() + " and "
                + rightResult.printFullName(),
            expr.get_SourcePositionStart(),
            expr.get_SourcePositionEnd()
        );
        result = createObscureType();
      }
    }
    else {
      Log.error("0xFD544 expected two collection types, instead got "
              + leftResult.printFullName() + " and "
              + rightResult.printFullName(),
          expr.get_SourcePositionStart(),
          expr.get_SourcePositionEnd()
      );
      result = createObscureType();
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }

  @Override
  public void endVisit(ASTSetAndExpression expr) {
    SymTypeExpression setResult = getType4Ast().getPartialTypeOfExpr(expr.getSet());
    calculateLogicalSetExpression(expr, setResult);
  }

  @Override
  public void endVisit(ASTSetOrExpression expr) {
    SymTypeExpression setResult = getType4Ast().getPartialTypeOfExpr(expr.getSet());
    calculateLogicalSetExpression(expr, setResult);
  }

  protected void calculateLogicalSetExpression(
      ASTExpression expr,
      SymTypeExpression setType
  ) {
    SymTypeExpression result;
    if (setType.isObscureType()) {
      result = createObscureType();
    }
    else if (getTypeRel().isOCLCollection(setType) &&
        getTypeRel().isBoolean(getTypeRel().getCollectionElementType(setType))) {
      result = createPrimitive(BasicSymbolsMill.BOOLEAN);
    }
    else {
      Log.error("0xFD545 expected Collection of booleans, but got "
              + setType.printFullName(),
          expr.get_SourcePositionStart(),
          expr.get_SourcePositionEnd()
      );
      result = createObscureType();
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }

  @Override
  public void endVisit(ASTSetVariableDeclaration varDecl) {
    // can we check something?
    if (!varDecl.isPresentMCType() || !varDecl.isPresentExpression()) {
      return;
    }
    SymTypeExpression mCType =
        getType4Ast().getPartialTypeOfTypeId(varDecl.getMCType());
    SymTypeExpression exprType =
        getType4Ast().getPartialTypeOfExpr(varDecl.getExpression());
    ;
    // error already logged?
    if (mCType.isObscureType() || exprType.isObscureType()) {
      return;
    }
    SymTypeExpression assigneeType;
    if (varDecl.sizeDim() == 0) {
      assigneeType = mCType;
    }
    else {
      assigneeType =
          SymTypeExpressionFactory.createTypeArray(mCType, varDecl.sizeDim());
    }
    if (!getTypeRel().isCompatible(assigneeType, exprType)) {
      Log.error("0xFD547 cannot assign" + exprType.printFullName()
              + " to " + assigneeType.printFullName(),
          varDecl.get_SourcePositionStart(),
          varDecl.get_SourcePositionEnd()
      );
    }
  }

  @Override
  public void endVisit(ASTSetComprehension expr) {
    boolean isObscure = false;
    SymTypeExpression result;

    // ASTSetVariableDeclaration and ASTGeneratorDeclaration
    // have been checked already, the expressions are left
    for (ASTExpression boolExpr : expr.getSetComprehensionItemList().stream()
        .filter(SetExpressionsMill.typeDispatcher()::isASTExpression)
        .map(SetExpressionsMill.typeDispatcher()::asASTExpression)
        .collect(Collectors.toList())
    ) {
      SymTypeExpression boolExprType =
          getType4Ast().getPartialTypeOfExpr(boolExpr);
      if (boolExprType.isObscureType()) {
        isObscure = true;
      }
      else if (!getTypeRel().isBoolean(boolExprType)) {
        Log.error("0xFD554 filter expression in set comprehension "
                + "need to be Boolean expressions, but got "
                + boolExprType.printFullName(),
            boolExpr.get_SourcePositionStart(),
            boolExpr.get_SourcePositionEnd()
        );
        isObscure = true;
      }
    }

    // now we try to find the type of the collection
    if (!isObscure) {
      SymTypeExpression elementType;
      if (expr.getLeft().isPresentExpression()) {
        elementType = getType4Ast().getPartialTypeOfExpr(expr.getLeft().getExpression());
      }
      else if (expr.getLeft().isPresentGeneratorDeclaration()) {
        elementType = expr.getLeft().getGeneratorDeclaration().getSymbol().getType();
      }
      else {
        elementType = expr.getLeft().getSetVariableDeclaration().getSymbol().getType();
      }
      if (!elementType.isObscureType()) {
        if (expr.isSet()) {
          result = OCLCollectionSymTypeFactory.createSet(elementType);
        }
        else {
          result = OCLCollectionSymTypeFactory.createList(elementType);
        }
      }
      else {
        result = createObscureType();
      }
    }
    else {
      result = createObscureType();
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }

  public void endVisit(ASTGeneratorDeclaration genDecl) {
    SymTypeExpression exprType =
        getType4Ast().getPartialTypeOfExpr(genDecl.getExpression());
    if (exprType.isObscureType()) {
      return;
    }
    if (!getTypeRel().isOCLCollection(exprType)) {
      Log.error("0xFD548 expected a collection for generator declaration,"
              + " but got " + exprType.printFullName(),
          genDecl.getExpression().get_SourcePositionStart(),
          genDecl.getExpression().get_SourcePositionEnd()
      );
      return;
    }
    SymTypeExpression elementType =
        getTypeRel().getCollectionElementType(exprType);
    if (genDecl.isPresentMCType()) {
      SymTypeExpression mCType =
          getType4Ast().getPartialTypeOfTypeId(genDecl.getMCType());
      if (!mCType.isObscureType() &&
          !getTypeRel().isCompatible(mCType, elementType)
      ) {
        Log.error("0xFD549 cannot assign elements of collection of type "
                + exprType.printFullName() + " to " + mCType.printFullName(),
            genDecl.get_SourcePositionStart(),
            genDecl.get_SourcePositionEnd()
        );
      }
    }
  }

  @Override
  public void endVisit(ASTSetEnumeration expr) {
    boolean isObscure = false;
    SymTypeExpression result;

    // get all expressions within the set enumeration
    List<ASTExpression> containedExpressions = new ArrayList<>();
    for (ASTSetCollectionItem cItem : expr.getSetCollectionItemList()) {
      if (SetExpressionsMill.typeDispatcher().isASTSetValueItem(cItem)) {
        containedExpressions.add(
            SetExpressionsMill.typeDispatcher().asASTSetValueItem(cItem)
                .getExpression()
        );
      }
      else if (SetExpressionsMill.typeDispatcher().isASTSetValueRange(cItem)) {
        ASTSetValueRange valueRange = SetExpressionsMill.typeDispatcher()
            .asASTSetValueRange(cItem);
        containedExpressions.add(valueRange.getLowerBound());
        containedExpressions.add(valueRange.getUpperBound());
      }
      else {
        Log.error("0xFD550 internal error: "
                + "unexpected subtype of ASTSetCollectionItem",
            expr.get_SourcePositionStart(),
            expr.get_SourcePositionEnd()
        );
        isObscure = true;
      }
    }
    // each contained type has to be numeric
    List<SymTypeExpression> containedExprTypes = new ArrayList<>();
    for (ASTExpression containedExpr : containedExpressions) {
      SymTypeExpression containedExprType = type4Ast.getPartialTypeOfExpr(containedExpr);
      if (containedExprType.isObscureType()) {
        isObscure = true;
      }
      else if (!getTypeRel().isNumericType(containedExprType)) {
        Log.error("0xFD551 expected numeric type in set enumeration, "
                + "but got " + containedExprType.printFullName(),
            containedExpr.get_SourcePositionStart(),
            containedExpr.get_SourcePositionEnd()
        );
        isObscure = true;
      }
      containedExprTypes.add(containedExprType);
    }
    // numeric promotion of all contained expression types
    if (!isObscure) {
      SymTypeExpression promoted =
          getTypeRel().numericPromotion(containedExprTypes);
      if (expr.isSet()) {
        result = OCLCollectionSymTypeFactory.createSet(promoted);
      }
      else {
        result = OCLCollectionSymTypeFactory.createList(promoted);
      }
    }
    else {
      result = createObscureType();
    }
    getType4Ast().setTypeOfExpression(expr, result);
  }

  @Override
  public void endVisit(ASTSetValueRange expr) {
    var leftResult = getType4Ast().getPartialTypeOfExpr(expr.getLowerBound());
    var rightResult = getType4Ast().getPartialTypeOfExpr(expr.getUpperBound());
    if (!leftResult.isObscureType() && !rightResult.isObscureType()) {
      if (!getTypeRel().isIntegralType(leftResult) ||
          !getTypeRel().isIntegralType(rightResult)) {
        Log.error("0xFD217 bounds in SetValueRange "
                + "are not integral types, but have to be, got "
                + leftResult.printFullName() + " and "
                + rightResult.printFullName(),
            expr.get_SourcePositionStart(),
            expr.get_SourcePositionEnd()
        );
      }
    }
  }

}