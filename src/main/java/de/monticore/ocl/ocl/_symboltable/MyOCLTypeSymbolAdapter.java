/*
 *  * (c) https://github.com/MontiCore/monticore
 *  *
 *  * The license generally applicable for this project
 *  * can be found under https://github.com/MontiCore/monticore.
 */

package de.monticore.ocl.ocl._symboltable;

import de.monticore.cd.cd4analysis._ast.ASTCDField;
import de.monticore.cd.cd4analysis._ast.CD4AnalysisMill;
import de.monticore.cd.cd4analysis._symboltable.CD4AnalysisSymTabMill;
import de.monticore.cd.cd4analysis._symboltable.CDFieldSymbol;
import de.monticore.cd.cd4analysis._symboltable.CDTypeSymbol;
import de.monticore.cd.cd4analysis._symboltable.CDTypeSymbolLoader;
import de.monticore.mcbasics._ast.MCBasicsMill;
import de.monticore.types.check.SymTypeExpression;
import de.monticore.types.check.SymTypeExpressionFactory;
import de.monticore.types.check.SymTypeOfObject;
import de.monticore.types.typesymbols._ast.ASTField;
import de.monticore.types.typesymbols._symboltable.FieldSymbol;
import de.monticore.types.typesymbols._symboltable.ITypeSymbolsScope;
import de.monticore.types.typesymbols._symboltable.TypeSymbol;

import java.util.List;
import java.util.stream.Collectors;

public class MyOCLTypeSymbolAdapter extends TypeSymbol {
  CDTypeSymbol delegate;

  public MyOCLTypeSymbolAdapter(CDTypeSymbol delegate) {
    super(delegate.getName());
    this.delegate = delegate;
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public ITypeSymbolsScope getEnclosingScope() {
    return delegate.getEnclosingScope();
  }

  @Override
  public ITypeSymbolsScope getSpannedScope() {
    return delegate.getSpannedScope();
  }

  @Override
  public List<FieldSymbol> getFieldList() {
    return delegate.getFields().stream().map(this::fromCDFieldSymbol).collect(Collectors.toList());
  }

  public FieldSymbol fromCDFieldSymbol(CDFieldSymbol symbol) {
    return CD4AnalysisSymTabMill.fieldSymbolBuilder()
        .setAstNode(astFieldFromCDField(symbol.getAstNode()))
        .setEnclosingScope(symbol.getEnclosingScope())
        .setName(symbol.getName())
        .setPackageName(symbol.getPackageName())
        .setAccessModifier(symbol.getAccessModifier())
        .setFullName(symbol.getFullName())
        .setType(cdTypeSymbolLoaderToSymTypeExpression(symbol.getType()))
        .build();
  }

  private SymTypeExpression cdTypeSymbolLoaderToSymTypeExpression(CDTypeSymbolLoader type) {
    if (type.getDimension() == 0) {
      return SymTypeExpressionFactory.createTypeObject(type.getName(), type.getEnclosingScope());
    } else {
      return SymTypeExpressionFactory
          .createGenerics(type.getName(), type.getEnclosingScope(),
              type.getActualTypeArguments().stream().map(this::cdTypeSymbolLoaderToSymTypeExpression).collect(Collectors.toList()));
    }
  }

  public ASTField astFieldFromCDField(ASTCDField astNode) {
    return (ASTField) astNode;
  }

  @Override
  public List<FieldSymbol> getFieldList(String fieldname) {
    return getFieldList().stream().filter(f -> f.getName().contains(fieldname)).collect(Collectors.toList());
  }
}
