// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.types.check.types3wrapper;

import de.monticore.ocl.ocl.types3.OCLTypeTraverserFactory;
import de.monticore.types.check.types3wrapper.TypeCheck3AsISynthesize;
import de.monticore.types3.Type4Ast;

/**
 * OCL Defaults
 */
public class TypeCheck3AsOCLSynthesizer extends TypeCheck3AsISynthesize {

  public TypeCheck3AsOCLSynthesizer() {
    super(null, new Type4Ast());
    this.typeTraverser = new OCLTypeTraverserFactory()
        .createTraverser(type4Ast);
  }

}
