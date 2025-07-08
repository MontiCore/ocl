package de.monticore.ocl;

import de.monticore.ast.ASTNode;
import de.monticore.expressions.commonexpressions.CommonExpressionsAdaptationVisitor;
import de.monticore.expressions.commonexpressions.CommonExpressionsBindingVariantsVisitor;
import de.monticore.expressions.expressionsbasis.ExpressionsBasisAdaptationVisitor;
import de.monticore.expressions.expressionsbasis.ExpressionsBasisBindingVariantsVisitor;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.refadaptation.AdaptationContextHolder;
import de.monticore.refadaptation.Adaptations4Ast;
import de.monticore.refadaptation.ReferenceArtifactAdapter;
import de.monticore.symbols.OOSymbolsIncMapping;
import de.monticore.symbols.OOSymbolsLocalIncMapping;
import de.monticore.types.mcbasictypes.MCBasicTypesAdaptationVisitor;
import de.monticore.types.mcbasictypes.MCBasicTypesBindingVariantsVisitor;
import de.monticore.types.mccollectiontypes.MCCollectionTypesAdaptationVisitor;
import de.monticore.types.mccollectiontypes.MCCollectionTypesBindingVariantsVisitor;
import de.monticore.visitor.ITraverser;
import de.se_rwth.commons.logging.Log;

import java.util.List;

public class OCLReferenceArtifactAdapter extends ReferenceArtifactAdapter<OCLAdaptationContext> {

