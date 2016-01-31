package fitnesse.testsystems;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
    this(Collections.singletonList(defaultPath), pathSeparator);
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

  public ClassPath withLocationForClass(String testRunner) {
    String location = findLocationForClass(testRunner);
    if (location != null) {
      List<String> newElements = new ArrayList<String>();
      newElements.add(location);
      newElements.addAll(elements);
      return new ClassPath(newElements, separator);
    }
    return this;
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
      return result;
    }
  }

  private String findLocationForClass(String mainClass) {
    String mainClassFile = mainClass.replaceAll("\\.", "/") + ".class";
    URL url = getClass().getClassLoader().getResource(mainClassFile);
    if (url == null) return null;
    String path = url.getPath();
    if ("file".equals(url.getProtocol())) {
      return new File(path.substring(0, path.length() - mainClassFile.length())).getAbsolutePath();
    } else if ("jar".equals(url.getProtocol())) {
      return new File(URI.create(path.substring(0, path.indexOf("!/")))).getAbsolutePath();
    }
    return null;
  }
}
