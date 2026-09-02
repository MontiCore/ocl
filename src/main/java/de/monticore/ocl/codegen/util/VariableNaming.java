/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl.codegen.util;

import com.google.common.base.Preconditions;
import de.monticore.ast.ASTNode;
import java.util.LinkedHashMap;

/**
 * @deprecated is now part of MontiCore
 * @see <a href="https://github.com/MontiCore/monticore/blob/dev/monticore-grammar/src/main/java/de/monticore/codegen/javagen/JavaCodeGenerator.md">JavaCodeGenerator.md</a>
 */
@Deprecated(forRemoval = true)
public class VariableNaming {

  // Map that keeps track of the number of variables that have already been generated of this type
  protected LinkedHashMap<Class<?>, Integer> counter = new LinkedHashMap<Class<?>, Integer>();

  // Maps each type to its number
  protected LinkedHashMap<Integer, Integer> instances = new LinkedHashMap<Integer, Integer>();

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
    if (!instances.containsKey(hash)) {
      int prev = 0;
      if (counter.containsKey(c)) prev = counter.get(c);
      instances.put(hash, prev);
      counter.put(c, prev + 1);
    }

    String ret = "_";
    ret += c.getSimpleName().substring(3);
    ret += String.valueOf(instances.get(hash));
    return ret;
  }
}
