package de.monticore.ocl2smt.ocl2smt.expressionconverter;

import de.monticore.literals.mccommonliterals._ast.*;

public abstract class ExprBuilder {
  
  protected ExpressionKind kind;
  
  protected abstract  <T> T expr();

  abstract ExprBuilder  mkBool(ASTBooleanLiteral node);

  abstract ExprBuilder mkString(ASTStringLiteral node);

  abstract ExprBuilder mkInt(ASTNatLiteral node);

  abstract ExprBuilder mkChar(ASTCharLiteral node);

  abstract ExprBuilder mkDouble(ASTBasicDoubleLiteral node);

  abstract ExprBuilder mkNot(ExprBuilder node);

  abstract ExprBuilder mkAnd(ExprBuilder leftNode, ExprBuilder rightNode);

  abstract ExprBuilder mkOr(ExprBuilder leftNode, ExprBuilder rightNode);

  abstract ExprBuilder mkEq(ExprBuilder leftNode, ExprBuilder rightNode);

  abstract ExprBuilder mkImplies(ExprBuilder leftNode, ExprBuilder rightNode);

  abstract ExprBuilder mkNeq(ExprBuilder leftNode, ExprBuilder rightNode);

  abstract ExprBuilder mkLt(ExprBuilder leftNode, ExprBuilder rightNode);

  abstract ExprBuilder mkLeq(ExprBuilder leftNode, ExprBuilder rightNode);

  abstract ExprBuilder mkGt(ExprBuilder leftNode, ExprBuilder rightNode);

  abstract ExprBuilder mkGe(ExprBuilder leftNode, ExprBuilder rightNode);

  abstract ExprBuilder mkSub(ExprBuilder leftNode, ExprBuilder rightNode);

  abstract ExprBuilder mkPlus(ExprBuilder leftNode, ExprBuilder rightNode);

  abstract ExprBuilder mkMul(ExprBuilder leftNode, ExprBuilder rightNode);

  abstract ExprBuilder mkDiv(ExprBuilder leftNode, ExprBuilder rightNode);

  abstract ExprBuilder mkMod(ExprBuilder leftNode, ExprBuilder rightNode);

  abstract ExprBuilder mkPlusPrefix(ExprBuilder leftNode);

  abstract ExprBuilder mkMinusPrefix(ExprBuilder leftNode);

  abstract ExprBuilder mkIte(ExprBuilder cond, ExprBuilder expr1, ExprBuilder expr2);

  abstract ExprBuilder mkReplace(ExprBuilder s,ExprBuilder s1,ExprBuilder s2);
  abstract ExprBuilder mkContains(ExprBuilder s1,ExprBuilder s2);
  abstract ExprBuilder mkPrefixOf(ExprBuilder s1,ExprBuilder s2);
  abstract ExprBuilder mkSuffixOf(ExprBuilder s1,ExprBuilder s2);


 boolean isArithExpr(){
   return kind == ExpressionKind.INTEGER || kind == ExpressionKind.DOUBLE ;
 }


  public boolean isString() {
   return kind == ExpressionKind.STRING ;
  }

  boolean isPresent(){
   return  expr() !=null ;
  }

  boolean isBool(){
   return  kind == ExpressionKind.BOOL;
  }
}
