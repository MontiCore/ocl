// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.util;

import de.monticore.ast.ASTNode;
import de.se_rwth.commons.logging.Log;

public class LogHelper {

  static ASTNode currentNode;

  public static void error(String errorCode, String msg) {
    error(currentNode, errorCode, msg);
  }

  public static void error(ASTNode node, String errorCode, String msg) {
    Log.error(errorCode + " " + node.get_SourcePositionStart() + " " + msg);
  }

  public static ASTNode getCurrentNode() {
    return currentNode;
  }

  public static void setCurrentNode(ASTNode currentNode) {
    LogHelper.currentNode = currentNode;
  }
}
