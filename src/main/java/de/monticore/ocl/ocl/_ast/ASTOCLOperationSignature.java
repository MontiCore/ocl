package de.monticore.ocl.ocl._ast;

import de.monticore.ocl.ocl._symboltable.OCLOperationData;

public interface ASTOCLOperationSignature extends ASTOCLOperationSignatureTOP {
  
  OCLOperationData getOperationData();
}
