/**
 * ******************************************************************************
 *  MontiCAR Modeling Family, www.se-rwth.de
 *  Copyright (c) 2017, Software Engineering Group at RWTH Aachen,
 *  All rights reserved.
 *
 *  This project is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version.
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * *******************************************************************************
 */
package ocl.monticoreocl.ocl._types;

import de.monticore.ast.ASTNode;
import de.monticore.commonexpressions._ast.*;
import de.monticore.expressionsbasis._ast.ASTExpression;
import de.monticore.literals.literals._ast.*;
import de.monticore.numberunit._ast.ASTNumberWithUnit;
import de.monticore.numberunit.prettyprint.NumberUnitPrettyPrinter;
import de.monticore.numberunit.prettyprint.UnitsPrinter;
import de.monticore.oclexpressions._ast.*;
import de.monticore.prettyprint.IndentPrinter;
import de.monticore.setexpressions._ast.ASTIsInExpression;
import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.Scope;
import de.monticore.symboltable.types.references.ActualTypeArgument;
import de.monticore.types.TypesPrinter;
import de.monticore.umlcd4a.symboltable.*;
import de.monticore.umlcd4a.symboltable.references.CDTypeSymbolReference;
import de.se_rwth.commons.logging.Log;
import ocl.monticoreocl.ocl._ast.ASTOCLNonNumberPrimary;
import ocl.monticoreocl.ocl._symboltable.OCLVariableDeclarationSymbol;
import ocl.monticoreocl.ocl._visitor.OCLVisitor;

import javax.measure.unit.Unit;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This visitor tries to infer the return type of an ocl expression
 */
public class OCLExpressionTypeInferingVisitor implements OCLVisitor {

  private CDTypeSymbolReference returnTypeRef;
  private OCLVisitor realThis = this;
  private MutableScope scope;
  private Optional<Unit<?>> returnUnit;

  public OCLExpressionTypeInferingVisitor(MutableScope scope) {
    this.returnTypeRef = null;
    this.scope = scope;
    this.returnUnit = Optional.empty();
  }

  public CDTypeSymbolReference getTypeFromExpression(ASTExpression node) {
    node.accept(realThis);
    if (returnTypeRef == null) {
      Log.error("0xOCLI0 The variable type could not be resolved from this expression: " + node.get_SourcePositionStart(), node.get_SourcePositionStart(), node.get_SourcePositionEnd());
      return new CDTypeSymbolReference("Class", scope);
    }
    else {
      return returnTypeRef;
    }
  }

  public CDTypeSymbolReference getTypeFromExpression(ASTOCLComprehensionExpr node) {
    node.accept(realThis);
    if (returnTypeRef == null) {
      return new CDTypeSymbolReference("Class", scope);
    }
    else {
      return returnTypeRef;
    }
  }

  public CDTypeSymbolReference getReturnTypeReference() {
    return returnTypeRef;
  }

  public Optional<Unit<?>> getReturnUnit() {
    return returnUnit;
  }

  private CDTypeSymbolReference createTypeRef(String typeName, ASTNode node) {
    // map int to Integer , etc.
    typeName = CDTypes.primitiveToWrapper(typeName);
    CDTypeSymbolReference typeReference = new CDTypeSymbolReference(typeName, this.scope);
    typeReference.setStringRepresentation(typeName);
    // Check if type was found in CD loaded CD models
    if (!typeReference.existsReferencedSymbol()) {
      Log.error("0xOCLI9 This type could not be found: " + typeName + " at " + node.get_SourcePositionStart(), node.get_SourcePositionStart(), node.get_SourcePositionEnd());
    }
    return typeReference;
  }

  /**
   * ********** traverse methods **********
   */

  @Override
  public void traverse(ASTIsInExpression node) {
    returnTypeRef = createTypeRef("Boolean", node);
  }

  @Override
  public void traverse(ASTForallExpr node) {
    returnTypeRef = createTypeRef("Boolean", node);
  }

  @Override
  public void traverse(ASTExistsExpr node) {
    returnTypeRef = createTypeRef("Boolean", node);
  }

