package de.monticore.types.mcbasictypes;

import de.monticore.refadapt.AbstractAdaptationHandler;
import de.monticore.refmodel.BindingConflictException;
import de.monticore.symbols.basicsymbols._symboltable.IBasicSymbolsScope;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;
import de.monticore.types.mcbasictypes._ast.ASTMCPrimitiveType;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType;
import de.monticore.types.mcbasictypes._symboltable.IMCBasicTypesScope;
import de.monticore.types.mcbasictypes._visitor.MCBasicTypesHandler;
import de.monticore.types.mcbasictypes._visitor.MCBasicTypesTraverser;
import de.monticore.types.mcbasictypes._visitor.MCBasicTypesVisitor2;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;
import java.util.Set;

public class MCBasicTypesAdaptationVariantsVisitor
        extends AbstractAdaptationHandler<IMCBasicTypesAdaptationContext, IMCBasicTypesAdaptationVariant>
        implements MCBasicTypesVisitor2, MCBasicTypesHandler {

  private static final String LOG_NAME = MCBasicTypesAdaptationVariantsVisitor.class.getName();

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
    getVariants4Ast().clearVariants(node);
    MCBasicTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCPrimitiveType node) {
    getVariants4Ast().clearVariants(node);
    MCBasicTypesHandler.super.handle(node);
  }

  @Override
  public void handle(ASTMCImportStatement node) {
    getVariants4Ast().clearVariants(node);
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
        try {
          IMCBasicTypesAdaptationVariant adaptationVariant = getAdaptationContext()
                  .createVariantForIncarnation(typeSymbol, typeSymbolInc, refType.get_SourcePositionStart());
          getVariants4Ast().addVariant(refType, adaptationVariant);
        } catch (BindingConflictException e) {
          // add no variant for this incarnation.
        }
      }
    } else {
      Log.warn("Unexpected! ASTMCQualifiedType without type info: " + refType.getMCQualifiedName());
    }
  }

  @Override
  public void endVisit(ASTMCPrimitiveType node) {
    // keep primitives as is
    getVariants4Ast().addVariant(node, getAdaptationContext().createVariant());
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
          try {
            IMCBasicTypesAdaptationVariant adaptationVariant = getAdaptationContext()
                    .createVariantForIncarnation(typeSymbol.get(), typeSymbolInc, refImport.get_SourcePositionStart());
            getVariants4Ast().addVariant(refImport, adaptationVariant);
          } catch (BindingConflictException e) {
            // add no variant for this incarnation.
          }
        }
      } else {
        // this is not an error. e.g., package star imports have no specific type symbol
        Log.debug("No type symbol found for import: " + refImport.getQName(), LOG_NAME);
      }
    }
  }
}
