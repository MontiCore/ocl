/* (c) https://github.com/MontiCore/monticore */
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
