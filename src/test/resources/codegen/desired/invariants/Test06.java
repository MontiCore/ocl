/* (c) https://github.com/MontiCore/monticore */
package invariants;

import de.se_rwth.commons.logging.Log;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.*;
import javax.measure.unit.*;
import java.util.*;

import static de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.*;

public class Test06 {

    static Scope globalScope;

    private static List<OCLWitness> witnesses = new LinkedList<>();

    public static List<OCLWitness> getWitnesses() {
        return witnesses;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant0(EMAComponentSymbol cmp) {
        Map<String, Object> witnessElements = new HashMap<>();
        witnessElements.put("cmp", cmp);
        Boolean _OCLInvariant0 = true;
        try {
            EMAComponentSymbol _OCLQualifiedPrimary0 = cmp;
            Object _OCLNonNumberPrimary0 = null;
            Boolean _NotEqualsExpression0 = !_OCLQualifiedPrimary0.equals(_OCLNonNumberPrimary0);
            _OCLInvariant0 &= _NotEqualsExpression0;
        } catch (Exception _OCLInvariant0Exception) {
            _OCLInvariant0 = false;
            _OCLInvariant0Exception.printStackTrace();
            Log.error("Error while executing Test06.check_OCLInvariant0() !");
        }
        return _OCLInvariant0;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant1(EMAComponentSymbol cmp) {
        Map<String, Object> witnessElements = new HashMap<>();
        witnessElements.put("cmp", cmp);
        Boolean _OCLInvariant1 = true;
        try {
            String _OCLQualifiedPrimary1 = cmp.getName();
            String _OCLNonNumberPrimary1 = "";
            Boolean _NotEqualsExpression1 = !_OCLQualifiedPrimary1.equals(_OCLNonNumberPrimary1);
            _OCLInvariant1 &= _NotEqualsExpression1;
        } catch (Exception _OCLInvariant1Exception) {
            _OCLInvariant1 = false;
            _OCLInvariant1Exception.printStackTrace();
            Log.error("Error while executing Test06.check_OCLInvariant1() !");
        }
        return _OCLInvariant1;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant2(EMAComponentSymbol object) {
        Map<String, Object> witnessElements = new HashMap<>();
        witnessElements.put("this", object);
        Boolean _OCLInvariant2 = true;
        try {
            Integer _OCLQualifiedPrimary2 = object.getName().length();
            Integer _OCLNonNumberPrimary2 = 0;
            Boolean _GreaterThanExpression0 = _OCLQualifiedPrimary2 > _OCLNonNumberPrimary2;
            _OCLInvariant2 &= _GreaterThanExpression0;
            Integer _OCLQualifiedPrimary3 = object.getName().length();
            Integer _OCLNonNumberPrimary3 = 0;
            Boolean _GreaterThanExpression1 = _OCLQualifiedPrimary3 > _OCLNonNumberPrimary3;
            _OCLInvariant2 &= _GreaterThanExpression1;
        } catch (Exception _OCLInvariant2Exception) {
            _OCLInvariant2 = false;
            _OCLInvariant2Exception.printStackTrace();
            Log.error("Error while executing Test06.check_OCLInvariant2() !");
        }
        return _OCLInvariant2;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant3(EMAComponentSymbol cmp) {
        Map<String, Object> witnessElements = new HashMap<>();
        witnessElements.put("cmp", cmp);
        Boolean _OCLInvariant3 = true;
        try {
            Integer _OCLQualifiedPrimary4 = cmp.getSubComponents().size();
            Integer _OCLNonNumberPrimary4 = 0;
            Boolean _GreaterThanExpression2 = _OCLQualifiedPrimary4 > _OCLNonNumberPrimary4;
            _OCLInvariant3 &= _GreaterThanExpression2;
            Integer _OCLQualifiedPrimary5 = cmp.getPortsList().size();
            Integer _OCLNonNumberPrimary5 = 0;
            Boolean _GreaterThanExpression3 = _OCLQualifiedPrimary5 > _OCLNonNumberPrimary5;
            _OCLInvariant3 &= _GreaterThanExpression3;
        } catch (Exception _OCLInvariant3Exception) {
            _OCLInvariant3 = false;
            _OCLInvariant3Exception.printStackTrace();
            Log.error("Error while executing Test06.check_OCLInvariant3() !");
        }
        return _OCLInvariant3;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant4(EMAComponentSymbol cmp) {
        Map<String, Object> witnessElements = new HashMap<>();
        witnessElements.put("cmp", cmp);
        Boolean _OCLInvariant4 = true;
        try {
            String _OCLNonNumberPrimary6 = "Component";
            String _OCLNonNumberPrimary7 = "";
            String _OCLNonNumberPrimary8 = "Component";
            Boolean _OCLQualifiedPrimary6 = cmp.getName().replace(_OCLNonNumberPrimary6, _OCLNonNumberPrimary7).contains(_OCLNonNumberPrimary8);
            Boolean _LogicalNotExpression0 = !_OCLQualifiedPrimary6;
            _OCLInvariant4 &= _LogicalNotExpression0;
        } catch (Exception _OCLInvariant4Exception) {
            _OCLInvariant4 = false;
            _OCLInvariant4Exception.printStackTrace();
            Log.error("Error while executing Test06.check_OCLInvariant4() !");
        }
        return _OCLInvariant4;
    }
}
