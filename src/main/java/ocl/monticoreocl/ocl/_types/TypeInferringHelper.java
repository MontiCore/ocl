/**
 * ******************************************************************************
 *  MontiCAR Modeling Family, www.se-rwth.de
 *  Copyright (c) 2017, Software Engineering Group at RWTH Aachen,
 *  All rights reserved.
 *
 *  This project is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 3.0 of the License, or (at your option) any later version.
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this project. If not, see <http://www.gnu.org/licenses/>.
 * *******************************************************************************
 */
package ocl.monticoreocl.ocl._types;

import de.monticore.symboltable.types.references.ActualTypeArgument;
import de.monticore.umlcd4a.symboltable.references.CDTypeSymbolReference;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fmehlan on 02.03.18.
 */
public class TypeInferringHelper {

    public static CDTypeSymbolReference getContainerGeneric(CDTypeSymbolReference type) {
        return ((CDTypeSymbolReference)type.getActualTypeArguments().get(0).getType());
    }

    /** if it has no generics it returns type */
    public static CDTypeSymbolReference getMostNestedGeneric(CDTypeSymbolReference type) {
        CDTypeSymbolReference ret = type;
        while (!ret.getActualTypeArguments().isEmpty()) {
            ret = getContainerGeneric(ret);
        }
        return ret;
    }

    public static void addActualArgument(CDTypeSymbolReference typeOuter, CDTypeSymbolReference typeInner) {
        String stringRepresentation = typeOuter.getStringRepresentation() + "<";

        List<ActualTypeArgument> actualTypeArguments = new ArrayList<>();
        ActualTypeArgument actualTypeArgument = new ActualTypeArgument(typeInner);
        actualTypeArguments.add(actualTypeArgument);

        stringRepresentation += typeInner.getStringRepresentation() + ">";
        typeOuter.setStringRepresentation(stringRepresentation);
        typeOuter.setActualTypeArguments(actualTypeArguments);
    }

    /**
     * Removes nested leading Optionals:
     * Optional<Optional<A>> --> A
     */
    public static CDTypeSymbolReference removeAllOptionals(CDTypeSymbolReference type) {
        List<ActualTypeArgument> arguments = type.getActualTypeArguments();
        if (type.getName().equals("Optional") && !arguments.isEmpty()) {
            CDTypeSymbolReference innerType = (CDTypeSymbolReference) arguments.get(0).getType();
            return innerType.getName().equals("Optional") ? removeAllOptionals(innerType) : innerType;
        }
        return type;
    }

    /**
     * Similiar to flatten once, but flattens all nested
     * container types. Used for implicit flattening in OCL
     * E.g. Collection<Optional<Set<X>>> -> X
     */
    public static CDTypeSymbolReference flattenAll(CDTypeSymbolReference type) {
        List<ActualTypeArgument> arguments = type.getActualTypeArguments();
        while (!arguments.isEmpty() &&
                isContainer((CDTypeSymbolReference) arguments.get(0).getType())) {
            type = flattenOnce(type);
            arguments = type.getActualTypeArguments();
        }

        if (!arguments.isEmpty())
            type = (CDTypeSymbolReference) arguments.get(0).getType();

        return type;
    }

    /**
     * Takes a Type and flattens them according to:
     * http://mbse.se-rwth.de/book1/index.php?c=chapter3-3#x1-560003.3.6
     */
    public static CDTypeSymbolReference flattenOnce(CDTypeSymbolReference type) {
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
                type = flattenCollection(type, innerType);
            }
            else if (typeName.equals("List")) {
                type = flattenList(type, innerType);
            }
            else if (typeName.equals("Optional")) {
                type = flattenOptional(type, innerType);
            }
        }

        return type;
    }

    public static CDTypeSymbolReference flattenSet(CDTypeSymbolReference type, CDTypeSymbolReference innerType) {
        // if Set<Optional<X>> return Set<X>
        if (innerType.getName().equals("Optional")) {
            return handleReturnType(innerType, "Set");
        }
        // if Set<B<X>> return  B<X> , B in {List, Set, Collection}
        // if Set<X> return Set<X>
        return isContainer(innerType) ? innerType : type;
    }

    public static CDTypeSymbolReference flattenList(CDTypeSymbolReference type, CDTypeSymbolReference innerType) {
        // if List<B<X>> return  List<X> , B in {List, Set, Collection, Optional}
        if (isContainer(innerType)) {
            return handleReturnType(innerType, "List");
        }
        // if List<X> return  List<X>
        else {
            return type;
        }
    }

    public static CDTypeSymbolReference flattenCollection(CDTypeSymbolReference type, CDTypeSymbolReference innerType) {
        // if Collection<List<X>> return List<X>
        if (innerType.getName().equals("List")) {
            return innerType;
        }
        // if Collection<B<X>> return  Collection<X> , B in {List, Set, Optional}
        if (isContainer(innerType)) {
            return handleReturnType(innerType, "Collection");
        }
        // if Collection<X> return Collection<X>
        else {
            return type;
        }
    }

    public static CDTypeSymbolReference flattenOptional(CDTypeSymbolReference type, CDTypeSymbolReference innerType) {
        // if Optional<B<X>> return  B<X> , B in {List, Set, Optional, Collection}
        return isContainer(innerType) ? innerType : type;
    }

    public static CDTypeSymbolReference handleReturnType(CDTypeSymbolReference innerType, String containerName) {
        List<ActualTypeArgument> innerArguments = innerType.getActualTypeArguments();
        CDTypeSymbolReference collectionType = new CDTypeSymbolReference(containerName, innerType.getEnclosingScope());
        if (!innerArguments.isEmpty()) {
            CDTypeSymbolReference innerInnerType = (CDTypeSymbolReference) innerArguments.get(0).getType();
            addActualArgument(collectionType, innerInnerType);
        }
        return collectionType;
    }

    public static Boolean isContainer(CDTypeSymbolReference type) {
        String typeName = type.getName();
        return typeName.equals("Set") || typeName.equals("List") || typeName.equals("Collection")
                || typeName.equals("Optional");
    }


}
