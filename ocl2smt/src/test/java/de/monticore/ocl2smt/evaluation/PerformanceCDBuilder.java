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
          .parse_String(String.format("classdiagram CDStar%sChain%s{ }", starSize, chainSize));

      if (cd.isEmpty()) {
        Log.error("Error while parsing the test model.");
        return cd;
      }

      for (int i = 0; i < chainSize; i++) {
        Optional<ASTCDClass> core = CD4CodeMill.parser()
            .parse_StringCDClass(String.format("  class Star%sCore{"
                +System.lineSeparator()
                +"    int num;"
                +System.lineSeparator()
                +"    String text",i));
        if (core.isPresent()) {
          cd.get().getCDDefinition().addCDElement(core.get());
        }
        if (i > 1) {
          Optional<ASTCDAssociation> chainAssoc = CD4CodeMill.parser()
              .parse_StringCDAssociation(String.format("  association Star%sCore -- Star%sCore;",i,i-1));
          if (chainAssoc.isPresent()) {
            cd.get().getCDDefinition().addCDElement(chainAssoc.get());
          }
        }
        for (int j = 0; j < starSize; j++) {
          Optional<ASTCDClass> star = CD4CodeMill.parser()
              .parse_StringCDClass(String.format("  class Star%sLeaf%s{"
                  +System.lineSeparator()
                  +"    int num;"
                  +System.lineSeparator()
                  +"    String text",i,j));
          if (star.isPresent()) {
            cd.get().getCDDefinition().addCDElement(star.get());
          }
          Optional<ASTCDAssociation> starAssoc =  CD4CodeMill.parser()
              .parse_StringCDAssociation(String.format("  association Star%sCore -- Star%sLeaf%s;",i,i,j));
          if (starAssoc.isPresent()) {
            cd.get().getCDDefinition().addCDElement(starAssoc.get());
          }
        }
      }
      return cd;

    } catch (IOException e){
      Log.error("Error while parsing the test model: " + e.getMessage());
    }
    return Optional.empty();
  }
}
