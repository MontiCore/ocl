/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class OCLSMTPlugin implements Plugin<Project> {
  @Override
  public void apply(Project project) {
    project.getExtensions().getExtraProperties().set("OCLSemDiff", OCLSemDiffTask.class);
  }
}
