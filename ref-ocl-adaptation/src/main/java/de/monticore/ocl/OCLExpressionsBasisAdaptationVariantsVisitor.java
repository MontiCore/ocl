package de.monticore.ocl;

import de.monticore.cdconcretization.util.SymbolUtil;
import de.monticore.expressions.expressionsbasis.ExpressionsBasisAdaptationVariantsVisitor;
import de.monticore.expressions.expressionsbasis.IExpressionsBasisAdaptationVariant;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.refadaptation.BindingConflictException;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types3.TypeCheck3;

import java.util.Optional;
import java.util.Set;

/**
 * OCL-specific additions to the variant discovery for the ExpressionsBasis language.
 * This visitor resolves symbols in the original reference model of the incarnation mapping
 * to ensure that the correct symbols are used for the incarnation lookup.
 * <h5>Adaptations</h5>
 * <code>NameExpression</code>:
 * <ul>
 *   <li>Special support for NameExpressions that reference types directly, e.g. 'MyClass'
 *       representing all instances of 'MyClass' with type `Set<MyClass>`. Introduces one variant
 *       for each incarnation of the type.</li>
 * </ul>
 */
public class OCLExpressionsBasisAdaptationVariantsVisitor extends ExpressionsBasisAdaptationVariantsVisitor {

  private static final String LOG_NAME = OCLExpressionsBasisAdaptationVariantsVisitor.class.getName();

  @Override
  public void endVisit(ASTNameExpression refExpr) {
    /*
     * This is special case for OCL: OCLWithinScopeBasicSymbolsResolver returns Type 'Set<T>' if
     * a NameExpression does not reference a VariableSymbol but has the name of a CDType.
     * e.g. for expressions like: 'x in MyClass' 'MyClass' represents the set of all instances of
     * MyClass.
     * How can we detect this case?
     *   - If the expression type is a Set<T> and there is NO source symbol (especially no variable
     *     source symbol)
     *   - And if the name of the NameExpression is exactly the (simple) type name, e.g. 'MyClass'
     * Then we can create variants for all incarnations of 'MyClass'.
     */
    SymTypeExpression symTypeExpression = TypeCheck3.typeOf(refExpr);
    if (isSetType(symTypeExpression) && symTypeExpression.getSourceInfo().getSourceSymbol().isEmpty()) {
      TypeSymbol typeSymbol = symTypeExpression.asGenericType().getArgument(0).getTypeInfo();
      if (refExpr.getName().equals(typeSymbol.getName())) {
        addVariantsForTypeIncarnations(refExpr, typeSymbol);
        return;
      }
    }
    super.endVisit(refExpr);
  }

  protected void addVariantsForTypeIncarnations(ASTNameExpression refExpr, TypeSymbol refType) {
    // TODO check if typeSymbol is present in incarnation mapping
    Set<TypeSymbol> incarnations = getAdaptationContext().getBasicSymbolsIncMapping().getIncarnations(refType);
    if (incarnations.isEmpty()) {
      // TODO only pass variant upwards if the refSymbol is not defined in the inc mapping
      //  if it is defined, no incarnation is a sign that we should drop this variant
      getAdaptations4Ast().addVariant(refExpr, getAdaptationContext().createVariant());
    } else {
      // we have the incarnations which are possible in this context
      getAdaptations4Ast().addVariants(refExpr, tryCreateVariantsForIncarnations(incarnations,
              (incarnation) -> createVariantForTypeIncarnation(refExpr, refType, incarnation)));
    }
  }

  /**
   * Creates a new variant which adapts the <i>special</i> NameExpression representing the set of
   * all instances according to the given incarnation of the type symbol.
   *
   * @param refExpr the ASTFieldAccessExpression to adapt
   * @param refType the reference TypeSymbol referenced in the expression
   * @param typeIncarnation the incarnation of the TypeSymbol to adapt to
   * @return a new variant that adapts the expression
   *
   * @throws BindingConflictException if the binding conflicts with existing bindings in the context
   */
  protected IExpressionsBasisAdaptationVariant createVariantForTypeIncarnation(
          ASTNameExpression refExpr,
          TypeSymbol refType,
          TypeSymbol typeIncarnation)
          throws BindingConflictException {
    IExpressionsBasisAdaptationVariant newVariant = getAdaptationContext().createVariantForIncarnation(refType, typeIncarnation, refExpr.get_SourcePositionStart());
    newVariant.addASTAdaptation(refExpr, (adaptedNode) -> {
      adaptedNode.setName(typeIncarnation.getName());
      return adaptedNode;
    });
    return newVariant;
  }

  protected boolean isSetType(SymTypeExpression symTypeExpression) {
    return symTypeExpression.isGenericType()
            && symTypeExpression.asGenericType().getTypeInfo().getName().equals("Set");
  }

  @Override
  protected void addVariantsForVariableSymbol(ASTNameExpression refExpr, VariableSymbol variableSymbol) {
    /*
     * Some symbols in the OCL reference model are located in the OCL global scope and not some same
     * instances as the related symbols in the CD4Code scope. In order for the incarnation mapping
     * to work, we try to resolve the symbol name in the scope of the reference model.
     * If it is found, we use the symbol from the reference model if no we proceed with the
     * original symbol as there could still be bindings defined for.it.
     */
    Optional<VariableSymbol> cd4cTranslatedSymbolOpt = getAdaptationContext()
            .getOriginalBasicSymbolsIncMapping().getReferenceScope()
            .resolveVariableDown(SymbolUtil.getFullNameWithoutCD(variableSymbol));
    if (cd4cTranslatedSymbolOpt.isPresent()) {
      super.addVariantsForVariableSymbol(refExpr, cd4cTranslatedSymbolOpt.get());
    } else {
      // normal handling as defined for ExpressionsBasis language
      super.addVariantsForVariableSymbol(refExpr, variableSymbol);
    }
  }

  @Override
  protected void addVariantsForFunctionSymbol(ASTNameExpression refExpr, FunctionSymbol refFunctionSymbol) {
    Optional<FunctionSymbol> cd4cTranslatedSymbolOpt = getAdaptationContext()
            .getOriginalBasicSymbolsIncMapping().getReferenceScope()
            .resolveFunctionDown(SymbolUtil.getFullNameWithoutCD(refFunctionSymbol));
    if (cd4cTranslatedSymbolOpt.isPresent()) {
      super.addVariantsForFunctionSymbol(refExpr, cd4cTranslatedSymbolOpt.get());
    } else {
      // normal handling as defined for ExpressionsBasis language
      super.addVariantsForFunctionSymbol(refExpr, refFunctionSymbol);
    }
  }
}
