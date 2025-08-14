package de.monticore.refadapt;

import de.monticore.ast.ASTNode;

/**
 * Utility class for reference artifact adaptation<br>
 * Specifically, it is used to reduce duplication in language specific (generated) code because in
 * fact we do not generate any code (yet).
 */
public class RefAdaptationUtils {

  private RefAdaptationUtils() {
  }

  public static void deepCloneComments(ASTNode adapted, ASTNode original) {
    for (de.monticore.ast.Comment x : original.get_PreCommentList()) {
      adapted.get_PreCommentList().add(new de.monticore.ast.Comment(x.getText()));
    }
    for (de.monticore.ast.Comment x : original.get_PostCommentList()) {
      adapted.get_PostCommentList().add(new de.monticore.ast.Comment(x.getText()));
    }
  }
}
