/* (c) https://github.com/MontiCore/monticore */
package invariants;

import de.se_rwth.commons.logging.Log;

public class Test08 {

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
            Collection _OCLComprehensionEnumerationStyle0 = new LinkedList<>();
            Collection _OCLComprehensionPrimary0 = (new LinkedList<>(_OCLComprehensionEnumerationStyle0));
            Collection a = _OCLComprehensionPrimary0;
            witnessElements.put("a", a);
            Collection<Integer> _OCLComprehensionExpressionStyle0 = new LinkedList<>();
            Set<String> _OCLQualifiedPrimary0 = names;
            for (String n : _OCLQualifiedPrimary0) {
                Integer _OCLNonNumberPrimary0 = 0;
                Integer i = _OCLNonNumberPrimary0;
                witnessElements.put("i", i);
                Integer _OCLQualifiedPrimary1 = n.length();
                Integer _OCLQualifiedPrimary2 = i;
                Boolean _GreaterThanExpression0 = _OCLQualifiedPrimary1 > _OCLQualifiedPrimary2;
                if (_GreaterThanExpression0) {
                    Integer _OCLQualifiedPrimary3 = n.length();
                    _OCLComprehensionExpressionStyle0.add(_OCLQualifiedPrimary3);
                }
            }
            Set<Integer> _OCLComprehensionPrimary1 = (new HashSet<>(_OCLComprehensionExpressionStyle0));
            Set<Integer> b = _OCLComprehensionPrimary1;
            witnessElements.put("b", b);
            Integer _OCLQualifiedPrimary4 = a.size();
            Integer _OCLQualifiedPrimary5 = b.size();
            Boolean _LessThanExpression0 = _OCLQualifiedPrimary4 < _OCLQualifiedPrimary5;
            Boolean _LetinExpr0 = _LessThanExpression0;
            _OCLInvariant0 &= _LetinExpr0;
        } catch (Exception _OCLInvariant0Exception) {
            _OCLInvariant0 = false;
            _OCLInvariant0Exception.printStackTrace();
            Log.error("Error while executing Test08.check_OCLInvariant0() !");
        }
        return _OCLInvariant0;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant1(Set<String> names) {
        Map<String, Object> witnessElements = new HashMap<>();
        witnessElements.put("names", names);
        Boolean _OCLInvariant1 = true;
        try {
            Collection _OCLComprehensionEnumerationStyle1 = new LinkedList<>();
            Integer _OCLComprehensionPrimary2 = (new LinkedList<>(_OCLComprehensionEnumerationStyle1)).size();
            Integer a = _OCLComprehensionPrimary2;
            witnessElements.put("a", a);
            Collection<Integer> _OCLComprehensionExpressionStyle1 = new LinkedList<>();
            Set<String> _OCLQualifiedPrimary6 = names;
            for (String n : _OCLQualifiedPrimary6) {
                Integer _OCLNonNumberPrimary1 = 0;
                Integer i = _OCLNonNumberPrimary1;
                witnessElements.put("i", i);
                Integer _OCLQualifiedPrimary7 = n.length();
                Integer _OCLQualifiedPrimary8 = i;
                Boolean _GreaterThanExpression1 = _OCLQualifiedPrimary7 > _OCLQualifiedPrimary8;
                if (_GreaterThanExpression1) {
                    Integer _OCLQualifiedPrimary9 = n.length();
                    _OCLComprehensionExpressionStyle1.add(_OCLQualifiedPrimary9);
                }
            }
            Set<Integer> _OCLComprehensionPrimary3 = (new HashSet<>(_OCLComprehensionExpressionStyle1));
            Set<Integer> b = _OCLComprehensionPrimary3;
            witnessElements.put("b", b);
            Integer _OCLQualifiedPrimary10 = a;
            Integer _OCLQualifiedPrimary11 = b.size();
            Boolean _LessThanExpression1 = _OCLQualifiedPrimary10 < _OCLQualifiedPrimary11;
            Boolean _LetinExpr1 = _LessThanExpression1;
            _OCLInvariant1 &= _LetinExpr1;
        } catch (Exception _OCLInvariant1Exception) {
            _OCLInvariant1 = false;
            _OCLInvariant1Exception.printStackTrace();
            Log.error("Error while executing Test08.check_OCLInvariant1() !");
        }
        return _OCLInvariant1;
    }
}
