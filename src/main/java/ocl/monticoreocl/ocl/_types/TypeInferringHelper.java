package ocl.monticoreocl.ocl._types;

import de.monticore.symboltable.MutableScope;
import de.monticore.symboltable.types.references.ActualTypeArgument;
import de.monticore.umlcd4a.symboltable.references.CDTypeSymbolReference;

import java.util.List;

/**
 * Created by fmehlan on 02.03.18.
 */
public class FlattenHelper {


    /**
     * Flattens Optional<A> --> A
     */
    protected static CDTypeSymbolReference flattenOpt(CDTypeSymbolReference type) {
        List<ActualTypeArgument> arguments = type.getActualTypeArguments();
        if (type.getName().equals("Optional") && !arguments.isEmpty()) {
            type = (CDTypeSymbolReference) arguments.get(0).getType();
        }

        return type;
    }

    /**
     * Takes a Type and flattens them according to:
     * http://mbse.se-rwth.de/book1/index.php?c=chapter3-3#x1-560003.3.6
     */
    protected static CDTypeSymbolReference flatten(CDTypeSymbolReference type) {
        // remove first Optional
        type = flattenOpt(type);

        // flatten containers
        String typeName = type.getName();
        List<ActualTypeArgument> arguments = type.getActualTypeArguments();

        // Only proceed if type is container and has arguments
        if (!type.getActualTypeArguments().isEmpty()) {
            CDTypeSymbolReference innerType = (CDTypeSymbolReference) arguments.get(0).getType();
            if(typeName.equals("Set")) {
                type = flattenSet(type, innerType);
            }
            else if (typeName.equals("Collection")) {

            }
            else if (typeName.equals("List")) {
                type = flattenList(type, innerType);
            }
        }

        return type;
    }

    private static CDTypeSymbolReference flattenSet(CDTypeSymbolReference type, CDTypeSymbolReference innerType) {
        return  isContainer(innerType) ? innerType : type;
    }

    private static CDTypeSymbolReference flattenList(CDTypeSymbolReference type, CDTypeSymbolReference innerType) {
        return  isContainer(innerType) ? innerType : type;
    }

    private static Boolean isContainer(CDTypeSymbolReference type) {
        String typeName = type.getName();
        return typeName.equals("Set") || typeName.equals("List") || typeName.equals("Collection");
    }

    private static CDTypeSymbolReference flattenOptionalorSet(CDTypeSymbolReference previousType) {
        String typeName = previousType.getName();
        List<ActualTypeArgument> arguments = previousType.getActualTypeArguments();
        if (typeName.equals("Set") && !arguments.isEmpty()) {
            CDTypeSymbolReference innerType = (CDTypeSymbolReference) arguments.get(0).getType();
            if (innerType.getName().equals("Set"))
                return flattenOptionalorSet(innerType);
            if (innerType.getName().equals("Optional") && !innerType.getActualTypeArguments().isEmpty()) {
                addActualArgument(previousType, (CDTypeSymbolReference)innerType.getActualTypeArguments().get(0).getType());
                return flattenOptionalorSet(previousType);
            }
        }
        if (typeName.equals("Optional") && !arguments.isEmpty()) {
            CDTypeSymbolReference innerType = (CDTypeSymbolReference) arguments.get(0).getType();
            if (innerType.getName().equals("Set") || innerType.getName().equals("Optional"))
                return flattenOptionalorSet(innerType);
        }
        return previousType;
    }
}
