package de.monticore.ocl.ocl.types3;

import de.monticore.expressions.bitexpressions.types3.BitExpressionsTypeVisitor;
import de.monticore.expressions.commonexpressions.types3.OCLCommonExpressionsTypeVisitor;
import de.monticore.expressions.expressionsbasis.types3.ExpressionBasisCTTIVisitor;
import de.monticore.literals.mccommonliterals.types3.MCCommonLiteralsTypeVisitor;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.oclexpressions.types3.OCLExpressionsTypeVisitor;
import de.monticore.ocl.optionaloperators.types3.OptionalOperatorsTypeVisitor;
import de.monticore.ocl.setexpressions.types3.SetExpressionsCTTIVisitor;
import de.monticore.ocl.types3.OCLSymTypeRelations;
import de.monticore.ocl.types3.util.OCLNameExpressionTypeCalculator;
import de.monticore.ocl.types3.util.OCLWithinTypeBasicSymbolsResolver;
import de.monticore.types.mcbasictypes.types3.MCBasicTypesTypeVisitor;
import de.monticore.types.mccollectiontypes.types3.MCCollectionTypesTypeVisitor;
import de.monticore.types.mcsimplegenerictypes.types3.MCSimpleGenericTypesTypeVisitor;
import de.monticore.types3.Type4Ast;
import de.monticore.types3.generics.context.InferenceContext4Ast;
import de.monticore.types3.util.MapBasedTypeCheck3;
import de.monticore.types3.util.WithinScopeBasicSymbolsResolver;
import de.monticore.types3.util.WithinTypeBasicSymbolsResolver;
import de.monticore.visitor.ITraverser;
import de.se_rwth.commons.logging.Log;

/**
 * TypeCheck3 implementation for the OCL language. After calling {@link #init()}, this
 * implementation will be available through the TypeCheck3 interface.
 */
public class OCLTypeCheck3 extends MapBasedTypeCheck3 {

  public static void init() {
    initTC3Delegate();
    OCLSymTypeRelations.init();
  }

  protected static void initTC3Delegate() {
    Log.trace("init OCLTypeCheck3", "TypeCheck setup");

    OCLTraverser traverser = OCLMill.inheritanceTraverser();
    Type4Ast type4Ast = new Type4Ast();
    InferenceContext4Ast ctx4Ast = new InferenceContext4Ast();

    // for some Visitors
    WithinTypeBasicSymbolsResolver withinTypeBasicSymbolsResolver =
        new OCLWithinTypeBasicSymbolsResolver();
    WithinScopeBasicSymbolsResolver withinScopeBasicSymbolsResolver =
        new OCLNameExpressionTypeCalculator();

    // Expressions

    BitExpressionsTypeVisitor visBitExpressions = new BitExpressionsTypeVisitor();
    visBitExpressions.setType4Ast(type4Ast);
    traverser.add4BitExpressions(visBitExpressions);

    OCLCommonExpressionsTypeVisitor visCommonExpressions = new OCLCommonExpressionsTypeVisitor();
    visCommonExpressions.setWithinTypeBasicSymbolsResolver(withinTypeBasicSymbolsResolver);
    visCommonExpressions.setWithinScopeResolver(withinScopeBasicSymbolsResolver);
    visCommonExpressions.setType4Ast(type4Ast);
    visCommonExpressions.setContext4Ast(ctx4Ast);
    traverser.add4CommonExpressions(visCommonExpressions);
    traverser.setCommonExpressionsHandler(visCommonExpressions);

    ExpressionBasisCTTIVisitor visExpressionBasis = new ExpressionBasisCTTIVisitor();
    visExpressionBasis.setWithinScopeResolver(withinScopeBasicSymbolsResolver);
    visExpressionBasis.setType4Ast(type4Ast);
    visExpressionBasis.setContext4Ast(ctx4Ast);
    traverser.add4ExpressionsBasis(visExpressionBasis);
    traverser.setExpressionsBasisHandler(visExpressionBasis);

    MCCommonLiteralsTypeVisitor visMCCommonLiterals = new MCCommonLiteralsTypeVisitor();
    visMCCommonLiterals.setType4Ast(type4Ast);
    traverser.add4MCCommonLiterals(visMCCommonLiterals);

    OCLExpressionsTypeVisitor visOCLExpressions = new OCLExpressionsTypeVisitor();
    visOCLExpressions.setType4Ast(type4Ast);
    traverser.add4OCLExpressions(visOCLExpressions);

    OptionalOperatorsTypeVisitor visOptionalOperators = new OptionalOperatorsTypeVisitor();
    visOptionalOperators.setType4Ast(type4Ast);
    traverser.add4OptionalOperators(visOptionalOperators);

    SetExpressionsCTTIVisitor visSetExpressions = new SetExpressionsCTTIVisitor();
    visSetExpressions.setType4Ast(type4Ast);
    visSetExpressions.setContext4Ast(ctx4Ast);
    traverser.add4SetExpressions(visSetExpressions);
    traverser.setSetExpressionsHandler(visSetExpressions);

    // MCTypes

    MCBasicTypesTypeVisitor visMCBasicTypes = new MCBasicTypesTypeVisitor();
    visMCBasicTypes.setWithinTypeResolver(withinTypeBasicSymbolsResolver);
    visMCBasicTypes.setWithinScopeResolver(withinScopeBasicSymbolsResolver);
    visMCBasicTypes.setType4Ast(type4Ast);
    traverser.add4MCBasicTypes(visMCBasicTypes);

    MCCollectionTypesTypeVisitor visMCCollectionTypes = new MCCollectionTypesTypeVisitor();
    visMCCollectionTypes.setType4Ast(type4Ast);
    traverser.add4MCCollectionTypes(visMCCollectionTypes);

    MCSimpleGenericTypesTypeVisitor visMCSimpleGenericTypes = new MCSimpleGenericTypesTypeVisitor();
    visMCSimpleGenericTypes.setType4Ast(type4Ast);
    traverser.add4MCSimpleGenericTypes(visMCSimpleGenericTypes);

    // create delegate
    OCLTypeCheck3 oclTC3 = new OCLTypeCheck3(traverser, type4Ast, ctx4Ast);
    oclTC3.setThisAsDelegate();
  }

  protected OCLTypeCheck3(
      ITraverser typeTraverser, Type4Ast type4Ast, InferenceContext4Ast ctx4Ast) {
    super(typeTraverser, type4Ast, ctx4Ast);
  }
}
