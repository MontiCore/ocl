package de.monticore.ocl.ocl._symboltable;

import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symboltable.IScopeSpanningSymbol;
import de.monticore.symboltable.modifiers.AccessModifier;
import de.monticore.types.check.SymTypeExpression;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Predicate;

/**
 * Add Inheritance Information to typecheck. Methods and Variables from superclass should be usable
 * in OCL Expression.
 */
public class OCLScope extends OCLScopeTOP {
  public OCLScope() {
    super();
  }

  public OCLScope(boolean b) {
    super(b);
  }

  private static <E> List<E> concatIfNew(
      List<E> first, List<E> second, BiFunction<E, E, Boolean> equal) {
    List<E> result = new ArrayList<>(first);
    second.forEach(
        elem -> {
          if (result.stream().noneMatch(e -> equal.apply(e, elem))) {
            result.add(elem);
          }
        });
    return result;
  }

  @Override
  public List<FunctionSymbol> resolveFunctionLocallyMany(
      boolean foundSymbols,
      String name,
      AccessModifier modifier,
      Predicate<FunctionSymbol> predicate) {
    // resolve methods by using overridden method
    List<FunctionSymbol> superFunctions = new ArrayList<>();
    {
      if (this.isPresentSpanningSymbol()) {
        IScopeSpanningSymbol spanningSymbol = getSpanningSymbol();
        // if the methodsymbol is in the spanned scope of a typesymbol then look for method in super
        // types too
        if (spanningSymbol instanceof TypeSymbol) {
          TypeSymbol typeSymbol = ((TypeSymbol) spanningSymbol);
          for (SymTypeExpression t : typeSymbol.getSuperTypesList()) {
            superFunctions.addAll(t.getMethodList(name, false));
          }
        }
      }
    }

    List<FunctionSymbol> thisFunctions =
        super.resolveFunctionLocallyMany(foundSymbols, name, modifier, predicate);

    // Filter duplicate methods, e.g. "add" from List and "add" from Collection. The more abstract
    // method is removed
    return concatIfNew(
        thisFunctions, superFunctions, (fst, snd) -> fst.getName().equals(snd.getName()));
  }

  /* @Override
  public List<VariableSymbol> resolveVariableLocallyMany(
      boolean foundSymbols, String name, AccessModifier modifier, Predicate predicate) {
    // resolve methods by using overridden method
    List<VariableSymbol> result =
        super.resolveVariableLocallyMany(foundSymbols, name, modifier, predicate);
    if (this.isPresentSpanningSymbol()) {
      IScopeSpanningSymbol spanningSymbol = getSpanningSymbol();
      // if the fieldsymbol is in the spanned scope of a typesymbol then look for method in super
      // types too
      if (spanningSymbol instanceof TypeSymbol) {
        TypeSymbol typeSymbol = (TypeSymbol) spanningSymbol;
        for (SymTypeExpression superType : typeSymbol.getSuperTypesList()) {
          result.addAll(superType.getFieldList(name, false));
        }
      }
    }
    return result;
  }*/
}
