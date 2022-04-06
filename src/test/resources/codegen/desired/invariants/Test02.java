/* (c) https://github.com/MontiCore/monticore */
package invariants;

import de.se_rwth.commons.logging.Log;

public class Test02 {

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
            Integer _PlusExpression0 = _OCLQualifiedPrimary0 + _OCLQualifiedPrimary1;
            Integer _OCLNonNumberPrimary0 = 0;
            Boolean _NotEqualsExpression0 = !_PlusExpression0.equals(_OCLNonNumberPrimary0);
            _OCLInvariant0 &= _NotEqualsExpression0;
            Integer _OCLQualifiedPrimary2 = a;
            Integer _OCLQualifiedPrimary3 = b;
            Integer _MinusExpression0 = _OCLQualifiedPrimary2 - _OCLQualifiedPrimary3;
            Integer _OCLNonNumberPrimary1 = 0;
            Boolean _NotEqualsExpression1 = !_MinusExpression0.equals(_OCLNonNumberPrimary1);
            _OCLInvariant0 &= _NotEqualsExpression1;
            Integer _OCLQualifiedPrimary4 = a;
            Integer _OCLQualifiedPrimary5 = b;
            Integer _MultExpression0 = _OCLQualifiedPrimary4 * _OCLQualifiedPrimary5;
            Integer _OCLNonNumberPrimary2 = 0;
            Boolean _NotEqualsExpression2 = !_MultExpression0.equals(_OCLNonNumberPrimary2);
            _OCLInvariant0 &= _NotEqualsExpression2;
            Integer _OCLQualifiedPrimary6 = a;
            Integer _OCLQualifiedPrimary7 = b;
            Integer _DivideExpression0 = _OCLQualifiedPrimary6 / _OCLQualifiedPrimary7;
            Integer _OCLNonNumberPrimary3 = 0;
            Boolean _NotEqualsExpression3 = !_DivideExpression0.equals(_OCLNonNumberPrimary3);
            _OCLInvariant0 &= _NotEqualsExpression3;
            Integer _OCLQualifiedPrimary8 = a;
            Integer _OCLQualifiedPrimary9 = b;
            Integer _ModuloExpression0 = _OCLQualifiedPrimary8 % _OCLQualifiedPrimary9;
            Integer _OCLNonNumberPrimary4 = 0;
            Boolean _NotEqualsExpression4 = !_ModuloExpression0.equals(_OCLNonNumberPrimary4);
            _OCLInvariant0 &= _NotEqualsExpression4;
        } catch (Exception _OCLInvariant0Exception) {
            _OCLInvariant0 = false;
            _OCLInvariant0Exception.printStackTrace();
            Log.error("Error while executing Test02.check_OCLInvariant0() !");
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
            Amount<Duration> _OCLQualifiedPrimary10 = a;
            Amount<Duration> _OCLQualifiedPrimary11 = b;
            Amount<Duration> _PlusExpression1 = _OCLQualifiedPrimary10.plus(_OCLQualifiedPrimary11);
            Amount<Duration> _OCLNumberPrimary0 = (Amount<Duration>) Amount.valueOf("0 s");
            Boolean _NotEqualsExpression5 = !_PlusExpression1.approximates(_OCLNumberPrimary0);
            _OCLInvariant1 &= _NotEqualsExpression5;
            Amount<Duration> _OCLQualifiedPrimary12 = a;
            Amount<Duration> _OCLQualifiedPrimary13 = b;
            Amount<Duration> _MinusExpression1 = _OCLQualifiedPrimary12.minus(_OCLQualifiedPrimary13);
            Amount<Duration> _OCLNumberPrimary1 = (Amount<Duration>) Amount.valueOf("0 s");
            Boolean _NotEqualsExpression6 = !_MinusExpression1.approximates(_OCLNumberPrimary1);
            _OCLInvariant1 &= _NotEqualsExpression6;
            Amount<Duration> _OCLQualifiedPrimary14 = a;
            Amount<Duration> _OCLQualifiedPrimary15 = b;
            Amount<Angle> _MultExpression1 = _OCLQualifiedPrimary14.times(_OCLQualifiedPrimary15);
            Amount<Duration> _OCLNumberPrimary2 = (Amount<Duration>) Amount.valueOf("0 s");
            Boolean _NotEqualsExpression7 = !_MultExpression1.approximates(_OCLNumberPrimary2);
            _OCLInvariant1 &= _NotEqualsExpression7;
            Amount<Duration> _OCLQualifiedPrimary16 = a;
            Amount<Duration> _OCLQualifiedPrimary17 = b;
            Amount<Angle> _DivideExpression1 = _OCLQualifiedPrimary16.divide(_OCLQualifiedPrimary17);
            Amount<Duration> _OCLNumberPrimary3 = (Amount<Duration>) Amount.valueOf("0 s");
            Boolean _NotEqualsExpression8 = !_DivideExpression1.approximates(_OCLNumberPrimary3);
            _OCLInvariant1 &= _NotEqualsExpression8;
        } catch (Exception _OCLInvariant1Exception) {
            _OCLInvariant1 = false;
            _OCLInvariant1Exception.printStackTrace();
            Log.error("Error while executing Test02.check_OCLInvariant1() !");
        }
        return _OCLInvariant1;
    }
}
