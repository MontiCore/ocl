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
package ocl.monticoreocl;


import de.monticore.symboltable.*;
import de.monticore.umlcd4a.cd4analysis._ast.ASTCDCompilationUnit;
import de.monticore.umlcd4a.cd4analysis._parser.CD4AnalysisParser;
import de.monticore.umlcd4a.symboltable.*;
import ocl.monticoreocl.ocl._cocos.OCLCoCoChecker;
import ocl.monticoreocl.ocl._cocos.OCLCoCos;
import ocl.monticoreocl.ocl._symboltable.*;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

public class OCLDeclarationTypeInferringTest extends AbstractOCLTest {

    @Override
    protected OCLCoCoChecker getChecker() {
        return OCLCoCos.createChecker();
    }

    @Test
    public void testCDModelCnC() throws IOException{
        CD4AnalysisParser parser = new CD4AnalysisParser();
        Path model = Paths.get("src/test/resources/example/CDs/MontiArc.cd");
        Optional<ASTCDCompilationUnit> root = parser.parseCDCompilationUnit(model.toString());
        assertFalse(parser.hasErrors());
        assertTrue(root.isPresent());
    }

    @Test
    public void declaredTypesTest() {
        final GlobalScope globalScope = OCLGlobalScopeTestFactory.create("src/test/resources/");

        final OCLFileSymbol oclFileSymbol = globalScope.<OCLFileSymbol> resolve("example.typeInferringModels.declaredTypes", OCLFileSymbol.KIND).orElse(null);
        assertNotNull(oclFileSymbol);
        assertEquals(3, globalScope.getSubScopes().size());
        OCLInvariantSymbol oclInvariantSymbol = oclFileSymbol.getOCLInvariant("test").orElse(null);
        assertNotNull(oclInvariantSymbol);

        OCLVariableDeclarationSymbol declVarSymbol = oclInvariantSymbol.getOCLVariableDecl("cmp").orElse(null);
        assertNotNull(declVarSymbol);
        assertEquals("Cmp", declVarSymbol.getVarTypeName());

        OCLVariableDeclarationSymbol declVarSymbol2 = oclInvariantSymbol.getOCLVariableDecl("ports").orElse(null);
        assertNotNull(declVarSymbol2);
        assertEquals("List", declVarSymbol2.getVarTypeName());
        assertEquals("List<Port>", declVarSymbol2.getType().getStringRepresentation());
        assertEquals(1, declVarSymbol2.getType().getActualTypeArguments().size());
        assertEquals("Port", declVarSymbol2.getType().getActualTypeArguments().get(0).getType().toString());

        OCLVariableDeclarationSymbol declVarSymbol3 = oclInvariantSymbol.getOCLVariableDecl("ports2").orElse(null);
        assertNotNull(declVarSymbol3);
        assertEquals("List", declVarSymbol3.getVarTypeName());
        assertEquals("List<List<Port>>", declVarSymbol3.getType().getStringRepresentation());

        OCLVariableDeclarationSymbol declVarSymbol5 = oclInvariantSymbol.getOCLVariableDecl("i").orElse(null);
        assertNotNull(declVarSymbol5);
        assertEquals("Integer", declVarSymbol5.getVarTypeName());

        OCLVariableDeclarationSymbol declVarSymbol6 = oclInvariantSymbol.getOCLVariableDecl("p2").orElse(null);
        assertNotNull(declVarSymbol6);
        assertEquals("Port", declVarSymbol6.getVarTypeName());
    }

