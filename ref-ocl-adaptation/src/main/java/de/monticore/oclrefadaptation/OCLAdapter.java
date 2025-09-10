package de.monticore.oclrefadaptation;

import de.monticore.cd4analysis._visitor.CD4AnalysisTraverser;
import de.monticore.cd4analysis.trafo.CDAssociationCreateFieldsFromAllRoles;
import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdconformance.CDConfParameter;
import de.monticore.cdconformance.CDConformanceChecker;
import de.monticore.cdconformance.inc.CDIncarnationMapping;
import de.monticore.ocl.OCLReferenceArtifactAdapter;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.symbols.oosymbols.refmodel.IOOSymbolsIncMapping;
import de.se_rwth.commons.logging.Log;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class OCLAdapter {

  public static final String DEFAULT_UNDERSPECIFIED_TYPE_NAME = "anytype";
  protected Set<CDConfParameter> confParams;

  protected String mappingName = "ref";

  protected String underspecifiedTypeName = DEFAULT_UNDERSPECIFIED_TYPE_NAME;

  protected OCLReferenceArtifactAdapter oclRefAdapter = OCLReferenceArtifactAdapter.create();

  public OCLAdapter(Set<CDConfParameter> confParams) {
    this.confParams = confParams;
  }

  public void adapt(
          File concreteCD,
          File refCD,
          Path refHwcPath,
          Path outputPath) {
    // TODO implement later
  }

  public ASTOCLCompilationUnit adapt(
          ASTCDCompilationUnit concreteCD,
          ASTCDCompilationUnit referenceCD,
          ASTOCLCompilationUnit refOCL) {
    List<ASTOCLCompilationUnit> adaptedOCL = adapt(concreteCD, referenceCD, List.of(refOCL));
    if (adaptedOCL.isEmpty()) {
      throw new IllegalStateException("Unexpected result: No adapted OCL artifacts as result for a " +
              "single reference artifact");
    }
    return adaptedOCL.get(0);
  }

  public List<ASTOCLCompilationUnit> adapt(
      ASTCDCompilationUnit concreteCD,
      ASTCDCompilationUnit referenceCD,
      List<ASTOCLCompilationUnit> refOCLArtifacts) {

    IOOSymbolsIncMapping incMapping = new CD4CAdaptedOOSymbolsMapping(
            createIncarnationMapping(concreteCD, referenceCD).asOOSymbolsIncMapping());

    List<ASTOCLCompilationUnit> adaptedArtifacts = new ArrayList<>();
    for (ASTOCLCompilationUnit refOCL : refOCLArtifacts) {
      List<ASTOCLCompilationUnit> adaptedOCL = oclRefAdapter.adapt(refOCL, incMapping);
      if (adaptedOCL.isEmpty()) {
        Log.warn("No OCL artifacts adapted for the given reference artifact. ");
      }
      adaptedArtifacts.addAll(adaptedOCL);
    }

    return adaptedArtifacts;
  }

  public void setUnderspecifiedTypeName(String underspecifiedTypeName) {
    Validate.notBlank(underspecifiedTypeName);
    if ("any".equals(underspecifiedTypeName)) {
      throw new IllegalArgumentException("'any' is a reserved keyword in OCL and cannot be used.");
    }
    this.underspecifiedTypeName = underspecifiedTypeName;
  }

  public void setMappingName(String mappingName) {
    Validate.notBlank(mappingName);
    this.mappingName = mappingName;
  }

  /**
   * Transforms the concrete and reference CDs such that fields are created for all roles in
   * each CD (see {@link CDAssociationCreateFieldsFromAllRoles}).
   * Additionally, we leverage the incarnation mapping to add stereotypes to the created
   * fields in the concrete CD, so they are recognized as incarnations of the reference fields
   * (see {@link CreateFieldFromAllRolesWithRefST}).<br>
   * <br>
   * The CDs are transformed in place.
   *
   * @param conCD the concrete class diagram
   * @param refCD the reference class diagram
   */
  protected void applyFieldsFromRolesTrafo(ASTCDCompilationUnit conCD, ASTCDCompilationUnit refCD) {
    // 1. create temporary incarnation mapping before transforming mapping before
    CDIncarnationMapping incMapping = createIncarnationMapping(conCD, refCD);
    // 2. transform the reference CD (nothing special)
    applyFieldsFromRolesTrafo(refCD, new CDAssociationCreateFieldsFromAllRoles());
    // 3. transform the concrete CD, using the incarnation mapping to associate created fields with
    // the reference fields via stereotype
    applyFieldsFromRolesTrafo(conCD, new CreateFieldFromAllRolesWithRefST(incMapping, mappingName,
            !confParams.contains(CDConfParameter.NAME_MAPPING)));
  }

  protected static void applyFieldsFromRolesTrafo(ASTCDCompilationUnit cdAST, CDAssociationCreateFieldsFromAllRoles trafo) {
    final CD4AnalysisTraverser traverser = CD4CodeMill.inheritanceTraverser();
    traverser.add4CDAssociation(trafo);
    traverser.setCDAssociationHandler(trafo);
    trafo.transform(cdAST);
  }

  /**
   * Creates an incarnation mapping for the given concrete and reference CDs, if the concrete CD
   * conforms to the reference CD.
   *
   * @param conCD the concrete class diagram
   * @param refCD the reference class diagram
   * @return the incarnation mapping, if conformance holds
   */
  protected CDIncarnationMapping createIncarnationMapping(ASTCDCompilationUnit conCD, ASTCDCompilationUnit refCD) {
    CDConformanceChecker checker = new CDConformanceChecker(confParams);
    // We need to override the default underspecified type name of the conformance checker because
    // 'any' is a reserved keyword in OCL!
    checker.setUnderspecifiedTypeName(underspecifiedTypeName);
    boolean conform = checker.checkConformance(conCD, refCD, mappingName);
    if (!conform) {
      // TODO Custom exception / error
      throw new IllegalStateException("Concrete CD does not conform to reference CD");
    }
    return checker.getIncarnationMapping();
  }
}
