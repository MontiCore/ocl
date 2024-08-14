// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types.check.types3wrapper;

import de.monticore.expressions.commonexpressions.types3.util.CommonExpressionsLValueRelations;
import de.monticore.ocl.ocl.types3.OCLTypeTraverserFactory;
import de.monticore.types.check.types3wrapper.TypeCheck3AsIDerive;
import de.monticore.types3.Type4Ast;

/**
 * OCL Defaults
 *
 * @deprecated use {@link de.monticore.ocl.ocl.types3.OCLTypeCheck3}
 */
@Deprecated
public class TypeCheck3AsOCLDeriver extends TypeCheck3AsIDerive {

  public TypeCheck3AsOCLDeriver() {
    super(null, new Type4Ast(), new CommonExpressionsLValueRelations());
    this.typeTraverser = new OCLTypeTraverserFactory().createTraverser(type4Ast);
  }
}
