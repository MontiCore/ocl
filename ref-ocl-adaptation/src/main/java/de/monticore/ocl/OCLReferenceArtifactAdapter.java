package de.monticore.ocl;

import de.monticore.expressions.commonexpressions.CommonExpressionsAdaptationVisitor;
import de.monticore.expressions.commonexpressions.CommonExpressionsBindingVariantsVisitor;
import de.monticore.expressions.expressionsbasis.ExpressionsBasisAdaptationVisitor;
import de.monticore.expressions.expressionsbasis.ExpressionsBasisBindingVariantsVisitor;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.refadaptation.AdaptationContextHolder;
import de.monticore.refadaptation.Adaptations4Ast;
import de.monticore.refadaptation.ReferenceArtifactAdapter;
import de.monticore.types.mcbasictypes.MCBasicTypesAdaptationVisitor;
import de.monticore.types.mcbasictypes.MCBasicTypesBindingVariantsVisitor;
import de.monticore.visitor.ITraverser;
import de.se_rwth.commons.logging.Log;

public class OCLReferenceArtifactAdapter extends ReferenceArtifactAdapter {

  protected OCLReferenceArtifactAdapter(
          ITraverser bindingVariantsTraverser,
          ITraverser adaptationTraverser,
          AdaptationContextHolder contextHolder,
          Adaptations4Ast adaptations4Ast) {
    super(bindingVariantsTraverser, adaptationTraverser, contextHolder, adaptations4Ast);
  }

  public static OCLReferenceArtifactAdapter create() {
    Log.trace("init OCLTypeCheck3", "TypeCheck setup");

    OCLTraverser bindingVariantsTraverser = OCLMill.inheritanceTraverser();
    OCLTraverser adaptationTraverser = OCLMill.inheritanceTraverser();
    AdaptationContextHolder contextHolder = new AdaptationContextHolder();
    Adaptations4Ast adaptations4Ast = new Adaptations4Ast();

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

    // create instance
    return new OCLReferenceArtifactAdapter(
        bindingVariantsTraverser,
        adaptationTraverser,
        contextHolder,
        adaptations4Ast
    );
  }
}
