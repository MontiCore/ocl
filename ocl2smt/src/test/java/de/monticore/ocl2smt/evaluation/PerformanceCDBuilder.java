package de.monticore.ocl2smt.evaluation;

import de.monticore.cd4code.CD4CodeMill;
import de.monticore.cdassociation._ast.ASTCDAssociation;
import de.monticore.cdbasis._ast.ASTCDClass;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.se_rwth.commons.logging.Log;

import java.io.IOException;
import java.util.Optional;

public class PerformanceCDBuilder {
  Optional<ASTCDCompilationUnit> buildCD(int starSize, int chainSize){
    CD4CodeMill.init();
    CD4CodeMill.globalScope().clear();
    try {
      Optional<ASTCDCompilationUnit> cd = CD4CodeMill.parser()
          .parse_String(
              String.format("import java.lang.String;"
                      + System.lineSeparator()
                      + "classdiagram CDStar%sChain%s{}"
                  , starSize, chainSize));

      if (cd.isEmpty()) {
        Log.error("Error while parsing the test model.");
        return cd;
      }

      for (int i = 0; i < chainSize; i++) {
        Optional<ASTCDClass> core = CD4CodeMill.parser()
            .parse_StringCDClass(String.format("  class Star%sCore{"
                + System.lineSeparator()
                + "    int num;"
                + System.lineSeparator()
                + "    String text;"
                +System.lineSeparator()
                + "  }",i));
        core.ifPresent(astcdClass -> cd.get().getCDDefinition().addCDElement(astcdClass));
        if (i > 1) {
          Optional<ASTCDAssociation> chainAssoc = CD4CodeMill.parser()
              .parse_StringCDAssociation(String.format("  association Star%sCore -- Star%sCore;",i,i-1));
          chainAssoc.ifPresent(
              astcdAssociation -> cd.get().getCDDefinition().addCDElement(astcdAssociation));
        }
        for (int j = 0; j < starSize; j++) {
          Optional<ASTCDClass> star = CD4CodeMill.parser()
              .parse_StringCDClass(String.format("  class Star%sLeaf%s{"
                  +System.lineSeparator()
                  +"    int num;"
                  +System.lineSeparator()
                  +"    String text;"
                  +System.lineSeparator()
                  + "  }",i,j));
          star.ifPresent(astcdClass -> cd.get().getCDDefinition().addCDElement(astcdClass));
          Optional<ASTCDAssociation> starAssoc =  CD4CodeMill.parser()
              .parse_StringCDAssociation(String.format("  association Star%sCore -- Star%sLeaf%s;",i,i,j));
          starAssoc.ifPresent(
              astcdAssociation -> cd.get().getCDDefinition().addCDElement(astcdAssociation));
        }
      }
      return cd;

    } catch (IOException e){
      Log.error("Error while parsing the test model: " + e.getMessage());
    }
    return Optional.empty();
  }
}
