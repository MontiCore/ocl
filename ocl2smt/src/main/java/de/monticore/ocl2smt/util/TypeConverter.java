/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.util;

import com.microsoft.z3.Context;
import com.microsoft.z3.Sort;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.types.check.OCLDeriver;
import de.monticore.ocl2smt.helpers.IOHelper;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCPrimitiveType;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.se_rwth.commons.logging.Log;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class TypeConverter {
  public static Map<OCLType, Sort> typeMap;
  protected Context ctx;
  protected CD2SMTGenerator cd2SMTGenerator;

  public TypeConverter(CD2SMTGenerator cd2SMTGenerator) {
    this.cd2SMTGenerator = cd2SMTGenerator;
    ctx = cd2SMTGenerator.getContext();
    buildTypeMap();
  }

  private ASTCDDefinition getCD() {
    return cd2SMTGenerator.getClassDiagram().getCDDefinition();
  }

  private void buildTypeMap() {
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

  public OCLType buildOCLType(ASTMCType type) {
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

  public OCLType buildOCLType(VariableSymbol symbol) {
    if (symbol.getType() == null) {
      Log.error(
          "Unable to derive type of Variable "
              + symbol.getName()
              + ", the variable type is empty ");
    }
    return buildOCLType(symbol.getType());
  }

  public OCLType buildOCLType(SymTypeExpression typeExpr) {
    if (typeExpr.isObjectType() || typeExpr.isPrimitive()) {
      String typename = typeExpr.getTypeInfo().getFullName();
      return OCLType.buildOCLType(typename);
    }
    Log.error("Building OCLType not implement for the type " + typeExpr.printFullName());
    return null;
  }

  public Sort getSort(OCLType type) {
    if (typeMap.containsKey(type)) {
      return typeMap.get(type);
    } else {
      Optional<Sort> res = getSortFromCD2SMT(type);
      if (res.isEmpty()) {
        Log.error("Type " + type.getName() + " Not found in the CD2SMTGenerator");
        assert false;
      }
      return res.get();
    }
  }

  private OCLType convertPrim(ASTMCPrimitiveType type) {
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

  private OCLType convertQualf(ASTMCQualifiedType type) {
    String typeName = type.getMCQualifiedName().getQName();
    if (typeMap.containsKey(OCLType.buildOCLType(typeName))) {
      return OCLType.typeNames.get(typeName);
    }
    return OCLType.buildOCLType(typeName);
  }

  public Optional<Sort> getSortFromCD2SMT(OCLType type) {
    Sort sort = cd2SMTGenerator.getSort(CDHelper.getASTCDType(type.getName(), getCD()));
    if (sort == null) {
      Log.error("Type or Class " + type.getName() + "not found in CDContext");
    }
    return Optional.ofNullable(sort);
  }

  public static boolean isSet(ASTExpression node) {
    return hasGenericType(node, Set.of("Set", "java.util.set"));
  }

  public static boolean isOptional(ASTExpression node) {
    return hasGenericType(node, Set.of("Optional", "java.util.Optional"));
  }

  public static boolean isDate(ASTExpression node) {
    return hasObjectType(node, Set.of("Date", "java.util.Date"));
  }

  public static boolean isString(ASTExpression node) {
    return hasObjectType(node, Set.of("String", "java.lang.String"));
  }

  public static SymTypeExpression deriveType(ASTExpression node) {
    Optional<SymTypeExpression> typeExpr =
        Optional.ofNullable(new OCLDeriver().deriveType(node).getResult());
    if (typeExpr.isEmpty()) {
      Log.error("Unable to derive the type of the expression " + IOHelper.print(node));
      assert false;
    }

    return typeExpr.get();
  }

  public static boolean hasObjectType(ASTExpression node, Set<String> typeNames) {
    SymTypeExpression typeExpr = deriveType(node);
    return typeExpr.isObjectType() && typeNames.contains(typeExpr.getTypeInfo().getName());
  }

  private static boolean hasGenericType(ASTExpression node, Set<String> typeNames) {
    SymTypeExpression typeExpr = deriveType(node);
    return (typeExpr.isGenericType() && typeNames.contains(typeExpr.getTypeInfo().getName()));
  }

  public static boolean isPrimitiv(Sort sort) {
    return typeMap.containsKey(OCLType.buildOCLType(sort.toString()));
  }

  public static boolean isPrimitiv(String typeName) {
    return typeMap.containsKey(OCLType.buildOCLType(typeName));
  }
}
