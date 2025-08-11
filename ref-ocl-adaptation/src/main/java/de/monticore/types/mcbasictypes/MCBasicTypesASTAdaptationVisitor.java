package de.monticore.types.mcbasictypes;

import de.monticore.cd.facade.MCQualifiedNameFacade;
import de.monticore.refadaptation.AbstractAdaptationVisitor;
import de.monticore.refadaptation.Binding;
import de.monticore.symbols.basicsymbols._symboltable.IBasicSymbolsScope;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType;
import de.monticore.types.mcbasictypes._symboltable.IMCBasicTypesScope;
import de.monticore.types.mcbasictypes._visitor.MCBasicTypesVisitor2;
import de.monticore.types3.TypeCheck3;
import de.se_rwth.commons.logging.Log;

import java.util.List;
import java.util.Optional;

public class MCBasicTypesASTAdaptationVisitor
        extends AbstractAdaptationVisitor<IMCBasicTypesAdaptationContext>
        implements MCBasicTypesVisitor2 {

  private static final String LOG_NAME = MCBasicTypesASTAdaptationVisitor.class.getName();

  @Override
  public void endVisit(ASTMCQualifiedType refType) {
    List<IMCBasicTypesAdaptationVariant> variants = getAdaptations4Ast().getVariants(refType);

    SymTypeExpression symType = TypeCheck3.symTypeFromAST(refType);

    // 2. We can now create a new ASTEqualsExpression with the adapted left and right expressions.
    for (IMCBasicTypesAdaptationVariant variant : variants) {
      // 3. create the adapted expression
      ASTMCQualifiedType adaptedType = refType.deepClone();

      if (symType.hasTypeInfo()) {
        TypeSymbol typeSymbol = symType.getTypeInfo();
        Optional<Binding<TypeSymbol>> binding = variant.getBasicSymbolsBindings().getBinding(typeSymbol);
        if (binding.isPresent()) {
          // a type binding attached to an MCType node is always required to be strict (??)
          TypeSymbol typeSymbolInc = binding.get().getStrictConcreteElement();
          adaptedType.setMCQualifiedName(MCQualifiedNameFacade
                  .createQualifiedName(typeSymbolInc.getFullName()));
        } else {
          // this is not an error. e.g., completely normal for "java.lang.String"
          Log.debug("No binding found for type symbol: " + typeSymbol.getFullName()
                  + ". Using original type name: " + refType.getMCQualifiedName(), LOG_NAME);
        }
      } else {
        Log.warn("Unexpected! ASTMCQualifiedType without type info: " + refType.getMCQualifiedName());
      }
      variant.setAdaptedNode(refType, adaptedType);
    }
  }

  @Override
  public void endVisit(ASTMCImportStatement refImport) {
    List<IMCBasicTypesAdaptationVariant> variants = getAdaptations4Ast().getVariants(refImport);

    IMCBasicTypesScope scope = refImport.getEnclosingScope();
    Optional<TypeSymbol> typeSymbol;
    if (scope instanceof IBasicSymbolsScope) {
      typeSymbol = ((IBasicSymbolsScope) scope).resolveType(refImport.getQName());
    } else {
      typeSymbol = Optional.empty();
    }

    for (IMCBasicTypesAdaptationVariant variant : variants) {
      ASTMCImportStatement adaptedImport = refImport.deepClone();

      if (typeSymbol.isPresent()) {
        Optional<Binding<TypeSymbol>> binding = variant.getBasicSymbolsBindings().getBinding(typeSymbol.get());
        if (binding.isPresent()) {
          // a type binding attached to an import is always required to be strict (??)
          TypeSymbol typeSymbolInc = binding.get().getStrictConcreteElement();
          adaptedImport.setMCQualifiedName(MCQualifiedNameFacade
                  .createQualifiedName(typeSymbolInc.getFullName()));
        }
      }
      variant.setAdaptedNode(refImport, adaptedImport);
    }
  }
}
