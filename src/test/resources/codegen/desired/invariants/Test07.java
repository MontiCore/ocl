/* (c) https://github.com/MontiCore/monticore */
package invariants;

import de.se_rwth.commons.logging.Log;

import static de.monticore.lang.embeddedmontiarc.embeddedmontiarc._symboltable.cncModel.*;

public class Test07 {

    static Scope globalScope;

    private static List<OCLWitness> witnesses = new LinkedList<>();

    public static List<OCLWitness> getWitnesses() {
        return witnesses;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant0(List<EMAComponentSymbol> cmps) {
        Map<String, Object> witnessElements = new HashMap<>();
        witnessElements.put("cmps", cmps);
        Boolean _OCLInvariant0 = true;
        try {
            Boolean _ForallExpr0 = true;
            List<EMAComponentSymbol> _OCLQualifiedPrimary0 = cmps;
            for (EMAComponentSymbol cmp : _OCLQualifiedPrimary0) {
                Integer _OCLQualifiedPrimary1 = cmp.getName().length();
                Integer _OCLNonNumberPrimary0 = 0;
                Boolean _GreaterThanExpression0 = _OCLQualifiedPrimary1 > _OCLNonNumberPrimary0;
                _ForallExpr0 &= _GreaterThanExpression0;
                if (!_GreaterThanExpression0) {
                    witnessElements.put("cmp", cmp);
                }
            }
            _OCLInvariant0 &= _ForallExpr0;
        } catch (Exception _OCLInvariant0Exception) {
            _OCLInvariant0 = false;
            _OCLInvariant0Exception.printStackTrace();
            Log.error("Error while executing Test07.check_OCLInvariant0() !");
        }
        return _OCLInvariant0;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant1(List<EMAComponentSymbol> cmps) {
        Map<String, Object> witnessElements = new HashMap<>();
        witnessElements.put("cmps", cmps);
        Boolean _OCLInvariant1 = true;
        try {
            Boolean _ForallExpr1 = true;
            List<EMAComponentSymbol> _OCLQualifiedPrimary2 = cmps;
            for (EMAComponentSymbol cmp : _OCLQualifiedPrimary2) {
                Boolean _ForallExpr2 = true;
                List<EMAComponentSymbol> _OCLQualifiedPrimary3 = cmps;
                for (EMAComponentSymbol cmp2 : _OCLQualifiedPrimary3) {
                    EMAComponentSymbol _OCLQualifiedPrimary4 = cmp;
                    EMAComponentSymbol _OCLQualifiedPrimary5 = cmp2;
                    Boolean _NotEqualsExpression0 = !_OCLQualifiedPrimary4.equals(_OCLQualifiedPrimary5);
                    String _OCLQualifiedPrimary6 = cmp.getName();
                    String _OCLQualifiedPrimary7 = cmp2.getName();
                    Boolean _NotEqualsExpression1 = !_OCLQualifiedPrimary6.equals(_OCLQualifiedPrimary7);
                    Boolean _ImpliesExpression0 = !_NotEqualsExpression0 || _NotEqualsExpression1;
                    Boolean _ParenthizedExpression0 = (_ImpliesExpression0);
                    _ForallExpr2 &= _ParenthizedExpression0;
                    if (!_ParenthizedExpression0) {
                        witnessElements.put("cmp2", cmp2);
                    }
                }
                _ForallExpr1 &= _ForallExpr2;
                if (!_ForallExpr2) {
                    witnessElements.put("cmp", cmp);
                }
            }
            _OCLInvariant1 &= _ForallExpr1;
        } catch (Exception _OCLInvariant1Exception) {
            _OCLInvariant1 = false;
            _OCLInvariant1Exception.printStackTrace();
            Log.error("Error while executing Test07.check_OCLInvariant1() !");
        }
        return _OCLInvariant1;
    }

    @SuppressWarnings("unchecked")
    public static Boolean check_OCLInvariant2(List<EMAComponentSymbol> cmps) {
        Map<String, Object> witnessElements = new HashMap<>();
        witnessElements.put("cmps", cmps);
        Boolean _OCLInvariant2 = true;
        try {
            Boolean _ForallExpr3 = true;
            List<EMAComponentSymbol> _OCLQualifiedPrimary8 = cmps;
            for (EMAComponentSymbol cmp : _OCLQualifiedPrimary8) {
                for (EMAComponentSymbol cmp2 : _OCLQualifiedPrimary8) {
                    EMAComponentSymbol _OCLQualifiedPrimary9 = cmp;
                    EMAComponentSymbol _OCLQualifiedPrimary10 = cmp2;
                    Boolean _NotEqualsExpression2 = !_OCLQualifiedPrimary9.equals(_OCLQualifiedPrimary10);
                    String _OCLQualifiedPrimary11 = cmp.getName();
                    String _OCLQualifiedPrimary12 = cmp2.getName();
                    Boolean _NotEqualsExpression3 = !_OCLQualifiedPrimary11.equals(_OCLQualifiedPrimary12);
                    Boolean _ImpliesExpression1 = !_NotEqualsExpression2 || _NotEqualsExpression3;
                    Boolean _ParenthizedExpression1 = (_ImpliesExpression1);
                    _ForallExpr3 &= _ParenthizedExpression1;
                    if (!_ParenthizedExpression1) {
                        witnessElements.put("cmp", cmp);
                        witnessElements.put("cmp2", cmp2);
                    }
                }
            }
            _OCLInvariant2 &= _ForallExpr3;
        } catch (Exception _OCLInvariant2Exception) {
            _OCLInvariant2 = false;
            _OCLInvariant2Exception.printStackTrace();
            Log.error("Error while executing Test07.check_OCLInvariant2() !");
        }
        return _OCLInvariant2;
    }
}
