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
package ocl.cli;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;

import ocl.monticoreocl.ocl._parser.OCLParser;

public class OCLCLI {

  private static String JAR_NAME = "ocl-<Version>-cli.jar";
  private static String PARSING_SUCCESSFUL = "Parsing Successful!";

  private String modelFile;

  private OCLCLI() {
  }

  public static void main(String[] args) throws IOException {

    OCLCLI cli = new OCLCLI();

    if (cli.handleArgs(args)) {
      cli.parse();
    }
  }

  protected void parse() throws IOException {
    OCLParser parser = new OCLParser();
    parser.parse(modelFile);
    System.out.println(PARSING_SUCCESSFUL);
  }

  protected boolean handleArgs(String[] args) throws NoSuchFileException {

    if (args.length != 1 || args.length == 1 && "-h".equals(args[0])) {
      printUsage();
      return false;
    }

    modelFile = args[0];
    if (!modelFileExists()) {
      throw new NoSuchFileException(modelFile);
    }
    return true;
  }

  private boolean modelFileExists() {
    Path filePath = Paths.get(modelFile);
    return Files.exists(filePath);
  }

  private void printUsage() {
    System.out.println("Usage: " + JAR_NAME + " OCLMODELFILE");
  }

}
