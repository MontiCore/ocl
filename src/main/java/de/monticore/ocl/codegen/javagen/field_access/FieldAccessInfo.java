package de.monticore.ocl.codegen.javagen.field_access;

import de.monticore.expressions.commonexpressions._ast.ASTFieldAccessExpression;
import de.monticore.ocl.codegen.DomainTypeUtil;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mccollectiontypes.types3.MCCollectionSymTypeRelations;
import de.monticore.types3.TypeCheck3;

public class FieldAccessInfo {
  private final FieldAccessKind kind;
  private final SymTypeExpression fieldType;   // element type for collections

  public FieldAccessInfo(
      FieldAccessKind kind,
      SymTypeExpression fieldType
  ) {
    this.kind = kind;
    this.fieldType = fieldType;
  }

  public FieldAccessKind getKind() { return kind; }
  public SymTypeExpression getFieldType() { return fieldType; }

  public static FieldAccessInfo from(ASTFieldAccessExpression node) {
    SymTypeExpression ownerType = resolveOwnerType(node);
    SymTypeExpression fieldType = null;

    boolean isCollection =
        MCCollectionSymTypeRelations.isMCCollection(ownerType)
            && !MCCollectionSymTypeRelations.isOptional(ownerType)
            && ownerType.isGenericType()
            && !ownerType.asGenericType().getArgumentList().isEmpty();

    if (isCollection) {
      fieldType = ownerType.asGenericType().getArgument(0);
    }

    boolean toManyDomain = isCollection && DomainTypeUtil.getInstance().isDomainType(fieldType);
    boolean toOneDomain  = !isCollection && DomainTypeUtil.getInstance().isDomainType(ownerType);

    if (toManyDomain) {
      return new FieldAccessInfo(
          FieldAccessKind.TO_MANY_DOMAIN,
          fieldType
      );
    }

    if (toOneDomain) {
      return new FieldAccessInfo(
          FieldAccessKind.TO_ONE_DOMAIN,
          ownerType
      );
    }

    if (isCollection) {
      return new FieldAccessInfo(
          FieldAccessKind.COLLECTION_OF_SIMPLE_VALUE,
          fieldType
      );
    }

    return new FieldAccessInfo(
        FieldAccessKind.SIMPLE_VALUE,
        ownerType
    );
  }

  private static SymTypeExpression resolveOwnerType(ASTFieldAccessExpression node) {
    SymTypeExpression exprType = TypeCheck3.typeOf(node.getExpression());

    if (exprType.isIntersectionType()) {
      var parts = exprType.asIntersectionType().getIntersectedTypeSet();
      if (!parts.isEmpty()
          && parts.stream()
          .map(SymTypeExpression::printFullName)
          .distinct()
          .count() == 1) {

        var owner = parts.iterator().next();
        if (owner.isObjectType()) {
          var varSymOpt = owner.asObjectType()
              .getTypeInfo()
              .getSpannedScope()
              .resolveVariable(node.getName());

          if (varSymOpt.isPresent()) {
            return varSymOpt.get().getType();
          }
        }
      }
    }

    return TypeCheck3.typeOf(node);
  }

}
