package de.monticore.ocl;

import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.ocl._visitor.OCLHandler;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.ocl._visitor.OCLVisitor2;
import de.monticore.refadaptation.AbstractAdaptationVisitor;

public class OCLBindingVariantsVisitor extends AbstractAdaptationVisitor implements OCLVisitor2, OCLHandler {

  private OCLTraverser traverser;

  @Override
  public OCLTraverser getTraverser() {
    return traverser;
  }

  @Override
  public void setTraverser(OCLTraverser traverser) {
    this.traverser = traverser;
  }

  @Override
  public void traverse(ASTOCLCompilationUnit node) {
    OCLHandler.super.traverse(node);
  }

  @Override
  public void traverse(ASTOCLInvariant refInvariant) {
    // 1. identify context definition
    // 2. for each context definition variant, identify the invariant expression variants

    // TODO refactor after -> extract helper methods that help with the usual
    //   chaining / constraint propagation form one child to another
    // Idea: we only pass a list of ASTNode instances to a function and it will do for each
  }
}