    @Test
    public void simpleTypesTest() {
        final GlobalScope globalScope = OCLGlobalScopeTestFactory.create("src/test/resources/");

        final OCLFileSymbol oclFileSymbol = globalScope.<OCLFileSymbol> resolve("example.typeInferringModels.simpleTypes", OCLFileSymbol.KIND).orElse(null);
        assertNotNull(oclFileSymbol);
        assertEquals(3, globalScope.getSubScopes().size());
        OCLInvariantSymbol oclInvariantSymbol = oclFileSymbol.getOCLInvariant("test").orElse(null);
        assertNotNull(oclInvariantSymbol);

        OCLVariableDeclarationSymbol declVarSymbol = oclInvariantSymbol.getOCLVariableDecl("numberUnit").orElse(null);
        assertNotNull(declVarSymbol);
        assertEquals("Amount", declVarSymbol.getVarTypeName());

        OCLVariableDeclarationSymbol declVarSymbol2 = oclInvariantSymbol.getOCLVariableDecl("number").orElse(null);
        assertNotNull(declVarSymbol2);
        assertEquals("Double", declVarSymbol2.getVarTypeName());

        OCLVariableDeclarationSymbol declVarSymbol3 = oclInvariantSymbol.getOCLVariableDecl("i").orElse(null);
        assertNotNull(declVarSymbol3);
        assertEquals("Integer", declVarSymbol3.getVarTypeName());

        OCLVariableDeclarationSymbol declVarSymbol4 = oclInvariantSymbol.getOCLVariableDecl("s").orElse(null);
        assertNotNull(declVarSymbol4);
        assertEquals("String", declVarSymbol4.getVarTypeName());

        OCLVariableDeclarationSymbol declVarSymbol5 = oclInvariantSymbol.getOCLVariableDecl("c").orElse(null);
        assertNotNull(declVarSymbol5);
        assertEquals("Character", declVarSymbol5.getVarTypeName());
    }

    @Test
    public void concatenationsTest() {
        final GlobalScope globalScope = OCLGlobalScopeTestFactory.create("src/test/resources/");

        final OCLFileSymbol oclFileSymbol = globalScope.<OCLFileSymbol> resolve("example.typeInferringModels.concatenations", OCLFileSymbol.KIND).orElse(null);
        assertNotNull(oclFileSymbol);
        assertEquals(3, globalScope.getSubScopes().size());
        OCLInvariantSymbol oclInvariantSymbol = oclFileSymbol.getOCLInvariant("test").orElse(null);
        assertNotNull(oclInvariantSymbol);

        OCLVariableDeclarationSymbol declVarSymbol = oclInvariantSymbol.getOCLVariableDecl("p").orElse(null);
        assertNotNull(declVarSymbol);
        assertEquals("Set", declVarSymbol.getVarTypeName());
        assertEquals("Set<Person>", declVarSymbol.getType().getStringRepresentation());
        assertEquals(1, declVarSymbol.getType().getActualTypeArguments().size());
        assertEquals("Person", declVarSymbol.getType().getActualTypeArguments().get(0).getType().toString());

        OCLVariableDeclarationSymbol declVarSymbol2 = oclInvariantSymbol.getOCLVariableDecl("p").orElse(null);
        assertNotNull(declVarSymbol2);
        assertEquals("Set", declVarSymbol2.getVarTypeName());
        assertEquals("Set<Person>", declVarSymbol2.getType().getStringRepresentation());
        assertEquals(1, declVarSymbol2.getType().getActualTypeArguments().size());
        assertEquals("Person", declVarSymbol2.getType().getActualTypeArguments().get(0).getType().toString());


        OCLVariableDeclarationSymbol declVarSymbol3 = oclInvariantSymbol.getOCLVariableDecl("m").orElse(null);
        assertNotNull(declVarSymbol3);
        assertEquals("List", declVarSymbol3.getVarTypeName());
        assertEquals("List<Message>", declVarSymbol3.getType().getStringRepresentation());
        assertEquals(1, declVarSymbol3.getType().getActualTypeArguments().size());
        assertEquals("Message", declVarSymbol3.getType().getActualTypeArguments().get(0).getType().toString());

        OCLVariableDeclarationSymbol declVarSymbol4 = oclInvariantSymbol.getOCLVariableDecl("i").orElse(null);
        assertNotNull(declVarSymbol4);
        assertEquals("Integer", declVarSymbol4.getVarTypeName());

        OCLVariableDeclarationSymbol declVarSymbol5 = oclInvariantSymbol.getOCLVariableDecl("i2").orElse(null);
        assertNotNull(declVarSymbol5);
        assertEquals("Integer", declVarSymbol5.getVarTypeName());
    }