  @Override
  public void traverse(ASTInstanceOfExpression node) {
    returnTypeRef = createTypeRef("Boolean", node);
  }

  @Override
  public void traverse(ASTOCLNonNumberPrimary node) {
    node.getValue().accept(realThis);
  }

  @Override
  public void traverse(ASTTypeIfExpr node) {
    node.getElseExpressionPart().accept(realThis);
  }

  @Override
  public void traverse(ASTBooleanLiteral node) {
    returnTypeRef = createTypeRef("Boolean", node);
  }

  @Override
  public void traverse(ASTIntLiteral node) {
    returnTypeRef = createTypeRef("Integer", node);
  }

  @Override
  public void traverse(ASTNullLiteral node) {
    returnTypeRef = createTypeRef("Object", node);
  }

  @Override
  public void traverse(ASTDoubleLiteral node) {
    returnTypeRef = createTypeRef("Double", node);
  }

  @Override
  public void traverse(ASTFloatLiteral node) {
    returnTypeRef = createTypeRef("Float", node);
  }

  @Override
  public void traverse(ASTNumberWithUnit node) {
    if (node.isPresentUn()) {
      NumberUnitPrettyPrinter printer = new NumberUnitPrettyPrinter(new IndentPrinter());
      printer.prettyprint(node.getUn());
      String unitString = printer.getPrinter().getContent();

      CDTypeSymbolReference amountType = createTypeRef("Number", node);
      returnUnit = Optional.of(Unit.valueOf(unitString));
      CDTypeSymbolReference returnUnitRef = createTypeRef(UnitsPrinter.unitToUnitName(returnUnit.get()), node);
      TypeInferringHelper.addActualArgument(amountType, returnUnitRef);
      returnTypeRef = amountType;

    }
  }

  @Override
  public void traverse(ASTStringLiteral node) {
    returnTypeRef = createTypeRef("String", node);
  }

  @Override
  public void traverse(ASTCharLiteral node) {
    returnTypeRef = createTypeRef("char", node);
  }

  @Override
  public void traverse(ASTParenthizedExpression node) {
    OCLExpressionTypeInferingVisitor innerVisitor = new OCLExpressionTypeInferingVisitor(scope);
    returnTypeRef = innerVisitor.getTypeFromExpression(node.getExpression());
    returnUnit = innerVisitor.getReturnUnit();
    if (node.isPresentQualification()) {
      node.getQualification().accept(realThis);
    }
  }

  @Override
  public void traverse(ASTIfThenElseExpr node) {
    node.getThenExpressionPart().accept(realThis);
  }

  @Override
  public void traverse(ASTConditionalExpression node) {
    node.getTrueExpression().accept(realThis);
  }

  @Override
  public void traverse(ASTOCLQualifiedPrimary node) {
    LinkedList<String> names = new LinkedList<>(node.getNameList());

    CDTypeSymbolReference firstType = handlePrefixName(node, names);
    if (firstType.isEnum())
      returnTypeRef = firstType;
    else
      returnTypeRef = handleNames(names, firstType, node);

    if (node.isPresentPostfixQualification()) {
      node.getPostfixQualification().accept(realThis);
    }

    // process following primaries
    if (node.isPresentOCLQualifiedPrimary()) {
      node.getOCLQualifiedPrimary().accept(realThis);
    }
  }

  @Override
  public void traverse(ASTOCLTransitivQualification node) {
    CDTypeSymbolReference setType = createTypeRef("Set", node);
    TypeInferringHelper.addActualArgument(setType, returnTypeRef);
    setType = TypeInferringHelper.flattenOnce(setType);
    returnTypeRef = setType;
  }

  @Override
  public void traverse(ASTOCLArrayQualification node) {
    List<ActualTypeArgument> arguments = returnTypeRef.getActualTypeArguments();
    if (arguments.size() == 0) {
      Log.error("0xOCLI4 Could not resolve container argument from: " + returnTypeRef + " at " + node.get_SourcePositionStart(), node.get_SourcePositionStart(), node.get_SourcePositionEnd());
    }
    returnTypeRef = (CDTypeSymbolReference) arguments.get(0).getType();
  }

