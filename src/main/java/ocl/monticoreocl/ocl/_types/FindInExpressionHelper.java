/* (c) https://github.com/MontiCore/monticore */
package ocl.monticoreocl.ocl._types;

import de.monticore.symboltable.MutableScope;
import de.monticore.types.types._ast.ASTType;
import de.monticore.umlcd4a.symboltable.references.CDTypeSymbolReference;
import ocl.monticoreocl.ocl._ast.ASTOCLNode;
import ocl.monticoreocl.ocl._visitor.OCLVisitor;
import ocl.monticoreocl.oclexpressions._ast.ASTInExpr;
import ocl.monticoreocl.oclexpressions._ast.ASTName2;
import ocl.monticoreocl.oclexpressions._ast.ASTOCLExpressionsNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static ocl.monticoreocl.ocl._types.TypeInferringHelper.flattenOnce;
import static ocl.monticoreocl.ocl._types.TypeInferringHelper.getContainerGeneric;

public class FindInExpressionHelper implements OCLVisitor {

  MutableScope scope;
  Map<String, CDTypeSymbolReference> inTypes = new LinkedHashMap<>();

  public FindInExpressionHelper(ASTOCLExpressionsNode node, MutableScope scope) {
    this.scope = scope;
    node.accept(this);
  }

  public void endVisit(final ASTInExpr astInExpr) {
    if (astInExpr.isPresentExpression()) {
      List<String> varNames = astInExpr.getVarNameList();
      OCLExpressionTypeInferingVisitor typeVisitor = new OCLExpressionTypeInferingVisitor(scope, false);
      CDTypeSymbolReference type = typeVisitor.getTypeFromExpression(astInExpr.getExpression());
      CDTypeSymbolReference type2 = type.getActualTypeArguments().isEmpty() ? type : getContainerGeneric(type);
      varNames.stream().forEach(s -> inTypes.put(s, type2));
    }
  }

  public Map<String, CDTypeSymbolReference> getInTypes() { return inTypes; }
}
