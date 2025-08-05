package de.monticore.ocl;

import de.monticore.ast.ASTNode;
import de.monticore.expressions.commonexpressions.CommonExpressionsASTAdaptationVisitor;
import de.monticore.expressions.expressionsbasis.ExpressionsBasisASTAdaptationVisitor;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.oclexpressions.OCLExpressionsASTAdaptationVisitor;
import de.monticore.ocl.oclexpressions.OCLExpressionsAdaptationVariantsVisitor;
import de.monticore.ocl.setexpressions.SetExpressionsASTAdaptationVisitor;
import de.monticore.ocl.setexpressions.SetExpressionsAdaptationVariantsVisitor;
import de.monticore.refadaptation.AdaptationContextHolder;
import de.monticore.refadaptation.Adaptations4Ast;
import de.monticore.refadaptation.ReferenceArtifactAdapter;
import de.monticore.symbols.OOSymbolsIncMapping;
import de.monticore.types.mcbasictypes.MCBasicTypesAdaptationVisitor;
import de.monticore.types.mcbasictypes.MCBasicTypesBindingVariantsVisitor;
import de.monticore.types.mcbasictypes.refadaptation.MCTypeFactory;
import de.monticore.types.mccollectiontypes.MCCollectionTypesASTAdaptationVisitor;
import de.monticore.types.mccollectiontypes.MCCollectionTypesBindingVariantsVisitor;
import de.monticore.visitor.ITraverser;
import de.se_rwth.commons.logging.Log;

import java.util.List;

/**
 * Reference artifact adapter for the OCL language. Given an incarnation mapping
 * of the OOSymbols language ({@link OOSymbolsIncMapping}) this adapter produces valid
 * concrete OCL artifacts from reference artifacts.
 */
public class OCLReferenceArtifactAdapter extends ReferenceArtifactAdapter<OCLAdaptationContext> {

