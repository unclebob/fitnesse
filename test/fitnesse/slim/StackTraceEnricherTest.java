package fitnesse.slim;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringWriter;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class StackTraceEnricherTest {
  private static final String JUNIT_JAR_PATTERN = "[junit";
  private static final String RT_JAR = "rt.jar";
  private static final String NON_EXISTING_FILE = "/this/should/not/exist/at/all/sadfbas";

  private Throwable exception;
  private Throwable exceptionWithCause;
  private String javaVersion;
  private StackTraceEnricher enricher;

  @Before
  public void setUp() {
    exception = createIOException();
    exceptionWithCause = createIllegalArgumentExceptionWithCause();
    javaVersion = getJavaVersion();
    enricher = new StackTraceEnricher();
  }

  private IOException createIOException() {
    IOException exception = null;
    try {
      new FileInputStream(new File(NON_EXISTING_FILE));
      fail("Managed to find a file that shouldn't exist " + NON_EXISTING_FILE);
    } catch (IOException ioe) {
      if (ioe.getStackTrace() == null || ioe.getStackTrace().length == 0) {
        ioe.fillInStackTrace();
      }
      exception = ioe;
    }
    return exception;
  }

  private IllegalArgumentException createIllegalArgumentExceptionWithCause() {
    IllegalArgumentException exception = null;
    try {
      throwIllegalArgumentExceptionWithIOExceptionCause();
    } catch (IllegalArgumentException iae) {
      exception = iae;
    }
    return exception;
  }

  private void throwIOException() throws IOException {
    throw createIOException();
  }

  private void throwIllegalArgumentExceptionWithIOExceptionCause() {
    try {
      throwIOException();
      fail("No IOException was thrown.");
    } catch (Exception e) {
      throw new IllegalArgumentException("Custom IllegalArgumentException message", e);
    }
  }

  private String getJavaVersion() {
    String version;
    String fullVersion = System.getProperty("java.runtime.version", "Failed to read Java RT version");
    if (fullVersion.contains("-")) {
      version = fullVersion.substring(0, fullVersion.indexOf('-'));
    } else {
      version = fullVersion;
    }
    return version;
  }

  private StackTraceElement getJavaLangStackTraceElement(Throwable exception) {
    StackTraceElement javaLangElement = null;
    for (StackTraceElement element : exception.getStackTrace()) {
      if (element.getClassName().startsWith("java.lang")) {
        javaLangElement = element;
        break;
      }
    }
    if (javaLangElement == null) {
      fail("Unable to find a java.lang class in the stack trace.");
    }
    return javaLangElement;
  }

  @Test
  public void shouldWriteEnrichedStackTraceToWriter() throws Exception {
    StringWriter writer = new StringWriter();
    enricher.printStackTrace(exception, writer);
    assertTrue("JUnit jar " + JUNIT_JAR_PATTERN + " not found in stack trace written to writer",
        writer.toString().contains(JUNIT_JAR_PATTERN));
  }

  @Test
  public void shouldWriteEnrichedStackTraceToOutputStream() throws Exception {
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    enricher.printStackTrace(exception, outputStream);
    String output = new String(outputStream.toByteArray());
    assertTrue("JUnit jar " + JUNIT_JAR_PATTERN + " not found in stack trace written to output stream",
        output.contains(JUNIT_JAR_PATTERN));
  }

  @Test
  public void shouldReturnEnrichedStackTraceAsString() {
    assertTrue("JUnit jar " + JUNIT_JAR_PATTERN + " not found in stack trace as String",
        enricher.getStackTraceAsString(exception).contains(JUNIT_JAR_PATTERN));
  }

  @Test
  public void shouldParseRtJar() {
    assertTrue("Java RT jar not properly determined.", enricher.getStackTraceAsString(exception).contains(RT_JAR));
  }

  @Test
  public void shouldAddVersionWhenAvailable() {
    assertTrue("Version not added for rt.jar", enricher.getStackTraceAsString(exception).contains(RT_JAR + ":" +
        javaVersion));
  }

  @Test
  public void shouldGetVersionForClassInJarWithVersion() {
    assertTrue("Version not retrieved for java.lang.reflect.Method",
        enricher.getVersion(java.lang.reflect.Method.class).contains(javaVersion));
  }

  @Test
  public void shouldGetVersionForStackTraceElementInJarWithVersion() {
    StackTraceElement javaLangElement = getJavaLangStackTraceElement(exception);
    assertTrue("Version not retrieved for " + javaLangElement.getClassName(), enricher.getVersion(javaLangElement).contains(javaVersion));
  }

  @Test
  public void shouldGetLocationForClassInJarWithVersion() {
    assertEquals("Location not retrieved for java.lang.reflect.Method",
        enricher.getLocation(java.lang.reflect.Method.class), RT_JAR);
  }

  @Test
  public void shouldGetLocationForStackTraceElementInJarWithVersion() {
    StackTraceElement javaLangElement = getJavaLangStackTraceElement(exception);
    assertEquals("Version not retrieved for " + javaLangElement.getClassName(), enricher.getLocation(javaLangElement),
        RT_JAR);
  }

  @Test
  public void shouldParseDirectories() {
    String classPath = System.getProperty("java.class.path");
    classPath = classPath.replace('\\', '/');
    String testLocation = enricher.getLocation(this.getClass());
    if (!testLocation.contains(".jar")) {
      testLocation = testLocation.replace('\\', '/');
      if (testLocation.startsWith("file:/")) {
        testLocation = testLocation.substring("file:/".length());
      }
      if (testLocation.endsWith("/")) {
        testLocation = testLocation.substring(0, testLocation.length() - 1);
      }
      // Under Windows a path with a SPACE has "%20" in the testLocation and " " in the classpath
      // Fix this before the test
      testLocation = testLocation.replace("%20", " ");
      
      assertTrue("Location of unit test (" + testLocation + ") not found on the classpath (" + classPath + ").",
          classPath.contains(testLocation));
    } else {
      // TODO Find a way to test this if the unit test is executed from a jar.
      System.err.println("Unit test executed from jar file, no stable method available for testing the parsing of " +
          "directory locations.");
    }
  }

  @Test
  public void shouldWriteToSystemErrByDefault() {
    PrintStream originalErrStream = System.err;
    try {
      ByteArrayOutputStream out = new ByteArrayOutputStream();
      System.setErr(new PrintStream(out));
      enricher.printStackTrace(exception);
      String output = new String(out.toByteArray());
      assertTrue("JUnit jar " + JUNIT_JAR_PATTERN + " not found in stack trace written to default System.err stream",
          output.contains(JUNIT_JAR_PATTERN));
    } finally {
      System.setErr(originalErrStream);
    }
  }

  @Test
  public void shouldParseCauseExceptions() {
    String parsedString = enricher.getStackTraceAsString(exceptionWithCause);
    assertTrue("No FileNotFoundException found as cause in stacktrace.",
            parsedString.contains("\nCaused by: java.io.FileNotFoundException:"));
  }
}