  @Override
  public void traverse(ASTOCLArgumentQualification node) {
  }

  @Override
  public void traverse(ASTOCLComprehensionPrimary node) {
    String typeName;
    if (node.isPresentType()) {
      typeName = TypesPrinter.printType(node.getType());
    }
    else {
      typeName = "Collection";
    }
    returnTypeRef = createTypeRef(typeName, node);

    if (node.isPresentExpression()) {

      OCLExpressionTypeInferingVisitor exprVisitor = new OCLExpressionTypeInferingVisitor(scope);
      CDTypeSymbolReference innerType = exprVisitor.getTypeFromExpression(node.getExpression());

      if (!innerType.getName().equals("Class")) // Only add when innerType is present
        TypeInferringHelper.addActualArgument(returnTypeRef, innerType);
    }

    if (node.isPresentQualification()) {
      node.getQualification().accept(realThis);
    }
  }

  @Override
  public void traverse(ASTOCLComprehensionExpressionStyle node) {
    node.getExpression().accept(realThis);
  }

  @Override
  public void traverse(ASTOCLComprehensionEnumerationStyle node) {
    if (!node.getOCLCollectionItemList().isEmpty()) {
      node.getOCLCollectionItem(0).getExpression(0).accept(realThis);
    }

  }

  @Override
  public void traverse(ASTInExpr node) {
    if (node.isPresentType()) {
      String typeName = TypesPrinter.printType(node.getType());
      returnTypeRef = createTypeRef(typeName, node);
    }
    else if (node.isPresentExpression()) {
      OCLExpressionTypeInferingVisitor exprVisitor = new OCLExpressionTypeInferingVisitor(scope);
      CDTypeSymbolReference containerType = exprVisitor.getTypeFromExpression(node.getExpression());

      if (containerType.getActualTypeArguments().size() == 0) {
        Log.error("0xOCLI5 Could not resolve inner type from InExpression, " + node.getVarNameList() + " in " + containerType + " at " + node.get_SourcePositionStart(), node.get_SourcePositionStart(), node.get_SourcePositionEnd());
      }
      else {
        returnTypeRef = containerType;
      }
    }
  }

  /**
   * ********** math expressions **********
   */

  @Override
  public void traverse(ASTPlusExpression node) {
    node.getLeftExpression().accept(realThis);
  }

  @Override
  public void traverse(ASTMinusExpression node) {
    node.getLeftExpression().accept(realThis);
  }

  @Override
  public void traverse(ASTModuloExpression node) {
    node.getLeftExpression().accept(realThis);
  }

  @Override
  public void traverse(ASTDivideExpression node) {
    OCLExpressionTypeInferingVisitor leftVisitor = new OCLExpressionTypeInferingVisitor(scope);
    CDTypeSymbolReference leftType = leftVisitor.getTypeFromExpression(node.getLeftExpression());
    OCLExpressionTypeInferingVisitor rightVisitor = new OCLExpressionTypeInferingVisitor(scope);
    CDTypeSymbolReference rightType = rightVisitor.getTypeFromExpression(node.getRightExpression());
    CDTypeSymbolReference amountType = createTypeRef("Number", node);
    CDTypeSymbolReference numberType = createTypeRef("Number", node);

    if (leftType.getName().equals("Number") && rightType.getName().equals("Number")) {
      returnTypeRef = numberType;
    }
    else if (isImplementing(leftType, numberType) && isImplementing(rightType, numberType)) {
      if (leftType.getName().equals(rightType.getName())) {
        returnTypeRef = createTypeRef(leftType.getName(), node);
      }
      else {
        returnTypeRef = numberType;
      }
    }
    else if (amountType.isSameOrSuperType(leftType) && amountType.isSameOrSuperType(rightType)) {
      Unit<?> leftUnit = leftVisitor.getReturnUnit().orElse(Unit.ONE);
      Unit<?> rightUnit = rightVisitor.getReturnUnit().orElse(Unit.ONE);
      returnUnit = Optional.of(leftUnit.divide(rightUnit));
      CDTypeSymbolReference returnUnitRef = createTypeRef(UnitsPrinter.unitToUnitName(returnUnit.get()), node);
      TypeInferringHelper.addActualArgument(amountType, returnUnitRef);
      returnTypeRef = amountType;
    }
  }

