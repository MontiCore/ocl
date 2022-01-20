/* (c) https://github.com/MontiCore/monticore */
package invariants;

import de.se_rwth.commons.logging.Log;
import org.jscience.physics.amount.Amount;

import javax.measure.quantity.*;
import javax.measure.unit.*;
import java.util.*;


public class Test03 {

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
            Boolean _BooleanOrOpExpression0 = _OCLQualifiedPrimary0 || _OCLQualifiedPrimary1;
            _OCLInvariant0 &= _BooleanOrOpExpression0;
            Boolean _OCLQualifiedPrimary2 = a;
            Boolean _OCLQualifiedPrimary3 = b;
            Boolean _BooleanAndOpExpression0 = _OCLQualifiedPrimary2 && _OCLQualifiedPrimary3;
            _OCLInvariant0 &= _BooleanAndOpExpression0;
            Boolean _OCLQualifiedPrimary4 = a;
            Boolean _LogicalNotExpression0 = !_OCLQualifiedPrimary4;
            _OCLInvariant0 &= _LogicalNotExpression0;
            Boolean _OCLQualifiedPrimary5 = a;
            Boolean _BooleanNotExpression0 = !_OCLQualifiedPrimary5;
            _OCLInvariant0 &= _BooleanNotExpression0;
        } catch (Exception _OCLInvariant0Exception) {
            _OCLInvariant0 = false;
            _OCLInvariant0Exception.printStackTrace();
            Log.error("Error while executing Test03.check_OCLInvariant0() !");
        }
        return _OCLInvariant0;
    }
}
