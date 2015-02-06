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
    this.separator = paths.get(0).getSeparator();

    for (ClassPath path : paths) {
      for (String element : path.getElements()) {
        if (!elements.contains(element)) {
          elements.add(element);
        }
      }
    }
  }

  public List<String> getElements() {
    return elements;
  }

  public String getSeparator() {
    return separator;
  }

  @Override
  public String toString() {
    if (elements.isEmpty()) {
      return "defaultPath";
    } else {
      String result = StringUtils.join(elements, separator);
      if (result.contains(" ") && !(result.startsWith("\"") && result.endsWith("\""))) {
    	 result = "\""+result +"\"";
      }
      return result;
    }
  }
}
