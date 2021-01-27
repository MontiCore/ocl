// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._symboltable;

import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;

import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO
 *
 * @since 27.01.21
 */
public class IOCLGlobalScopeHelper {
  public static List<OCLArtifactScope> getArtifactScopes(IOCLGlobalScope globalScope) {
    return globalScope.getSubScopes().stream()
      .filter(s -> s instanceof OCLArtifactScope)
      .map(s -> (OCLArtifactScope)s)
      .collect(Collectors.toList());
  }

  public static List<ASTOCLCompilationUnit> getCompilationUnits(IOCLGlobalScope globalScope) {
    return getArtifactScopes(globalScope).stream()
      .map(OCLScope::getAstNode)
      .map(s -> (ASTOCLCompilationUnit)s)
      .collect(Collectors.toList());
  }
}
