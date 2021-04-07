package fitnesse.testsystems;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClassPath {

  private static final Logger LOG = Logger.getLogger(ClassPath.class.getName());

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
    this.elements = new ArrayList<>();
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
      List<String> newElements = new ArrayList<>();
      newElements.addAll(elements);
      newElements.add(location);
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
      return StringUtils.join(elements, separator);
    }
  }

  private String findLocationForClass(String mainClass) {
    String mainClassFile = mainClass.replaceAll("\\.", "/") + ".class";
    URL url = getClass().getClassLoader().getResource(mainClassFile);
    if (url == null) return null;

    if ("file".equals(url.getProtocol())) {
      URI uri = toUri(url);
      if (uri != null) {
        String path = uri.getPath();
        return new File(path.substring(0, path.length() - mainClassFile.length())).getAbsolutePath();
      }
    } else if ("jar".equals(url.getProtocol())) {
      String path = url.getPath();
      return new File(URI.create(path.substring(0, path.indexOf("!/")))).getAbsolutePath();
    }
    return null;
  }

  private URI toUri(URL url) {
    try {
      return url.toURI();
    } catch (URISyntaxException e) {
      LOG.log(Level.SEVERE, "Could not convert URL '" + url + "' to URI. Ignoring it for now.", e);
      return null;
    }
  }
}
