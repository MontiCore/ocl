// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.util;

import de.monticore.ast.ASTNode;
import de.se_rwth.commons.logging.Log;

@Deprecated
public class LogHelper {

  @Deprecated
  static ASTNode currentNode;
  
  /**
   * @deprecated since it is so easy to forget setting the currentNode
   */
  @Deprecated
  public static void error(String errorCode, String msg) {
    error(currentNode, errorCode, msg);
  }
  
  /**
   * @deprecated incorporate the error code directly into the message
   */
  @Deprecated
  public static void error(ASTNode node, String errorCode, String msg) {
    Log.error(errorCode + " " + node.get_SourcePositionStart() + " " + msg);
  }
  
  @Deprecated
  public static ASTNode getCurrentNode() {
    return currentNode;
  }
  
  @Deprecated
  public static void setCurrentNode(ASTNode currentNode) {
    LogHelper.currentNode = currentNode;
  }
}