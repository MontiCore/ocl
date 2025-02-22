// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3.util;

import de.monticore.ocl.types3.OCLSymTypeRelations;
import de.monticore.symbols.basicsymbols._symboltable.IBasicSymbolsScope;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symboltable.modifiers.AccessModifier;
import de.monticore.symboltable.modifiers.StaticAccessModifier;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types.mccollectiontypes.types3.util.MCCollectionSymTypeFactory;
import de.monticore.types3.util.OOWithinTypeBasicSymbolsResolver;
import de.se_rwth.commons.logging.Log;
import java.util.Optional;
import java.util.function.Predicate;

public class OCLWithinTypeBasicSymbolsResolver extends OOWithinTypeBasicSymbolsResolver {

  public static void init() {
    Log.trace("init OOWithinTypeBasicSymbolsResolver", "TypeCheck setup");
    setDelegate(new OCLWithinTypeBasicSymbolsResolver());
  }

  /**
   * handles "MyClass" being a type identifier AND a Set of MyClass s.a. {@link
   * OCLNameExpressionTypeCalculator#typeOfNameAsExpr(IBasicSymbolsScope, String)}
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
        resolvedSymType =
            Optional.of(MCCollectionSymTypeFactory.createSet(typeId.get().deepClone()));
      }
    }
    // case thisType is a Set and we follow an association with multiplicity > 1
    // todo what about Optionals? same with flatten
    //  -> could be added? but should they?
    if (resolvedSymType.isEmpty() && OCLSymTypeRelations.isOCLCollection(thisType)) {
      SymTypeExpression elementThisType = OCLSymTypeRelations.getCollectionElementType(thisType);
      Optional<SymTypeExpression> elementResolvedSymType =
          resolveVariable(elementThisType, name, accessModifier, predicate);
      if (elementResolvedSymType.isPresent()) {
        // todo order correct? outer/inner coll type
        SymTypeOfGenerics unFlattenedSymType = (SymTypeOfGenerics) thisType.deepClone();
        unFlattenedSymType.setArgument(0, elementResolvedSymType.get());
        // need to flatten, as this is following an association
        resolvedSymType = Optional.of(OCLSymTypeRelations.flatten(unFlattenedSymType));
      }
    }
    return resolvedSymType;
  }
}
