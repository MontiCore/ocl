/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.ocl.types3;

import de.monticore.expressions.bitexpressions.types3.BitExpressionsTypeVisitor;
import de.monticore.expressions.commonexpressions.types3.CommonExpressionsTypeVisitor;
import de.monticore.expressions.expressionsbasis.types3.ExpressionBasisTypeVisitor;
import de.monticore.literals.mccommonliterals.types3.MCCommonLiteralsTypeVisitor;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.types.check.OCLExpressionsTypeVisitor;
import de.monticore.ocl.types.check.OptionalOperatorsTypeVisitor;
import de.monticore.ocl.types.check.SetExpressionsTypeVisitor;
import de.monticore.ocl.types3.util.OCLNameExpressionTypeCalculator;
import de.monticore.ocl.types3.util.OCLWithinTypeBasicSymbolsResolver;
import de.monticore.types.mcbasictypes.types3.MCBasicTypesTypeVisitor;
import de.monticore.types.mccollectiontypes.types3.MCCollectionTypesTypeVisitor;
import de.monticore.types.mcsimplegenerictypes.types3.MCSimpleGenericTypesTypeVisitor;
import de.monticore.types3.Type4Ast;
import de.monticore.types3.util.FunctionRelations;
import de.monticore.types3.util.NameExpressionTypeCalculator;
import de.monticore.types3.util.WithinTypeBasicSymbolsResolver;

public class OCLTypeTraverserFactory {

  public OCLTraverser createTraverser(Type4Ast type4Ast) {
    OCLTraverser traverser = OCLMill.inheritanceTraverser();
    VisitorList visitors = constructVisitors();
    setType4Ast(visitors, type4Ast);
    populateTraverser(visitors, traverser);
    return traverser;
  }

  protected void setType4Ast(VisitorList visitors, Type4Ast type4Ast) {
    // Expressions
    visitors.derBitExpressions.setType4Ast(type4Ast);
    visitors.derCommonExpressions.setType4Ast(type4Ast);
    visitors.derExpressionBasis.setType4Ast(type4Ast);
    visitors.derMCCommonLiterals.setType4Ast(type4Ast);
    visitors.derOCLExpressions.setType4Ast(type4Ast);
    visitors.derOptionalOperators.setType4Ast(type4Ast);
    visitors.derSetExpressions.setType4Ast(type4Ast);
    // MCTypes
    visitors.synMCBasicTypes.setType4Ast(type4Ast);
    visitors.synMCCollectionTypes.setType4Ast(type4Ast);
    visitors.synMCSimpleGenericTypes.setType4Ast(type4Ast);
  }

  protected VisitorList constructVisitorsDefault() {
    VisitorList visitors = new VisitorList();
    // Expressions
    visitors.derBitExpressions = new BitExpressionsTypeVisitor();
    visitors.derCommonExpressions = new CommonExpressionsTypeVisitor();
    visitors.derExpressionBasis = new ExpressionBasisTypeVisitor();
    visitors.derMCCommonLiterals = new MCCommonLiteralsTypeVisitor();
    visitors.derOCLExpressions = new OCLExpressionsTypeVisitor();
    visitors.derOptionalOperators = new OptionalOperatorsTypeVisitor();
    visitors.derSetExpressions = new SetExpressionsTypeVisitor();
    // MCTypes
    visitors.synMCBasicTypes = new MCBasicTypesTypeVisitor();
    visitors.synMCCollectionTypes = new MCCollectionTypesTypeVisitor();
    visitors.synMCSimpleGenericTypes = new MCSimpleGenericTypesTypeVisitor();
    return visitors;
  }

  protected VisitorList constructVisitors() {
    VisitorList visitors = constructVisitorsDefault();
    WithinTypeBasicSymbolsResolver withinTypeBasicSymbolsResolver =
        new OCLWithinTypeBasicSymbolsResolver();
    NameExpressionTypeCalculator nameExpressionTypeCalculator =
        new OCLNameExpressionTypeCalculator();
    FunctionRelations functionRelations = new FunctionRelations();
    visitors.derCommonExpressions.setWithinTypeBasicSymbolsResolver(withinTypeBasicSymbolsResolver);
    visitors.derCommonExpressions.setNameExpressionTypeCalculator(nameExpressionTypeCalculator);
    visitors.derExpressionBasis.setNameExpressionTypeCalculator(nameExpressionTypeCalculator);
    visitors.synMCBasicTypes.setWithinTypeResolver(withinTypeBasicSymbolsResolver);
    visitors.synMCBasicTypes.setNameExpressionTypeCalculator(nameExpressionTypeCalculator);
    return visitors;
  }

  protected void populateTraverser(VisitorList visitors, OCLTraverser traverser) {
    // Expressions
    traverser.add4BitExpressions(visitors.derBitExpressions);
    traverser.add4CommonExpressions(visitors.derCommonExpressions);
    traverser.setCommonExpressionsHandler(visitors.derCommonExpressions);
    traverser.add4ExpressionsBasis(visitors.derExpressionBasis);
    traverser.add4MCCommonLiterals(visitors.derMCCommonLiterals);
    traverser.add4OCLExpressions(visitors.derOCLExpressions);
    traverser.add4OptionalOperators(visitors.derOptionalOperators);
    traverser.add4SetExpressions(visitors.derSetExpressions);
    // MCTypes
    traverser.add4MCBasicTypes(visitors.synMCBasicTypes);
    traverser.add4MCCollectionTypes(visitors.synMCCollectionTypes);
    traverser.add4MCSimpleGenericTypes(visitors.synMCSimpleGenericTypes);
  }

  /** POD */
  protected static class VisitorList {

    // Expressions

    public BitExpressionsTypeVisitor derBitExpressions;

    public CommonExpressionsTypeVisitor derCommonExpressions;

    public ExpressionBasisTypeVisitor derExpressionBasis;

    public MCCommonLiteralsTypeVisitor derMCCommonLiterals;

    public OCLExpressionsTypeVisitor derOCLExpressions;

    public OptionalOperatorsTypeVisitor derOptionalOperators;

    public SetExpressionsTypeVisitor derSetExpressions;

    // MCTypes

    public MCBasicTypesTypeVisitor synMCBasicTypes;

    public MCCollectionTypesTypeVisitor synMCCollectionTypes;

    public MCSimpleGenericTypesTypeVisitor synMCSimpleGenericTypes;
  }
}