  // returns true if case could be handled
  protected boolean handleDoubleFloatInteger(CDTypeSymbolReference leftType, CDTypeSymbolReference rightType, ASTMultExpression node) {
    if (leftType.getName().equals("Double") && rightType.getName().equals("Double")) {
      returnTypeRef = createTypeRef("Double", node);
    }
    else if (leftType.getName().equals("Integer") && rightType.getName().equals("Integer")) {
      returnTypeRef = createTypeRef("Integer", node);
    }
    else if (leftType.getName().equals("Float") && rightType.getName().equals("Float")) {
      returnTypeRef = createTypeRef("Float", node);
    }
    else if (leftType.getName().equals("Double") && (rightType.getName().equals("Integer") || rightType.getName().equals("Float"))) {
      returnTypeRef = createTypeRef("Double", node);
    }
    else if ((leftType.getName().equals("Integer") || leftType.getName().equals("Float")) && rightType.getName().equals("Double")) {
      returnTypeRef = createTypeRef("Double", node);
    }
    else if ((leftType.getName().equals("Integer")) && rightType.getName().equals("Float")) {
      returnTypeRef = createTypeRef("Float", node);
    }
    else if ((leftType.getName().equals("Float")) && rightType.getName().equals("Integer")) {
      returnTypeRef = createTypeRef("Float", node);
    } else {
      return false;
    }
    return true;
  }

  @Override
  public void traverse(ASTMultExpression node) {
    OCLExpressionTypeInferingVisitor leftVisitor = new OCLExpressionTypeInferingVisitor(scope);
    CDTypeSymbolReference leftType = leftVisitor.getTypeFromExpression(node.getLeftExpression());
    OCLExpressionTypeInferingVisitor rightVisitor = new OCLExpressionTypeInferingVisitor(scope);
    CDTypeSymbolReference rightType = rightVisitor.getTypeFromExpression(node.getLeftExpression());
    CDTypeSymbolReference amountType = createTypeRef("Number", node);
    CDTypeSymbolReference numberType = createTypeRef("Number", node);

    if (!handleDoubleFloatInteger(leftType, rightType, node)) {
      if (isImplementing(leftType, numberType) && isImplementing(rightType, numberType)) {
        if (leftType.getName().equals(rightType.getName())) {
          returnTypeRef = createTypeRef(leftType.getName(), node);
        } else {
          returnTypeRef = createTypeRef("Number", node);
        }
      } else if (amountType.isSameOrSuperType(leftType) && amountType.isSameOrSuperType(rightType)) {
        Unit<?> leftUnit = leftVisitor.getReturnUnit().orElse(Unit.ONE);
        Unit<?> rightUnit = rightVisitor.getReturnUnit().orElse(Unit.ONE);
        returnUnit = Optional.of(leftUnit.times(rightUnit));
        CDTypeSymbolReference returnUnitRef = createTypeRef(UnitsPrinter.unitToUnitName(returnUnit.get()), node);
        TypeInferringHelper.addActualArgument(amountType, returnUnitRef);
        returnTypeRef = amountType;
      }
    }
  }

  /**
   * ********** boolean expressions **********
   */

  @Override
  public void traverse(ASTEqualsExpression node) {
    returnTypeRef = createTypeRef("boolean", node);
  }

  @Override
  public void traverse(ASTBooleanNotExpression node) {
    returnTypeRef = createTypeRef("boolean", node);
  }

  @Override
  public void traverse(ASTLogicalNotExpression node) {
    returnTypeRef = createTypeRef("boolean", node);
  }

  @Override
  public void traverse(ASTEquivalentExpression node) {
    returnTypeRef = createTypeRef("boolean", node);
  }

