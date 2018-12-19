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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
// this file is needed for the JavaScript version of OCLFiddle
// see https://github.com/leaningtech/cheerpj-meta/issues/53#issuecomment-448426101
// and https://github.com/EmbeddedMontiArc/webspace/blob/297c0d533c637ad45887bda3960e17597f6a674a/OCL/resources/js/cheerpj.js#L26
public class WriteFileContent {

  /**
   * This class shows how to write file in java
   * @param args
   * @throws IOException
   */
  public static void main(String[] args) {
    String path = args[0];
    String[] paths = path.split("/");

    String s = "";
    for (int i = 0; i < paths.length - 1; i++) {
      String p = paths[i];
      s += p + "/";
      File f = new File(s.substring(0, s.length()-1));
      if (!f.exists()) {
        if(!f.mkdir()) {
          System.out.println("could not create directory " + p);
          break;
        }
      }
    }


    String data = args[1];
    writeUsingFileWriter(path, data);
  }

  /**
   * Use WriteFileContent when number of write operations are less
   * @param data
   */
  private static void writeUsingFileWriter(String path, String data) {
    File file = new File(path);
    java.io.OutputStreamWriter fr = null;
    try {
      fr  = new OutputStreamWriter(new FileOutputStream(path),"UTF-8");
      fr.write(data);
    } catch (IOException e) {
      e.printStackTrace();
    }finally{
      //close resources
      try {
        fr.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

}
