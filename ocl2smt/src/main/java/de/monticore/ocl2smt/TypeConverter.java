package de.monticore.ocl2smt;

import com.microsoft.z3.Context;
import com.microsoft.z3.Sort;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.types.check.OCLDeriver;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCPrimitiveType;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.se_rwth.commons.logging.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class TypeConverter {

  public static Map<String, Sort> typeMap;
  protected final Context ctx;
  protected CD2SMTGenerator cd2SMTGenerator;

  public TypeConverter(CD2SMTGenerator cd2SMTGenerator) {
    this.cd2SMTGenerator = cd2SMTGenerator;
    this.ctx = cd2SMTGenerator.getContext();
    buildTypeMap();
  }

  private ASTCDDefinition getCD() {
    return cd2SMTGenerator.getClassDiagram().getCDDefinition();
  }

  private void buildTypeMap() {
    typeMap = new HashMap<>();
    typeMap.put("Boolean", ctx.mkBoolSort());
    typeMap.put("Double", ctx.getRealSort());
    typeMap.put("Integer", ctx.mkIntSort());
    typeMap.put("boolean", ctx.mkBoolSort());
    typeMap.put("double", ctx.getRealSort());
    typeMap.put("int", ctx.mkIntSort());
    typeMap.put("String", ctx.mkStringSort());
    typeMap.put("java.lang.String", ctx.mkStringSort());
    typeMap.put("java.lang.Boolean", ctx.mkBoolSort());
    typeMap.put("java.lang.Double", ctx.mkRealSort());
    typeMap.put("java.lang.Integer", ctx.mkIntSort());
  }

  public Sort mctype2Sort(ASTMCType type) {
    Sort res = null;
    if (type instanceof ASTMCPrimitiveType) {
      res = convertPrim((ASTMCPrimitiveType) type);
    } else if (type instanceof ASTMCQualifiedType) {
      res = convertQualf((ASTMCQualifiedType) type);
    } else {
      Log.error("type conversion ist only implemented for  primitives types and Qualified types");
    }
    return res;
  }

  public Sort symbol2Sort(VariableSymbol symbol) {
    Log.info("I tried to get a Type from the  VariableSymbol", "SymbolTable");
    SymTypeExpression symTypeExpression = symbol.getType();
    assert symTypeExpression != null
        && (symTypeExpression.isObjectType() || symTypeExpression.isPrimitive());
    String typename = symTypeExpression.getTypeInfo().getName();

    if (typeMap.containsKey(typename)) {
      return typeMap.get(typename);
    }
    return getSortFromCD2SMT(typename).orElse(null);
  }

  private Sort convertPrim(ASTMCPrimitiveType type) {
    Sort res = null;
    if (type.isBoolean()) {
      res = ctx.mkBoolSort();
    } else if (type.isDouble()) {
      res = ctx.mkRealSort();
    } else if (type.isInt()) {
      res = ctx.mkIntSort();
    } else {
      assert false;
      Log.error("primitive type conversion is only implemented for  int , double and boolean");
      res = null;
      // TODO: implement  the conversion of long , float , short , byte ...
    }
    return res;
  }

  private Sort convertQualf(ASTMCQualifiedType type) {
    String typeName = type.getMCQualifiedName().getQName();
    if (typeMap.containsKey(typeName)) {
      return typeMap.get(typeName);
    }
    return getSortFromCD2SMT(typeName).orElse(null);
  }

  public Optional<Sort> getSortFromCD2SMT(String typeName) {
    Sort sort = cd2SMTGenerator.getSort(CDHelper.getASTCDType(typeName, getCD()));
    if (sort == null) {
      Log.error("Type or Class " + typeName + "not found in CDContext");
    }
    return Optional.ofNullable(sort);
  }

  protected boolean isSet(ASTExpression node) {
    SymTypeExpression symTypeExpression = new OCLDeriver().deriveType(node).getResult();
    assert symTypeExpression != null;
    return (symTypeExpression.isGenericType()
        && symTypeExpression.getTypeInfo().getName().equals("Set"));
  }
}