  @Override
  public void traverse(ASTLessEqualExpression node) {
    returnTypeRef = createTypeRef("boolean", node);
  }

  @Override
  public void traverse(ASTGreaterEqualExpression node) {
    returnTypeRef = createTypeRef("boolean", node);
  }

  @Override
  public void traverse(ASTLessThanExpression node) {
    returnTypeRef = createTypeRef("boolean", node);
  }

  @Override
  public void traverse(ASTGreaterThanExpression node) {
    returnTypeRef = createTypeRef("boolean", node);
  }

  @Override
  public void traverse(ASTNotEqualsExpression node) {
    returnTypeRef = createTypeRef("boolean", node);
  }

  @Override
  public void traverse(ASTBooleanAndOpExpression node) {
    returnTypeRef = createTypeRef("boolean", node);
  }

  @Override
  public void traverse(ASTBooleanOrOpExpression node) {
    returnTypeRef = createTypeRef("boolean", node);
  }

  @Override
  public void traverse(ASTImpliesExpression node) {
    returnTypeRef = createTypeRef("boolean", node);
  }

  /**
   * ********** Handle Methods **********
   */

  private CDTypeSymbolReference handlePrefixName(ASTOCLQualifiedPrimary node, LinkedList<String> names) {
    // Try and look if name or this was declared as variable or try as ClassName of CD
    String prefixName = names.get(0);

    Optional<OCLVariableDeclarationSymbol> nameDecl = scope.resolve(prefixName, OCLVariableDeclarationSymbol.KIND);
    Optional<OCLVariableDeclarationSymbol> thisDecl = scope.resolve("this", OCLVariableDeclarationSymbol.KIND);
    Optional<CDTypeSymbol> typeName = scope.resolve(prefixName, CDTypeSymbol.KIND);

    if (!nameDecl.isPresent()) {
      // try to resolve enum
      Optional<CDTypeSymbol> enum1 = scope.resolve(names.subList(0, names.size() - 1).stream().collect(Collectors.joining(".")), CDTypeSymbol.KIND);
      if (enum1.isPresent() && enum1.get().isEnum()) {
        if (enum1.get().getEnumConstants().stream().map(e -> e.getName()).anyMatch(s -> s.equals(names.getLast()))) {
          return new CDTypeSymbolReference(enum1.get().getName(), enum1.get().getEnclosingScope());
        }
        else if (!names.getLast().isEmpty()) {
          Log.error(String.format("0xOCLI6 Could not resolve enum item `%s` of enumeration type `%s` at %s %s.", names.getLast(), enum1.get().getFullName(),
              node.get_SourcePositionStart(), node.get_SourcePositionEnd()), node.get_SourcePositionStart());
        } else {
          return new CDTypeSymbolReference("Class", scope); // for enum1 instanceof EnumX
        }
      }
    }

    CDTypeSymbolReference typeRef;
    if (returnTypeRef != null) { //Previous Type present from prefix-qualification
      typeRef = returnTypeRef;
    }
    else if (nameDecl.isPresent()) { // firstName as defined variable
      names.pop();
      typeRef = nameDecl.get().getType();
      returnUnit = nameDecl.get().getUnit();
    }
    else if (typeName.isPresent()) { // Class same as Class.allInstances()
      names.pop();
      typeRef = createTypeRef("Set", node);
      CDTypeSymbolReference argsTypeRef = createTypeRef(prefixName, node);
      TypeInferringHelper.addActualArgument(typeRef, argsTypeRef);
    }
    else if (thisDecl.isPresent()) { // implicit this
      typeRef = thisDecl.get().getType();
      returnUnit = thisDecl.get().getUnit();
    }
    else {
      Log.error("0xOCLI2 Could not resolve name or type: " + prefixName + " at " + node.get_SourcePositionStart(), node.get_SourcePositionStart(), node.get_SourcePositionEnd());
      typeRef = new CDTypeSymbolReference("Class", scope);
    }
    return typeRef;
  }

