/* (c) https://github.com/MontiCore/monticore */
package invariants;

import de.se_rwth.commons.logging.Log;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.*;
import javax.measure.unit.*;
import java.util.*;


public class Test05 {

    static Scope globalScope;

    private static List<OCLWitness> witnesses = new LinkedList<>();

    public static List<OCLWitness> getWitnesses() {
        return witnesses;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant0(List<String> names) {
        Map<String, Object> witnessElements = new HashMap<>();
        witnessElements.put("names", names);
        Boolean _OCLInvariant0 = true;
        try {
            Boolean _ForallExpr0 = true;
            List<String> _OCLQualifiedPrimary0 = names;
            for (String name : _OCLQualifiedPrimary0) {
                Integer _OCLQualifiedPrimary1 = name.length();
                Integer _OCLNonNumberPrimary0 = 0;
                Boolean _GreaterThanExpression0 = _OCLQualifiedPrimary1 > _OCLNonNumberPrimary0;
                _ForallExpr0 &= _GreaterThanExpression0;
                if (!_GreaterThanExpression0) {
                    witnessElements.put("name", name);
                }
            }
            _OCLInvariant0 &= _ForallExpr0;
        } catch (Exception _OCLInvariant0Exception) {
            _OCLInvariant0 = false;
            _OCLInvariant0Exception.printStackTrace();
            Log.error("Error while executing Test05.check_OCLInvariant0() !");
        }
        return _OCLInvariant0;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant1(List<String> names) {
        Map<String, Object> witnessElements = new HashMap<>();
        witnessElements.put("names", names);
        Boolean _OCLInvariant1 = true;
        try {
            Boolean _ExistsExpr0 = false;
            List<String> _OCLQualifiedPrimary2 = names;
            for (String name : _OCLQualifiedPrimary2) {
                Integer _OCLQualifiedPrimary3 = name.length();
                Integer _OCLNonNumberPrimary1 = 0;
                Boolean _GreaterThanExpression1 = _OCLQualifiedPrimary3 > _OCLNonNumberPrimary1;
                _ExistsExpr0 |= _GreaterThanExpression1;
            }
            _OCLInvariant1 &= _ExistsExpr0;
        } catch (Exception _OCLInvariant1Exception) {
            _OCLInvariant1 = false;
            _OCLInvariant1Exception.printStackTrace();
            Log.error("Error while executing Test05.check_OCLInvariant1() !");
        }
        return _OCLInvariant1;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant2() {
        Map<String, Object> witnessElements = new HashMap<>();
        Boolean _OCLInvariant2 = true;
        try {
            Integer _OCLNonNumberPrimary2 = 2;
            Integer a = _OCLNonNumberPrimary2;
            witnessElements.put("a", a);
            Integer _OCLNonNumberPrimary3 = 3;
            Integer b = _OCLNonNumberPrimary3;
            witnessElements.put("b", b);
            Integer _OCLQualifiedPrimary4 = a;
            Integer _OCLQualifiedPrimary5 = b;
            Boolean _LessThanExpression0 = _OCLQualifiedPrimary4 < _OCLQualifiedPrimary5;
            Boolean _LetinExpr0 = _LessThanExpression0;
            _OCLInvariant2 &= _LetinExpr0;
        } catch (Exception _OCLInvariant2Exception) {
            _OCLInvariant2 = false;
            _OCLInvariant2Exception.printStackTrace();
            Log.error("Error while executing Test05.check_OCLInvariant2() !");
        }
        return _OCLInvariant2;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant3(Number n, Integer a) {
        Map<String, Object> witnessElements = new HashMap<>();
        witnessElements.put("n", n);
        witnessElements.put("a", a);
        Boolean _OCLInvariant3 = true;
        try {
            Number _OCLQualifiedPrimary6 = n;
            Integer _TypeCastExpression0 = (Integer) _OCLQualifiedPrimary6;
            Integer _OCLQualifiedPrimary7 = a;
            Boolean _LessThanExpression1 = _TypeCastExpression0 < _OCLQualifiedPrimary7;
            _OCLInvariant3 &= _LessThanExpression1;
        } catch (Exception _OCLInvariant3Exception) {
            _OCLInvariant3 = false;
            _OCLInvariant3Exception.printStackTrace();
            Log.error("Error while executing Test05.check_OCLInvariant3() !");
        }
        return _OCLInvariant3;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant4(Integer a, Integer b, Integer c) {
        Map<String, Object> witnessElements = new HashMap<>();
        witnessElements.put("a", a);
        witnessElements.put("b", b);
        witnessElements.put("c", c);
        Boolean _OCLInvariant4 = true;
        try {
            Integer _OCLQualifiedPrimary8 = a;
            Integer _OCLQualifiedPrimary9 = b;
            Integer _PlusExpression0 = _OCLQualifiedPrimary8 + _OCLQualifiedPrimary9;
            Integer _ParenthizedExpression0 = (_PlusExpression0);
            Integer _OCLQualifiedPrimary10 = c;
            Integer _MultExpression0 = _ParenthizedExpression0 * _OCLQualifiedPrimary10;
            Integer _OCLNonNumberPrimary4 = 0;
            Boolean _GreaterThanExpression2 = _MultExpression0 > _OCLNonNumberPrimary4;
            _OCLInvariant4 &= _GreaterThanExpression2;
        } catch (Exception _OCLInvariant4Exception) {
            _OCLInvariant4 = false;
            _OCLInvariant4Exception.printStackTrace();
            Log.error("Error while executing Test05.check_OCLInvariant4() !");
        }
        return _OCLInvariant4;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant5() {
        Map<String, Object> witnessElements = new HashMap<>();
        Boolean _OCLInvariant5 = true;
        try {
            String _OCLNonNumberPrimary5 = "abc";
            String _OCLNonNumberPrimary6 = "def";
            String _PlusExpression1 = _OCLNonNumberPrimary5 + _OCLNonNumberPrimary6;
            Integer _ParenthizedExpression1 = (_PlusExpression1).length();
            _OCLInvariant5 &= _ParenthizedExpression1;
        } catch (Exception _OCLInvariant5Exception) {
            _OCLInvariant5 = false;
            _OCLInvariant5Exception.printStackTrace();
            Log.error("Error while executing Test05.check_OCLInvariant5() !");
        }
        return _OCLInvariant5;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant6() {
        Map<String, Object> witnessElements = new HashMap<>();
        Boolean _OCLInvariant6 = true;
        try {
            String _OCLNonNumberPrimary7 = "a";
            String _OCLNonNumberPrimary8 = "b";
            String _PlusExpression2 = _OCLNonNumberPrimary7 + _OCLNonNumberPrimary8;
            Integer _ParenthizedExpression2 = (_PlusExpression2).length();
            Integer _OCLNonNumberPrimary9 = 0;
            Boolean _GreaterThanExpression3 = _ParenthizedExpression2 > _OCLNonNumberPrimary9;
            _OCLInvariant6 &= _GreaterThanExpression3;
        } catch (Exception _OCLInvariant6Exception) {
            _OCLInvariant6 = false;
            _OCLInvariant6Exception.printStackTrace();
            Log.error("Error while executing Test05.check_OCLInvariant6() !");
        }
        return _OCLInvariant6;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant7() {
        Map<String, Object> witnessElements = new HashMap<>();
        Boolean _OCLInvariant7 = true;
        try {
            Boolean _OCLNonNumberPrimary10 = true;
            _OCLInvariant7 &= _OCLNonNumberPrimary10;
            Boolean _OCLNonNumberPrimary11 = false;
            _OCLInvariant7 &= _OCLNonNumberPrimary11;
        } catch (Exception _OCLInvariant7Exception) {
            _OCLInvariant7 = false;
            _OCLInvariant7Exception.printStackTrace();
            Log.error("Error while executing Test05.check_OCLInvariant7() !");
        }
        return _OCLInvariant7;
    }
}
