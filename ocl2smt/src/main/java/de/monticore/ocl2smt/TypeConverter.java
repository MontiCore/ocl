package de.monticore.ocl2smt;

import com.microsoft.z3.Sort;
import de.monticore.cd2smt.context.CDArtifacts.SMTCDType;
import de.monticore.cd2smt.context.CDArtifacts.SMTClass;
import de.monticore.cd2smt.context.CDContext;

import de.monticore.types.mcbasictypes._ast.ASTMCPrimitiveType;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;


public class TypeConverter {
    protected final CDContext cdContext;

    public TypeConverter(CDContext cdContext) {
        this.cdContext = cdContext;
    }

    public Sort convertType(ASTMCType type) {
        if (type instanceof ASTMCPrimitiveType) {
            return convertPrim((ASTMCPrimitiveType) type);
        } else if (type instanceof ASTMCQualifiedType) {
            return convertQualf((ASTMCQualifiedType) type);
        } else {
            assert false;
            Log.error("type conversion ist only implemented for  primitives types and Qualified types");
            return null;
        }

    }

    protected Sort convertPrim(ASTMCPrimitiveType type) {
        if (type.isBoolean()) {
            return cdContext.getContext().mkBoolSort();
        } else if (type.isDouble()) {
            return cdContext.getContext().mkRealSort();
        } else if (type.isInt()) {
            return cdContext.getContext().mkIntSort();
        } else {
            assert false;
            Log.error("primitive type conversion is only implemented for  int , double and boolean");
            return null;
            //TODO: implement  the conversion of long , float , short , byte ...
        }
    }

    protected Sort convertQualf(ASTMCQualifiedType type) {
        if (type.getMCQualifiedName().getQName().equals("Boolean")) {
            return cdContext.getContext().mkBoolSort();
        } else if (type.getMCQualifiedName().getQName().equals("Double")) {
            return cdContext.getContext().mkRealSort();
        } else if (type.getMCQualifiedName().getQName().equals("Int")) {
            return cdContext.getContext().mkIntSort();
        } else if (type.getMCQualifiedName().getQName().equals("java.lang.String")) {
            return cdContext.getContext().mkStringSort();
        } else {
            Optional<SMTCDType> smtcdType = cdContext.getSMTCDType(type.getMCQualifiedName().getQName());
            if (smtcdType.isPresent()) {
                return smtcdType.get().getSort();
            } else {
                Log.error("Type or Class " + type.getMCQualifiedName() + "not found in CDContext");
                return null;
            }

        }

    }
}