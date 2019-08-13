/* (c) https://github.com/MontiCore/monticore */
package ocl.monticoreocl.ocl._ast;

import ocl.monticoreocl.oclexpressions._ast.ASTOCLQualifiedPrimary;

import java.util.Optional;

public class ASTOCLConstructorSignature extends ASTOCLConstructorSignatureTOP {

  public ASTOCLConstructorSignature() {
    super();
  }

  public ASTOCLConstructorSignature (/* generated by template ast.ConstructorParametersDeclaration*/
      ocl.monticoreocl.oclexpressions._ast.ASTName2 referenceType2
      ,
      ocl.monticoreocl.ocl._ast.ASTOCLParameters oCLParameters
      ,
      Optional<ASTOCLThrowsClause> oCLThrowsClause

  ) {
    super(referenceType2, oCLParameters, oCLThrowsClause);
  }

  public String getReferenceType() {
    return ASTOCLQualifiedPrimary.name2String(getReferenceType2());
  }
}
