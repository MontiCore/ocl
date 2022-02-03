/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen.util;

import java.util.HashMap;

import com.google.common.base.Preconditions;
import de.monticore.ast.ASTNode;

public class VariableNaming {

  // Map that keeps track of the number of variables that have already been generated of this type
  protected HashMap<Class<?>, Integer> counter = new HashMap<Class<?>, Integer>();

  // Maps each type to its number
  protected HashMap<Integer, Integer> instances = new HashMap<Integer, Integer>();

  public void reset() {
    counter.clear();
    instances.clear();
  }

  public String getName(ASTNode ast) {
    Preconditions.checkNotNull(ast);

    return getName(ast.getClass(), ast.hashCode());
  }

  protected String getName(Class<?> c, int hash) {

    // Assign number to hash if it has not been done before and increment counter
    if(!instances.containsKey(hash)) {
      int prev = 0;
      if(counter.containsKey(c))
        prev = counter.get(c);
      instances.put(hash, prev);
      counter.put(c, prev + 1);
    }

    String ret = "_";
    ret += c.getSimpleName().substring(3);
    ret += String.valueOf(instances.get(hash));
    return ret;
  }
}
