/**
 *
 * (c) https://github.com/MontiCore/monticore
 *
 * The license generally applicable for this project
 * can be found under https://github.com/MontiCore/monticore.
 */
/**
 *
 * /* (c) https://github.com/MontiCore/monticore */
 *
 * The license generally applicable for this project
 * can be found under https://github.com/MontiCore/monticore.
 */
/* (c) https://github.com/MontiCore/monticore */
package ocl.monticoreocl.ocl._cocos;

;

public class OCLCoCos {

	public static OCLCoCoChecker createChecker() {
		return new OCLCoCoChecker()
		.addCoCo(new FileNameStartsWithLowerCaseLetter())
		.addCoCo(new MethSignatureStartsWithLowerCaseLetter())
		.addCoCo(new ConstructorNameStartsWithCapitalLetter())
		.addCoCo(new InvariantNameStartsWithCapitalLetter())
		.addCoCo(new MethodDeclarationStartsWithLowerCaseLetter())
		.addCoCo(new PreStatementNameStartsWithCapitalLetter())
		.addCoCo(new PostStatementNameStartsWithCapitalLetter())
		.addCoCo(new ParameterDeclarationNameStartsWithLowerCaseLetter())
		.addCoCo(new TypesCorrectInExpressions())
		.addCoCo(new VariableDeclarationStartsWithLowerCaseLetter())
		;
	}
}
