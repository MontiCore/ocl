/* (c) https://github.com/MontiCore/monticore */
package de.monticore.ocl2smt.util;

import com.microsoft.z3.Context;
import com.microsoft.z3.Sort;
import de.monticore.cd2smt.Helper.CDHelper;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.expressions.expressionsbasis._ast.ASTExpression;
import de.monticore.ocl.ocl._visitor.OCLTraverser;
import de.monticore.ocl.ocl.types3.OCLTypeTraverserFactory;
import de.monticore.ocl2smt.helpers.IOHelper;
import de.monticore.symbols.basicsymbols._symboltable.VariableSymbol;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.mcbasictypes._ast.ASTMCPrimitiveType;
import de.monticore.types.mcbasictypes._ast.ASTMCQualifiedType;
import de.monticore.types.mcbasictypes._ast.ASTMCType;
import de.monticore.types3.Type4Ast;
import de.se_rwth.commons.logging.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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

  /***
   * check if am expression is a String, boolean, Real/double, int or Date.
   * @param node the expression.
   */
  public static boolean hasSimpleType(ASTExpression node) {
    Set<String> primTypes =
            typeMap.keySet().stream().map(OCLType::getName).collect(Collectors.toSet());
    return hasType(node, primTypes);
  }

  /***
   * check if the sort is a String, boolean, Real/double, int or Date.
   * @param sort the sort.
   */
  public static boolean hasSimpleType(Sort sort) {
    return typeMap.containsKey(OCLType.buildOCLType(sort.toString()));
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

  /***
   * check if a typeName is a String, boolean, Real/double, int or Date.
   * @param typeName the typename to check.
   */
  public static boolean hasSimpleType(String typeName) {
    return typeMap.containsKey(OCLType.buildOCLType(typeName));
  }

  public static boolean hasSetType(ASTExpression node) {
    return hasType(node, Set.of("Set", "java.util.set"));
  }

  public static boolean hasOptionalType(ASTExpression node) {
    return hasType(node, Set.of("Optional", "java.util.Optional"));
  }

  public static boolean hasDateType(ASTExpression node) {
    return hasType(node, Set.of("Date", "java.util.Date"));
  }

  public static boolean hasStringType(ASTExpression node) {
    return hasType(node, Set.of("String", "java.lang.String"));
  }

  public static boolean hasBooleanType(ASTExpression node) {
    return hasType(node, Set.of("boolean", "java.lang.Boolean"));
  }

  public static boolean hasType(ASTExpression node, Set<String> typeNames) {
    SymTypeExpression type = deriveType(node);
    return typeNames.contains(type.getTypeInfo().getName());
  }

  private void buildTypeMap() {
    typeMap = new HashMap<>();
    typeMap.put(OCLType.buildOCLType("Boolean"), ctx.mkBoolSort());
    typeMap.put(OCLType.buildOCLType("Double"), ctx.getRealSort());
    typeMap.put(OCLType.buildOCLType("Integer"), ctx.mkIntSort());
    typeMap.put(OCLType.buildOCLType("boolean"), ctx.mkBoolSort());
    typeMap.put(OCLType.buildOCLType("double"), ctx.getRealSort());
    typeMap.put(OCLType.buildOCLType("Date"), ctx.getRealSort());
    typeMap.put(OCLType.buildOCLType("Real"), ctx.getRealSort());
    typeMap.put(OCLType.buildOCLType("int"), ctx.mkIntSort());
    typeMap.put(OCLType.buildOCLType("Int"), ctx.mkIntSort());
    typeMap.put(OCLType.buildOCLType("String"), ctx.mkStringSort());
    typeMap.put(OCLType.buildOCLType("java.lang.String"), ctx.mkStringSort());
    typeMap.put(OCLType.buildOCLType("java.lang.Boolean"), ctx.mkBoolSort());
    typeMap.put(OCLType.buildOCLType("java.lang.Double"), ctx.mkRealSort());
    typeMap.put(OCLType.buildOCLType("java.lang.Integer"), ctx.mkIntSort());
    typeMap.put(OCLType.buildOCLType("java.util.Date"), ctx.mkIntSort());
  }

  public OCLType buildOCLType(ASTMCType type) {
    OCLType res = null;
    if (type instanceof ASTMCPrimitiveType) {
      res = convertPrim((ASTMCPrimitiveType) type);
    } else if (type instanceof ASTMCQualifiedType) {
      res = convertQualifiedType((ASTMCQualifiedType) type);
    } else {
      Log.error("type conversion ist only implemented for  primitives types and Qualified types");
    }
    return res;
  }

  public Sort deriveSort(OCLType type) {
    if (typeMap.containsKey(type)) {
      return typeMap.get(type);
    }

    Sort res = cd2SMTGenerator.getSort(CDHelper.getASTCDType(type.getName(), getCD()));
    if (res == null) {
      Log.error("Type " + type.getName() + " Not found in the CD2SMTGenerator");
    }
    return res;
  }

  private OCLType convertPrim(ASTMCPrimitiveType type) {
    OCLType res;
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

  private OCLType convertQualifiedType(ASTMCQualifiedType type) {
    String typeName = type.getMCQualifiedName().getQName();
    if (typeMap.containsKey(OCLType.buildOCLType(typeName))) {
      return OCLType.typeNames.get(typeName);
    }
    return OCLType.buildOCLType(typeName);
  }

  public static SymTypeExpression deriveType(ASTExpression node) {
    Type4Ast type4Ast = new Type4Ast();
    OCLTraverser typeMapTraverser = new OCLTypeTraverserFactory().createTraverser(type4Ast);
    node.accept(typeMapTraverser);
    SymTypeExpression typeExpr = type4Ast.getTypeOfExpression(node);
    if (typeExpr == null) {
      Log.error("Unable to derive the type of the expression " + IOHelper.print(node));
      assert false;
    }

    return typeExpr;
  }
}
