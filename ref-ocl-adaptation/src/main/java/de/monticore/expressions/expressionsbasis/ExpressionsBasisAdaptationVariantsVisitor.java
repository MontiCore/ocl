package de.monticore.expressions.expressionsbasis;

import de.monticore.expressions.expressionsbasis._ast.ASTArguments;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisVisitor2;
import de.monticore.refadaptation.AbstractAdaptationVisitor;
import de.monticore.refadaptation.Binding;
import de.monticore.refadaptation.BindingConflictException;
import de.monticore.symbols.basicsymbols.BasicSymbolsBindings;
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
      // no field symbol, use the constraints from the parent expression
      getAdaptations4Ast().addVariant(refExpr, getAdaptationContext().createVariant());
    } else {
      // we have the incarnations which are possible in this context
      for (VariableSymbol variableIncarnation : incarnations) {
        ExpressionsBasisAdaptationVariant newVariant = getAdaptationContext().createVariant();
        // 1. Add strict binding for the selected variable
        // (Implicitly adds type bindings for variable type)
        try {
          newVariant.getBasicSymbolsBindings().addVariableBinding(Binding.createStrict(refVarSymbol, variableIncarnation));
        } catch (BindingConflictException e) {
          // This is unexpected as the current adaptation context should only return incarnations
          // that are valid in the current context, i.e., no conflicts with existing bindings.
          Log.warn("getIncarnations returned incarnation that conflicts with existing binding: "
                  + variableIncarnation.getFullName() + " in " + refExpr.get_SourcePositionStart(), e);
          continue;
        }
        // 2. Add bindings from the original model attached to the method
        BasicSymbolsBindings bindingsFromModel = getAdaptationContext().getOriginalBasicSymbolsIncMapping().getScopedBindings(variableIncarnation);
        try {
          newVariant.getBasicSymbolsBindings().addAll(bindingsFromModel);
        } catch (BindingConflictException e) {
          // This is expected as some bindings implied by the incarnation may not be compatible
          // with the existing bindings in the adaptation context.
          // We ignore this incarnation. Example: employee.firstName == employeeBuilder.lastName
          Log.debug("Ignoring incarnation due to binding conflict: "
                  + variableIncarnation.getFullName() + " in " + refExpr.get_SourcePositionStart(), LOG_NAME);
          continue;
        }
        // 3. Specify the AST Adaptation / transformation
        newVariant.addASTAdaptation(refExpr, adaptedNode -> {
          adaptedNode.setName(variableIncarnation.getName());
          return adaptedNode;
        });
        // 4. Add the new variant to the AST node
        getAdaptations4Ast().addVariant(refExpr, newVariant);
      }
    }
  }

  /**
   * Introduces one variant for each incarnation of the given function symbol.
   *
   * @param refExpr the ASTNameExpression that references the function symbol
   * @param refFunSymbol the FunctionSymbol from the reference model
   */
  protected void addVariantsForFunctionSymbol(ASTNameExpression refExpr, FunctionSymbol refFunSymbol) {
    Set<FunctionSymbol> incarnations = getAdaptationContext().getBasicSymbolsIncMapping().getIncarnations(refFunSymbol);
    if (incarnations.isEmpty()) {
      // no field symbol, use the constraints from the parent expression
      getAdaptations4Ast().addVariant(refExpr, getAdaptationContext().createVariant());
    } else {
      // we have the incarnations which are possible in this context
      for (FunctionSymbol functionIncarnation : incarnations) {
        ExpressionsBasisAdaptationVariant newVariant = getAdaptationContext().createVariant();
        // 1. Add strict binding for the selected variable
        // (Implicitly adds type bindings for variable type)
        try {
          newVariant.getBasicSymbolsBindings().addFunctionBinding(Binding.createStrict(refFunSymbol, functionIncarnation));
        } catch (BindingConflictException e) {
          // This is unexpected as the current adaptation context should only return incarnations
          // that are valid in the current context, i.e., no conflicts with existing bindings.
          Log.warn("getIncarnations returned incarnation that conflicts with existing binding: "
                  + functionIncarnation.getFullName() + " in " + refExpr.get_SourcePositionStart(), e);
          continue;
        }
        // 2. Add bindings from the original model attached to the method
        BasicSymbolsBindings bindingsFromModel = getAdaptationContext().getOriginalBasicSymbolsIncMapping().getScopedBindings(functionIncarnation);
        try {
          newVariant.getBasicSymbolsBindings().addAll(bindingsFromModel);
        } catch (BindingConflictException e) {
          // This is expected as some bindings implied by the incarnation may not be compatible
          // with the existing bindings in the adaptation context.
          // We ignore this incarnation. Example: employee.firstName == employeeBuilder.lastName
          Log.debug("Ignoring incarnation due to binding conflict: "
                  + functionIncarnation.getFullName() + " in " + refExpr.get_SourcePositionStart(), LOG_NAME);
          continue;
        }
        // 3. Specify the AST Adaptation / transformation
        newVariant.addASTAdaptation(refExpr, adaptedNode -> {
          adaptedNode.setName(functionIncarnation.getName());
          return adaptedNode;
        });
        // 4. Add the new variant to the AST node
        getAdaptations4Ast().addVariant(refExpr, newVariant);
      }
    }
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
