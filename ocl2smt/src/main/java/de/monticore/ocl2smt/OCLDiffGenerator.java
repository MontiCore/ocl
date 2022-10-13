package de.monticore.ocl2smt;

import com.microsoft.z3.*;
import de.monticore.cd.facade.MCQualifiedNameFacade;
import de.monticore.cd2smt.Helper.Identifiable;

import de.monticore.cd2smt.Helper.ODHelper;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cd2smt.context.CDContext;

import de.monticore.cd2smt.smt2odgenerator.SMT2ODGenerator;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.od4report.OD4ReportMill;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.monticore.odbasis._ast.ASTODAttribute;
import de.monticore.odbasis._ast.ASTODElement;
import de.monticore.odbasis._ast.ASTODNamedObject;
import de.monticore.odlink._ast.ASTODLink;
import de.se_rwth.commons.logging.Log;


import java.util.*;
import java.util.stream.Collectors;

public class OCLDiffGenerator {
    protected static CD2SMTGenerator cd2SMTGenerator = new CD2SMTGenerator();
    protected static CDContext cdContext;
    protected static OCL2SMTGenerator ocl2SMTGenerator;

    protected static List<Identifiable<BoolExpr>> getPositiveSolverConstraints(ASTCDCompilationUnit cd , Set<ASTOCLCompilationUnit>  in, Context context){
      //convert the cd to an SMT context
      cdContext = cd2SMTGenerator.cd2smt(cd, context);

      //transform positive ocl files    in a list of SMT BoolExpr
      ocl2SMTGenerator = new OCL2SMTGenerator(cdContext);

      return in.stream().flatMap(p ->ocl2SMTGenerator.ocl2smt(p.getOCLArtifact()).stream())
              .collect(Collectors.toList());
    }


    public static ASTODArtifact oclWitness(ASTCDCompilationUnit cd ,Set<ASTOCLCompilationUnit>  in, Context context, boolean partial ){
      List<Identifiable<BoolExpr>> solverConstraints = getPositiveSolverConstraints(cd, in,context);

      //check if they exist a model for the list of positive Constraint
      Solver solver = cdContext.makeSolver(cdContext.getContext(), solverConstraints);
      if (solver.check() != Status.SATISFIABLE){
        Log.error("there are no Model for the List Of Positive Constraints");
      }

      return buildOd(cdContext, "Witness", solverConstraints, partial).get();
    }
    public static ASTODArtifact oclWitness(ASTCDCompilationUnit cd ,Set<ASTOCLCompilationUnit>  in, Context context){
        return oclWitness(cd,in,context,false);
    }


