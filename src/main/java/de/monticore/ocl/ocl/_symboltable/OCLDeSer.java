// (c) https://github.com/MontiCore/monticore
package de.monticore.ocl.ocl._symboltable;

import de.se_rwth.commons.logging.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * OCLDeSer but also allows to suppress useless warnings about
 * not being able to deserialize symbols you never intended to
 * deserialize
 */
public class OCLDeSer extends OCLDeSerTOP {

  protected List<String> symbolKindsToIgnore = new ArrayList<>();

  public void ignoreSymbolKind(String fqn) {
    symbolKindsToIgnore.add(fqn);
  }

  /**
   * This is basically a copy paste from the generated code, but with an additional if statement
   * for ignored symbol kinds
   * @param scope OCLScope to put symbols in
   * @param scopeJson json to deserialize
   */
  @Override
  protected  void deserializeSymbols (de.monticore.ocl.ocl._symboltable.IOCLScope scope,de.monticore.symboltable.serialization.json.JsonObject scopeJson)  {
    for (de.monticore.symboltable.serialization.json.JsonObject symbol :
      de.monticore.symboltable.serialization.JsonDeSers.getSymbols(scopeJson)) {
      String kind = de.monticore.symboltable.serialization.JsonDeSers.getKind(symbol);
      de.monticore.symboltable.serialization.ISymbolDeSer deSer = de.monticore.ocl.ocl.OCLMill.globalScope().getSymbolDeSer(kind);
      if (null == deSer) {
        if (!symbolKindsToIgnore.contains(kind)) {
          Log.warn("0xA1234xx97163 No DeSer found to deserialize symbol of kind `" + kind
            + "`. The following will be ignored: " + symbol);
        }
        continue;
      }

      if ("de.monticore.ocl.ocl._symboltable.OCLInvariantSymbol".equals(kind)
        || "de.monticore.ocl.ocl._symboltable.OCLInvariantSymbol".equals(deSer.getSerializedKind())) {
        de.monticore.ocl.ocl._symboltable.OCLInvariantSymbol s0 = (de.monticore.ocl.ocl._symboltable.OCLInvariantSymbol) deSer.deserialize(symbol);
        scope.add(s0);
      }
      else if ("de.monticore.symbols.basicsymbols._symboltable.DiagramSymbol".equals(kind)
        || "de.monticore.symbols.basicsymbols._symboltable.DiagramSymbol".equals(deSer.getSerializedKind())) {
        de.monticore.symbols.basicsymbols._symboltable.DiagramSymbol s1 = (de.monticore.symbols.basicsymbols._symboltable.DiagramSymbol) deSer.deserialize(symbol);
        scope.add(s1);
      }
      else if ("de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol".equals(kind)
        || "de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol".equals(deSer.getSerializedKind())) {
        de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol s2 = (de.monticore.symbols.basicsymbols._symboltable.FunctionSymbol) deSer.deserialize(symbol);
        scope.add(s2);
        scope.addSubScope(s2.getSpannedScope());
      }
      else if ("de.monticore.symbols.basicsymbols._symboltable.TypeSymbol".equals(kind)
        || "de.monticore.symbols.basicsymbols._symboltable.TypeSymbol".equals(deSer.getSerializedKind())) {
        de.monticore.symbols.basicsymbols._symboltable.TypeSymbol s3 = (de.monticore.symbols.basicsymbols._symboltable.TypeSymbol) deSer.deserialize(symbol);
        scope.add(s3);
        scope.addSubScope(s3.getSpannedScope());
      }
      else if ("de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol".equals(kind)
        || "de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol".equals(deSer.getSerializedKind())) {
        de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol s4 = (de.monticore.symbols.basicsymbols._symboltable.TypeVarSymbol) deSer.deserialize(symbol);
        scope.add(s4);
      }
      else if ("de.monticore.symbols.basicsymbols._symboltable.VariableSymbol".equals(kind)
        || "de.monticore.symbols.basicsymbols._symboltable.VariableSymbol".equals(deSer.getSerializedKind())) {
        de.monticore.symbols.basicsymbols._symboltable.VariableSymbol s5 = (de.monticore.symbols.basicsymbols._symboltable.VariableSymbol) deSer.deserialize(symbol);
        scope.add(s5);
      }
      else {
        Log.warn("0xA8F45 Unable to integrate deserialization with DeSer for kind `" + kind
          + "`. The following will be ignored: " + symbol);
      }
    }

  }


}
