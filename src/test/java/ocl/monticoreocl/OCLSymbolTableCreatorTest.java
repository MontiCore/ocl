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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;

import org.junit.Ignore;
import org.junit.Test;

import de.monticore.symboltable.ArtifactScope;
import de.monticore.symboltable.GlobalScope;
import de.monticore.types.types._ast.ASTPrimitiveType;
import ocl.monticoreocl.ocl._ast.ASTOCLConstructorSignature;
import ocl.monticoreocl.ocl._ast.ASTOCLInvariant;
import ocl.monticoreocl.ocl._ast.ASTOCLMethodDeclaration;
import ocl.monticoreocl.ocl._ast.ASTOCLMethodSignature;
import ocl.monticoreocl.ocl._ast.ASTOCLParameterDeclaration;
import ocl.monticoreocl.ocl._ast.ASTOCLThrowsClause;
import ocl.monticoreocl.ocl._ast.ASTOCLVariableDeclaration;
import ocl.monticoreocl.ocl._symboltable.OCLConstructorSignatureSymbol;
import ocl.monticoreocl.ocl._symboltable.OCLFileSymbol;
import ocl.monticoreocl.ocl._symboltable.OCLInvariantSymbol;
import ocl.monticoreocl.ocl._symboltable.OCLMethodDeclarationSymbol;
import ocl.monticoreocl.ocl._symboltable.OCLMethodSignatureSymbol;
import ocl.monticoreocl.ocl._symboltable.OCLParameterDeclarationSymbol;
import ocl.monticoreocl.ocl._symboltable.OCLPostStatementSymbol;
import ocl.monticoreocl.ocl._symboltable.OCLPreStatementSymbol;
import ocl.monticoreocl.ocl._symboltable.OCLThrowsClauseSymbol;
import ocl.monticoreocl.ocl._symboltable.OCLVariableDeclarationSymbol;

public class OCLSymbolTableCreatorTest {

