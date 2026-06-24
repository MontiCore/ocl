package de.monticore.ocl.ocl._symboltable;

import de.monticore.symboltable.serialization.JsonDeSers;
import de.monticore.symboltable.serialization.JsonPrinter;
import de.monticore.symboltable.serialization.json.JsonObject;
import de.monticore.types.check.SymTypeExpressionDeSer;

import java.util.List;

public class OCLArtifactSymbolDeSer extends OCLArtifactSymbolDeSerTOP {
  
  @Override
  protected void serializeOperations(List<OCLOperationData> operations, OCLSymbols2Json s2j) {
    JsonPrinter p = s2j.getJsonPrinter();
    p.beginObject();
    p.member(JsonDeSers.KIND, getSerializedKind());
    p.array("operationData", operations, this::serializeOCLOperationData);
    p.endObject();
  }
  
  protected String serializeOCLOperationData(OCLOperationData operation) {
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
  protected List<OCLOperationData> deserializeOperations(JsonObject symbolJson) {
    return List.of();
  }
}