  /**
   * Takes a chain of names and recursively traces back the return type: Class.field.association.method().
   * E.g. Auction.members.size() -> Set<int>
   * Implicit flattening is used: E.g a type of List<List<Person>>> is also looked at as List<Person>
   */
  private CDTypeSymbolReference handleNames(LinkedList<String> names, CDTypeSymbolReference previousType, ASTNode node) {
    if (!names.isEmpty()) {
      String name = names.pop();

      // Try name as method/field/assoc
      Scope elementsScope = previousType.getSpannedScope();
      Optional<CDTypeSymbolReference> newType = handleName(node, name, elementsScope, previousType);

      //If it failed try implicit flattening
      if (!newType.isPresent()) {
        CDTypeSymbolReference flattendType = TypeInferringHelper.flattenAll(previousType);
        elementsScope = flattendType.getSpannedScope();
        newType = handleName(node, name, elementsScope, flattendType);
        // If it succeeded add container from previous type around it
        if (newType.isPresent()) {

          Optional<CDMethodSymbol> methodSymbol = elementsScope.resolve(name, CDMethodSymbol.KIND);
          if (!methodSymbol.isPresent() || !methodSymbol.get().isStatic()) {
            CDTypeSymbolReference containerType = createTypeRef(previousType.getName(), node);
            TypeInferringHelper.addActualArgument(containerType, newType.get());
            // implicit flattening with . operator
            containerType = TypeInferringHelper.flattenOnce(containerType);
            newType = Optional.of(containerType);
          }

        }
      }

      if (!newType.isPresent()) {
        Log.error("0xOCLI3 Could not resolve field/method/association: " + name + " on " + previousType.getStringRepresentation() + " at " + node.get_SourcePositionStart(), node.get_SourcePositionStart(), node.get_SourcePositionEnd());
        return createTypeRef("Class", node);
      }

      return handleNames(names, newType.get(), node);
    }
    else {
      return previousType;
    }
  }

  /**
   * Takes a single name and tries to resolve it as association/field/method on a scope
   */
  private Optional<CDTypeSymbolReference> handleName(ASTNode node, String name, Scope elementsScope, CDTypeSymbolReference typeSymbolReference) {
    Optional<CDFieldSymbol> fieldSymbol = elementsScope.resolve(name, CDFieldSymbol.KIND);
    Optional<CDAssociationSymbol> associationSymbol = resolveAssociationSymbol(typeSymbolReference, name);
    Optional<CDMethodSymbol> methodSymbol = elementsScope.resolve(name, CDMethodSymbol.KIND);

    if (fieldSymbol.isPresent()) { // Try name as field
      return Optional.of(createTypeRef(fieldSymbol.get().getType().getName(), node));
    }
    else if (associationSymbol.isPresent()) { // Try name as association
      return Optional.of(handleAssociationSymbol(node, associationSymbol.get(), name));
    }
    else if (methodSymbol.isPresent()) { // Try name as method
      if ((name.equals("add") || name.equals("addAll")) && typeSymbolReference.hasSuperType("Collection")) {
        return Optional.of(typeSymbolReference); // CD4A does not support generics
      }
      return Optional.of(createTypeRef(methodSymbol.get().getReturnType().getName(), node));
    }
    else {
      return Optional.empty();
    }
  }

