/*
 *  * (c) https://github.com/MontiCore/monticore
 *  *
 *  * The license generally applicable for this project
 *  * can be found under https://github.com/MontiCore/monticore.
 */

package de.monticore.ocl.ocl._symboltable;

import com.google.common.collect.LinkedListMultimap;
import de.monticore.cd.cd4analysis._symboltable.*;
import de.monticore.cd.cd4analysis._visitor.CD4AnalysisScopeVisitor;
import de.monticore.io.paths.ModelPath;
import de.monticore.types.mcsimplegenerictypes._symboltable.IMCSimpleGenericTypesScope;
import de.monticore.types.mcsimplegenerictypes._visitor.MCSimpleGenericTypesScopeVisitor;
import de.se_rwth.commons.logging.Log;

import java.util.List;
/*
public class OCLwithCDGlobalScope extends OCLGlobalScope {
  protected CD4AnalysisGlobalScope cdDelegate;

  public OCLwithCDGlobalScope(ModelPath cdModelPath, CD4AnalysisLanguage cdLanguage, ModelPath oclModelPath, OCLLanguage oclLanguage) {
    super(oclModelPath, oclLanguage);
    this.cdDelegate = new CD4AnalysisGlobalScope(cdModelPath, cdLanguage);
  }

  public OCLwithCDGlobalScope(ModelPath cdModelPath, ModelPath oclModelPath) {
    this(cdModelPath, CD4AnalysisSymTabMill.cD4AnalysisLanguageBuilder().build(), oclModelPath, OCLSymTabMill.oCLLanguageBuilder().build());
  }

  @Override
  public List<? extends IOCLwithCDGlobalScope> getSubScopes() {
    return super.getSubScopes();
  }

  @Override
  public boolean isCDDefinitionSymbolsAlreadyResolved() {
    return this.cdDelegate.isCDDefinitionSymbolsAlreadyResolved();
  }

  @Override
  public void setCDDefinitionSymbolsAlreadyResolved(boolean symbolAlreadyResolved) {
    this.cdDelegate.setCDDefinitionSymbolsAlreadyResolved(symbolAlreadyResolved);
  }

  @Override
  public boolean isCDAssociationSymbolsAlreadyResolved() {
    return this.cdDelegate.isCDAssociationSymbolsAlreadyResolved();
  }

  @Override
  public void setCDAssociationSymbolsAlreadyResolved(boolean symbolAlreadyResolved) {
    this.cdDelegate.setCDAssociationSymbolsAlreadyResolved(symbolAlreadyResolved);
  }

  @Override
  public boolean isCDQualifierSymbolsAlreadyResolved() {
    return this.cdDelegate.isCDQualifierSymbolsAlreadyResolved();
  }

  @Override
  public void setCDQualifierSymbolsAlreadyResolved(boolean symbolAlreadyResolved) {
    this.cdDelegate.setCDQualifierSymbolsAlreadyResolved(symbolAlreadyResolved);
  }

  @Override
  public boolean isCDTypeSymbolsAlreadyResolved() {
    return this.cdDelegate.isCDTypeSymbolsAlreadyResolved();
  }

  @Override
  public void setCDTypeSymbolsAlreadyResolved(boolean symbolAlreadyResolved) {
    this.cdDelegate.setCDTypeSymbolsAlreadyResolved(symbolAlreadyResolved);
  }

  @Override
  public boolean isCDFieldSymbolsAlreadyResolved() {
    return this.cdDelegate.isCDFieldSymbolsAlreadyResolved();
  }

  @Override
  public void setCDFieldSymbolsAlreadyResolved(boolean symbolAlreadyResolved) {
    this.cdDelegate.setCDTypeSymbolsAlreadyResolved(symbolAlreadyResolved);
  }

  @Override
  public boolean isCDMethOrConstrSymbolsAlreadyResolved() {
    return this.cdDelegate.isCDMethOrConstrSymbolsAlreadyResolved();
  }

  @Override
  public void setCDMethOrConstrSymbolsAlreadyResolved(boolean symbolAlreadyResolved) {
    this.cdDelegate.setCDMethOrConstrSymbolsAlreadyResolved(symbolAlreadyResolved);
  }

  @Override
  public LinkedListMultimap<String, CDDefinitionSymbol> getCDDefinitionSymbols() {
    return this.cdDelegate.getCDDefinitionSymbols();
  }

  @Override
  public void add(CDDefinitionSymbol symbol) {
    this.cdDelegate.add(symbol);
  }

  @Override
  public void remove(CDDefinitionSymbol symbol) {
    this.cdDelegate.remove(symbol);
  }

  @Override
  public LinkedListMultimap<String, CDAssociationSymbol> getCDAssociationSymbols() {
    return this.cdDelegate.getCDAssociationSymbols();
  }

  @Override
  public void add(CDAssociationSymbol symbol) {
    this.cdDelegate.add(symbol);
  }

  @Override
  public void remove(CDAssociationSymbol symbol) {
    this.cdDelegate.remove(symbol);
  }

  @Override
  public LinkedListMultimap<String, CDQualifierSymbol> getCDQualifierSymbols() {
    return this.cdDelegate.getCDQualifierSymbols();
  }

  @Override
  public void add(CDQualifierSymbol symbol) {
    this.cdDelegate.add(symbol);
  }

  @Override
  public void remove(CDQualifierSymbol symbol) {
    this.cdDelegate.remove(symbol);
  }

  @Override
  public LinkedListMultimap<String, CDTypeSymbol> getCDTypeSymbols() {
    return this.cdDelegate.getCDTypeSymbols();
  }

  @Override
  public void add(CDTypeSymbol symbol) {
    this.cdDelegate.add(symbol);
  }

  @Override
  public void remove(CDTypeSymbol symbol) {
    this.cdDelegate.remove(symbol);
  }

  @Override
  public LinkedListMultimap<String, CDFieldSymbol> getCDFieldSymbols() {
    return null;
  }

  @Override
  public void add(CDFieldSymbol symbol) {
    this.cdDelegate.add(symbol);
  }

  @Override
  public void remove(CDFieldSymbol symbol) {
    this.cdDelegate.remove(symbol);
  }

  @Override
  public LinkedListMultimap<String, CDMethOrConstrSymbol> getCDMethOrConstrSymbols() {
    return this.cdDelegate.getCDMethOrConstrSymbols();
  }

  @Override
  public void add(CDMethOrConstrSymbol symbol) {
    this.cdDelegate.add(symbol);
  }

  @Override
  public void remove(CDMethOrConstrSymbol symbol) {
    this.cdDelegate.remove(symbol);
  }

  @Override
  public OCLwithCDGlobalScope getEnclosingScope() {
    Log.error("0xA4234 GlobalScope OCLwithCDGlobalScope has no EnclosingScope, so you cannot call method getEnclosingScope.");
    return null;
  }

  @Override
  public void setEnclosingScope(ICD4AnalysisScope enclosingScope) {
    this.cdDelegate.setEnclosingScope(enclosingScope);
  }

  @Override
  public void accept(CD4AnalysisScopeVisitor visitor) {
    this.cdDelegate.accept(visitor);
  }

  @Override
  public void setEnclosingScope(IMCSimpleGenericTypesScope enclosingScope) {
    this.cdDelegate.setEnclosingScope(enclosingScope);
  }

  @Override
  public void accept(MCSimpleGenericTypesScopeVisitor visitor) {
    this.cdDelegate.accept(visitor);
  }

  @Override
  public CD4AnalysisLanguage getCD4AnalysisLanguage() {
    return CD4AnalysisSymTabMill.cD4AnalysisLanguageBuilder().build();
  }

  @Override
  public boolean continueWithModelLoader(String calculatedModelName, CD4AnalysisModelLoader modelLoader) {
    return false; // TODO
  }


}
*/