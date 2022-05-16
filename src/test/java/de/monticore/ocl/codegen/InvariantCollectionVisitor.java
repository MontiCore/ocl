// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.codegen;

import de.monticore.ast.ASTNode;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.ocl._visitor.OCLVisitor2;

import java.util.HashSet;
import java.util.Set;

/**
 * collects invariants
 */
public class InvariantCollectionVisitor implements OCLVisitor2 {

  protected Set<ASTOCLInvariant> invariants = new HashSet<>();

  @Override
  public void visit(ASTOCLInvariant node) {
    invariants.add(node);
  }

  public Set<ASTOCLInvariant> getInvariants() {
    return invariants;
  }

  public static Set<ASTOCLInvariant> getInvariants(ASTNode node) {
    OCLTraverser traverser = OCLMill.traverser();
    InvariantCollectionVisitor visitor = new InvariantCollectionVisitor();
    traverser.add4OCL(visitor);
    node.accept(traverser);
    return visitor.getInvariants();
  }

}
