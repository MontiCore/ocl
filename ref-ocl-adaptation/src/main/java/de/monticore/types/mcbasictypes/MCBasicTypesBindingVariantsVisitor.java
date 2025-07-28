package de.monticore.types.mcbasictypes;

import de.monticore.refadaptation.AbstractAdaptationHandler;
import de.monticore.refadaptation.AbstractAdaptationVisitor;
import de.monticore.refadaptation.Binding;
import de.monticore.refadaptation.BindingConflictException;
import de.monticore.symbols.basicsymbols._symboltable.IBasicSymbolsScope;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;
import de.monticore.types.mcbasictypes._ast.ASTMCPrimitiveType;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.types.mcbasictypes._symboltable.IMCBasicTypesScope;
import de.monticore.types.mcbasictypes._visitor.MCBasicTypesHandler;
import de.monticore.types.mcbasictypes._visitor.MCBasicTypesTraverser;
import de.monticore.types.mcbasictypes._visitor.MCBasicTypesVisitor2;
import de.monticore.types3.TypeCheck3;
import de.monticore.visitor.ITraverser;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;
import java.util.Set;

public class MCBasicTypesBindingVariantsVisitor
        extends AbstractAdaptationHandler<MCBasicTypesAdaptationContext, MCBasicTypesAdaptationVariant>
        implements MCBasicTypesVisitor2, MCBasicTypesHandler {

  private static final String LOG_NAME = MCBasicTypesBindingVariantsVisitor.class.getName();

  private MCBasicTypesTraverser traverser;

  @Override
  public void setTraverser(MCBasicTypesTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public MCBasicTypesTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void handle(ASTMCQualifiedType node) {
    getAdaptations4Ast().clearVariants(node);
    MCBasicTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCPrimitiveType node) {
    getAdaptations4Ast().clearVariants(node);
    MCBasicTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCImportStatement node) {
    getAdaptations4Ast().clearVariants(node);
    MCBasicTypesHandler.super.handle(node);
  }

  @Override
  public void endVisit(ASTMCQualifiedType refType) {
    SymTypeExpression symType = TypeCheck3.symTypeFromAST(refType);
    if (symType.hasTypeInfo()) {
      TypeSymbol typeSymbol = symType.getTypeInfo();
      Set<TypeSymbol> incarnations = getAdaptationContext().getBasicSymbolsIncMapping().getIncarnations(typeSymbol);
      if (incarnations.isEmpty()) {
        // this is not an error. e.g., completely normal for "java.lang.String"
        Log.debug("No binding found for type symbol: " + typeSymbol.getFullName()
                + ". Using original type name: " + refType.getMCQualifiedName(), LOG_NAME);
        return;
      }
      for (TypeSymbol typeSymbolInc : incarnations) {
        MCBasicTypesAdaptationVariant adaptationVariant = getAdaptationContext().createVariant();
        try {
          adaptationVariant.getBasicSymbolsBindings().addTypeBinding(Binding.createStrict(typeSymbol, typeSymbolInc));
        } catch (BindingConflictException e) {
          // This is unexpected as the current adaptation context should only return incarnations
          // that are valid in the current context, i.e., no conflicts with existing bindings.
          Log.warn("getIncarnations returned incarnation that conflicts with existing binding: "
                  + typeSymbolInc.getFullName() + " in " + refType.get_SourcePositionStart(), e);
          continue;
        }
        getAdaptations4Ast().addVariant(refType, adaptationVariant);
      }
    } else {
      Log.warn("Unexpected! ASTMCQualifiedType without type info: " + refType.getMCQualifiedName());
    }
  }

  @Override
  public void endVisit(ASTMCPrimitiveType node) {
    // keep primitives as is
    getAdaptations4Ast().addVariant(node, getAdaptationContext().createVariant());
  }

  @Override
  public void endVisit(ASTMCImportStatement refImport) {
    IMCBasicTypesScope scope = refImport.getEnclosingScope();
    if (scope instanceof IBasicSymbolsScope) {
      Optional<TypeSymbol> typeSymbol = ((IBasicSymbolsScope) scope).resolveType(refImport.getQName());
      if (typeSymbol.isPresent()) {
        Set<TypeSymbol> incarnations = getAdaptationContext().getBasicSymbolsIncMapping().getIncarnations(typeSymbol.get());
        if (incarnations.isEmpty()) {
          // this is not an error. e.g., completely normal for "java.lang.String"
          Log.debug("No binding found for type symbol: " + typeSymbol.get().getFullName()
                  + ". Using original type name: " + refImport.getQName(), LOG_NAME);
          return;
        }
        for (TypeSymbol typeSymbolInc : incarnations) {
          MCBasicTypesAdaptationVariant adaptationVariant = getAdaptationContext().createVariant();
          try {
            adaptationVariant.getBasicSymbolsBindings().addTypeBinding(Binding.createStrict(typeSymbol.get(), typeSymbolInc));
          } catch (BindingConflictException e) {
            // This is unexpected as the current adaptation context should only return incarnations
            // that are valid in the current context, i.e., no conflicts with existing bindings.
            Log.warn("getIncarnations returned incarnation that conflicts with existing binding: "
                    + typeSymbolInc.getFullName() + " in " + refImport.get_SourcePositionStart(), e);
            continue;
          }
          getAdaptations4Ast().addVariant(refImport, adaptationVariant);
        }
      } else {
        // this is not an error. e.g., package star imports have no specific type symbol
        Log.debug("No type symbol found for import: " + refImport.getQName(), LOG_NAME);
      }
    }
  }
}
