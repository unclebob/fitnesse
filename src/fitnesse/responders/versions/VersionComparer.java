package fitnesse.responders.versions;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import difflib.Delta;
import difflib.DiffUtils;
import difflib.Patch;

public class VersionComparer {

  private List<String> differences;
  
  public VersionComparer() {
    differences = new ArrayList<String>();
  }
  
  public boolean compare(String firstFilePath, String secondFilePath) throws IOException {
    Patch patch = DiffUtils.diff(fileToLines(firstFilePath), fileToLines(secondFilePath));
    for (Delta delta: patch.getDeltas()) {
      differences.add(delta.toString());
    }
    return true;
  }

  public List<String> getDifferences() {
    return differences;
  }
  
  private static List<String> fileToLines(String filename) throws IOException {
    List<String> lines = new LinkedList<String>();
    String line = "";
    BufferedReader in = null;
    try {
      in = new BufferedReader(new FileReader(filename));
      while ((line = in.readLine()) != null) {
        lines.add(line);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (in!=null)
        in.close();
    }
    return lines;
  }

}
