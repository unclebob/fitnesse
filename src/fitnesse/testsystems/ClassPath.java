package fitnesse.testsystems;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class ClassPath {

  private final List<String> elements;
  private final String separator;

  public ClassPath(List<String> elements, String separator) {
    this.elements = elements;
    this.separator = separator;
  }

  public ClassPath(String defaultPath, String pathSeparator) {
    this(Arrays.asList(defaultPath), pathSeparator);
  }

  public ClassPath(List<ClassPath> paths) {
    this.elements = new ArrayList<String>();
    this.separator = paths.get(0).separator;

    for (ClassPath path : paths) {
      for (String element : path.elements) {
        if (!elements.contains(element)) {
          elements.add(element);
        }
      }
    }
  }

  @Override
  public String toString() {
    if (elements.isEmpty()) {
      return "defaultPath";
    } else {
      List<String> newElements = new ArrayList<String>();
      for (String element : elements) {
        if (element.contains(" ") && !(element.startsWith("\"") && element.endsWith("\""))) {
          newElements.add("\""+element +"\"");
        } else {
          newElements.add(element);
        }
      }

      String result = StringUtils.join(newElements, separator);
      return result;
    }
  }
}
