// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.setexpressions._ast;

import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedName;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedNameBuilder;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedTypeBuilder;
import de.monticore.types.mcbasictypes._ast.ASTMCType;

public class ASTSetEnumeration extends ASTSetEnumerationTOP {

  public boolean isSet() {
    return getOpeningBracket().equals("{");
  }

  public boolean isList() {
    return getOpeningBracket().equals("[");
  }

  /** @deprecated was only needed for typecheck1 */
  @Deprecated
  public ASTMCType getMCType() {
    ASTMCQualifiedName qname;
    if (getOpeningBracket().equals("{")) {
      qname = new ASTMCQualifiedNameBuilder().addParts("Set").build();
    } else {
      qname = new ASTMCQualifiedNameBuilder().addParts("List").build();
    }
    qname.setEnclosingScope(getEnclosingScope());
    ASTMCQualifiedType mcType = new ASTMCQualifiedTypeBuilder().setMCQualifiedName(qname).build();
    mcType.setEnclosingScope(getEnclosingScope());
    return mcType;
  }
}
