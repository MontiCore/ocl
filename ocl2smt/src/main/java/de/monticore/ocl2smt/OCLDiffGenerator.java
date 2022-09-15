package de.monticore.ocl2smt;

import com.microsoft.z3.*;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import de.monticore.cd2smt.cd2smtGenerator.CD2SMTGenerator;
import de.monticore.cd2smt.context.CDContext;
import de.monticore.cd2smt.context.ODContext;
import de.monticore.cd2smt.smt2odgenerator.SMT2ODGenerator;
import de.monticore.cdbasis._ast.ASTCDCompilationUnit;
import de.monticore.cdbasis._ast.ASTCDDefinition;
import de.monticore.ocl.ocl._ast.ASTOCLCompilationUnit;
import de.monticore.odbasis._ast.ASTODArtifact;
import de.se_rwth.commons.logging.Log;


import java.util.*;
import java.util.stream.Collectors;

public class OCLDiffGenerator {
    protected static CD2SMTGenerator cd2SMTGenerator = new CD2SMTGenerator();
    protected static CDContext cdContext;
    protected static OCL2SMTGenerator ocl2SMTGenerator;

    protected static List<Pair<String,BoolExpr>> getPositiveSolverConstraints(ASTCDCompilationUnit cd ,Set<ASTOCLCompilationUnit>  in, Context context){
      //convert the cd to an SMT context
      cdContext = cd2SMTGenerator.cd2smt(cd, context);

      //transform positive ocl files    in a list of SMT BoolExpr
      ocl2SMTGenerator = new OCL2SMTGenerator(cdContext);

      return in.stream()
              .flatMap(p ->ocl2SMTGenerator.ocl2smt(p.getOCLArtifact()).stream())
               .map(p -> new ImmutablePair<>(p.getLeft().orElse("NoName"), p.getRight()))
               .collect(Collectors.toList());
    }


    public static ASTODArtifact oclWitness(ASTCDCompilationUnit cd ,Set<ASTOCLCompilationUnit>  in, Context context, boolean partial ){
      List<Pair<String,BoolExpr>> solverConstraints = getPositiveSolverConstraints(cd, in,context);

      //check if they exist a model for the list of positive Constraint
      Optional<Model> modelOptional = getModel(cdContext.getContext(), solverConstraints);

      if (modelOptional.isEmpty()){
        Log.error("there are no Model for the List Of Positive Constraints");
      }

      return buildOd(cdContext, "Witness", solverConstraints, cd.getCDDefinition(), partial);
    }
    public static ASTODArtifact oclWitness(ASTCDCompilationUnit cd ,Set<ASTOCLCompilationUnit>  in, Context context){
        return oclWitness(cd,in,context,false);
    }


    public static Set<ASTODArtifact> oclDiff(ASTCDCompilationUnit cd ,Set<ASTOCLCompilationUnit>  in , Set<ASTOCLCompilationUnit> notIn, Context context, boolean partial){
        Set<ASTODArtifact> res = new HashSet<>();
        List<Pair<String,BoolExpr>> solverConstraints = getPositiveSolverConstraints(cd, in,context);

        //negative ocl constraints
        List<Pair<Optional<String> ,BoolExpr>> negConstList = new ArrayList<>();
        notIn.forEach(p -> negConstList.addAll(ocl2SMTGenerator.ocl2smt(p.getOCLArtifact())));


        //check if they exist a model for the list of positive Constraint
        Optional<Model> modelOptional = getModel(cdContext.getContext(),solverConstraints );
        if (modelOptional.isEmpty()){
            Log.error("there are no Model for the List Of Positive Constraints");
            return res ;
        }

        //add one by one all Constraints to the Solver and check if  it can always produce a Model
        for (Pair<Optional<String>, BoolExpr> negConstraint:  negConstList) {
            Pair<String,BoolExpr> actualConstraint = new ImmutablePair<>("negated_" + negConstraint.getLeft().orElse("NoName"), cdContext.getContext().mkNot(negConstraint.getRight()));
            solverConstraints.add(actualConstraint);
            modelOptional = getModel(cdContext.getContext(), solverConstraints);
            if (modelOptional.isPresent()) {
                if (negConstraint.getLeft().isPresent()) {
                    res.add(buildOd(cdContext, negConstraint.getLeft().get(), solverConstraints, cd.getCDDefinition(), partial));
                } else {
                    res.add(buildOd(cdContext, "NoName", solverConstraints, cd.getCDDefinition(), partial));
                }

            }
            solverConstraints.remove(actualConstraint);
        }
        return  res  ;
    }
    public static Set<ASTODArtifact> oclDiff(ASTCDCompilationUnit cd ,Set<ASTOCLCompilationUnit>  in , Set<ASTOCLCompilationUnit> notIn, Context context){
        return oclDiff(cd,in, notIn, context, false);
    }
    protected static ASTODArtifact buildOd(CDContext cdContext, String ODName, List<Pair<String,BoolExpr>> solverConstraints, ASTCDDefinition cd, boolean partial) {
        cdContext.getClassConstrs().addAll(solverConstraints.stream().map(Pair::getRight).collect(Collectors.toList()));
        SMT2ODGenerator smt2ODGenerator = new SMT2ODGenerator();
        return smt2ODGenerator.buildOd(new ODContext(cdContext, cd,partial), ODName);
    }
    protected static ASTODArtifact buildOd(CDContext cdContext, String ODName, List<Pair<String,BoolExpr>> solverConstraints, ASTCDDefinition cd) {
        return buildOd(cdContext,ODName,solverConstraints,cd,false);
    }
    public  static   Optional<Model> getModel (Context ctx, List<Pair<String,BoolExpr>> constraints){
        Solver s = ctx.mkSolver();
        int i = 0;  // Names must be unique, hence we have a counter
        for (Pair<String,BoolExpr> expr : constraints){
            s.assertAndTrack(expr.getRight(), ctx.mkBoolConst("inv____" + expr.getLeft() + "____" + i));
            i++;
        }
        if (s.check() == Status.SATISFIABLE)
            return Optional.of(s.getModel());
        else {
            Log.warn("Found no instance. The following invariants lead to a contradiction: \n\t" +
                Arrays.stream(s.getUnsatCore())
                    .map(AST::getSExpr)
                    .map(name -> name.split("____")[1])
                    .collect(Collectors.toSet()));
            return Optional.empty();
        }

    }
}
