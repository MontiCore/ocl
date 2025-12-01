package de.monticore.ocl2smt.evaluation;
import de.monticore.ocl.ocl.OCLMill;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLInvariant;
import de.se_rwth.commons.logging.Log;

import java.io.IOException;
import java.util.Optional;

public class PerformanceOCLBuilder {
  Optional<ASTOCLCompilationUnit> buildOCL (int starSize, int chainSize, boolean diff) {
    OCLMill.init();
    try {
      Optional<ASTOCLCompilationUnit> ocl = OCLMill.parser().parse_String(
          String.format("ocl OCL%sStar%sChain%s{}"
              , diff, starSize, chainSize)
      );

      if (ocl.isEmpty()) {
        Log.error("Error while parsing the test model.");
        return ocl;
      }

      for (int i = 0; i < chainSize; i++) {
        String diffInv = "c.num > 5";
        if (diff && i == 0){
          diffInv = "c.num < 5";
        }
        Optional<ASTOCLInvariant> core = OCLMill.parser()
            .parse_StringOCLInvariant(String.format("  context Star%sCore c inv Star%sCoreInv :"
                + System.lineSeparator()
                + "  " + diffInv
                + " && !(c.text == \"\");"
                + System.lineSeparator()
                ,i,i));
        core.ifPresent(oclConstraint -> ocl.get().getOCLArtifact().addOCLConstraint(oclConstraint));

        for (int j = 0; j < starSize; j++) {
          Optional<ASTOCLInvariant> star = OCLMill.parser()
              .parse_StringOCLInvariant(String.format("  context Star%sLeaf%s l inv "
                      + "Star%sLeaf%sInv :"
                      + System.lineSeparator()
                      + "  l.num > 5"
                      + " && !(l.text == \"\");"
                      + System.lineSeparator()
                  ,i,j,i,j));
          star.ifPresent(oclConstraint -> ocl.get().getOCLArtifact().addOCLConstraint(oclConstraint));
        }
      }

      return ocl;

    } catch (IOException e) {
      Log.error("Error while parsing the test model: " + e.getMessage());
    }
    return Optional.empty();
  }

}