  /**
   * Factory method to create an instance of the {@link OCLReferenceArtifactAdapter} configured
   * with all the binding variant & adaptation visitors for the OCL language and sub-languages.
   *
   * @return a ready to use {@link OCLReferenceArtifactAdapter} instance
   */
  public static OCLReferenceArtifactAdapter create() {
    Log.trace("init OCLTypeCheck3", "TypeCheck setup");

    OCLTraverser bindingVariantsTraverser = OCLMill.inheritanceTraverser();
    OCLTraverser adaptationTraverser = OCLMill.inheritanceTraverser();
    AdaptationContextHolder contextHolder = new AdaptationContextHolder();
    Adaptations4Ast adaptations4Ast = new Adaptations4Ast();

    // OCL main language

    OCLBindingVariantsVisitor oclBindingVis = new OCLBindingVariantsVisitor();
    oclBindingVis.setContextHolder(contextHolder);
    oclBindingVis.setAdaptations4Ast(adaptations4Ast);
    bindingVariantsTraverser.add4OCL(oclBindingVis);
    bindingVariantsTraverser.setOCLHandler(oclBindingVis);

    OCLAdaptationVisitor oclAdaptVis = new OCLAdaptationVisitor();
    oclAdaptVis.setContextHolder(contextHolder);
    oclAdaptVis.setAdaptations4Ast(adaptations4Ast);
    adaptationTraverser.add4OCL(oclAdaptVis);

    // Expressions

    CommonExpressionsBindingVariantsVisitor commonExpressionsBindingVis = new CommonExpressionsBindingVariantsVisitor();
    commonExpressionsBindingVis.setContextHolder(contextHolder);
    commonExpressionsBindingVis.setAdaptations4Ast(adaptations4Ast);
    bindingVariantsTraverser.add4CommonExpressions(commonExpressionsBindingVis);
    bindingVariantsTraverser.setCommonExpressionsHandler(commonExpressionsBindingVis);

    CommonExpressionsAdaptationVisitor commonExpressionsAdaptVis = new CommonExpressionsAdaptationVisitor();
    commonExpressionsAdaptVis.setContextHolder(contextHolder);
    commonExpressionsAdaptVis.setAdaptations4Ast(adaptations4Ast);
    adaptationTraverser.add4CommonExpressions(commonExpressionsAdaptVis);

    ExpressionsBasisBindingVariantsVisitor expressionsBasisBindingVis = new ExpressionsBasisBindingVariantsVisitor();
    expressionsBasisBindingVis.setContextHolder(contextHolder);
    expressionsBasisBindingVis.setAdaptations4Ast(adaptations4Ast);
    bindingVariantsTraverser.add4ExpressionsBasis(expressionsBasisBindingVis);
    bindingVariantsTraverser.setExpressionsBasisHandler(expressionsBasisBindingVis);

    ExpressionsBasisAdaptationVisitor expressionsBasisAdaptVis = new ExpressionsBasisAdaptationVisitor();
    expressionsBasisAdaptVis.setContextHolder(contextHolder);
    expressionsBasisAdaptVis.setAdaptations4Ast(adaptations4Ast);
    adaptationTraverser.add4ExpressionsBasis(expressionsBasisAdaptVis);

    // MCTypes

    MCBasicTypesBindingVariantsVisitor mcBasicTypesBindingVis = new MCBasicTypesBindingVariantsVisitor();
    mcBasicTypesBindingVis.setContextHolder(contextHolder);
    mcBasicTypesBindingVis.setAdaptations4Ast(adaptations4Ast);
    bindingVariantsTraverser.add4MCBasicTypes(mcBasicTypesBindingVis);

    MCBasicTypesAdaptationVisitor mcBasicTypesAdaptVis = new MCBasicTypesAdaptationVisitor();
    mcBasicTypesAdaptVis.setContextHolder(contextHolder);
    mcBasicTypesAdaptVis.setAdaptations4Ast(adaptations4Ast);
    adaptationTraverser.add4MCBasicTypes(mcBasicTypesAdaptVis);

    MCCollectionTypesBindingVariantsVisitor mcCollectionTypesBindingVis = new MCCollectionTypesBindingVariantsVisitor();
    mcCollectionTypesBindingVis.setContextHolder(contextHolder);
    mcCollectionTypesBindingVis.setAdaptations4Ast(adaptations4Ast);
    bindingVariantsTraverser.add4MCCollectionTypes(mcCollectionTypesBindingVis);
    bindingVariantsTraverser.setMCCollectionTypesHandler(mcCollectionTypesBindingVis);

    MCCollectionTypesAdaptationVisitor mcCollectionTypesAdaptVis = new MCCollectionTypesAdaptationVisitor();
    mcCollectionTypesAdaptVis.setContextHolder(contextHolder);
    mcCollectionTypesAdaptVis.setAdaptations4Ast(adaptations4Ast);
    adaptationTraverser.add4MCCollectionTypes(mcCollectionTypesAdaptVis);

    // create instance
    return new OCLReferenceArtifactAdapter(
        bindingVariantsTraverser,
        adaptationTraverser,
        contextHolder,
        adaptations4Ast
    );
  }

  protected OCLReferenceArtifactAdapter(
          ITraverser bindingVariantsTraverser,
          ITraverser adaptationTraverser,
          AdaptationContextHolder contextHolder,
          Adaptations4Ast adaptations4Ast) {
    super(bindingVariantsTraverser, adaptationTraverser, contextHolder, adaptations4Ast);
  }

  /**
   * Creates an adaptation context for the given OOSymbolsIncMapping.
   *
   * @param ooSymbolsIncMapping the incarnation mapping of OOSymbols models to use for the
   *                            adaptation context
   * @return a context representing the given incarnation mapping
   */
  protected OCLAdaptationContext createAdaptationContext(OOSymbolsIncMapping ooSymbolsIncMapping) {
    return new OCLAdaptationContextImpl(ooSymbolsIncMapping);
  }

  /**
   * Convenience method to adapt the given reference node using the provided OOSymbolsIncMapping.
   *
   * @param refNode the reference node to adapt
   * @param ooSymbolsIncMapping the incarnation mapping of OOSymbols models to use for adaptation
   * @return a list of adapted AST nodes of type
   * @param <T> the type of ASTNode to adapt
   */
  public <T extends ASTNode> List<T> adapt(T refNode, OOSymbolsIncMapping ooSymbolsIncMapping) {
    return adapt(refNode, createAdaptationContext(ooSymbolsIncMapping));
  }
}
