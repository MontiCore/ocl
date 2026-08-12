package de.monticore.ocl.codegen;

import de.monticore.expressions.bitexpressions.types3.BitExpressionsTypeVisitor;
import de.monticore.expressions.commonexpressions.types3.OCLCommonExpressionsCTTIVisitor;
import de.monticore.expressions.commonexpressions.types3.util.CommonExpressionsLValueRelations;
import de.monticore.expressions.expressionsbasis.types3.ExpressionBasisCTTIVisitor;
import de.monticore.expressions.setexpressions.types3.OCLSetExpressionsCTTIVisitor;
import de.monticore.literals.mccommonliterals._ast.ASTStringLiteral;
import de.monticore.literals.mccommonliterals.types3.MCCommonLiteralsTypeVisitor;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.oclexpressions.OCLOCLExpressionsTypeVisitor;
import de.monticore.ocl.optionaloperators.types3.OptionalOperatorsTypeVisitor;
import de.monticore.ocl.types3.OCLCollectionSymTypeRelations;
import de.monticore.ocl.types3.OCLSymTypeRelations;
import de.monticore.ocl.types3.util.OCLWithinScopeBasicSymbolsResolver;
import de.monticore.ocl.types3.util.OCLWithinTypeBasicSymbolsResolver;
import de.monticore.types.mcbasictypes.types3.MCBasicTypesTypeVisitor;
import de.monticore.types.mccollectiontypes.types3.MCCollectionTypesTypeVisitor;
import de.monticore.types.mcsimplegenerictypes.types3.MCSimpleGenericTypesTypeVisitor;
import de.monticore.types3.Type4Ast;
import de.monticore.types3.TypeCheck3;
import de.monticore.types3.generics.TypeParameterRelations;
import de.monticore.types3.generics.context.InferenceContext4Ast;
import de.monticore.types3.util.MapBasedTypeCheck3;
import de.monticore.types3.util.TypeContextCalculator;
import de.monticore.types3.util.TypeVisitorOperatorCalculator;
import de.monticore.visitor.ITraverser;
import de.se_rwth.commons.logging.Log;

import static de.monticore.types.check.SymTypeExpressionFactory.createStringType;

public class NoRegexOCLTypeCheck3 extends MapBasedTypeCheck3 {

  public static void init() {
    initTC3Delegate();
    OCLSymTypeRelations.init();
    OCLCollectionSymTypeRelations.init();
    OCLWithinTypeBasicSymbolsResolver.init();
    OCLWithinScopeBasicSymbolsResolver.init();
    TypeContextCalculator.init();
    TypeVisitorOperatorCalculator.init();
    CommonExpressionsLValueRelations.init();
    TypeParameterRelations.init();
  }

  public static void reset() {
    TypeCheck3.resetDelegate();
    OCLCollectionSymTypeRelations.reset();
    OCLWithinTypeBasicSymbolsResolver.reset();
    OCLWithinScopeBasicSymbolsResolver.reset();
    TypeContextCalculator.reset();
    TypeVisitorOperatorCalculator.reset();
    CommonExpressionsLValueRelations.reset();
    TypeParameterRelations.reset();
  }

  protected static void initTC3Delegate() {
    Log.trace("init OCLTypeCheck3", "TypeCheck setup");

    OCLTraverser traverser = OCLMill.inheritanceTraverser();
    Type4Ast type4Ast = new Type4Ast();
    InferenceContext4Ast ctx4Ast = new InferenceContext4Ast();

    // Expressions

    BitExpressionsTypeVisitor visBitExpressions = new BitExpressionsTypeVisitor();
    visBitExpressions.setType4Ast(type4Ast);
    traverser.add4BitExpressions(visBitExpressions);

    OCLCommonExpressionsCTTIVisitor visCommonExpressions = new OCLCommonExpressionsCTTIVisitor();
    visCommonExpressions.setType4Ast(type4Ast);
    visCommonExpressions.setContext4Ast(ctx4Ast);
    traverser.add4CommonExpressions(visCommonExpressions);
    traverser.setCommonExpressionsHandler(visCommonExpressions);

    ExpressionBasisCTTIVisitor visExpressionBasis = new ExpressionBasisCTTIVisitor();
    visExpressionBasis.setType4Ast(type4Ast);
    visExpressionBasis.setContext4Ast(ctx4Ast);
    traverser.add4ExpressionsBasis(visExpressionBasis);
    traverser.setExpressionsBasisHandler(visExpressionBasis);

    MCCommonLiteralsTypeVisitor visMCCommonLiterals = new MCCommonLiteralsTypeVisitor(){
      @Override
      public void endVisit(ASTStringLiteral lit) {
        getType4Ast().setTypeOfExpression(lit, createStringType());
      }
    };
    visMCCommonLiterals.setType4Ast(type4Ast);
    traverser.add4MCCommonLiterals(visMCCommonLiterals);

    OCLOCLExpressionsTypeVisitor visOCLExpressions = new OCLOCLExpressionsTypeVisitor();
    visOCLExpressions.setType4Ast(type4Ast);
    traverser.add4OCLExpressions(visOCLExpressions);

    OptionalOperatorsTypeVisitor visOptionalOperators = new OptionalOperatorsTypeVisitor();
    visOptionalOperators.setType4Ast(type4Ast);
    traverser.add4OptionalOperators(visOptionalOperators);

    OCLSetExpressionsCTTIVisitor visSetExpressions = new OCLSetExpressionsCTTIVisitor();
    visSetExpressions.setType4Ast(type4Ast);
    visSetExpressions.setContext4Ast(ctx4Ast);
    traverser.add4SetExpressions(visSetExpressions);
    traverser.setSetExpressionsHandler(visSetExpressions);

    // MCTypes

    MCBasicTypesTypeVisitor visMCBasicTypes = new MCBasicTypesTypeVisitor();
    visMCBasicTypes.setType4Ast(type4Ast);
    traverser.add4MCBasicTypes(visMCBasicTypes);

    MCCollectionTypesTypeVisitor visMCCollectionTypes = new MCCollectionTypesTypeVisitor();
    visMCCollectionTypes.setType4Ast(type4Ast);
    traverser.add4MCCollectionTypes(visMCCollectionTypes);

    MCSimpleGenericTypesTypeVisitor visMCSimpleGenericTypes = new MCSimpleGenericTypesTypeVisitor();
    visMCSimpleGenericTypes.setType4Ast(type4Ast);
    traverser.add4MCSimpleGenericTypes(visMCSimpleGenericTypes);

    // create delegate
    NoRegexOCLTypeCheck3 oclTC3 = new NoRegexOCLTypeCheck3(traverser, type4Ast, ctx4Ast);
    oclTC3.setThisAsDelegate();
  }

  protected NoRegexOCLTypeCheck3(
      ITraverser typeTraverser, Type4Ast type4Ast, InferenceContext4Ast ctx4Ast) {
    super(typeTraverser, type4Ast, ctx4Ast);
  }

  public static NoRegexOCLTypeCheck3 getDelegate() {
    if (TypeCheck3.delegate == null) {
      Log.errorInternal("0xFD777 internal error: "
          + "TypeCheck has not been initialized."
          + " Please refer to the TypeCheck3 subclass(es) of your language."
      );
    }
    return (NoRegexOCLTypeCheck3) TypeCheck3.delegate;
  }
}