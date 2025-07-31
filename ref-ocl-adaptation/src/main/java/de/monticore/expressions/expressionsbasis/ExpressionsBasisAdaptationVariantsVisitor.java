package de.monticore.expressions.expressionsbasis;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.expressions.expressionsbasis._ast.ASTLiteralExpression;
import de.monticore.expressions.expressionsbasis._ast.ASTNameExpression;
import de.monticore.expressions.expressionsbasis._visitor.ExpressionsBasisVisitor2;
import de.monticore.refadaptation.AbstractAdaptationVisitor;
import de.monticore.refadaptation.Binding;
import de.monticore.refadaptation.BindingConflictException;
import de.monticore.symbols.basicsymbols.BasicSymbolsBindings;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.symboltable.ISymbol;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;
import java.util.Set;

public class ExpressionsBasisAdaptationVariantsVisitor
        extends AbstractAdaptationVisitor<ExpressionsBasisAdaptationContext>
        implements ExpressionsBasisVisitor2 {

  private static final String LOG_NAME = ExpressionsBasisAdaptationVariantsVisitor.class.getName();

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
    } else {
      // TODO add support for FunctionSymbol here -> NameExpression can be part of method call
      Log.warn("Unexpected symbol type: " + refSymbol.getClass().getSimpleName() + " for NameExpression: " + refExpr.get_SourcePositionStart());
      getAdaptations4Ast().addVariant(refExpr, getAdaptationContext().createVariant());
    }
  }

  protected void addVariantsForVariableSymbol(ASTNameExpression refExpr, VariableSymbol refVarSymbol) {
    // If we have a VariableSymbol, get all incarnations and create variants for it
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
