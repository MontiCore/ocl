// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3.util;

import de.monticore.ocl.types3.OCLCollectionSymTypeRelations;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symboltable.modifiers.AccessModifier;
import de.monticore.symboltable.modifiers.StaticAccessModifier;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types3.util.OOWithinTypeBasicSymbolsResolver;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;
import java.util.function.Predicate;

import static de.monticore.types.mccollectiontypes.types3.util.MCCollectionSymTypeFactory.createSet;

public class OCLWithinTypeBasicSymbolsResolver extends OOWithinTypeBasicSymbolsResolver {

  public static void init() {
    Log.trace("init OOWithinTypeBasicSymbolsResolver", "TypeCheck setup");
    setDelegate(new OCLWithinTypeBasicSymbolsResolver());
  }

  @Override
  protected Optional<SymTypeExpression> _getTypeAsExpression(
      SymTypeExpression thisType,
      AccessModifier accessModifier
  ) {
    SymTypeOfGenerics thisAsSet = createSet(thisType);
    // hacky, will break in some cases:
    thisAsSet.getSourceInfo().setSourceSymbol(thisType.getTypeInfo());
    return Optional.of(thisAsSet);
  }

  /**
   * handles "MyClass" being a type identifier AND a Set of MyClass
   */
  @Override
  protected Optional<SymTypeExpression> _resolveVariable(
      SymTypeExpression thisType,
      String name,
      AccessModifier accessModifier,
      Predicate<VariableSymbol> predicate) {
    // case "normal" variable
    Optional<SymTypeExpression> resolvedSymType =
        super._resolveVariable(thisType, name, accessModifier, predicate);
    // case thisType is a type identifier (not checked correctly for now...)
    // and thisType.name is another type identifier
    // create the Set of elements of thisType.name
    if (resolvedSymType.isEmpty() && StaticAccessModifier.STATIC.includes(accessModifier)) {
      Optional<SymTypeExpression> typeId = resolveType(thisType, name, accessModifier, t -> true);
      if (typeId.isPresent()) {
        SymTypeOfGenerics typeIdAsSet = createSet(typeId.get().deepClone());
        // hacky, will break in some cases:
        typeIdAsSet.getSourceInfo().setSourceSymbol(typeId.get().getTypeInfo());
        resolvedSymType = Optional.of(typeIdAsSet);
      }
    }
    if (resolvedSymType.isEmpty() && OCLCollectionSymTypeRelations.isOCLCollection(thisType)) {
      SymTypeExpression elementThisType =
          OCLCollectionSymTypeRelations.getCollectionElementType(thisType);
      Optional<SymTypeExpression> elementResolvedSymType =
          resolveVariable(elementThisType, name, accessModifier, predicate);
      if (elementResolvedSymType.isPresent()) {
        // case thisType is a Set and we instead we pretend it is not and get the static variable instead.
        // todo OCL needs proper name-chain-traversal if A~>Set<A> is to be supported properly (which is not part of standard OCL(? needs double checking))
        if (!StaticAccessModifier.NON_STATIC.includes(accessModifier)) {
          resolvedSymType = elementResolvedSymType;
        }
        else {
          // todo order correct? outer/inner coll type
          SymTypeOfGenerics unFlattenedSymType = (SymTypeOfGenerics) thisType.deepClone();
          unFlattenedSymType.setArgument(0, elementResolvedSymType.get());
          // need to flatten, as this is following an association
          resolvedSymType = Optional.of(OCLCollectionSymTypeRelations.flatten(unFlattenedSymType));
          // add the original source info to the flattened symType
          resolvedSymType.ifPresent(
              s -> s._internal_setSourceInfo(elementResolvedSymType.get()
                  .getSourceInfo())
          );
        }
      }
    }
    return resolvedSymType;
  }
}
