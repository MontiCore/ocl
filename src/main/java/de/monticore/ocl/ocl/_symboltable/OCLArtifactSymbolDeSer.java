// (c) https://github.com/MontiCore/monticore

package de.monticore.ocl.ocl._symboltable;

import de.monticore.symbols.basicsymbols.BasicSymbolsMill;
import de.monticore.symboltable.serialization.JsonPrinter;
import de.monticore.symboltable.serialization.json.JsonElement;
import de.monticore.symboltable.serialization.json.JsonObject;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionDeSer;

import java.util.List;

public class OCLArtifactSymbolDeSer extends OCLArtifactSymbolDeSerTOP {
  
  @Override
  protected void serializeOperations(List<OCLOperationConstraintData> operations,
      OCLSymbols2Json s2j) {
    JsonPrinter p = s2j.getJsonPrinter();
    p.array("operationConstraints", operations, this::serializeOCLOperationData);
  }
  
  protected String serializeOCLOperationData(OCLOperationConstraintData operation) {
    JsonPrinter p = new JsonPrinter();
    p.beginObject();
    
    p.member("fullyQualifiedName", operation.getFullyQualifiedName());
    SymTypeExpressionDeSer.serializeMember(p, "returnType", operation.getReturnType());
    SymTypeExpressionDeSer.serializeMember(p, "params", operation.getParameters());
    p.member("hasPrecondition", operation.hasPre());
    p.member("hasPostcondition", operation.hasPost());
    
    p.endObject();
    
    return p.toString();
  }
  
  @Override
  protected List<OCLOperationConstraintData> deserializeOperations(JsonObject symbolJson) {
    if (symbolJson.hasArrayMember("operationConstraints")) {
      return symbolJson.getArrayMember("operationConstraints").stream()
          .map(this::deserializeOCLOperationData).toList();
    }
    return List.of();
  }
  
  protected OCLOperationConstraintData deserializeOCLOperationData(JsonElement operationJson) {
    return deserializeOCLOperationData(operationJson.getAsJsonObject());
  }
  
  protected OCLOperationConstraintData deserializeOCLOperationData(JsonObject operationJson) {
    
    String fullyQualifiedName = operationJson.getStringMember("fullyQualifiedName");
    SymTypeExpression returnType =
        SymTypeExpressionDeSer.deserializeMember("returnType", operationJson,
            BasicSymbolsMill.globalScope());
    List<SymTypeExpression> params =
        SymTypeExpressionDeSer.deserializeListMember("params", operationJson,
            BasicSymbolsMill.globalScope());
    
    boolean hasPre =
        operationJson.hasBooleanMember("hasPrecondition") && operationJson.getBooleanMember(
            "hasPrecondition");
    boolean hasPost =
        operationJson.hasBooleanMember("hasPostcondition") && operationJson.getBooleanMember(
            "hasPostcondition");
    
    OCLOperationConstraintData constraintData =
        new OCLOperationConstraintData(returnType, fullyQualifiedName, params);
    constraintData.setHasPre(hasPre);
    constraintData.setHasPost(hasPost);
    
    return constraintData;
  }
}
