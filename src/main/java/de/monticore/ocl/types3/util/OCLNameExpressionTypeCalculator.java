// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3.util;

import de.monticore.symbols.basicsymbols._symboltable.IBasicSymbolsScope;
import de.monticore.symboltable.modifiers.AccessModifier;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mccollectiontypes.types3.util.MCCollectionSymTypeFactory;
import de.monticore.types3.util.OOWithinScopeBasicSymbolsResolver;
import de.monticore.types3.util.WithinScopeBasicSymbolsResolver;
import java.util.Optional;
import java.util.function.Predicate;

/** @deprecated use OCLWithinScopeBasicSymbolResolver */
@Deprecated(forRemoval = true)
public class OCLNameExpressionTypeCalculator extends OOWithinScopeBasicSymbolsResolver {

  public static void init() {
    OCLNameExpressionTypeCalculator oclResolver = new OCLNameExpressionTypeCalculator();
    WithinScopeBasicSymbolsResolver.delegate = oclResolver;
  }

  /**
   * handles "MyClass" being a type identifier AND a Set of MyClass s.a. {@link
   * OCLWithinTypeBasicSymbolsResolver#resolveVariable(SymTypeExpression, String, AccessModifier,
   * Predicate)}
   */
  @Override
  public Optional<SymTypeExpression> _resolveNameAsExpr(
      IBasicSymbolsScope enclosingScope, String name) {
    // case "normal" expression
    Optional<SymTypeExpression> type = super._resolveNameAsExpr(enclosingScope, name);
    // case type id -> create Set of the same type
    if (type.isEmpty()) {
      Optional<SymTypeExpression> typeId = resolveType(enclosingScope, name);
      if (typeId.isPresent()) {
        type = Optional.of(MCCollectionSymTypeFactory.createSet(typeId.get().deepClone()));
      }
    }
    return type;
  }
}