	@Test
	public void symbolTableCreatorTest() {
		final GlobalScope globalScope = OCLGlobalScopeTestFactory.create("src/test/resources/");

		// 1st case:
		// methodSignature - statement without any parameters but with the rest
		// of the production content
		final OCLFileSymbol OCLFileSymbol1 = globalScope.<OCLFileSymbol> resolve("example.symbolTableTestFiles.test1", OCLFileSymbol.KIND).orElse(null);
		assertNotNull(OCLFileSymbol1);
		assertEquals(1, globalScope.getSubScopes().size());
		final ArtifactScope artifactScope1 = (ArtifactScope) globalScope.getSubScopes().get(0);
		assertSame(artifactScope1, OCLFileSymbol1.getEnclosingScope());
		assertEquals(1, artifactScope1.getSubScopes().size());

		// methodSignatureSymbol1

		final OCLMethodSignatureSymbol methSigSymbol1 = OCLFileSymbol1.getOCLMethSig("isAuctionsSpecific").orElse(null);
		assertNotNull(methSigSymbol1);
		assertNotNull(methSigSymbol1.getSpannedScope());
		assertSame(methSigSymbol1, methSigSymbol1.getSpannedScope().getSpanningSymbol().get());
		assertEquals("isAuctionsSpecific", methSigSymbol1.getName());
		assertEquals(ASTPrimitiveType.class, methSigSymbol1.getReturnType().getClass());
		assertEquals("boolean", methSigSymbol1.getReturnType().toString());
		assertEquals("BidMessage", methSigSymbol1.getClassName());

		// AST assertions of methSigSymbol1
		assertTrue(methSigSymbol1.getAstNode().isPresent());
		assertTrue(methSigSymbol1.getAstNode().get() instanceof ASTOCLMethodSignature);
		assertSame(methSigSymbol1, methSigSymbol1.getAstNode().get().getSymbol().get());
		assertSame(methSigSymbol1.getEnclosingScope(), methSigSymbol1.getAstNode().get().getEnclosingScope().get());

		// methSigSymbol1 -> throwsClauseSymbol1
		final OCLThrowsClauseSymbol throwsClauseSymbol1 = methSigSymbol1.getOCLThrowsClause("Exception").orElse(null);
		assertNotNull(throwsClauseSymbol1);

		// AST assertions of throwsClauseSymbol1
		assertTrue(throwsClauseSymbol1.getAstNode().isPresent());
		assertTrue(throwsClauseSymbol1.getAstNode().get() instanceof ASTOCLThrowsClause);
		assertSame(throwsClauseSymbol1, throwsClauseSymbol1.getAstNode().get().getSymbol().get());
		assertSame(throwsClauseSymbol1.getEnclosingScope(),
				throwsClauseSymbol1.getAstNode().get().getEnclosingScope().get());

		final OCLPreStatementSymbol preSymbol1 = methSigSymbol1.getOCLPreStatement("IAS1").orElse(null);
		assertNotNull(preSymbol1);

		final OCLPostStatementSymbol postSymbol2 = methSigSymbol1.getOCLPostStatement("IAS2").orElse(null);
		assertNotNull(postSymbol2);

		// 2nd case:
		// methodSignature - statement with one parameter and also with the rest
		// of the production content
		final OCLFileSymbol OCLFileSymbol2 = globalScope.<OCLFileSymbol> resolve("example.symbolTableTestFiles.test2", OCLFileSymbol.KIND).orElse(null);
		assertNotNull(OCLFileSymbol2);
		assertEquals(4, globalScope.getSubScopes().size());
		final ArtifactScope artifactScope2 = (ArtifactScope) globalScope.getSubScopes().get(0);
		assertSame(artifactScope2, OCLFileSymbol1.getEnclosingScope());
		assertEquals(1, artifactScope2.getSubScopes().size());

		final OCLVariableDeclarationSymbol varDeclSymbol1 = OCLFileSymbol2.getOCLVariableDecl("oldCo").orElse(null);
		assertNotNull(varDeclSymbol1);

		// methodSignatureSymbol2
		final OCLMethodSignatureSymbol methSigSymbol2 = OCLFileSymbol2.getOCLMethSig("changeCompany").orElse(null);
		assertNotNull(methSigSymbol2);
		assertNotNull(methSigSymbol2.getSpannedScope());
		assertSame(methSigSymbol2, methSigSymbol2.getSpannedScope().getSpanningSymbol().get());
		assertEquals("changeCompany", methSigSymbol2.getName());
		assertEquals(ASTPrimitiveType.class, methSigSymbol2.getReturnType().getClass());
		assertEquals("boolean", methSigSymbol2.getReturnType().toString());
		assertEquals("Person", methSigSymbol2.getClassName());

		// AST assertions of methSigSymbol2
		assertTrue(methSigSymbol2.getAstNode().isPresent());
		assertTrue(methSigSymbol2.getAstNode().get() instanceof ASTOCLMethodSignature);
		assertSame(methSigSymbol2, methSigSymbol2.getAstNode().get().getSymbol().get());
		assertSame(methSigSymbol2.getEnclosingScope(), methSigSymbol2.getAstNode().get().getEnclosingScope().get());

		final OCLThrowsClauseSymbol throwsClauseSym1 = methSigSymbol2.getOCLThrowsClause("RecognitionException").orElse(null);
		assertNotNull(throwsClauseSym1);

		// AST assertions of throwsClauseSym1
		assertTrue(throwsClauseSym1.getAstNode().isPresent());
		assertTrue(throwsClauseSym1.getAstNode().get() instanceof ASTOCLThrowsClause);
		assertSame(throwsClauseSym1, throwsClauseSym1.getAstNode().get().getSymbol().get());
		assertSame(throwsClauseSym1.getEnclosingScope(), throwsClauseSym1.getAstNode().get().getEnclosingScope().get());

		// methodSignatureSymbol2 -> parameterDeclarationSymbol1
		final OCLParameterDeclarationSymbol parameterDeclarationSymbol1 = methSigSymbol2.getOCLParamDecl("gehalt").orElse(null);
		assertNotNull(parameterDeclarationSymbol1);
		assertEquals(ASTPrimitiveType.class, parameterDeclarationSymbol1.getType().getClass());
		assertEquals("int", parameterDeclarationSymbol1.getType().toString());

		// AST assertions of parameterDeclarationSymbol1
		assertTrue(parameterDeclarationSymbol1.getAstNode().isPresent());
		assertTrue(parameterDeclarationSymbol1.getAstNode().get() instanceof ASTOCLParameterDeclaration);
		assertSame(parameterDeclarationSymbol1, parameterDeclarationSymbol1.getAstNode().get().getSymbol().get());
		assertSame(parameterDeclarationSymbol1.getEnclosingScope(),
				parameterDeclarationSymbol1.getAstNode().get().getEnclosingScope().get());

		// methSigSymbol2 -> variableDeclarationSymbol1
		final OCLVariableDeclarationSymbol variableDeclarationSymbol1 = methSigSymbol2.getOCLVariableDecl("oldCo").orElse(null);
		assertNotNull(variableDeclarationSymbol1);
		assertEquals("ClassName", variableDeclarationSymbol1.getVarTypeName());

		// AST assertions of variableDeclarationSymbol1
		assertTrue(variableDeclarationSymbol1.getAstNode().isPresent());
		assertTrue(variableDeclarationSymbol1.getAstNode().get() instanceof ASTOCLVariableDeclaration);
		assertSame(variableDeclarationSymbol1, variableDeclarationSymbol1.getAstNode().get().getSymbol().get());
		assertSame(variableDeclarationSymbol1.getEnclosingScope(),
				variableDeclarationSymbol1.getAstNode().get().getEnclosingScope().get());


		// methSigSymbol2 -> variableDeclarationSymbol2
		final OCLVariableDeclarationSymbol variableDeclarationSymbol2 = methSigSymbol2.getOCLVariableDecl("newCos").orElse(null);
		assertNotNull(variableDeclarationSymbol2);
		assertEquals("Integer", variableDeclarationSymbol2.getType().toString());

		// AST assertions of variableDeclarationSymbol2
		assertTrue(variableDeclarationSymbol2.getAstNode().isPresent());
		assertTrue(variableDeclarationSymbol2.getAstNode().get() instanceof ASTOCLVariableDeclaration);
		assertSame(variableDeclarationSymbol2, variableDeclarationSymbol2.getAstNode().get().getSymbol().get());
		assertSame(variableDeclarationSymbol2.getEnclosingScope(),
				variableDeclarationSymbol2.getAstNode().get().getEnclosingScope().get());


		// methSigSymbol2 -> variableDeclarationSymbol3
		final OCLVariableDeclarationSymbol variableDeclarationSymbol3 = methSigSymbol2.getOCLVariableDecl("newCo").orElse(null);
		assertNotNull(variableDeclarationSymbol3);

		// AST assertions of variableDeclarationSymbol3
		assertTrue(variableDeclarationSymbol3.getAstNode().isPresent());
		assertTrue(variableDeclarationSymbol3.getAstNode().get() instanceof ASTOCLVariableDeclaration);
		assertSame(variableDeclarationSymbol3, variableDeclarationSymbol3.getAstNode().get().getSymbol().get());
		assertSame(variableDeclarationSymbol3.getEnclosingScope(),
				variableDeclarationSymbol3.getAstNode().get().getEnclosingScope().get());

		final OCLMethodDeclarationSymbol methDeclSymbol = methSigSymbol2.getOCLMethodDecl("min").orElse(null);
		assertNotNull(methDeclSymbol);

		assertEquals("Class", methDeclSymbol.getReturnType());

		final OCLParameterDeclarationSymbol parameterDeclarationOfMethodDecl1 = methDeclSymbol.getOCLParamDecl("x").orElse(null);
		assertNotNull(parameterDeclarationOfMethodDecl1);
		assertEquals("int", parameterDeclarationOfMethodDecl1.getType().toString());

		final OCLParameterDeclarationSymbol parameterDeclarationOfMethodDecl2 = methDeclSymbol.getOCLParamDecl("y").orElse(null);
		assertNotNull(parameterDeclarationOfMethodDecl2);
		assertEquals("int", parameterDeclarationOfMethodDecl2.getType().toString());

		// file with a constructor
		final OCLFileSymbol constructorSigSymbol1 = globalScope.<OCLFileSymbol> resolve("example.symbolTableTestFiles.test3", OCLFileSymbol.KIND).orElse(null);
		assertNotNull(constructorSigSymbol1);

		// constructorSigSymbol
		final OCLConstructorSignatureSymbol constructorSigSymbol = constructorSigSymbol1.getOCLConstructorSig("Auction").orElse(null);
		assertNotNull(constructorSigSymbol);
		assertNotNull(constructorSigSymbol.getSpannedScope());
		assertSame(constructorSigSymbol, constructorSigSymbol.getSpannedScope().getSpanningSymbol().get());
		assertEquals("Auction", constructorSigSymbol.getName());

		// AST assertions of constructorSigSymbol
		assertTrue(constructorSigSymbol.getAstNode().isPresent());
		assertTrue(constructorSigSymbol.getAstNode().get() instanceof ASTOCLConstructorSignature);
		assertSame(constructorSigSymbol, constructorSigSymbol.getAstNode().get().getSymbol().get());
		assertSame(constructorSigSymbol.getEnclosingScope(),
				constructorSigSymbol.getAstNode().get().getEnclosingScope().get());

		// constructorSigSymbol -> parameterDeclarationSymbol2
		final OCLParameterDeclarationSymbol parameterDeclarationSymbol2 = constructorSigSymbol.getOCLParamDecl("bidder").orElse(null);
		assertNotNull(parameterDeclarationSymbol2);
		assertEquals(ASTPrimitiveType.class, parameterDeclarationSymbol2.getType().getClass());
		assertEquals("int", parameterDeclarationSymbol2.getType().toString());

		// AST assertions of parameterDeclarationSymbol2
		assertTrue(parameterDeclarationSymbol2.getAstNode().isPresent());
		assertTrue(parameterDeclarationSymbol2.getAstNode().get() instanceof ASTOCLParameterDeclaration);
		assertSame(parameterDeclarationSymbol2, parameterDeclarationSymbol2.getAstNode().get().getSymbol().get());
		assertSame(parameterDeclarationSymbol2.getEnclosingScope(),
				parameterDeclarationSymbol2.getAstNode().get().getEnclosingScope().get());

		// constructorSigSymbol -> parameterDeclarationSymbol2
		final OCLParameterDeclarationSymbol parameterDeclarationSymbol3 = constructorSigSymbol.getOCLParamDecl("bidObjects").orElse(null);
		assertNotNull(parameterDeclarationSymbol3);
		assertEquals(ASTPrimitiveType.class, parameterDeclarationSymbol3.getType().getClass());
		assertEquals("int", parameterDeclarationSymbol3.getType().toString());

		// AST assertions of parameterDeclarationSymbol3
		assertTrue(parameterDeclarationSymbol3.getAstNode().isPresent());
		assertTrue(parameterDeclarationSymbol3.getAstNode().get() instanceof ASTOCLParameterDeclaration);
		assertSame(parameterDeclarationSymbol3, parameterDeclarationSymbol3.getAstNode().get().getSymbol().get());
		assertSame(parameterDeclarationSymbol3.getEnclosingScope(),
				parameterDeclarationSymbol3.getAstNode().get().getEnclosingScope().get());

		final OCLFileSymbol OCLFileSymbol4 = globalScope.<OCLFileSymbol> resolve("example.symbolTableTestFiles.test4", OCLFileSymbol.KIND).orElse(null);
		assertNotNull(OCLFileSymbol4);
		final OCLInvariantSymbol invSymbol4 = OCLFileSymbol4.getOCLInvariant("Name").orElse(null);
		assertNotNull(invSymbol4);
		assertNotNull(invSymbol4.getSpannedScope());
		assertSame(invSymbol4, invSymbol4.getSpannedScope().getSpanningSymbol().get());
		assertEquals("Message", invSymbol4.getClassN());
		assertEquals("m", invSymbol4.getClassO());
		assertTrue(invSymbol4.getContext());
		assertFalse(invSymbol4.getImport());

		// AST assertions of invSymbol4
		assertTrue(invSymbol4.getAstNode().isPresent());
		assertTrue(invSymbol4.getAstNode().get() instanceof ASTOCLInvariant);
		assertSame(invSymbol4, invSymbol4.getAstNode().get().getSymbol().get());
		assertSame(invSymbol4.getEnclosingScope(), invSymbol4.getAstNode().get().getEnclosingScope().get());

		final OCLFileSymbol OCLFileSymbol5 = globalScope.<OCLFileSymbol> resolve("example.symbolTableTestFiles.test5", OCLFileSymbol.KIND).orElse(null);
		assertNotNull(OCLFileSymbol5);

		// methodSignatureSymbol5
		final OCLMethodSignatureSymbol methSigSymbol5 = OCLFileSymbol5.getOCLMethSig("changeUniversity").orElse(null);
		assertNotNull(methSigSymbol5);
		assertNotNull(methSigSymbol5.getSpannedScope());
		assertSame(methSigSymbol5, methSigSymbol5.getSpannedScope().getSpanningSymbol().get());
		assertEquals("changeUniversity", methSigSymbol5.getName());
		assertEquals(ASTPrimitiveType.class, methSigSymbol2.getReturnType().getClass());
		assertEquals("boolean", methSigSymbol5.getReturnType().toString());
		assertEquals("Student", methSigSymbol5.getClassName());

		// AST assertions of methSigSymbol5
		assertTrue(methSigSymbol5.getAstNode().isPresent());
		assertTrue(methSigSymbol5.getAstNode().get() instanceof ASTOCLMethodSignature);
		assertSame(methSigSymbol5, methSigSymbol5.getAstNode().get().getSymbol().get());
		assertSame(methSigSymbol5.getEnclosingScope(), methSigSymbol5.getAstNode().get().getEnclosingScope().get());

		final OCLThrowsClauseSymbol throwsClauseSym2 = methSigSymbol5.getOCLThrowsClause("Exception").orElse(null);
		assertNotNull(throwsClauseSym2);

		// AST assertions of throwsClauseSym2
		assertTrue(throwsClauseSym2.getAstNode().isPresent());
		assertTrue(throwsClauseSym2.getAstNode().get() instanceof ASTOCLThrowsClause);
		assertSame(throwsClauseSym2, throwsClauseSym2.getAstNode().get().getSymbol().get());
		assertSame(throwsClauseSym2.getEnclosingScope(), throwsClauseSym2.getAstNode().get().getEnclosingScope().get());

		// methSigSymbol5 -> parameterDeclarationSymbol4
		final OCLParameterDeclarationSymbol parameterDeclarationSymbol4 = methSigSymbol5.getOCLParamDecl("semester").orElse(null);
		assertNotNull(parameterDeclarationSymbol4);
		assertEquals(ASTPrimitiveType.class, parameterDeclarationSymbol4.getType().getClass());
		assertEquals("int", parameterDeclarationSymbol4.getType().toString());

		// AST assertions of parameterDeclarationSymbol4
		assertTrue(parameterDeclarationSymbol4.getAstNode().isPresent());
		assertTrue(parameterDeclarationSymbol4.getAstNode().get() instanceof ASTOCLParameterDeclaration);
		assertSame(parameterDeclarationSymbol4, parameterDeclarationSymbol4.getAstNode().get().getSymbol().get());
		assertSame(parameterDeclarationSymbol4.getEnclosingScope(),
				parameterDeclarationSymbol4.getAstNode().get().getEnclosingScope().get());

		// methSigSymbol5 -> variableDeclarationSymbol4
		final OCLVariableDeclarationSymbol variableDeclarationSymbol4 = methSigSymbol5.getOCLVariableDecl("oldUniName").orElse(null);
		assertNotNull(variableDeclarationSymbol4);
		assertEquals("Name", variableDeclarationSymbol4.getVarTypeName());

		// AST assertions of variableDeclarationSymbol4
		assertTrue(variableDeclarationSymbol4.getAstNode().isPresent());
		assertTrue(variableDeclarationSymbol4.getAstNode().get() instanceof ASTOCLVariableDeclaration);
		assertSame(variableDeclarationSymbol4, variableDeclarationSymbol4.getAstNode().get().getSymbol().get());
		assertSame(variableDeclarationSymbol4.getEnclosingScope(),
				variableDeclarationSymbol4.getAstNode().get().getEnclosingScope().get());

		// methSigSymbol5 -> variableDeclarationSymbol5
		final OCLVariableDeclarationSymbol variableDeclarationSymbol5 = methSigSymbol5.getOCLVariableDecl("newUniName").orElse(null);
		assertNotNull(variableDeclarationSymbol5);
		assertEquals("Name", variableDeclarationSymbol5.getVarTypeName());

		// AST assertions of variableDeclarationSymbol5
		assertTrue(variableDeclarationSymbol5.getAstNode().isPresent());
		assertTrue(variableDeclarationSymbol5.getAstNode().get() instanceof ASTOCLVariableDeclaration);
		assertSame(variableDeclarationSymbol5, variableDeclarationSymbol5.getAstNode().get().getSymbol().get());
		assertSame(variableDeclarationSymbol5.getEnclosingScope(),
				variableDeclarationSymbol5.getAstNode().get().getEnclosingScope().get());

		final OCLFileSymbol OCLFileSymbol6 = globalScope.<OCLFileSymbol> resolve("example.symbolTableTestFiles.test6", OCLFileSymbol.KIND).orElse(null);
		assertNotNull(OCLFileSymbol6);

		final OCLInvariantSymbol invSymbol6 = OCLFileSymbol6.getOCLInvariant("Test6").orElse(null);
		assertNotNull(invSymbol6);
		assertNotNull(invSymbol6.getSpannedScope());
		assertSame(invSymbol6, invSymbol6.getSpannedScope().getSpanningSymbol().get());
		assertEquals("Auction", invSymbol6.getClassN());
		assertEquals("a", invSymbol6.getClassO());
		assertTrue(invSymbol6.getContext());
		assertFalse(invSymbol6.getImport());

		// AST assertions of invSymbol6
		assertTrue(invSymbol6.getAstNode().isPresent());
		assertTrue(invSymbol6.getAstNode().get() instanceof ASTOCLInvariant);
		assertSame(invSymbol6, invSymbol6.getAstNode().get().getSymbol().get());
		assertSame(invSymbol6.getEnclosingScope(), invSymbol6.getAstNode().get().getEnclosingScope().get());

		final OCLFileSymbol OCLFileSymbol8 = globalScope.<OCLFileSymbol> resolve("example.symbolTableTestFiles.test8", OCLFileSymbol.KIND).orElse(null);
		assertNotNull(OCLFileSymbol8);

		// methSigSymbol6
		final OCLMethodSignatureSymbol methSigSymbol6 = OCLFileSymbol8.getOCLMethSig("testMethodSignature").orElse(null);
		assertNotNull(methSigSymbol6);
		assertNotNull(methSigSymbol6.getSpannedScope());
		assertSame(methSigSymbol6, methSigSymbol6.getSpannedScope().getSpanningSymbol().get());
		assertEquals("testMethodSignature", methSigSymbol6.getName());
		assertEquals(ASTPrimitiveType.class, methSigSymbol6.getReturnType().getClass());
		assertEquals("int", methSigSymbol6.getReturnType().toString());
		assertEquals("Testing", methSigSymbol6.getClassName());

		// AST assertions of methSigSymbol6
		assertTrue(methSigSymbol6.getAstNode().isPresent());
		assertTrue(methSigSymbol6.getAstNode().get() instanceof ASTOCLMethodSignature);
		assertSame(methSigSymbol6, methSigSymbol6.getAstNode().get().getSymbol().get());
		assertSame(methSigSymbol6.getEnclosingScope(), methSigSymbol6.getAstNode().get().getEnclosingScope().get());

		// methodSignatureSymbol6 -> parameterDeclarationSymbol5
		final OCLParameterDeclarationSymbol parameterDeclarationSymbol5 = methSigSymbol6.getOCLParamDecl("d").orElse(null);
		assertNotNull(parameterDeclarationSymbol5);
		assertEquals(ASTPrimitiveType.class, parameterDeclarationSymbol5.getType().getClass());
		assertEquals("double", parameterDeclarationSymbol5.getType().toString());

		// AST assertions of parameterDeclarationSymbol5
		assertTrue(parameterDeclarationSymbol5.getAstNode().isPresent());
		assertTrue(parameterDeclarationSymbol5.getAstNode().get() instanceof ASTOCLParameterDeclaration);
		assertSame(parameterDeclarationSymbol5, parameterDeclarationSymbol5.getAstNode().get().getSymbol().get());
		assertSame(parameterDeclarationSymbol5.getEnclosingScope(),
				parameterDeclarationSymbol5.getAstNode().get().getEnclosingScope().get());

		final OCLMethodDeclarationSymbol methDeclSymbol2 = methSigSymbol6.getOCLMethodDecl("function").orElse(null);
		assertNotNull(methDeclSymbol2);

		assertEquals("HelpFunction", methDeclSymbol2.getReturnType());

		// AST assertions of methDeclSymbol2
		assertTrue(methDeclSymbol2.getAstNode().isPresent());
		assertTrue(methDeclSymbol2.getAstNode().get() instanceof ASTOCLMethodDeclaration);
		assertSame(methDeclSymbol2, methDeclSymbol2.getAstNode().get().getSymbol().get());
		assertSame(methDeclSymbol2.getEnclosingScope(), methDeclSymbol2.getAstNode().get().getEnclosingScope().get());

		// parameterDeclarationOfMethodDecl3
		final OCLParameterDeclarationSymbol parameterDeclarationOfMethodDecl3 = methDeclSymbol2.getOCLParamDecl("a").orElse(null);
		assertNotNull(parameterDeclarationOfMethodDecl3);
		assertEquals("float", parameterDeclarationOfMethodDecl3.getType().toString());

		// AST assertions of parameterDeclarationSymbol3
		assertTrue(parameterDeclarationOfMethodDecl3.getAstNode().isPresent());
		assertTrue(parameterDeclarationOfMethodDecl3.getAstNode().get() instanceof ASTOCLParameterDeclaration);
		assertSame(parameterDeclarationOfMethodDecl3,
				parameterDeclarationOfMethodDecl3.getAstNode().get().getSymbol().get());
		assertSame(parameterDeclarationOfMethodDecl3.getEnclosingScope(),
				parameterDeclarationOfMethodDecl3.getAstNode().get().getEnclosingScope().get());

		final OCLParameterDeclarationSymbol parameterDeclarationOfMethodDecl4 = methDeclSymbol2.getOCLParamDecl("b").orElse(null);
		assertNotNull(parameterDeclarationOfMethodDecl4);
		assertEquals("float", parameterDeclarationOfMethodDecl4.getType().toString());

		// AST assertions of parameterDeclarationSymbol4
		assertTrue(parameterDeclarationOfMethodDecl4.getAstNode().isPresent());
		assertTrue(parameterDeclarationOfMethodDecl4.getAstNode().get() instanceof ASTOCLParameterDeclaration);
		assertSame(parameterDeclarationOfMethodDecl4,
				parameterDeclarationOfMethodDecl4.getAstNode().get().getSymbol().get());
		assertSame(parameterDeclarationOfMethodDecl4.getEnclosingScope(),
				parameterDeclarationOfMethodDecl4.getAstNode().get().getEnclosingScope().get());

		final OCLFileSymbol OCLFileSymbol13 = globalScope.<OCLFileSymbol> resolve("example.symbolTableTestFiles.test13", OCLFileSymbol.KIND).orElse(null);
		assertNotNull(OCLFileSymbol13);

		// methSigSymbol7
		final OCLMethodSignatureSymbol methSigSymbol7 = OCLFileSymbol13.getOCLMethSig("testing").orElse(null);
		assertNotNull(methSigSymbol7);
		assertNotNull(methSigSymbol7.getSpannedScope());
		assertSame(methSigSymbol7, methSigSymbol7.getSpannedScope().getSpanningSymbol().get());
		assertEquals("testing", methSigSymbol7.getName());
		assertEquals(ASTPrimitiveType.class, methSigSymbol7.getReturnType().getClass());
		assertEquals("double", methSigSymbol7.getReturnType().toString());
		assertEquals("Arbeit", methSigSymbol7.getClassName());

		// AST assertions of methSigSymbol7
		assertTrue(methSigSymbol7.getAstNode().isPresent());
		assertTrue(methSigSymbol7.getAstNode().get() instanceof ASTOCLMethodSignature);
		assertSame(methSigSymbol7, methSigSymbol7.getAstNode().get().getSymbol().get());
		assertSame(methSigSymbol7.getEnclosingScope(), methSigSymbol7.getAstNode().get().getEnclosingScope().get());

		final OCLMethodDeclarationSymbol methDeclSymbol3 = methSigSymbol7.getOCLMethodDecl("f1").orElse(null);
		assertNotNull(methDeclSymbol3);

		assertEquals("Help1", methDeclSymbol3.getReturnType());

		// AST assertions of methDeclSymbol3
		assertTrue(methDeclSymbol3.getAstNode().isPresent());
		assertTrue(methDeclSymbol3.getAstNode().get() instanceof ASTOCLMethodDeclaration);
		assertSame(methDeclSymbol3, methDeclSymbol3.getAstNode().get().getSymbol().get());
		assertSame(methDeclSymbol3.getEnclosingScope(), methDeclSymbol3.getAstNode().get().getEnclosingScope().get());

		// parameterDeclarationOfMethodDecl5
		final OCLParameterDeclarationSymbol parameterDeclarationOfMethodDecl5 = methDeclSymbol3.getOCLParamDecl("e").orElse(null);
		assertNotNull(parameterDeclarationOfMethodDecl5);
		assertEquals("int", parameterDeclarationOfMethodDecl5.getType().toString());

		// AST assertions of parameterDeclarationOfMethodDecl5
		assertTrue(parameterDeclarationOfMethodDecl5.getAstNode().isPresent());
		assertTrue(parameterDeclarationOfMethodDecl5.getAstNode().get() instanceof ASTOCLParameterDeclaration);
		assertSame(parameterDeclarationOfMethodDecl5,
				parameterDeclarationOfMethodDecl5.getAstNode().get().getSymbol().get());
		assertSame(parameterDeclarationOfMethodDecl5.getEnclosingScope(),
				parameterDeclarationOfMethodDecl5.getAstNode().get().getEnclosingScope().get());

		final OCLParameterDeclarationSymbol parameterDeclarationOfMethodDecl6 = methDeclSymbol3.getOCLParamDecl("f").orElse(null);
		assertNotNull(parameterDeclarationOfMethodDecl6);
		assertEquals("float", parameterDeclarationOfMethodDecl6.getType().toString());

		// AST assertions of parameterDeclarationOfMethodDecl6
		assertTrue(parameterDeclarationOfMethodDecl6.getAstNode().isPresent());
		assertTrue(parameterDeclarationOfMethodDecl6.getAstNode().get() instanceof ASTOCLParameterDeclaration);
		assertSame(parameterDeclarationOfMethodDecl6,
				parameterDeclarationOfMethodDecl6.getAstNode().get().getSymbol().get());
		assertSame(parameterDeclarationOfMethodDecl6.getEnclosingScope(),
				parameterDeclarationOfMethodDecl6.getAstNode().get().getEnclosingScope().get());

		// parameterDeclarationOfMethodDecl7
		final OCLParameterDeclarationSymbol parameterDeclarationOfMethodDecl7 = methDeclSymbol3.getOCLParamDecl("g").orElse(null);
		assertNotNull(parameterDeclarationOfMethodDecl7);
		assertEquals("double", parameterDeclarationOfMethodDecl7.getType().toString());

		// AST assertions of parameterDeclarationOfMethodDecl7
		assertTrue(parameterDeclarationOfMethodDecl7.getAstNode().isPresent());
		assertTrue(parameterDeclarationOfMethodDecl7.getAstNode().get() instanceof ASTOCLParameterDeclaration);
		assertSame(parameterDeclarationOfMethodDecl7,
				parameterDeclarationOfMethodDecl7.getAstNode().get().getSymbol().get());
		assertSame(parameterDeclarationOfMethodDecl7.getEnclosingScope(),
				parameterDeclarationOfMethodDecl7.getAstNode().get().getEnclosingScope().get());

		// parameterDeclarationOfMethodDecl7
		final OCLParameterDeclarationSymbol parameterDeclarationOfMethodDecl8 = methSigSymbol7.getOCLParamDecl("b").orElse(null);
		assertNotNull(parameterDeclarationOfMethodDecl8);
		assertEquals("boolean", parameterDeclarationOfMethodDecl8.getType().toString());

		// AST assertions of parameterDeclarationOfMethodDecl8
		assertTrue(parameterDeclarationOfMethodDecl8.getAstNode().isPresent());
		assertTrue(parameterDeclarationOfMethodDecl8.getAstNode().get() instanceof ASTOCLParameterDeclaration);
		assertSame(parameterDeclarationOfMethodDecl8,
				parameterDeclarationOfMethodDecl8.getAstNode().get().getSymbol().get());
		assertSame(parameterDeclarationOfMethodDecl8.getEnclosingScope(),
				parameterDeclarationOfMethodDecl8.getAstNode().get().getEnclosingScope().get());

		// methSigSymbol7 -> variableDeclarationSymbol6
		final OCLVariableDeclarationSymbol variableDeclarationSymbol6 = methSigSymbol7.getOCLVariableDecl("helpVariable").orElse(null);
		assertNotNull(variableDeclarationSymbol6);
		assertEquals("VariableClass", variableDeclarationSymbol6.getVarTypeName());

		// AST assertions of variableDeclarationSymbol6
		assertTrue(variableDeclarationSymbol6.getAstNode().isPresent());
		assertTrue(variableDeclarationSymbol6.getAstNode().get() instanceof ASTOCLVariableDeclaration);
		assertSame(variableDeclarationSymbol6, variableDeclarationSymbol6.getAstNode().get().getSymbol().get());
		assertSame(variableDeclarationSymbol6.getEnclosingScope(),
				variableDeclarationSymbol6.getAstNode().get().getEnclosingScope().get());

		// OCLFileSymbol7
		final OCLFileSymbol OCLFileSymbol7 = globalScope.<OCLFileSymbol> resolve("example.symbolTableTestFiles.test7", OCLFileSymbol.KIND).orElse(null);
		assertNotNull(OCLFileSymbol7);

		// OCLFileSymbol7 -> constructorSigSymbol2
		final OCLConstructorSignatureSymbol constructorSigSymbol2 = OCLFileSymbol7.getOCLConstructorSig("Constructor").orElse(null);
		assertNotNull(constructorSigSymbol2);
		assertNotNull(constructorSigSymbol2.getSpannedScope());
		assertSame(constructorSigSymbol2, constructorSigSymbol2.getSpannedScope().getSpanningSymbol().get());
		assertEquals("Constructor", constructorSigSymbol2.getName());

		// AST assertions of constructorSigSymbol2
		assertTrue(constructorSigSymbol2.getAstNode().isPresent());
		assertTrue(constructorSigSymbol2.getAstNode().get() instanceof ASTOCLConstructorSignature);
		assertSame(constructorSigSymbol2, constructorSigSymbol2.getAstNode().get().getSymbol().get());
		assertSame(constructorSigSymbol2.getEnclosingScope(),
				constructorSigSymbol2.getAstNode().get().getEnclosingScope().get());

		// constructorSigSymbol2 -> parameterDeclarationSymbol6
		final OCLParameterDeclarationSymbol parameterDeclarationSymbol6 = constructorSigSymbol2.getOCLParamDecl("Param1").orElse(null);
		assertNotNull(parameterDeclarationSymbol6);
		assertEquals(ASTPrimitiveType.class, parameterDeclarationSymbol6.getType().getClass());
		assertEquals("int", parameterDeclarationSymbol6.getType().toString());

		// AST assertions of parameterDeclarationSymbol6
		assertTrue(parameterDeclarationSymbol6.getAstNode().isPresent());
		assertTrue(parameterDeclarationSymbol6.getAstNode().get() instanceof ASTOCLParameterDeclaration);
		assertSame(parameterDeclarationSymbol6, parameterDeclarationSymbol6.getAstNode().get().getSymbol().get());
		assertSame(parameterDeclarationSymbol6.getEnclosingScope(),
				parameterDeclarationSymbol6.getAstNode().get().getEnclosingScope().get());

		// constructorSigSymbol2 -> parameterDeclarationSymbol7
		final OCLParameterDeclarationSymbol parameterDeclarationSymbol7 = constructorSigSymbol2.getOCLParamDecl("Param2").orElse(null);
		assertNotNull(parameterDeclarationSymbol7);
		assertEquals(ASTPrimitiveType.class, parameterDeclarationSymbol7.getType().getClass());
		assertEquals("int", parameterDeclarationSymbol7.getType().toString());

		// AST assertions of parameterDeclarationSymbol7
		assertTrue(parameterDeclarationSymbol7.getAstNode().isPresent());
		assertTrue(parameterDeclarationSymbol7.getAstNode().get() instanceof ASTOCLParameterDeclaration);
		assertSame(parameterDeclarationSymbol7, parameterDeclarationSymbol7.getAstNode().get().getSymbol().get());
		assertSame(parameterDeclarationSymbol7.getEnclosingScope(),
				parameterDeclarationSymbol7.getAstNode().get().getEnclosingScope().get());

		// OCLFileSymbol12
		final OCLFileSymbol OCLFileSymbol12 = globalScope.<OCLFileSymbol> resolve("example.symbolTableTestFiles.test12", OCLFileSymbol.KIND).orElse(null);
		assertNotNull(OCLFileSymbol12);

		// OCLFileSymbol12 -> constructorSigSymbol3
		final OCLConstructorSignatureSymbol constructorSigSymbol3 = OCLFileSymbol12.getOCLConstructorSig("Test").orElse(null);
		assertNotNull(constructorSigSymbol3);
		assertNotNull(constructorSigSymbol3.getSpannedScope());
		assertSame(constructorSigSymbol3, constructorSigSymbol3.getSpannedScope().getSpanningSymbol().get());
		assertEquals("Test", constructorSigSymbol3.getName());

		// AST assertions of constructorSigSymbol3
		assertTrue(constructorSigSymbol3.getAstNode().isPresent());
		assertTrue(constructorSigSymbol3.getAstNode().get() instanceof ASTOCLConstructorSignature);
		assertSame(constructorSigSymbol3, constructorSigSymbol3.getAstNode().get().getSymbol().get());
		assertSame(constructorSigSymbol3.getEnclosingScope(),
				constructorSigSymbol3.getAstNode().get().getEnclosingScope().get());

		// constructorSigSymbol3 -> parameterDeclarationSymbol8
		final OCLParameterDeclarationSymbol parameterDeclarationSymbol8 = constructorSigSymbol3.getOCLParamDecl("Param1").orElse(null);
		assertNotNull(parameterDeclarationSymbol8);
		assertEquals(ASTPrimitiveType.class, parameterDeclarationSymbol8.getType().getClass());
		assertEquals("double", parameterDeclarationSymbol8.getType().toString());

		// AST assertions of parameterDeclarationSymbol8
		assertTrue(parameterDeclarationSymbol8.getAstNode().isPresent());
		assertTrue(parameterDeclarationSymbol8.getAstNode().get() instanceof ASTOCLParameterDeclaration);
		assertSame(parameterDeclarationSymbol8, parameterDeclarationSymbol8.getAstNode().get().getSymbol().get());
		assertSame(parameterDeclarationSymbol8.getEnclosingScope(),
				parameterDeclarationSymbol8.getAstNode().get().getEnclosingScope().get());

		// constructorSigSymbol3 -> parameterDeclarationSymbol9
		final OCLParameterDeclarationSymbol parameterDeclarationSymbol9 = constructorSigSymbol3.getOCLParamDecl("Param2").orElse(null);
		assertNotNull(parameterDeclarationSymbol9);
		assertEquals(ASTPrimitiveType.class, parameterDeclarationSymbol9.getType().getClass());
		assertEquals("float", parameterDeclarationSymbol9.getType().toString());

		// AST assertions of parameterDeclarationSymbol9
		assertTrue(parameterDeclarationSymbol9.getAstNode().isPresent());
		assertTrue(parameterDeclarationSymbol9.getAstNode().get() instanceof ASTOCLParameterDeclaration);
		assertSame(parameterDeclarationSymbol9, parameterDeclarationSymbol9.getAstNode().get().getSymbol().get());
		assertSame(parameterDeclarationSymbol9.getEnclosingScope(),
				parameterDeclarationSymbol9.getAstNode().get().getEnclosingScope().get());

		// constructorSigSymbol3 -> parameterDeclarationSymbol10
		final OCLParameterDeclarationSymbol parameterDeclarationSymbol10 = constructorSigSymbol3.getOCLParamDecl("Param3").orElse(null);
		assertNotNull(parameterDeclarationSymbol10);
		assertEquals(ASTPrimitiveType.class, parameterDeclarationSymbol10.getType().getClass());
		assertEquals("double", parameterDeclarationSymbol10.getType().toString());

		// AST assertions of parameterDeclarationSymbol10
		assertTrue(parameterDeclarationSymbol10.getAstNode().isPresent());
		assertTrue(parameterDeclarationSymbol10.getAstNode().get() instanceof ASTOCLParameterDeclaration);
		assertSame(parameterDeclarationSymbol10, parameterDeclarationSymbol10.getAstNode().get().getSymbol().get());
		assertSame(parameterDeclarationSymbol10.getEnclosingScope(),
				parameterDeclarationSymbol10.getAstNode().get().getEnclosingScope().get());

		// OCLFileSymbol9
		final OCLFileSymbol OCLFileSymbol9 = globalScope.<OCLFileSymbol> resolve("example.symbolTableTestFiles.test9", OCLFileSymbol.KIND).orElse(null);
		assertNotNull(OCLFileSymbol9);

		// invSymbol5
		final OCLInvariantSymbol invSymbol5 = OCLFileSymbol9.getOCLInvariant("Voraussetzung").orElse(null);
		assertNotNull(invSymbol5);
		assertNotNull(invSymbol5.getSpannedScope());
		assertSame(invSymbol5, invSymbol5.getSpannedScope().getSpanningSymbol().get());
		assertEquals("Student", invSymbol5.getClassN());
		assertEquals("st", invSymbol5.getClassO());
		assertTrue(invSymbol5.getContext());
		assertFalse(invSymbol5.getImport());

		// AST assertions of invSymbol5
		assertTrue(invSymbol5.getAstNode().isPresent());
		assertTrue(invSymbol5.getAstNode().get() instanceof ASTOCLInvariant);
		assertSame(invSymbol5, invSymbol5.getAstNode().get().getSymbol().get());
		assertSame(invSymbol5.getEnclosingScope(), invSymbol5.getAstNode().get().getEnclosingScope().get());

		// invSymbol5 -> varDeclSymbol3
		final OCLVariableDeclarationSymbol varDeclSymbol3 = invSymbol5.getOCLVariableDecl("p").orElse(null);
		assertNotNull(varDeclSymbol3);

		// AST assertions of varDeclSymbol3
		assertTrue(varDeclSymbol3.getAstNode().isPresent());
		assertTrue(varDeclSymbol3.getAstNode().get() instanceof ASTOCLVariableDeclaration);
		assertSame(varDeclSymbol3, varDeclSymbol3.getAstNode().get().getSymbol().get());
		assertSame(varDeclSymbol3.getEnclosingScope(), varDeclSymbol3.getAstNode().get().getEnclosingScope().get());

		// OCLFileSymbol10
		final OCLFileSymbol OCLFileSymbol10 = globalScope.<OCLFileSymbol> resolve("example.symbolTableTestFiles.test10", OCLFileSymbol.KIND).orElse(null);
		assertNotNull(OCLFileSymbol10);

		// invSymbol7
		final OCLInvariantSymbol invSymbol7 = OCLFileSymbol10.getOCLInvariant("Betreuung").orElse(null);
		assertNotNull(invSymbol7);
		assertNotNull(invSymbol7.getSpannedScope());
		assertSame(invSymbol7, invSymbol7.getSpannedScope().getSpanningSymbol().get());
		assertEquals("Betreuer", invSymbol7.getClassN());
		assertEquals("b", invSymbol7.getClassO());
		assertFalse(invSymbol7.getContext());
		assertTrue(invSymbol7.getImport());

		// invSymbol7 -> methDeclSymbol4
		final OCLMethodDeclarationSymbol methDeclSymbol4 = invSymbol7.getOCLMethodDecl("bewertung").orElse(null);
		assertNotNull(methDeclSymbol4);

		// AST assertions of methDeclSymbol4
		assertTrue(methDeclSymbol4.getAstNode().isPresent());
		assertTrue(methDeclSymbol4.getAstNode().get() instanceof ASTOCLMethodDeclaration);
		assertSame(methDeclSymbol4, methDeclSymbol4.getAstNode().get().getSymbol().get());
		assertSame(methDeclSymbol4.getEnclosingScope(), methDeclSymbol4.getAstNode().get().getEnclosingScope().get());

		// invSymbol7 -> parameterDeclarationSymbol11
		final OCLParameterDeclarationSymbol parameterDeclarationSymbol11 = methDeclSymbol4.getOCLParamDecl("k").orElse(null);
		assertNotNull(parameterDeclarationSymbol11);
		assertEquals(ASTPrimitiveType.class, parameterDeclarationSymbol11.getType().getClass());
		assertEquals("int", parameterDeclarationSymbol11.getType().toString());

		// AST assertions of parameterDeclarationSymbol11
		assertTrue(parameterDeclarationSymbol11.getAstNode().isPresent());
		assertTrue(parameterDeclarationSymbol11.getAstNode().get() instanceof ASTOCLParameterDeclaration);
		assertSame(parameterDeclarationSymbol11, parameterDeclarationSymbol11.getAstNode().get().getSymbol().get());
		assertSame(parameterDeclarationSymbol11.getEnclosingScope(),
				parameterDeclarationSymbol11.getAstNode().get().getEnclosingScope().get());

		// invSymbol7 -> parameterDeclarationSymbol12
		final OCLParameterDeclarationSymbol parameterDeclarationSymbol12 = methDeclSymbol4.getOCLParamDecl("l").orElse(null);
		assertNotNull(parameterDeclarationSymbol12);
		assertEquals(ASTPrimitiveType.class, parameterDeclarationSymbol12.getType().getClass());
		assertEquals("int", parameterDeclarationSymbol12.getType().toString());

		// AST assertions of parameterDeclarationSymbol12
		assertTrue(parameterDeclarationSymbol12.getAstNode().isPresent());
		assertTrue(parameterDeclarationSymbol12.getAstNode().get() instanceof ASTOCLParameterDeclaration);
		assertSame(parameterDeclarationSymbol12, parameterDeclarationSymbol12.getAstNode().get().getSymbol().get());
		assertSame(parameterDeclarationSymbol12.getEnclosingScope(),
				parameterDeclarationSymbol12.getAstNode().get().getEnclosingScope().get());

	}

}
