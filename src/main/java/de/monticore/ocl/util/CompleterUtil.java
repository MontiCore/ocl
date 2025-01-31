// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.util;

import com.google.common.collect.Iterables;
import de.monticore.ocl.ocl._symboltable.OCLScope;
import de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol;
import de.monticore.symbols.basicsymbols._symboltable.TypeSymbol;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.mcbasictypes._ast.ASTMCImportStatement;
import de.se_rwth.commons.Names;
import de.se_rwth.commons.logging.Log;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** @deprecated unused (at least in OCL) */
@Deprecated
public class CompleterUtil {
  protected static final String USED_BUT_UNDEFINED = "0xB0028: Type '%s' is used but not defined.";

  protected static final String DEFINED_MUTLIPLE_TIMES =
      "0xB0031: Type '%s' is defined more than once.";

  /*
   * computes possible full-qualified name candidates for the symbol named simpleName.
   * The symbol may be imported,
   * be located in the same package,
   * or be defined inside the model itself.
   */
  public static List<String> calcFQNameCandidates(
      List<ASTMCImportStatement> imports, String packageDeclaration, String simpleName) {
    List<String> fqNameCandidates = new ArrayList<>();
    for (ASTMCImportStatement anImport : imports) {
      if (anImport.isStar()) {
        // star import imports everything one level below the qualified model element
        fqNameCandidates.add(anImport.getQName() + "." + simpleName);
      } else if (Names.getSimpleName(anImport.getQName()).equals(simpleName)) {
        // top level symbol that has the same name as the node, e.g. diagram symbol
        fqNameCandidates.add(anImport.getQName());
      }
    }
    // The searched symbol might be located in the same package as the artifact
    if (!packageDeclaration.isEmpty()) {
      fqNameCandidates.add(packageDeclaration + "." + simpleName);
    }

    // Symbol might be defined in the model itself
    fqNameCandidates.add(simpleName);

    return fqNameCandidates;
  }

  public static void visit(
      FunctionSymbol function, List<ASTMCImportStatement> imports, String packageDeclaration) {
    String typeName = function.getType().getTypeInfo().getName();
    Set<TypeSymbol> typeSymbols = new HashSet<>();
    for (String fqNameCandidate : calcFQNameCandidates(imports, packageDeclaration, typeName)) {
      OCLScope scope = (OCLScope) function.getEnclosingScope();
      typeSymbols.addAll(scope.resolveTypeMany(fqNameCandidate));
      // typeSymbols.addAll(scope.resolveOOTypeMany(fqNameCandidate));
    }

    if (typeSymbols.isEmpty()) {
      Log.error(
          String.format(USED_BUT_UNDEFINED, typeName),
          function.getAstNode().get_SourcePositionStart());
    } else if (typeSymbols.size() > 1) {
      Log.error(
          String.format(DEFINED_MUTLIPLE_TIMES, typeName),
          function.getAstNode().get_SourcePositionStart());
    } else {
      TypeSymbol typeSymbol = Iterables.getFirst(typeSymbols, null);
      function.setType(SymTypeExpressionFactory.createTypeExpression(typeSymbol));
    }
  }

  public static void visit(
      TypeSymbol type, List<ASTMCImportStatement> imports, String packageDeclaration) {
    for (SymTypeExpression sym : type.getSuperTypesList()) {
      String typeName = sym.getTypeInfo().getName();
      Set<TypeSymbol> typeSymbols = new HashSet<>();
      for (String fqNameCandidate : calcFQNameCandidates(imports, packageDeclaration, typeName)) {
        OCLScope scope = (OCLScope) type.getEnclosingScope();
        typeSymbols.addAll(scope.resolveTypeMany(fqNameCandidate));
        // typeSymbols.addAll(scope.resolveOOTypeMany(fqNameCandidate));
      }

      if (typeSymbols.isEmpty()) {
        Log.error(
            String.format(USED_BUT_UNDEFINED, typeName),
            type.getAstNode().get_SourcePositionStart());
      } else if (typeSymbols.size() > 1) {
        Log.error(
            String.format(DEFINED_MUTLIPLE_TIMES, typeName),
            type.getAstNode().get_SourcePositionStart());
      } else {
        List<SymTypeExpression> superTypes = type.getSuperTypesList();
        superTypes.remove(sym);
        superTypes.add(
            SymTypeExpressionFactory.createTypeExpression(Iterables.getFirst(typeSymbols, null)));
      }
    }
  }

  public static void visit(
      VariableSymbol var, List<ASTMCImportStatement> imports, String packageDeclaration) {
    String typeName = var.getType().getTypeInfo().getName();
    Set<TypeSymbol> typeSymbols = new HashSet<>();
    for (String fqNameCandidate : calcFQNameCandidates(imports, packageDeclaration, typeName)) {
      OCLScope scope = (OCLScope) var.getEnclosingScope();
      typeSymbols.addAll(scope.resolveTypeMany(fqNameCandidate));
      // typeSymbols.addAll(scope.resolveOOTypeMany(fqNameCandidate));
    }

    if (typeSymbols.isEmpty()) {
      Log.error(
          String.format(USED_BUT_UNDEFINED, typeName), var.getAstNode().get_SourcePositionStart());
    } else if (typeSymbols.size() > 1) {
      Log.error(
          String.format(DEFINED_MUTLIPLE_TIMES, typeName),
          var.getAstNode().get_SourcePositionStart());
    } else {
      TypeSymbol typeSymbol = Iterables.getFirst(typeSymbols, null);
      var.setType(SymTypeExpressionFactory.createTypeExpression(typeSymbol));
    }
  }
}