    @Test
    public void thisAndClassesTest() {
        final GlobalScope globalScope = OCLGlobalScopeTestFactory.create("src/test/resources/");

        final OCLFileSymbol oclFileSymbol = globalScope.<OCLFileSymbol>resolve("example.typeInferringModels.thisAndClasses", OCLFileSymbol.KIND).orElse(null);
        assertNotNull(oclFileSymbol);
        assertEquals(3, globalScope.getSubScopes().size());
        OCLInvariantSymbol oclInvariantSymbol = oclFileSymbol.getOCLInvariant("test").orElse(null);
        assertNotNull(oclInvariantSymbol);

        OCLVariableDeclarationSymbol declVarSymbol = oclInvariantSymbol.getOCLVariableDecl("a").orElse(null);
        assertNotNull(declVarSymbol);
        assertEquals("Set", declVarSymbol.getVarTypeName());
        assertEquals("Set<Auction>", declVarSymbol.getType().getStringRepresentation());
        assertEquals(1, declVarSymbol.getType().getActualTypeArguments().size());
        assertEquals("Auction", declVarSymbol.getType().getActualTypeArguments().get(0).getType().toString());

        OCLVariableDeclarationSymbol declVarSymbol2 = oclInvariantSymbol.getOCLVariableDecl("a2").orElse(null);
        assertNotNull(declVarSymbol2);
        assertEquals("Set", declVarSymbol2.getVarTypeName());
        assertEquals("Set<Auction>", declVarSymbol2.getType().getStringRepresentation());
        assertEquals(1, declVarSymbol2.getType().getActualTypeArguments().size());
        assertEquals("Auction", declVarSymbol2.getType().getActualTypeArguments().get(0).getType().toString());

        OCLVariableDeclarationSymbol declVarSymbol3 = oclInvariantSymbol.getOCLVariableDecl("s").orElse(null);
        assertNotNull(declVarSymbol3);
        assertEquals("Set", declVarSymbol3.getVarTypeName());
        assertEquals("Set<Company>", declVarSymbol3.getType().getStringRepresentation());
        assertEquals(1, declVarSymbol3.getType().getActualTypeArguments().size());
        assertEquals("Company", declVarSymbol3.getType().getActualTypeArguments().get(0).getType().toString());

        OCLVariableDeclarationSymbol declVarSymbol4 = oclInvariantSymbol.getOCLVariableDecl("s2").orElse(null);
        assertNotNull(declVarSymbol4);
        assertEquals("Integer", declVarSymbol4.getVarTypeName());
    }

    @Ignore
    @Test
    public void overwriteVariablesTest() {
        final GlobalScope globalScope = OCLGlobalScopeTestFactory.create("src/test/resources/");

        final OCLFileSymbol oclFileSymbol = globalScope.<OCLFileSymbol>resolve("example.typeInferringModels.overwriteVariables", OCLFileSymbol.KIND).orElse(null);
        assertNotNull(oclFileSymbol);
        assertEquals(3, globalScope.getSubScopes().size());
        OCLInvariantSymbol oclInvariantSymbol = oclFileSymbol.getOCLInvariant("test").orElse(null);
        assertNotNull(oclInvariantSymbol);

        OCLVariableDeclarationSymbol declVarSymbol = oclInvariantSymbol.getOCLVariableDecl("var").orElse(null);
        assertNotNull(declVarSymbol);
        assertEquals("String", declVarSymbol.getVarTypeName());
    }