    public static Set<ASTODArtifact> oclDiff(ASTCDCompilationUnit cd ,Set<ASTOCLCompilationUnit>  in , Set<ASTOCLCompilationUnit> notIn, Context context, boolean partial){
        Set<ASTODArtifact> res = new HashSet<>();
        List<Identifiable<BoolExpr>> solverConstraints = getPositiveSolverConstraints(cd, in,context);

        //negative ocl constraints
        List<Identifiable<BoolExpr>> negConstList = new ArrayList<>();
        notIn.forEach(p -> ocl2SMTGenerator.ocl2smt(p.getOCLArtifact()).forEach(idf-> negConstList.add(Identifiable
                .buildBoolExprIdentifiable(cdContext.getContext().mkNot(idf.getValue()),idf.getSourcePosition(),Optional
                        .of( idf.getInvariantName().orElse("NoInvName")+"_____NegInv" )))));

        //check if they exist a model for the list of positive Constraint
        oclWitness(cd,in,context,false);

        //add one by one all Constraints to the Solver and check if  it can always produce a Model
        for (Identifiable<BoolExpr> negConstraint:  negConstList) {
            solverConstraints.add(negConstraint);
            Optional<ASTODArtifact> optOd = buildOd(cdContext, negConstraint.getInvariantName().orElse("NoInvName").split("_____NegInv")[0], solverConstraints, partial);
            if (optOd.isPresent()){
                res.add(optOd.get());
            }else {
                Solver solver = cdContext.makeSolver( cdContext.getContext(),solverConstraints);
                res.add(buildUnSatOD(solver, "UNSAT_CORE_"+negConstraint.getInvariantName().orElse("NoInvName").split("_")[1]));
            }

            solverConstraints.remove(negConstraint);
        }
        return  res  ;
    }
    public static Set<ASTODArtifact> oclDiff(ASTCDCompilationUnit cd ,Set<ASTOCLCompilationUnit>  in , Set<ASTOCLCompilationUnit> notIn, Context context){
        return oclDiff(cd,in, notIn, context, false);
    }
    protected static Optional<ASTODArtifact> buildOd(CDContext cdContext, String ODName, List<Identifiable<BoolExpr>> oclConstraints, boolean partial) {
        SMT2ODGenerator smt2ODGenerator = new SMT2ODGenerator();
        return smt2ODGenerator.buildOd(cdContext,ODName,oclConstraints,partial);
    }
    protected static Optional<ASTODArtifact> buildOd(CDContext cdContext, String ODName, List<Identifiable<BoolExpr>> solverConstraints) {
        return buildOd(cdContext,ODName,solverConstraints,false);
    }
    protected static List<ASTODAttribute> buildInvODAttributeList(Identifiable<BoolExpr> identifiable){
        List<ASTODAttribute> attributeList = new ArrayList<>();
        attributeList.add(ODHelper.buildAttribute("line", OD4ReportMill.mCPrimitiveTypeBuilder().setPrimitive(6).build()
                , ""+identifiable.getSourcePosition().getLine()));
        attributeList.add(ODHelper.buildAttribute("file", OD4ReportMill.mCQualifiedTypeBuilder().
                setMCQualifiedName(MCQualifiedNameFacade.createQualifiedName("Path")).build(), ""+identifiable.getFile()));

        attributeList.add(ODHelper.buildAttribute("name", OD4ReportMill.mCQualifiedTypeBuilder().
                setMCQualifiedName(MCQualifiedNameFacade.createQualifiedName("String")).build(), ""+identifiable.getInvariantName().orElse("")));

        return attributeList;
    }

    protected static ASTODArtifact buildUnSatOD (Solver solver ,String name){
        List<ASTODElement> elementList = new ArrayList<>();
        List<Identifiable<BoolExpr>> posConstraints = new ArrayList<>();
        List<Identifiable<BoolExpr>> negConstraints = new ArrayList<>();

        solver.check();
        //get the constraints from the id
        Arrays.stream( solver.getUnsatCore()).forEach( b ->{
            int i =  Integer.parseInt(b.getSExpr().replace("|",""));
            Identifiable<BoolExpr> constraint =  Identifiable.getBoolExprIdentifiable(i);

            //add the constraints to the corresponding constraints list
            if (constraint.getInvariantName().get().contains("_____NegInv")){
                constraint.setInvariantName(Optional.of( constraint.getInvariantName().get().split("_____NegInv")[0]));
                negConstraints.add(constraint);
            }
            else {
                posConstraints.add(constraint);
            }});

        //add objects for each invariant
        posConstraints.forEach( i-> elementList.add(buildInvObject(i)));
        negConstraints.forEach( i-> elementList.add(buildInvObject(i)));


        //add links
        for (Identifiable<BoolExpr> left : posConstraints){
            for (Identifiable<BoolExpr> right: negConstraints){
                ASTODLink link = ODHelper.buildLink(left.getInvariantName().get(),right.getInvariantName().get(),"trace");
                link.setODLinkDirection(OD4ReportMill.oDLeftToRightDirBuilder().build());
                elementList.add(link);
            }
        }

        return ODHelper.buildOD(name,elementList);
    }

    protected static ASTODNamedObject buildInvObject(Identifiable<BoolExpr> identifiable){
        String name = identifiable.getInvariantName().isPresent()? identifiable.getInvariantName().get():""+identifiable.getSourcePosition().getLine() ;
        return ODHelper.buildObject(name,"OCLInv",buildInvODAttributeList(identifiable));
    }


}
