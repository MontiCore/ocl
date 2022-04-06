/* (c) https://github.com/MontiCore/monticore */
package invariants;

import de.se_rwth.commons.logging.Log;

public class Test04 {

    static Scope globalScope;

    private static List<OCLWitness> witnesses = new LinkedList<>();

    public static List<OCLWitness> getWitnesses() {
        return witnesses;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant0(Boolean a, Boolean b) {
        Map<String, Object> witnessElements = new HashMap<>();
        witnessElements.put("a", a);
        witnessElements.put("b", b);
        Boolean _OCLInvariant0 = true;
        try {
            Boolean _OCLQualifiedPrimary0 = a;
            Boolean _OCLQualifiedPrimary1 = b;
            Boolean _EqualsExpression0 = _OCLQualifiedPrimary0.equals(_OCLQualifiedPrimary1);
            Boolean _IfThenElseExpr0;
            if (_EqualsExpression0) {
                Boolean _OCLQualifiedPrimary2 = a;
                _IfThenElseExpr0 = _OCLQualifiedPrimary2;
            } else {
                Boolean _OCLQualifiedPrimary3 = b;
                _IfThenElseExpr0 = _OCLQualifiedPrimary3;
            }
            _OCLInvariant0 &= _IfThenElseExpr0;
            Boolean _OCLQualifiedPrimary4 = a;
            Boolean _OCLQualifiedPrimary5 = b;
            Boolean _EqualsExpression1 = _OCLQualifiedPrimary4.equals(_OCLQualifiedPrimary5);
            Boolean _ConditionalExpression0;
            if (_EqualsExpression1) {
                Boolean _OCLQualifiedPrimary6 = a;
                _ConditionalExpression0 = _OCLQualifiedPrimary6;
            } else {
                Boolean _OCLQualifiedPrimary7 = b;
                _ConditionalExpression0 = _OCLQualifiedPrimary7;
            }
            _OCLInvariant0 &= _ConditionalExpression0;
            Boolean _OCLQualifiedPrimary8 = a;
            Boolean _InstanceOfExpression0 = _OCLQualifiedPrimary8 instanceof Boolean;
            _OCLInvariant0 &= _InstanceOfExpression0;
            Boolean _OCLQualifiedPrimary9 = a;
            Boolean _OCLQualifiedPrimary10 = b;
            Boolean _ImpliesExpression0 = !_OCLQualifiedPrimary9 || _OCLQualifiedPrimary10;
            _OCLInvariant0 &= _ImpliesExpression0;
        } catch (Exception _OCLInvariant0Exception) {
            _OCLInvariant0 = false;
            _OCLInvariant0Exception.printStackTrace();
            Log.error("Error while executing Test04.check_OCLInvariant0() !");
        }
        return _OCLInvariant0;
    }
}
