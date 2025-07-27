package de.monticore.oclrefadaptation;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.ocl.OCLReferenceArtifactAdapter;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.symbols.OOSymbolsIncMapping;
import de.se_rwth.commons.logging.Log;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OCLAdapter {

  public static final String DEFAULT_UNDERSPECIFIED_TYPE_NAME = "anytype";
  protected Set<CDConfParameter> confParams;
  protected String underspecifiedTypeName = DEFAULT_UNDERSPECIFIED_TYPE_NAME;

  protected OCLReferenceArtifactAdapter oclRefAdapter = OCLReferenceArtifactAdapter.create();

  public OCLAdapter(Set<CDConfParameter> confParams) {
    this.confParams = confParams;
  }

  public void adapt(
          File concreteCD,
          File refCD,
          String mapping,
          Path refHwcPath,
          Path outputPath) {
    // TODO implement later
  }

  public ASTOCLCompilationUnit adapt(
          ASTCDCompilationUnit concreteCD,
          ASTCDCompilationUnit referenceCD,
          String mapping,
          ASTOCLCompilationUnit refOCL) {
    List<ASTOCLCompilationUnit> adaptedOCL = adapt(concreteCD, referenceCD, mapping, List.of(refOCL));
    if (adaptedOCL.isEmpty()) {
      throw new IllegalStateException("Unexpected result: No adapted OCL artifacts as result for a " +
              "single reference artifact");
    }
    return adaptedOCL.get(0);
  }

  public List<ASTOCLCompilationUnit> adapt(
      ASTCDCompilationUnit concreteCD,
      ASTCDCompilationUnit referenceCD,
      String mapping,
      List<ASTOCLCompilationUnit> refOCLArtifacts) {

    CDConformanceChecker checker = new CDConformanceChecker(confParams);
    // We need to override the default underspecified type name of the conformance checker because
    // 'any' is a reserved keyword in OCL!
    checker.setUnderspecifiedTypeName(underspecifiedTypeName);

    boolean conform = checker.checkConformance(concreteCD, referenceCD, mapping);
    if (!conform) {
      // TODO Custom exception
      throw new IllegalStateException("Concrete CD does not conform to reference CD");
    }

    OOSymbolsIncMapping incMapping = new LegacyCDIncarnationMapping2OOSymbolsIncMapping(checker.getIncarnationMapping());

    List<ASTOCLCompilationUnit> adaptedArtifacts = new ArrayList<>();
    for (ASTOCLCompilationUnit refOCL : refOCLArtifacts) {
      List<ASTOCLCompilationUnit> adaptedOCL = oclRefAdapter.adapt(refOCL, CD4CodeMill.globalScope(), incMapping);
      if (adaptedOCL.isEmpty()) {
        Log.warn("0xA1234 No OCL artifacts adapted for the given reference artifact. ");
      }
      adaptedArtifacts.addAll(adaptedOCL);
    }

    return adaptedArtifacts;
  }

  public void setUnderspecifiedTypeName(String underspecifiedTypeName) {
    if ("any".equals(underspecifiedTypeName)) {
      throw new IllegalArgumentException("'any' is a reserved keyword in OCL and cannot be used.");
    }
    this.underspecifiedTypeName = underspecifiedTypeName;
  }
}
