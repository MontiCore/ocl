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
import java.io.*;
import java.util.ArrayList;
import java.util.stream.Collectors;

// this file is needed for the JavaScript version of OCLFiddle
// see https://github.com/leaningtech/cheerpj-meta/issues/53#issuecomment-448426101
// and https://github.com/EmbeddedMontiArc/webspace/blob/297c0d533c637ad45887bda3960e17597f6a674a/OCL/resources/js/cheerpj.js#L26
public class ReadFileContent {

  public static String read(String path) throws IOException {
    File file = new File(path);
    int len = (int)file.length();
    InputStreamReader fr  = new InputStreamReader(new FileInputStream(path),"UTF-8");
    char[] buffer = new char[len];
    fr.read(buffer);
    fr.close();
    return new String(buffer);
  }

  public static String getAllFiles(String path) throws IOException {
    ArrayList<String> files = new ArrayList<>();
    File file = new File(path);
    addFile(file, files);
    return files.stream().collect(Collectors.joining("\n"));
  }

  private static void addFile(File directory, ArrayList<String> files) {
    String[] elements = directory.list();
    for (String el : elements) {
      File f = new File(directory.getPath() + "/" + el);
      if (f.isFile())
        files.add(f.getPath());
      else if (f.isDirectory())
        addFile(f, files);
    }
  }

}
