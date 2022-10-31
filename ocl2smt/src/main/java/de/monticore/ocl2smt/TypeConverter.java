package de.monticore.ocl2smt;

import com.microsoft.z3.Context;
import com.microsoft.z3.Sort;


import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.types.mcbasictypes._ast.ASTMCPrimitiveType;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.se_rwth.commons.logging.Log;

import java.util.Optional;


public class TypeConverter {
    protected final Context ctx;
    protected CD2SMTGenerator cd2SMTGenerator;


    public TypeConverter(CD2SMTGenerator cd2SMTGenerator) {
        this.cd2SMTGenerator = cd2SMTGenerator ;
        this.ctx = cd2SMTGenerator.getContext();
    }

    public Sort convertType(ASTMCType type, ASTCDDefinition cd) {
        if (type instanceof ASTMCPrimitiveType) {
            return convertPrim((ASTMCPrimitiveType) type);
        } else if (type instanceof ASTMCQualifiedType) {
            return convertQualf((ASTMCQualifiedType) type,cd);
        } else {
            assert false;
            Log.error("type conversion ist only implemented for  primitives types and Qualified types");
            return null;
        }

    }

    protected Sort convertPrim(ASTMCPrimitiveType type) {
        if (type.isBoolean()) {
            return ctx.mkBoolSort();
        } else if (type.isDouble()) {
            return ctx.mkRealSort();
        } else if (type.isInt()) {
            return ctx.mkIntSort();
        } else {
            assert false;
            Log.error("primitive type conversion is only implemented for  int , double and boolean");
            return null;
            //TODO: implement  the conversion of long , float , short , byte ...
        }
    }

    protected Sort convertQualf(ASTMCQualifiedType type, ASTCDDefinition cd) {
        if (type.getMCQualifiedName().getQName().equals("Boolean")) {
            return ctx.mkBoolSort();
        } else if (type.getMCQualifiedName().getQName().equals("Double")) {
            return ctx.mkRealSort();
        } else if (type.getMCQualifiedName().getQName().equals("Int")) {
            return ctx.mkIntSort();
        } else if (type.getMCQualifiedName().getQName().equals("java.lang.String")) {
            return ctx.mkStringSort();
        } else {
            Sort sort = cd2SMTGenerator.getSort(CDHelper.getASTCDType(type.getMCQualifiedName().getQName(),cd));
            if (sort != null) {
                return sort;
            } else {
                Log.error("Type or Class " + type.getMCQualifiedName() + "not found in CDContext");
                return null;
            }

        }

    }
}