package de.monticore.ocl2smt.util;

import de.monticore.ast.ASTNode;
import de.monticore.expressions.commonexpressions._ast.ASTEqualsExpression;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsHandler;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsTraverser;
import de.monticore.expressions.commonexpressions._visitor.CommonExpressionsVisitor2;
import de.monticore.ocl.ocl._ast.ASTOCLArtifact;
import de.monticore.ocl.ocl._visitor.OCLTraverser;

public class OCLFieldAccessExpressionTrafo implements CommonExpressionsVisitor2 , CommonExpressionsHandler {
  private ASTOCLArtifact astoclArtifact ;
   OCLTraverser traverser ;
    @Override
    public void visit(ASTEqualsExpression node) {

    }

    @Override
    public CommonExpressionsTraverser getTraverser() {
        return traverser ;
    }

    @Override
    public void setTraverser(CommonExpressionsTraverser traverser) {
      this.traverser = (OCLTraverser)traverser ;
    }
}
