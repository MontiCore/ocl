/* (c) https://github.com/MontiCore/monticore */
package invariants;

import de.se_rwth.commons.logging.Log;

public class Test01 {

    static Scope globalScope;

    private static List<OCLWitness> witnesses = new LinkedList<>();

    public static List<OCLWitness> getWitnesses() {
        return witnesses;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant0(Integer a, Integer b) {
        Map<String, Object> witnessElements = new HashMap<>();
        witnessElements.put("a", a);
        witnessElements.put("b", b);
        Boolean _OCLInvariant0 = true;
        try {
            Integer _OCLQualifiedPrimary0 = a;
            Integer _OCLQualifiedPrimary1 = b;
            Boolean _GreaterThanExpression0 = _OCLQualifiedPrimary0 > _OCLQualifiedPrimary1;
            _OCLInvariant0 &= _GreaterThanExpression0;
            Integer _OCLQualifiedPrimary2 = a;
            Integer _OCLQualifiedPrimary3 = b;
            Boolean _GreaterEqualExpression0 = _OCLQualifiedPrimary2 >= _OCLQualifiedPrimary3;
            _OCLInvariant0 &= _GreaterEqualExpression0;
            Integer _OCLQualifiedPrimary4 = a;
            Integer _OCLQualifiedPrimary5 = b;
            Boolean _LessThanExpression0 = _OCLQualifiedPrimary4 < _OCLQualifiedPrimary5;
            _OCLInvariant0 &= _LessThanExpression0;
            Integer _OCLQualifiedPrimary6 = a;
            Integer _OCLQualifiedPrimary7 = b;
            Boolean _LessEqualExpression0 = _OCLQualifiedPrimary6 <= _OCLQualifiedPrimary7;
            _OCLInvariant0 &= _LessEqualExpression0;
            Integer _OCLQualifiedPrimary8 = a;
            Integer _OCLQualifiedPrimary9 = b;
            Boolean _EqualsExpression0 = _OCLQualifiedPrimary8.equals(_OCLQualifiedPrimary9);
            _OCLInvariant0 &= _EqualsExpression0;
            Integer _OCLQualifiedPrimary10 = a;
            Integer _OCLQualifiedPrimary11 = b;
            Boolean _NotEqualsExpression0 = !_OCLQualifiedPrimary10.equals(_OCLQualifiedPrimary11);
            _OCLInvariant0 &= _NotEqualsExpression0;
        } catch (Exception _OCLInvariant0Exception) {
            _OCLInvariant0 = false;
            _OCLInvariant0Exception.printStackTrace();
            Log.error("Error while executing Test01.check_OCLInvariant0() !");
        }
        return _OCLInvariant0;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant1(Amount<Duration> a, Amount<Duration> b) {
        Map<String, Object> witnessElements = new HashMap<>();
        witnessElements.put("a", a);
        witnessElements.put("b", b);
        Boolean _OCLInvariant1 = true;
        try {
            Amount<Duration> _OCLQualifiedPrimary12 = a;
            Amount<Duration> _OCLQualifiedPrimary13 = b;
            Boolean _GreaterThanExpression1 = _OCLQualifiedPrimary12.isGreaterThan(_OCLQualifiedPrimary13);
            _OCLInvariant1 &= _GreaterThanExpression1;
            Amount<Duration> _OCLQualifiedPrimary14 = a;
            Amount<Duration> _OCLQualifiedPrimary15 = b;
            Boolean _GreaterEqualExpression1 = _OCLQualifiedPrimary14.isGreaterThan(_OCLQualifiedPrimary15);
            _GreaterEqualExpression1 |= _OCLQualifiedPrimary14.approximates(_OCLQualifiedPrimary15);
            _OCLInvariant1 &= _GreaterEqualExpression1;
            Amount<Duration> _OCLQualifiedPrimary16 = a;
            Amount<Duration> _OCLQualifiedPrimary17 = b;
            Boolean _LessThanExpression1 = _OCLQualifiedPrimary16.isLessThan(_OCLQualifiedPrimary17);
            _OCLInvariant1 &= _LessThanExpression1;
            Amount<Duration> _OCLQualifiedPrimary18 = a;
            Amount<Duration> _OCLQualifiedPrimary19 = b;
            Boolean _LessEqualExpression1 = _OCLQualifiedPrimary18.isLessThan(_OCLQualifiedPrimary19);
            _LessEqualExpression1 |= _OCLQualifiedPrimary18.approximates(_OCLQualifiedPrimary19);
            _OCLInvariant1 &= _LessEqualExpression1;
            Amount<Duration> _OCLQualifiedPrimary20 = a;
            Amount<Duration> _OCLQualifiedPrimary21 = b;
            Boolean _EqualsExpression1 = _OCLQualifiedPrimary20.approximates(_OCLQualifiedPrimary21);
            _OCLInvariant1 &= _EqualsExpression1;
            Amount<Duration> _OCLQualifiedPrimary22 = a;
            Amount<Duration> _OCLQualifiedPrimary23 = b;
            Boolean _NotEqualsExpression1 = !_OCLQualifiedPrimary22.approximates(_OCLQualifiedPrimary23);
            _OCLInvariant1 &= _NotEqualsExpression1;
        } catch (Exception _OCLInvariant1Exception) {
            _OCLInvariant1 = false;
            _OCLInvariant1Exception.printStackTrace();
            Log.error("Error while executing Test01.check_OCLInvariant1() !");
        }
        return _OCLInvariant1;
    }
}