  /**
   * [*] Auction (auctions) -> (bidder) Person [*];
   * If this is Auction then bidder is added as role name
   * If this is Person then auctions is added as role name
   * Purpose: Auction.bidder and Person.auctions can be resolved
   * [*] Auction -> Person [*];
   * If the role name is missing, then the the lowercase type name is used as role name
   * If this is Auction then person is added as role name
   * If this is Person then auction is added as role name
   */
  protected Optional<CDAssociationSymbol> resolveAssociationSymbol(CDTypeSymbolReference typeSymbolReference, String name) {
    boolean nameEqualsDerivedName;
    boolean nameEqualsTargetRole;
    boolean nameEqualsSourceRole;

    ArrayList<CDAssociationSymbol> matchedAssoc = new ArrayList<>();

    for (CDAssociationSymbol assoc : typeSymbolReference.getAllAssociations()) {
      nameEqualsDerivedName = assoc.getDerivedName().equals(name);
      nameEqualsTargetRole = assoc.getTargetRole().isPresent() && assoc.getTargetRole().get().equals(name);
      if (nameEqualsDerivedName || nameEqualsTargetRole) {
        matchedAssoc.add(assoc);
      }
    }


    for (CDAssociationSymbol assoc : typeSymbolReference.getAllSpecAssociations()) {
      nameEqualsDerivedName = assoc.getDerivedNameSourceRole().equals(name);
      nameEqualsSourceRole = assoc.getSourceRole().isPresent() && assoc.getSourceRole().get().equals(name);
      if (nameEqualsDerivedName || nameEqualsSourceRole) {
        // use opposite direction of association symbol
        matchedAssoc.add(assoc.getInverseAssociation());
      }
    }

    return getMostSpecificAssoc(matchedAssoc);
  }

  private Optional<CDAssociationSymbol> getMostSpecificAssoc(ArrayList<CDAssociationSymbol> matchedAssoc) {
    if (matchedAssoc.isEmpty())
      return Optional.empty();
    CDAssociationSymbol ret = matchedAssoc.get(matchedAssoc.size() - 1);
    for (int i = matchedAssoc.size() - 2; i >= 0; i--) {
      CDAssociationSymbol ass = matchedAssoc.get(i);
      if (ass.getTargetType().hasSuperTypeByFullName(ret.getTargetType().getFullName()) ||
          ass.getSourceType().hasSuperTypeByFullName(ret.getSourceType().getFullName())) {
        ret = ass;
      }
    }
    return Optional.of(ret);
  }

  private CDTypeSymbolReference handleAssociationSymbol(ASTNode node, CDAssociationSymbol associationSymbol, String roleName) {
    CDTypeSymbolReference newType;
    CDTypeSymbolReference targetType ;//= (CDTypeSymbolReference) associationSymbol.getTargetType();
    Cardinality cardinality;// = associationSymbol.getTargetCardinality();
    List<Stereotype> stereotypes = associationSymbol.getStereotypes();

    if (associationSymbol.getSourceRole().isPresent() && associationSymbol.getSourceRole().get().equals(roleName)) {
      targetType = (CDTypeSymbolReference) associationSymbol.getSourceType();
      cardinality = associationSymbol.getSourceCardinality();
    } else if (associationSymbol.getTargetRole().isPresent() && associationSymbol.getTargetRole().get().equals(roleName)) {
      targetType = (CDTypeSymbolReference) associationSymbol.getTargetType();
      cardinality = associationSymbol.getTargetCardinality();
    } else if (associationSymbol.getSourceType().getName().equals(roleName)) {
      targetType = (CDTypeSymbolReference) associationSymbol.getSourceType();
      cardinality = associationSymbol.getSourceCardinality();
    } else if (associationSymbol.getTargetType().getName().equals(roleName)) {
      targetType = (CDTypeSymbolReference) associationSymbol.getTargetType();
      cardinality = associationSymbol.getTargetCardinality();
    } else {
      targetType = (CDTypeSymbolReference) associationSymbol.getTargetType();
      cardinality = associationSymbol.getTargetCardinality();
    }

    if (cardinality.isMultiple()) {
      if (stereotypes.stream().filter(s -> s.getName().equals("ordered")).count() > 0) {
        newType = createTypeRef("List", node);
      }
      else {
        newType = createTypeRef("Collection", node);
      }
      TypeInferringHelper.addActualArgument(newType, targetType);
    }
    else if (!cardinality.isDefault()) {
      newType = createTypeRef("Optional", node);
      TypeInferringHelper.addActualArgument(newType, targetType);
    }
    else {
      newType = targetType;
    }
    return newType;
  }

  private boolean isImplementing(CDTypeSymbolReference a, CDTypeSymbolReference b) {
    List<String> interfaces = new LinkedList<>();
    a.getInterfaces().forEach(i -> interfaces.add(i.getName()));
    return interfaces.contains(b.getName());
  }

}