    @Test
    public void qualifiedPrimariesTest() {
        final GlobalScope globalScope = OCLGlobalScopeTestFactory.create("src/test/resources/");

        final OCLFileSymbol oclFileSymbol = globalScope.<OCLFileSymbol> resolve("example.typeInferringModels.qualifiedPrimaries", OCLFileSymbol.KIND).orElse(null);
        assertNotNull(oclFileSymbol);
        assertEquals(3, globalScope.getSubScopes().size());
        OCLInvariantSymbol oclInvariantSymbol = oclFileSymbol.getOCLInvariant("test").orElse(null);
        assertNotNull(oclInvariantSymbol);

        OCLVariableDeclarationSymbol declVarSymbol = oclInvariantSymbol.getOCLVariableDecl("m").orElse(null);
        assertNotNull(declVarSymbol);
        assertEquals("List", declVarSymbol.getVarTypeName());

        OCLVariableDeclarationSymbol declVarSymbol2 = oclInvariantSymbol.getOCLVariableDecl("s").orElse(null);
        assertNotNull(declVarSymbol2);
        assertEquals("Integer", declVarSymbol2.getVarTypeName());

        OCLVariableDeclarationSymbol declVarSymbol3 = oclInvariantSymbol.getOCLVariableDecl("b").orElse(null);
        assertNotNull(declVarSymbol3);
        assertEquals("Boolean", declVarSymbol3.getVarTypeName());

        OCLVariableDeclarationSymbol declVarSymbol4 = oclInvariantSymbol.getOCLVariableDecl("m2").orElse(null);
        assertNotNull(declVarSymbol4);
        assertEquals("Message", declVarSymbol4.getVarTypeName());

        OCLVariableDeclarationSymbol declVarSymbol5 = oclInvariantSymbol.getOCLVariableDecl("t").orElse(null);
        assertNotNull(declVarSymbol5);
        assertEquals("Time", declVarSymbol5.getVarTypeName());
    }

    @Test
    public void comprehensionsTest() {
        final GlobalScope globalScope = OCLGlobalScopeTestFactory.create("src/test/resources/");

        final OCLFileSymbol oclFileSymbol = globalScope.<OCLFileSymbol> resolve("example.typeInferringModels.comprehensions", OCLFileSymbol.KIND).orElse(null);
        assertNotNull(oclFileSymbol);
        assertEquals(3, globalScope.getSubScopes().size());
        OCLInvariantSymbol oclInvariantSymbol = oclFileSymbol.getOCLInvariant("test").orElse(null);
        assertNotNull(oclInvariantSymbol);

        OCLVariableDeclarationSymbol declVarSymbol = oclInvariantSymbol.getOCLVariableDecl("comp").orElse(null);
        assertNotNull(declVarSymbol);
        assertEquals("List", declVarSymbol.getVarTypeName());
        assertEquals("List<String>", declVarSymbol.getType().getStringRepresentation());
        assertEquals(1, declVarSymbol.getType().getActualTypeArguments().size());
        assertEquals("String", declVarSymbol.getType().getActualTypeArguments().get(0).getType().toString());

        OCLVariableDeclarationSymbol declVarSymbol2 = oclInvariantSymbol.getOCLVariableDecl("comp2").orElse(null);
        assertNotNull(declVarSymbol2);
        assertEquals("Set", declVarSymbol2.getVarTypeName());
        assertEquals("Set<Integer>", declVarSymbol2.getType().getStringRepresentation());
        assertEquals(1, declVarSymbol2.getType().getActualTypeArguments().size());
        assertEquals("Integer", declVarSymbol2.getType().getActualTypeArguments().get(0).getType().toString());

        OCLVariableDeclarationSymbol declVarSymbol3 = oclInvariantSymbol.getOCLVariableDecl("comp3").orElse(null);
        assertNotNull(declVarSymbol3);
        assertEquals("Collection", declVarSymbol3.getVarTypeName());
        assertEquals("Collection<Person>", declVarSymbol3.getType().getStringRepresentation());
        assertEquals(1, declVarSymbol3.getType().getActualTypeArguments().size());
        assertEquals("Person", declVarSymbol3.getType().getActualTypeArguments().get(0).getType().toString());

        OCLVariableDeclarationSymbol declVarSymbol4 = oclInvariantSymbol.getOCLVariableDecl("s").orElse(null);
        assertNotNull(declVarSymbol4);
        assertEquals("Integer", declVarSymbol4.getVarTypeName());

        OCLVariableDeclarationSymbol declVarSymbol5 = oclInvariantSymbol.getOCLVariableDecl("comp4").orElse(null);
        assertNotNull(declVarSymbol5);
        assertEquals("Collection", declVarSymbol5.getVarTypeName());
    }

