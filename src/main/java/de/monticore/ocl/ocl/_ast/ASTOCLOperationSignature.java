// (c) https://github.com/MontiCore/monticore

package de.monticore.ocl.ocl._ast;

import de.monticore.ocl.ocl._symboltable.OCLOperationConstraintData;

public interface ASTOCLOperationSignature extends ASTOCLOperationSignatureTOP {
  
  OCLOperationConstraintData getOperationData();
}
