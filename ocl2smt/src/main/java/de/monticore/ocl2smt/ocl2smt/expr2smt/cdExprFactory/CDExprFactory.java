package de.monticore.ocl2smt.ocl2smt.expr2smt.cdExprFactory;

import de.monticore.ocl2smt.ocl2smt.expr2smt.exprAdapter.ExprAdapter;
import de.monticore.ocl2smt.ocl2smt.expr2smt.exprFactory.ExprFactory;
import java.util.List;

/***
 * @author valdes-voufo
 * This interface is a factory to build  SMT expressions that can result form the
 * tranformation of the class diagram to SMT.
 * @param <E> the expression adapter used.
 */
public interface CDExprFactory<E extends ExprAdapter<?>> extends ExprFactory<E> {

  /***
   * This method access properties of an object. This can be an attribute or links.
   * @param obj the object which properties have to be accessed.
   * @param property  the property can be an attribute name or an association role.
   * @return the property as Expr.
   */
  E getLink(E obj, String property);

  /**
   * This method is a special case of the method getLink when the property can be transitive. this
   * is only possible for associations with the same type on both sides.
   *
   * @param obj the object which properties have to be accessed.
   * @param role the property must be a role of a reflexive association.
   * @return a set of Expr.
   */
  E getLinkTransitive(E obj, String role);

  /***
   * this function unwrap (Optional.get()) an optional expression resulting form a 1 ->[0..1] association.
   * @param opt the optional expression to be unwrapped.
   * @return the unwrap object as Expr.
   */
  E unWrap(E opt);

  /***
   * this method quantified object and depends on the strategy used to transform CD to SMT
   */
  E mkForall(List<E> params, E body);

  /***
   * this method quantified object and depends on the strategy used to transform CD to SMT
   */
  E mkExists(List<E> params, E body);
}
