package de.monticore.expressions.commonexpressions;

import de.monticore.expressions.commonexpressions._ast.*;
import de.monticore.refadaptation.Binding;
import de.monticore.refadaptation.BindingConflictException;
import de.monticore.symbols.basicsymbols.BasicSymbolsBindings;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symboltable.ISymbol;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Adaptation variant visitor for the CommonExpressions language.
 * <h5>Adaptations</h5>
 * <code>FieldAccessExpression</code>:
 * <ul>
 *   <li>One variant for each incarnation of the related VariableSymbol</li>
 *   <li>One variant for each incarnation of the related FunctionSymbol</li>
 * </ul>
 */
public class CommonExpressionsAdaptationVariantsVisitor
        extends CommonExpressionsAdaptationVariantsVisitorTOP {

  private static final String LOG_NAME = CommonExpressionsAdaptationVariantsVisitor.class.getName();

  @Override
  public void endVisit(ASTFieldAccessExpression refExpr) {
    /*
     * TODO maybe we can refactor this to a common helper method for introducing
     *     variants for each incarnation of some symbol?
     */
    // 1. Get the source symbol for the field name
    Optional<ISymbol> sourceSymbolOpt = TypeCheck3.typeOf(refExpr).getSourceInfo().getSourceSymbol();
    if (sourceSymbolOpt.isPresent()) {
      ISymbol sourceSymbol = sourceSymbolOpt.get();
      System.out.println("FieldAccessExpression Variable Source symbol: " + sourceSymbol);
      System.out.println("symbol full name: " + sourceSymbol.getFullName());
      // 2. identify variants depending on the symbol kind
      if (sourceSymbol instanceof VariableSymbol) {
        addVariantsForEachVariableIncarnation(refExpr, (VariableSymbol) sourceSymbol);
      } else if (sourceSymbol instanceof FunctionSymbol) {
        /*
         * This is required as FieldAccessExpressions are also used to represent method calls as
         * part of a CallExpression.
         */
        addVariantsForEachFunctionIncarnation(refExpr, (FunctionSymbol) sourceSymbol);
      }
    } else {
      passChildVariantsUpwards(refExpr, refExpr.getExpression());
    }
  }

  /**
   * Introduces one variant for each incarnation of the given variable symbol (including field
   * symbols).
   *
   * @param refExpr the ASTFieldAccessExpression that references the variable
   * @param refVariableSymbol the VariableSymbol form the reference model
   */
  protected void addVariantsForEachVariableIncarnation(ASTFieldAccessExpression refExpr, VariableSymbol refVariableSymbol) {
    // 1. get all variants of the parent expression
    List<CommonExpressionsAdaptationVariant> parentVariants = getAdaptations4Ast().getVariants(refExpr.getExpression());
    // 2. for each variant we can now check the available FieldSymbols incarnations
    for (CommonExpressionsAdaptationVariant parentVariant : parentVariants) {
      Set<VariableSymbol> incarnations = getAdaptationContext().getBasicSymbolsIncMapping().getIncarnations(refVariableSymbol);
      if (incarnations.isEmpty()) {
        // no field symbol, use the constraints from the parent expression
        getAdaptations4Ast().addVariant(refExpr, parentVariant);
        continue;
      }
      // we have the incarnations which are possible in this context
      List<CommonExpressionsAdaptationVariant> newVariants = new ArrayList<>();
      for (VariableSymbol fieldIncarnation : incarnations) {
        CommonExpressionsAdaptationVariant newVariant = parentVariant.copy();
        // 1. Add strict binding for the selected variable
        // (Implicitly adds type bindings for variable type)
        try {
          newVariant.getBasicSymbolsBindings().addVariableBinding(Binding.createStrict(refVariableSymbol, fieldIncarnation));
        } catch (BindingConflictException e) {
          // This is unexpected as the current adaptation context should only return incarnations
          // that are valid in the current context, i.e., no conflicts with existing bindings.
          Log.warn("getIncarnations returned incarnation that conflicts with existing binding: "
                  + fieldIncarnation.getFullName() + " in " + refExpr.get_SourcePositionStart(), e);
          continue;
        }
        // 2. Add bindings from the original model attached to the method
        BasicSymbolsBindings bindingsFromModel = getAdaptationContext().getOriginalBasicSymbolsIncMapping().getScopedBindings(fieldIncarnation);
        try {
          newVariant.getBasicSymbolsBindings().addAll(bindingsFromModel);
        } catch (BindingConflictException e) {
          // This is expected as some bindings implied by the incarnation may not be compatible
          // with the existing bindings in the adaptation context.
          // We ignore this incarnation. Example: employee.firstName == employeeBuilder.lastName
          Log.debug("Ignoring incarnation due to binding conflict: "
                  + fieldIncarnation.getFullName() + " in " + refExpr.get_SourcePositionStart(), LOG_NAME);
          continue;
        }
        // 3. Specify the AST Adaptation / transformation
        newVariant.addASTAdaptation(refExpr, adaptedNode -> {
          adaptedNode.setName(fieldIncarnation.getName());
          return adaptedNode;
        });
        getAdaptations4Ast().addVariant(refExpr, newVariant);
        newVariants.add(newVariant);
      }
      /*
       * IMPORTANT: If we do not replace the variants of the "parent" / left expression,
       * the subtree of the left expression looses connection to the higher level variants.
       * (same issue as in traverseForConsistentVariants)
       * TODO Try to introduce a helper method for this kind of adaption:
       *   input: - a list of source variants from some child expression
       *          - a function to generate X variants from a parent variant
       *   output: - a list of new variants that replace the parent variants
       *
       * TODO likely we can reuse the logic from 'AbstractAdaptationHandler.traverseForEachVariant'
       *  then traverseForEachVariant would reuse this new function and adds the traversal behavior
       *  there
       */
      getAdaptations4Ast().replaceVariant(parentVariant, newVariants);
    }
  }

  /**
   * Introduces one variant for each incarnation of the given function symbol (including method
   * symbols).
   *
   * @param refExpr the ASTFieldAccessExpression that references the function
   * @param refFunctionSymbol the FunctionSymbol from the reference model
   */
  protected void addVariantsForEachFunctionIncarnation(ASTFieldAccessExpression refExpr, FunctionSymbol refFunctionSymbol) {
    // 1. get all variants of the parent expression
    List<CommonExpressionsAdaptationVariant> parentVariants = getAdaptations4Ast().getVariants(refExpr.getExpression());
    // 2. for each variant we can now check the available FieldSymbols incarnations
    for (CommonExpressionsAdaptationVariant parentVariant : parentVariants) {
      Set<FunctionSymbol> incarnations = getAdaptationContext().getBasicSymbolsIncMapping().getIncarnations(refFunctionSymbol);
      if (incarnations.isEmpty()) {
        // no function symbol, use the constraints from the parent expression
        // TODO pass the parent variant upwards vs. error. vs. no variant?
        getAdaptations4Ast().addVariant(refExpr, parentVariant);
        continue;
      }
      List<CommonExpressionsAdaptationVariant> newVariants = new ArrayList<>();
      // we have the incarnations which are possible in this context
      for (FunctionSymbol incarnation : incarnations) {
        CommonExpressionsAdaptationVariant newVariant = parentVariant.copy();
        // 1. Add strict binding for the selected function
        // (Implicitly adds type bindings for return & parameter types)
        try {
          newVariant.getBasicSymbolsBindings().addFunctionBinding(Binding.createStrict(refFunctionSymbol, incarnation));
        } catch (BindingConflictException e) {
          // This is unexpected as the current adaptation context should only return incarnations
          // that are valid in the current context, i.e., no conflicts with existing bindings.
          Log.warn("getIncarnations returned incarnation that conflicts with existing binding: "
                  + incarnation.getFullName() + " in " + refExpr.get_SourcePositionStart(), e);
          continue;
        }
        // 2. Add bindings from the original model attached to the method
        BasicSymbolsBindings bindingsFromModel = getAdaptationContext().getOriginalBasicSymbolsIncMapping().getScopedBindings(incarnation);
        try {
          newVariant.getBasicSymbolsBindings().addAll(bindingsFromModel);
        } catch (BindingConflictException e) {
          // This is expected as some bindings implied by the incarnation may not be compatible
          // with the existing bindings in the adaptation context.
          // We ignore this incarnation. Example: employee.firstName == employeeBuilder.lastName
          Log.debug("Ignoring incarnation due to binding conflict: "
                  + incarnation.getFullName() + " in " + refExpr.get_SourcePositionStart(), LOG_NAME);
          continue;
        }
        // 3. Specify the AST Adaptation / transformation
        newVariant.addASTAdaptation(refExpr, adaptedNode -> {
          adaptedNode.setName(incarnation.getName());
          return adaptedNode;
        });
        getAdaptations4Ast().addVariant(refExpr, newVariant);
        newVariants.add(newVariant);
      }
      // TODO replace with helper method (see above)
      getAdaptations4Ast().replaceVariant(parentVariant, newVariants);
    }
  }

  @Override
  public void traverse(ASTEqualsExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTNotEqualsExpression node) {
    traverseForConsistentVariants(node, node.getLeft(), node.getRight());
  }

  @Override
  public void traverse(ASTLessThanExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTLessEqualExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTGreaterThanExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTGreaterEqualExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTBooleanAndOpExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTBooleanOrOpExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTPlusExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTMinusExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTMultExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTDivideExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTModuloExpression expr) {
    traverseForConsistentVariants(expr, expr.getLeft(), expr.getRight());
  }

  @Override
  public void traverse(ASTCallExpression callExpr) {
    traverseForConsistentVariants(callExpr, callExpr.getExpression(), callExpr.getArguments());
  }

  @Override
  public void endVisit(ASTBooleanNotExpression expr) {
    passChildVariantsUpwards(expr, expr.getExpression());
  }

  @Override
  public void endVisit(ASTLogicalNotExpression expr) {
    passChildVariantsUpwards(expr, expr.getExpression());
  }

  @Override
  public void endVisit(ASTBracketExpression expr) {
    passChildVariantsUpwards(expr, expr.getExpression());
  }
}
