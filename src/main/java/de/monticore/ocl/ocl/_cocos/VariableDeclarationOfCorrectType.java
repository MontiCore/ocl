/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.ocl._cocos; /* (c) https://github.com/MontiCore/monticore */

import de.monticore.ocl.oclexpressions._ast.ASTOCLVariableDeclaration;
import de.monticore.ocl.oclexpressions._cocos.OCLExpressionsASTOCLVariableDeclarationCoCo;
import de.monticore.ocl.types3.OCLSymTypeRelations;
import de.monticore.types.check.IDerive;
import de.monticore.types.check.ISynthesize;
import de.monticore.types.check.TypeCheckResult;
import de.monticore.types3.ISymTypeRelations;
import de.se_rwth.commons.logging.Log;

public class VariableDeclarationOfCorrectType
    implements OCLExpressionsASTOCLVariableDeclarationCoCo {

  /** @deprecated use other constructor */
  @Deprecated
  public VariableDeclarationOfCorrectType(IDerive iDerive, ISynthesize iSynthesize) {
    this(iDerive, iSynthesize, new OCLSymTypeRelations());
  }

  public VariableDeclarationOfCorrectType(
      IDerive iDerive, ISynthesize iSynthesize, ISymTypeRelations symTypeRelations) {
    setIDerive(iDerive);
    setISynthesize(iSynthesize);
    setSymTypeRelations(symTypeRelations);
  }

  protected IDerive iDerive;

  protected ISynthesize iSynthesize;

  protected ISymTypeRelations symTypeRelations;

  public IDerive getIDerive() {
    return iDerive;
  }

  public void setIDerive(IDerive iDerive) {
    this.iDerive = iDerive;
  }

  public ISynthesize getISynthesize() {
    return iSynthesize;
  }

  public void setISynthesize(ISynthesize iSynthesize) {
    this.iSynthesize = iSynthesize;
  }

  public void setSymTypeRelations(ISymTypeRelations symTypeRelations) {
    this.symTypeRelations = symTypeRelations;
  }

  @Override
  public void check(ASTOCLVariableDeclaration node) {
    if (!node.isPresentMCType()) {
      Log.error(
          String.format("0xOCL30 Variable at %s has no type.", node.get_SourcePositionStart()));
    } else {
      TypeCheckResult result = getIDerive().deriveType(node.getExpression());
      if (!result.isPresentResult() || result.getResult().isObscureType()) {
        Log.error(
            String.format(
                "0xOCL31 Type of Variable at %s could not be calculated.",
                node.get_SourcePositionStart()));
      }
      TypeCheckResult type = getISynthesize().synthesizeType(node.getMCType());
      if (!type.isPresentResult() || type.getResult().isObscureType()) {
        Log.error(
            String.format(
                "0xOCL32 Type of Variable at %s could not be calculated.",
                node.get_SourcePositionStart()));
      }
      if (!symTypeRelations.isCompatible(result.getResult(), type.getResult())) {
        Log.error(
            String.format(
                "0xOCL33 (%s): Type of variable %s is incompatible with expression type %s.",
                node.get_SourcePositionStart(),
                type.getResult().printFullName(),
                result.getResult().printFullName()));
      }
    }
  }
}