    @Test
    public void parenthesesTest() {
        final GlobalScope globalScope = OCLGlobalScopeTestFactory.create("src/test/resources/");

        final OCLFileSymbol oclFileSymbol = globalScope.<OCLFileSymbol>resolve("example.typeInferringModels.parentheses", OCLFileSymbol.KIND).orElse(null);
        assertNotNull(oclFileSymbol);
        assertEquals(3, globalScope.getSubScopes().size());
        OCLInvariantSymbol oclInvariantSymbol = oclFileSymbol.getOCLInvariant("test").orElse(null);
        assertNotNull(oclInvariantSymbol);

        OCLVariableDeclarationSymbol declVarSymbol = oclInvariantSymbol.getOCLVariableDecl("name").orElse(null);
        assertNotNull(declVarSymbol);
        assertEquals("String", declVarSymbol.getVarTypeName());
    }

    @Test
    public void implicitFlatteningTest() {
        final GlobalScope globalScope = OCLGlobalScopeTestFactory.create("src/test/resources/");

        final OCLFileSymbol oclFileSymbol = globalScope.<OCLFileSymbol> resolve("example.typeInferringModels.implicitFlattening", OCLFileSymbol.KIND).orElse(null);
        assertNotNull(oclFileSymbol);
        assertEquals(3, globalScope.getSubScopes().size());
        OCLInvariantSymbol oclInvariantSymbol = oclFileSymbol.getOCLInvariant("test").orElse(null);
        assertNotNull(oclInvariantSymbol);

        OCLVariableDeclarationSymbol declVarSymbol = oclInvariantSymbol.getOCLVariableDecl("b").orElse(null);
        assertNotNull(declVarSymbol);
        assertEquals("Set", declVarSymbol.getVarTypeName());
        assertEquals("Set<Person>", declVarSymbol.getType().getStringRepresentation());
        assertEquals(1, declVarSymbol.getType().getActualTypeArguments().size());
        assertEquals("Person", declVarSymbol.getType().getActualTypeArguments().get(0).getType().toString());

        OCLVariableDeclarationSymbol declVarSymbol2 = oclInvariantSymbol.getOCLVariableDecl("m").orElse(null);
        assertNotNull(declVarSymbol2);
        assertEquals("Set", declVarSymbol2.getVarTypeName());
        assertEquals("Set<List<Message>>", declVarSymbol2.getType().getStringRepresentation());
        assertEquals(1, declVarSymbol2.getType().getActualTypeArguments().size());
        assertEquals("List", declVarSymbol2.getType().getActualTypeArguments().get(0).getType().toString());

        OCLVariableDeclarationSymbol declVarSymbol3 = oclInvariantSymbol.getOCLVariableDecl("n").orElse(null);
        assertNotNull(declVarSymbol3);
        assertEquals("Set", declVarSymbol3.getVarTypeName());
        assertEquals("Set<String>", declVarSymbol3.getType().getStringRepresentation());
        assertEquals(1, declVarSymbol3.getType().getActualTypeArguments().size());
        assertEquals("String", declVarSymbol3.getType().getActualTypeArguments().get(0).getType().toString());
    }
}
