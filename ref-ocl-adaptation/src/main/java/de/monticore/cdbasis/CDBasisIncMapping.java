package de.monticore.cdbasis;

import de.monticore.cdbasis._symboltable.CDTypeSymbol;

import java.util.Set;

public interface CDBasisIncMapping {

  Set<CDTypeSymbol> getIncarnations(CDTypeSymbol typeSymbol);
}
