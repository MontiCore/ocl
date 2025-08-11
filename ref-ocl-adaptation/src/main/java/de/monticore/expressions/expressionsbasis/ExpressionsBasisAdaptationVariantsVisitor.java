package de.monticore.expressions.expressionsbasis;

import de.monticore.expressions.expressionsbasis._ast.ASTArguments;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.refadaptation.BindingConflictException;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symboltable.ISymbol;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;
import java.util.Set;

/**
 * Adaptation variant visitor for the ExpressionsBasis language.
 * <h5>Adaptations</h5>
 * <code>NameExpression</code>:
 * <ul>
 *   <li>One variant for each incarnation of the related VariableSymbol</li>
 *   <li>One variant for each incarnation of the related FunctionSymbol</li>
 * </ul>
 */
public class ExpressionsBasisAdaptationVariantsVisitor
        extends ExpressionsBasisAdaptationVariantsVisitorTOP {

  private static final String LOG_NAME = ExpressionsBasisAdaptationVariantsVisitor.class.getName();

  @Override
  public void traverse(ASTArguments arguments) {
    traverseForConsistentVariants(arguments, arguments.getExpressionList());
  }

  @Override
  public void endVisit(ASTNameExpression refExpr) {
    Optional<ISymbol> sourceSymbolOpt = TypeCheck3.typeOf(refExpr).getSourceInfo().getSourceSymbol();
    if (sourceSymbolOpt.isPresent()) {
      ISymbol sourceSymbol = sourceSymbolOpt.get();
      System.out.println("NameExpression Variable Source symbol: " + sourceSymbol);
      System.out.println("symbol full name: " + sourceSymbol.getFullName());
      addVariantsForSymbol(refExpr, sourceSymbol);
    } else {
      // make sure to create a default variant if we cannot adapt anything
      getAdaptations4Ast().addVariant(refExpr, getAdaptationContext().createVariant());
    }
  }

  protected void addVariantsForSymbol(ASTNameExpression refExpr, ISymbol refSymbol) {
    // identify variants depending on the symbol kind
    if (refSymbol instanceof VariableSymbol) {
      addVariantsForVariableSymbol(refExpr, (VariableSymbol) refSymbol);
    } else if (refSymbol instanceof FunctionSymbol) {
      addVariantsForFunctionSymbol(refExpr, (FunctionSymbol) refSymbol);
    } else {
      Log.warn("Unexpected symbol type: " + refSymbol.getClass().getSimpleName() + " for NameExpression: " + refExpr.get_SourcePositionStart());
      getAdaptations4Ast().addVariant(refExpr, getAdaptationContext().createVariant());
    }
  }

  /**
   * Introduces one variant for each incarnation of the given variable symbol.
   *
   * @param refExpr the ASTNameExpression that references the variable symbol
   * @param refVarSymbol the VariableSymbol from the reference model
   */
  protected void addVariantsForVariableSymbol(ASTNameExpression refExpr, VariableSymbol refVarSymbol) {
    // TODO maybe add "isReferenceSymbol" so we can check if the incarnation mapping is applicable here?
    //  then, if it is applicable but get zero incarnations -> we know we ran into a conflict
    //        if not, we can safely ignore it an create a default variant
    Set<VariableSymbol> incarnations = getAdaptationContext().getBasicSymbolsIncMapping().getIncarnations(refVarSymbol);
    if (incarnations.isEmpty()) {
      // TODO only pass variant upwards if the refSymbol is not defined in the inc mapping
      //  if it is defined, no incarnation is a sign that we should drop this variant
      getAdaptations4Ast().addVariant(refExpr, getAdaptationContext().createVariant());
      return;
    }
    getAdaptations4Ast().addVariants(refExpr, tryCreateVariantsForIncarnations(incarnations,
            (incarnation) -> createVariantForVariableIncarnation(refExpr, refVarSymbol, incarnation)));
  }

  /**
   * Introduces one variant for each incarnation of the given function symbol.
   *
   * @param refExpr the ASTNameExpression that references the function symbol
   * @param refFunctionSymbol the FunctionSymbol from the reference model
   */
  protected void addVariantsForFunctionSymbol(ASTNameExpression refExpr, FunctionSymbol refFunctionSymbol) {
    Set<FunctionSymbol> incarnations = getAdaptationContext().getBasicSymbolsIncMapping().getIncarnations(refFunctionSymbol);
    if (incarnations.isEmpty()) {
      // TODO only pass variant upwards if the refSymbol is not defined in the inc mapping
      //  if it is defined, no incarnation is a sign that we should drop this variant
      getAdaptations4Ast().addVariant(refExpr, getAdaptationContext().createVariant());
      return;
    }
    getAdaptations4Ast().addVariants(refExpr, tryCreateVariantsForIncarnations(incarnations,
            (incarnation) -> createVariantForFunctionIncarnation(refExpr, refFunctionSymbol, incarnation)));
  }

  /**
   * Creates a new variant which adapts the FieldAccessExpression according to the given
   * incarnation of the variable symbol.
   *
   * @param refExpr the ASTFieldAccessExpression to adapt
   * @param refVariableSymbol the reference VariableSymbol referenced in the expression
   * @param incarnation the incarnation of the variable symbol to adapt to
   * @return a new CommonExpressionsAdaptationVariant that adapts the expression
   *
   * @throws BindingConflictException if the binding conflicts with existing bindings in the context
   */
  protected IExpressionsBasisAdaptationVariant createVariantForVariableIncarnation(
          ASTNameExpression refExpr,
          VariableSymbol refVariableSymbol,
          VariableSymbol incarnation) throws BindingConflictException {
    // 1. Create a variant for the incarnation
    IExpressionsBasisAdaptationVariant newVariant = getAdaptationContext()
            .createVariantForIncarnation(refVariableSymbol, incarnation,
                    refExpr.get_SourcePositionStart());
    // 2. Specify the AST Adaptation / transformation
    newVariant.addASTAdaptation(refExpr, adaptedNode -> {
      adaptedNode.setName(incarnation.getName());
      return adaptedNode;
    });
    return newVariant;
  }

  /**
   * Creates a new variant which adapts the FieldAccessExpression according to the given
   * incarnation of the variable symbol.
   *
   * @param refExpr the ASTFieldAccessExpression to adapt
   * @param refFunctionSymbol the reference FunctionSymbol referenced in the expression
   * @param incarnation the incarnation of the variable symbol to adapt to
   * @return a new CommonExpressionsAdaptationVariant that adapts the expression
   *
   * @throws BindingConflictException if the binding conflicts with existing bindings in the context
   */
  protected IExpressionsBasisAdaptationVariant createVariantForFunctionIncarnation(
          ASTNameExpression refExpr,
          FunctionSymbol refFunctionSymbol,
          FunctionSymbol incarnation) throws BindingConflictException {
    // 1. Create a variant for the incarnation
    IExpressionsBasisAdaptationVariant newVariant = getAdaptationContext()
            .createVariantForIncarnation(refFunctionSymbol, incarnation,
                    refExpr.get_SourcePositionStart());
    // 2. Specify the AST Adaptation / transformation
    newVariant.addASTAdaptation(refExpr, adaptedNode -> {
      adaptedNode.setName(incarnation.getName());
      return adaptedNode;
    });
    return newVariant;
  }

  @Override
  public void visit(ASTLiteralExpression node) {
    // TODO Either we do not process these at all and introduce the convention:
    // - If no binding variant is present -> just use the reference node
    // OR
    // - we return an atomic "empty binding variant" and pass this upwards
    // TODO We need to decide in "traverse(AST...)" what we do if the list is empty -> default variant or is this a conflict?
    getAdaptations4Ast().addVariant(node, getAdaptationContext().createVariant());
  }
}
