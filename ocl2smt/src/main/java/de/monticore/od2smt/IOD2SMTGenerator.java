package de.monticore.od2smt;

import com.microsoft.z3.Context;
import com.microsoft.z3.Expr;
import de.monticore.cd2smt.Helper.IdentifiableBoolExpr;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odbasis._ast.ASTODObject;
import de.monticore.odlink._ast.ASTODLink;
import java.util.Set;

/***
 *this class is the interface of the OD2SMTGenerator
 */
public interface IOD2SMTGenerator {

  /***
   * this method transforms an object diagram  to smt
   * @param od the object diagram to be transformed
   * @param cd the class diagram associated to the object diagram
   * @param ctx the smt context
   */
  void od2smt(ASTODArtifact od, ASTCDCompilationUnit cd, Context ctx);

  /***
   * this method returns the smt-expression declared for the namedObject
   * @param namedObject the named object in the object diagram
   * @return the smt-expression
   */
  Expr<?> getObject(ASTODObject namedObject);

  /***
   * this method returns the smt-constrains defined for an object
   * @param object the object
   * @return the smt-constraint as BoolExpr
   */

  Set<IdentifiableBoolExpr> getObjectConstraints(ASTODObject object);

  /***
   * this methode returns the smt-constraints defined for a link.
   * @param link the link
   * @return the smt-constraint as BoolExpr
   */
  IdentifiableBoolExpr getLinkConstraint(ASTODLink link);

  /***
   *
   * @return all the constraints defined for thw object diagram
   */
  Set<IdentifiableBoolExpr> getAllODConstraints();
}
