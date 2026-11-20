// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types3.util;

import de.monticore.symboltable.modifiers.AccessModifier;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeOfGenerics;
import de.monticore.types3.util.TypeCheck3NameHandler;
import de.monticore.types3.util.TypeContextCalculator;

import java.util.Optional;

import static de.monticore.types.mccollectiontypes.types3.util.MCCollectionSymTypeFactory.createSet;

/**
 * In OCL, a type identifier can be used as the set of its instances,
 * e.g., Person.name is the set of the names of all persons.
 */
public class OCLTypeCheck3NameHandler extends TypeCheck3NameHandler {

  @Override
  protected Optional<SymTypeExpression> calculateMCTypeStaticAccess(
      TypeCheck3NameHandlerASTInput astInput,
      SymTypeExpression innerMCType,
      boolean resultsAreOptional
  ) {
    Optional<SymTypeExpression> type =
        super.calculateMCTypeStaticAccess(astInput, innerMCType, resultsAreOptional);
    if (type.isEmpty() && !resultsAreOptional) {
      SymTypeOfGenerics innerAsSetOfInstances = createSet(innerMCType);
      // hacky, will break in some cases:
      innerAsSetOfInstances.getSourceInfo().setSourceSymbol(innerMCType.getTypeInfo());
      AccessModifier accessModifier = TypeContextCalculator.getAccessModifier(
          innerAsSetOfInstances.getTypeInfo(), astInput.getEnclosingScope()
      );
      type = resolveVariablesAndFunctionsWithinType(
          innerAsSetOfInstances,
          astInput.getNameParts().getLast(),
          accessModifier,
          v -> true,
          f -> true
      );
    }
    return type;
  }

  public static void init() {
    setDelegate(new OCLTypeCheck3NameHandler());
  }

}
