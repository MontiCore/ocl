/* (c) https://github.com/MontiCore/monticore */
package invariants;

import de.se_rwth.commons.logging.Log;

public class Test09 {

    static Scope globalScope;

    private static List<OCLWitness> witnesses = new LinkedList<>();

    public static List<OCLWitness> getWitnesses() {
        return witnesses;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant0(Set<String> names) {
        Map<String, Object> witnessElements = new HashMap<>();
        witnessElements.put("names", names);
        Boolean _OCLInvariant0 = true;
        try {
            String _OCLNonNumberPrimary0 = "name";
            Set<String> _OCLQualifiedPrimary0 = names;
            Boolean _IsInExpression0 = _OCLQualifiedPrimary0.contains(_OCLNonNumberPrimary0);
            _OCLInvariant0 &= _IsInExpression0;
        } catch (Exception _OCLInvariant0Exception) {
            _OCLInvariant0 = false;
            _OCLInvariant0Exception.printStackTrace();
            Log.error("Error while executing Test09.check_OCLInvariant0() !");
        }
        return _OCLInvariant0;
    }
}
