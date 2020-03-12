@rem (c) https://github.com/MontiCore/monticore

@echo off




set jar= %~dp0%ocl-1.2.2-cli.jar
set parent_dir= "%~dp0\"
set ocl= "example.typeInferringModels.declaredTypes"


cd %JDK_PATH%

java -jar %JAR% -path %parent_dir% -ocl %ocl%


pause

