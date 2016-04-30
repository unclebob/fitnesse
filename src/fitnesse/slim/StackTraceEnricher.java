package fitnesse.slim;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.security.CodeSource;
import java.util.HashMap;
import java.util.Map;

import static util.FileUtil.CHARENCODING;

public class StackTraceEnricher {
  private Map<String, ClassMetaInformation> elementInformation;

  public StackTraceEnricher() {
    this.elementInformation = new HashMap<>();
  }

  public void printStackTrace(Throwable throwable) {
    try {
      printStackTrace(throwable, System.err);
    } catch (IOException ioe) {
      // Ignore.
    }
  }

  public void printStackTrace(Throwable throwable, OutputStream stream) throws IOException {
    stream.write(getStackTraceAsString(throwable).getBytes(CHARENCODING));
    stream.flush();
  }

  public void printStackTrace(Throwable throwable, Writer writer) throws IOException {
    writer.write(getStackTraceAsString(throwable));
    writer.flush();
  }

  public String getStackTraceAsString(Throwable throwable) {
    StringBuilder sb = new StringBuilder();
    Throwable t = throwable;
    if (throwable.getStackTrace() == null || throwable.getStackTrace().length == 0) {
      t = throwable.fillInStackTrace();
    }
    for (StackTraceElement ste : t.getStackTrace()) {
      sb.append("\n\tat ").append(ste.toString());
      ClassMetaInformation cmi = getMetaInformation(ste);
      sb.append(" [");
      sb.append(cmi.getLocation());
      if (!cmi.getVersion().equals(ClassMetaInformation.UNKNOWN)) {
        sb.append(":").append(cmi.getVersion());
      }
      sb.append("]");
    }
    if (throwable.getCause() != null) {
      Throwable cause = throwable.getCause();
      sb.append("\nCaused by: ").append(cause.getClass().getName()).append(": ").append(cause.getMessage());
      sb.append(getStackTraceAsString(cause));
    }
    return sb.toString();
  }

  public String getVersion(Class<?> clazz) {
    return getMetaInformation(clazz).getVersion();
  }

  public String getVersion(StackTraceElement element) {
    return getMetaInformation(element).getVersion();
  }

  public String getLocation(Class<?> clazz) {
    return getMetaInformation(clazz).getLocation();
  }

  public String getLocation(StackTraceElement element) {
    return getMetaInformation(element).getLocation();
  }

  private ClassMetaInformation getMetaInformation(Class<?> clazz) {
    ClassMetaInformation information;
    if (elementInformation.containsKey(clazz.getName())) {
      information = elementInformation.get(clazz.getName());
    } else {
      information = new ClassMetaInformation(clazz);
      elementInformation.put(clazz.getName(), information);
    }
    return information;
  }

  private ClassMetaInformation getMetaInformation(StackTraceElement element) {
    ClassMetaInformation information;
    if (elementInformation.containsKey(element.getClassName())) {
      information = elementInformation.get(element.getClassName());
    } else {
      information = new ClassMetaInformation(element);
      elementInformation.put(element.getClassName(), information);
    }
    return information;
  }

  private static class ClassMetaInformation {
    private static final String UNKNOWN = "n/a";
    private static final String[] PACKAGE_PREFIXES_RTJAR = {"java.", "javax.", "sun.", "sunw.", "javafx.", "com.sun."};
    private String version = UNKNOWN;
    private String location = UNKNOWN;
    private String className = UNKNOWN;
    private String file = UNKNOWN;

    public ClassMetaInformation(Class<?> clazz) {
      analyse(clazz.getName());
    }

    public ClassMetaInformation(StackTraceElement ste) {
      file = ste.getFileName();
      analyse(ste.getClassName());
    }

    private void analyse(String className) {
      try {
        Class<?> elementClass = loadClass(className, ClassLoader.getSystemClassLoader());
        analyse(elementClass);
      } catch (ClassNotFoundException cnfe) {
        this.className = className;
      }
    }

    private void analyse(Class clazz) {
      className = clazz.getName();
      version = getVersion(clazz);
      location = getLocation(clazz);
    }

    public String getVersion() {
      return version;
    }

    public String getLocation() {
      return location;
    }

    private static Class<?> loadClass(String className, ClassLoader classLoader) throws ClassNotFoundException {
      if (className == null || className.isEmpty()) {
        throw new ClassNotFoundException("Unable to load a class with an empty or null name.");
      }
      Class<?> resolvedClass = null;
      if (classLoader != null) {
        try {
          resolvedClass = classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
          // Ignore, try to resolve the class via other class loaders.
        }
      }
      if (resolvedClass == null) {
        ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
        if (contextClassLoader != null) {
          resolvedClass = contextClassLoader.loadClass(className);
        } else {
          resolvedClass = StackTraceEnricher.class.getClassLoader().loadClass(className);
        }
      }
      return resolvedClass;
    }

    private static String getLocation(Class<?> clazz) {
      String location = UNKNOWN;
      if (clazz != null) {
        try {
          CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
          if (codeSource != null) {
            String fullLocation = codeSource.getLocation().toString();
            if (isDirectory(fullLocation)) {
              location = fullLocation;
            } else {
              location = removeParentDirectories(fullLocation);
            }
          } else {
            for (String rtPackage : PACKAGE_PREFIXES_RTJAR) {
              if (clazz.getName().startsWith(rtPackage)) {
                location = "rt.jar";
                break;
              }
            }
          }
        } catch (Exception e) {
          // Ignore.
        }
      }
      return location;
    }

    private static String getVersion(Class<?> clazz) {
      String version = UNKNOWN;
      if (clazz != null) {
        try {
          Package pack = clazz.getPackage();
          if (pack != null) {
            if (pack.getImplementationVersion() != null) {
              version = pack.getImplementationVersion();
            } else if (pack.getSpecificationVersion() != null) {
              version = pack.getSpecificationVersion();
            }
          }
        } catch (Exception e) {
          // Ignore.
        }
      }
      return version;
    }

    private static String removeParentDirectories(String path) {
      String parsedPath = path;
      if (path.contains("/")) {
        parsedPath = removeParentDirectories(path, "/");
      } else if (path.contains("\\")) {
        parsedPath = removeParentDirectories(path, "\\");
      }
      return parsedPath;
    }

    private static String removeParentDirectories(String path, String separator) {
      String parsedPath = path;
      if (path.contains(separator) && !path.endsWith(separator) || (path.indexOf(separator) < path.lastIndexOf
          (separator))) {
        parsedPath = parsedPath.substring(parsedPath.indexOf(separator) + 1);
        parsedPath = removeParentDirectories(parsedPath, separator);
      }
      return parsedPath;
    }

    private static boolean isDirectory(String path) {
      return path != null && (path.endsWith("/") || (path.endsWith("\\")));
    }
  }
}
