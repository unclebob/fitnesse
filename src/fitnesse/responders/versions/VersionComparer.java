package fitnesse.responders.versions;

import java.util.LinkedList;
import java.util.List;

import difflib.DiffUtils;
import difflib.Patch;

public class VersionComparer {

  private List<String> differences;

  public boolean compare(String originalVersion, String originalContent, String revisedVersion, String revisedContent) {
    Patch<String> patch = DiffUtils.diff(contentToLines(originalContent), contentToLines(revisedContent));
    differences = DiffUtils.generateUnifiedDiff(originalVersion, revisedVersion,
        contentToLines(originalContent), patch, 5);
    return true;
  }

  public List<String> getDifferences() {
    return differences;
  }

  private List<String> contentToLines(String content) {
    List<String> lines = new LinkedList<>();
    for(String line : content.split("\n"))
      lines.add(line);
    return lines;
  }

}
