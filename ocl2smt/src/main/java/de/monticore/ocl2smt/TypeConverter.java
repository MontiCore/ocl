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

  public static Map<OCLType, Sort> typeMap;
  protected static Context ctx;
  protected static CD2SMTGenerator cd2SMTGenerator;

  public static void setup(CD2SMTGenerator cd2SMTGenerator) {
    TypeConverter.cd2SMTGenerator = cd2SMTGenerator;
    ctx = cd2SMTGenerator.getContext();
    buildTypeMap();
  }

  private static ASTCDDefinition getCD() {
    return cd2SMTGenerator.getClassDiagram().getCDDefinition();
  }

  private static void buildTypeMap() {
    typeMap = new HashMap<>();
    typeMap.put(OCLType.buildOCLType("Boolean"), ctx.mkBoolSort());
    typeMap.put(OCLType.buildOCLType("Double"), ctx.getRealSort());
    typeMap.put(OCLType.buildOCLType("Integer"), ctx.mkIntSort());
    typeMap.put(OCLType.buildOCLType("boolean"), ctx.mkBoolSort());
    typeMap.put(OCLType.buildOCLType("double"), ctx.getRealSort());
    typeMap.put(OCLType.buildOCLType("Real"), ctx.getRealSort());
    typeMap.put(OCLType.buildOCLType("int"), ctx.mkIntSort());
    typeMap.put(OCLType.buildOCLType("Int"), ctx.mkIntSort());
    typeMap.put(OCLType.buildOCLType("String"), ctx.mkStringSort());
    typeMap.put(OCLType.buildOCLType("java.lang.String"), ctx.mkStringSort());
    typeMap.put(OCLType.buildOCLType("java.lang.Boolean"), ctx.mkBoolSort());
    typeMap.put(OCLType.buildOCLType("java.lang.Double"), ctx.mkRealSort());
    typeMap.put(OCLType.buildOCLType("java.lang.Integer"), ctx.mkIntSort());
  }

  public static OCLType buildOCLType(ASTMCType type) {
    OCLType res = null;
    if (type instanceof ASTMCPrimitiveType) {
      res = convertPrim((ASTMCPrimitiveType) type);
    } else if (type instanceof ASTMCQualifiedType) {
      res = convertQualf((ASTMCQualifiedType) type);
    } else {
      Log.error("type conversion ist only implemented for  primitives types and Qualified types");
    }
    return res;
  }

  public static OCLType buildOCLType(VariableSymbol symbol) {
    Log.info("I tried to get a Type from the  VariableSymbol", "SymbolTable");
    SymTypeExpression symTypeExpression = symbol.getType();
    assert symTypeExpression != null
        && (symTypeExpression.isObjectType() || symTypeExpression.isPrimitive());
    String typename = symTypeExpression.getTypeInfo().getName();
    return OCLType.buildOCLType(typename);
  }

  public static Sort getSort(OCLType type) {
    if (typeMap.containsKey(type)) {
      return typeMap.get(type);
    } else {
      Optional<Sort> res = getSortFromCD2SMT(type);
      if (res.isEmpty()) {
        System.out.println("Je suis un idiot");
      }
      return res.get();
    }
  }

  private static OCLType convertPrim(ASTMCPrimitiveType type) {
    OCLType res = null;
    if (type.isBoolean()) {
      res = OCLType.buildOCLType("boolean");
    } else if (type.isDouble()) {
      res = OCLType.buildOCLType("double");
    } else if (type.isInt()) {
      res = OCLType.buildOCLType("int");
    } else {
      assert false;
      Log.error("primitive type conversion is only implemented for  int , double and boolean");
      res = null;
      // TODO: implement  the conversion of long , float , short , byte ...
    }
    return res;
  }

  private static OCLType convertQualf(ASTMCQualifiedType type) {
    String typeName = type.getMCQualifiedName().getQName();
    if (typeMap.containsKey(typeName)) {
      return OCLType.buildOCLType(typeName);
    }
    return OCLType.buildOCLType(typeName);
  }

  public static Optional<Sort> getSortFromCD2SMT(OCLType type) {
    Sort sort = cd2SMTGenerator.getSort(CDHelper.getASTCDType(type.getName(), getCD()));
    if (sort == null) {
      Log.error("Type or Class " + type.getName() + "not found in CDContext");
    }
    return Optional.ofNullable(sort);
  }

  protected static boolean isSet(ASTExpression node) {
    SymTypeExpression symTypeExpression = new OCLDeriver().deriveType(node).getResult();
    assert symTypeExpression != null;
    return (symTypeExpression.isGenericType()
        && symTypeExpression.getTypeInfo().getName().equals("Set"));
  }
}