  /**
   * Factory method to create an instance of the {@link OCLReferenceArtifactAdapter} configured
   * with all the binding variant & adaptation visitors for the OCL language and sub-languages.
   *
   * @return a ready to use {@link OCLReferenceArtifactAdapter} instance
   */
  public static OCLReferenceArtifactAdapter create() {
    OCLTraverser bindingVariantsTraverser = OCLMill.inheritanceTraverser();
    OCLTraverser adaptationTraverser = OCLMill.inheritanceTraverser();
    AdaptationContextHolder contextHolder = new AdaptationContextHolder();
    Adaptations4Ast adaptations4Ast = new Adaptations4Ast();

    MCTypeFactory mcTypeFactory = new OCLMCTypeFactory();

    // OCL main language

    OCLBindingVariantsVisitor oclBindingVis = new OCLBindingVariantsVisitor();
    oclBindingVis.setContextHolder(contextHolder);
    oclBindingVis.setAdaptations4Ast(adaptations4Ast);
    bindingVariantsTraverser.add4OCL(oclBindingVis);
    bindingVariantsTraverser.setOCLHandler(oclBindingVis);

    OCLASTAdaptationVisitor oclAdaptVis = new OCLASTAdaptationVisitor(mcTypeFactory);
    oclAdaptVis.setContextHolder(contextHolder);
    oclAdaptVis.setAdaptations4Ast(adaptations4Ast);
    adaptationTraverser.add4OCL(oclAdaptVis);

    // Expressions

    OCLExpressionsAdaptationVariantsVisitor oclExpressionsBindingVis = new OCLExpressionsAdaptationVariantsVisitor();
    oclExpressionsBindingVis.setContextHolder(contextHolder);
    oclExpressionsBindingVis.setAdaptations4Ast(adaptations4Ast);
    bindingVariantsTraverser.add4OCLExpressions(oclExpressionsBindingVis);
    bindingVariantsTraverser.setOCLExpressionsHandler(oclExpressionsBindingVis);

    OCLExpressionsASTAdaptationVisitor oclExpressionsAdaptVis = new OCLExpressionsASTAdaptationVisitor();
    oclExpressionsAdaptVis.setContextHolder(contextHolder);
    oclExpressionsAdaptVis.setAdaptations4Ast(adaptations4Ast);
    adaptationTraverser.add4OCLExpressions(oclExpressionsAdaptVis);

    SetExpressionsAdaptationVariantsVisitor setExpressionsBindingVis = new SetExpressionsAdaptationVariantsVisitor();
    setExpressionsBindingVis.setContextHolder(contextHolder);
    setExpressionsBindingVis.setAdaptations4Ast(adaptations4Ast);
    bindingVariantsTraverser.add4SetExpressions(setExpressionsBindingVis);
    bindingVariantsTraverser.setSetExpressionsHandler(setExpressionsBindingVis);

    SetExpressionsASTAdaptationVisitor setExpressionsAdaptVis = new SetExpressionsASTAdaptationVisitor();
    setExpressionsAdaptVis.setContextHolder(contextHolder);
    setExpressionsAdaptVis.setAdaptations4Ast(adaptations4Ast);
    adaptationTraverser.add4SetExpressions(setExpressionsAdaptVis);

    OCLCommonExpressionsAdaptationVariantsVisitor commonExpressionsBindingVis = new OCLCommonExpressionsAdaptationVariantsVisitor();
    commonExpressionsBindingVis.setContextHolder(contextHolder);
    commonExpressionsBindingVis.setAdaptations4Ast(adaptations4Ast);
    bindingVariantsTraverser.add4CommonExpressions(commonExpressionsBindingVis);
    bindingVariantsTraverser.setCommonExpressionsHandler(commonExpressionsBindingVis);

    CommonExpressionsASTAdaptationVisitor commonExpressionsAdaptVis = new CommonExpressionsASTAdaptationVisitor();
    commonExpressionsAdaptVis.setContextHolder(contextHolder);
    commonExpressionsAdaptVis.setAdaptations4Ast(adaptations4Ast);
    adaptationTraverser.add4CommonExpressions(commonExpressionsAdaptVis);

    OCLExpressionsBasisAdaptationVariantsVisitor expressionsBasisBindingVis = new OCLExpressionsBasisAdaptationVariantsVisitor();
    expressionsBasisBindingVis.setContextHolder(contextHolder);
    expressionsBasisBindingVis.setAdaptations4Ast(adaptations4Ast);
    bindingVariantsTraverser.add4ExpressionsBasis(expressionsBasisBindingVis);
    bindingVariantsTraverser.setExpressionsBasisHandler(expressionsBasisBindingVis);

    ExpressionsBasisASTAdaptationVisitor expressionsBasisAdaptVis = new ExpressionsBasisASTAdaptationVisitor();
    expressionsBasisAdaptVis.setContextHolder(contextHolder);
    expressionsBasisAdaptVis.setAdaptations4Ast(adaptations4Ast);
    adaptationTraverser.add4ExpressionsBasis(expressionsBasisAdaptVis);

    // MCTypes

    MCBasicTypesBindingVariantsVisitor mcBasicTypesBindingVis = new MCBasicTypesBindingVariantsVisitor();
    mcBasicTypesBindingVis.setContextHolder(contextHolder);
    mcBasicTypesBindingVis.setAdaptations4Ast(adaptations4Ast);
    bindingVariantsTraverser.add4MCBasicTypes(mcBasicTypesBindingVis);
    bindingVariantsTraverser.setMCBasicTypesHandler(mcBasicTypesBindingVis);

    MCBasicTypesAdaptationVisitor mcBasicTypesAdaptVis = new MCBasicTypesAdaptationVisitor();
    mcBasicTypesAdaptVis.setContextHolder(contextHolder);
    mcBasicTypesAdaptVis.setAdaptations4Ast(adaptations4Ast);
    adaptationTraverser.add4MCBasicTypes(mcBasicTypesAdaptVis);

    MCCollectionTypesBindingVariantsVisitor mcCollectionTypesBindingVis = new MCCollectionTypesBindingVariantsVisitor();
    mcCollectionTypesBindingVis.setContextHolder(contextHolder);
    mcCollectionTypesBindingVis.setAdaptations4Ast(adaptations4Ast);
    bindingVariantsTraverser.add4MCCollectionTypes(mcCollectionTypesBindingVis);
    bindingVariantsTraverser.setMCCollectionTypesHandler(mcCollectionTypesBindingVis);

    MCCollectionTypesASTAdaptationVisitor mcCollectionTypesAdaptVis = new MCCollectionTypesASTAdaptationVisitor();
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
  protected OCLAdaptationContext createAdaptationContext(
          OOSymbolsIncMapping ooSymbolsIncMapping) {
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
