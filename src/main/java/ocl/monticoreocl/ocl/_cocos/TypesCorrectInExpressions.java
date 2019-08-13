/* (c) https://github.com/MontiCore/monticore */
package ocl.monticoreocl.ocl._cocos;

import de.monticore.symboltable.MutableScope;
import ocl.monticoreocl.ocl._ast.ASTOCLInvariant;
import ocl.monticoreocl.ocl._types.OCLTypeCheckingVisitor;

/**
 * Created by Ferdinand Mehlan on 15.01.2018.
 */
public class TypesCorrectInExpressions implements OCLASTOCLInvariantCoCo {

    @Override
    public void check(ASTOCLInvariant astInvariant){
        MutableScope scope = (MutableScope) astInvariant.getSpannedScope();
        OCLTypeCheckingVisitor.checkInvariants(astInvariant, scope);
    }

}
