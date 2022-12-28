package de.monticore.ocl2smt.helpers;

import com.microsoft.z3.BoolExpr;
import com.microsoft.z3.Expr;
import com.microsoft.z3.Sort;
import de.monticore.cd4analysis.CD4AnalysisMill;
import de.monticore.cdassociation._visitor.CDAssociationTraverser;
import de.monticore.cdassociation.trafo.CDAssociationRoleNameTrafo;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._visitor.CDBasisTraverser;
import de.monticore.ocl2smt.trafo.AddPreAttributeTrafo;
import de.monticore.ocl2smt.trafo.BuildPreAssociationTrafo;

public class Helper {

  public static void buildPreCD(ASTCDCompilationUnit ast) {
    final AddPreAttributeTrafo preAttributeTrafo = new AddPreAttributeTrafo();

    final CDBasisTraverser traverser = CD4AnalysisMill.traverser();
    traverser.add4CDBasis(preAttributeTrafo);
    traverser.setCDBasisHandler(preAttributeTrafo);


    final BuildPreAssociationTrafo preAssociations = new BuildPreAssociationTrafo();
    CDAssociationTraverser associationTraverser = CD4AnalysisMill.traverser() ;
    associationTraverser.add4CDAssociation(preAssociations);
    associationTraverser.setCDAssociationHandler(preAssociations);
    ast.accept(traverser);
    ast.accept(associationTraverser);

  }






}
