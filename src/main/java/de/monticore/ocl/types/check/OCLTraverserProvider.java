package de.monticore.ocl.types.check;

import de.monticore.expressions.commonexpressions.types3.CommonExpressionsTypeVisitor;
import de.monticore.expressions.expressionsbasis.types3.ExpressionBasisTypeVisitor;
import de.monticore.literals.mccommonliterals.types3.MCCommonLiteralsTypeVisitor;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.types.mcbasictypes.types3.MCBasicTypesTypeVisitor;
import de.monticore.types.mccollectiontypes.types3.MCCollectionTypesTypeVisitor;
import de.monticore.types.mcsimplegenerictypes.types3.MCSimpleGenericTypesTypeVisitor;
import de.monticore.types3.Type4Ast;

public class OCLTraverserProvider {
  
  // TODO MSm add Bit Expressions
  
  // Expressions
  
  protected OCLExpressionsTypeVisitor derOCLExpressions;
  
  protected OptionalOperatorsTypeVisitor derOptionalOperators;
  
  protected SetExpressionsTypeVisitor derSetExpressions;
  
  protected CommonExpressionsTypeVisitor derCommonExpressions;
  
  protected ExpressionBasisTypeVisitor derExpressionBasis;
  
  protected MCCommonLiteralsTypeVisitor derOfMCCommonLiterals;
  
  // MCTypes
  
  protected MCBasicTypesTypeVisitor synMCBasicTypes;
  
  protected MCCollectionTypesTypeVisitor synMCCollectionTypes;
  
  protected MCSimpleGenericTypesTypeVisitor synMCSimpleGenericTypes;
  
  public OCLTraverserProvider() {
    init();
  }
  
  protected void init() {
    // Expressions
    derOCLExpressions = new OCLExpressionsTypeVisitor();
    derOptionalOperators = new OptionalOperatorsTypeVisitor();
    derSetExpressions = new SetExpressionsTypeVisitor();
    
    derCommonExpressions = new CommonExpressionsTypeVisitor();
    derExpressionBasis = new ExpressionBasisTypeVisitor();
    derOfMCCommonLiterals = new MCCommonLiteralsTypeVisitor();
    
    // MCTypes
    synMCBasicTypes = new MCBasicTypesTypeVisitor();
    synMCCollectionTypes = new MCCollectionTypesTypeVisitor();
    synMCSimpleGenericTypes = new MCSimpleGenericTypesTypeVisitor();
  }
  
  public OCLTraverser init(OCLTraverser traverser) {
    // Expressions
    traverser.add4OCLExpressions(derOCLExpressions);
    traverser.add4OptionalOperators(derOptionalOperators);
    traverser.add4SetExpressions(derSetExpressions);
    
    traverser.add4CommonExpressions(derCommonExpressions);
    traverser.setCommonExpressionsHandler(derCommonExpressions);
    traverser.add4ExpressionsBasis(derExpressionBasis);
    traverser.add4MCCommonLiterals(derOfMCCommonLiterals);
    
    // MCTypes
    traverser.add4MCBasicTypes(synMCBasicTypes);
    traverser.add4MCCollectionTypes(synMCCollectionTypes);
    traverser.add4MCSimpleGenericTypes(synMCSimpleGenericTypes);
    
    return traverser;
  }
  
  public void setType4Ast(Type4Ast type4Ast) {
    // Expressions
    derOCLExpressions.setType4Ast(type4Ast);
    derOptionalOperators.setType4Ast(type4Ast);
    derSetExpressions.setType4Ast(type4Ast);
    
    derCommonExpressions.setType4Ast(type4Ast);
    derExpressionBasis.setType4Ast(type4Ast);
    derOfMCCommonLiterals.setType4Ast(type4Ast);
    
    // MCTypes
    synMCBasicTypes.setType4Ast(type4Ast);
    synMCCollectionTypes.setType4Ast(type4Ast);
    synMCSimpleGenericTypes.setType4Ast(type4Ast);
  }
}