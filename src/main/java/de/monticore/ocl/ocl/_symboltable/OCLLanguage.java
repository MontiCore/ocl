/*
 *  * (c) https://github.com/MontiCore/monticore
 *  *
 *  * The license generally applicable for this project
 *  * can be found under https://github.com/MontiCore/monticore.
 */

package de.monticore.ocl.ocl._symboltable;

public class OCLLanguage extends OCLLanguageTOP {

  public static final String FILE_ENDING = "ocl";

  public OCLLanguage() {
    super("OCL Language", FILE_ENDING);
  }

  @Override
  protected OCLModelLoader provideModelLoader() {
    return new OCLModelLoader(this);
  }
}
