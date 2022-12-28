package de.monticore.ocl2smt;

import com.microsoft.z3.Context;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLArtifact;

public class Wrapper {
  public void computeSemDiff(
      ASTCDCompilationUnit ast, Context ctx, ASTOCLArtifact in, ASTOCLArtifact notin) {}
}
