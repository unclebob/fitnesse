package fitnesse.slim;

import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class StackTraceEnricherTest {
  private static final String JUNIT_JAR_PATTERN = "[junit";
  private static final String RT_JAR = "rt.jar";
  private static final String COMMONS_LANG_VERSION = "3.17.0";
  private static final Pattern COMMONS_LANG_JAR = Pattern.compile("commons-lang3(-3.17.0)?.jar");

  private Throwable exception;
  private Throwable exceptionWithCause;
  private StackTraceEnricher enricher;

  @Before
  public void setUp() {
    exception = createException();
    exceptionWithCause = createIllegalArgumentExceptionWithCause();
    enricher = new StackTraceEnricher();
  }

  private Exception createException() {
    Exception exception = null;
    try {
      org.apache.commons.lang3.StringUtils.getLevenshteinDistance(null, null);
      fail("StringUtils.getLevenshteinDistance() changed its contract, expected IllegalArgumentException");
    } catch (IllegalArgumentException iae) {
      if (iae.getStackTrace() == null || iae.getStackTrace().length == 0) {
        iae.fillInStackTrace();
      }
      exception = iae;
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

  private void throwException() throws Exception {
    throw createException();
  }

  private void throwIllegalArgumentExceptionWithIOExceptionCause() {
    try {
      throwException();
      fail("No Exception was thrown.");
    } catch (Exception e) {
      throw new IllegalArgumentException("Custom IllegalArgumentException message", e);
    }
  }

  private StackTraceElement getCommonsLangStackTraceElement(Throwable exception) {
    StackTraceElement javaLangElement = null;
    for (StackTraceElement element : exception.getStackTrace()) {
      if (element.getClassName().startsWith("org.apache.commons.lang")) {
        javaLangElement = element;
        break;
      }
    }
    if (javaLangElement == null) {
      fail("Unable to find a org.apache.commons.lang class in the stack trace.");
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
    String stackTraceAsString = enricher.getStackTraceAsString(exception);
    String fragment = ".jar:" + COMMONS_LANG_VERSION;

    assertTrue(String.format("Version not added for commons-lang.jar. Did not find '%s' in \n%s", fragment, stackTraceAsString),
            stackTraceAsString.contains(fragment));
  }

  @Test
  public void shouldGetVersionForClassInJarWithVersion() {
    assertTrue("Version not retrieved for org.apache.commons.lang.ArrayUtils",
        enricher.getVersion(org.apache.commons.lang3.ArrayUtils.class).contains(COMMONS_LANG_VERSION));
  }

  @Test
  public void shouldGetVersionForStackTraceElementInJarWithVersion() {
    StackTraceElement commonsLangElement = getCommonsLangStackTraceElement(exception);
    assertTrue("Version not retrieved for " + commonsLangElement.getClassName(), enricher.getVersion(commonsLangElement).contains(COMMONS_LANG_VERSION));
  }

  @Test
  public void shouldGetLocationForClassInJarWithVersion() {
    assertEquals("Location not retrieved for java.lang.reflect.Method",
        enricher.getLocation(java.lang.reflect.Method.class), RT_JAR);
  }

  @Test
  public void shouldGetLocationForStackTraceElementInJarWithVersion() {
    StackTraceElement commonsLangElement = getCommonsLangStackTraceElement(exception);
    assertTrue("Version not retrieved for " + commonsLangElement.getClassName(),
            COMMONS_LANG_JAR.matcher(enricher.getLocation(commonsLangElement)).matches());
  }

  @Test
  public void shouldParseDirectories() throws Exception {
    String classPath = System.getProperty("java.class.path");
    String testLocation = enricher.getLocation(this.getClass());
    if (!testLocation.contains(".jar")) {
      assertContainsPath("Location of unit test (" + testLocation + ") not found on the classpath (" + classPath + ").",
              testLocation, classPath);
    } else {
      // TODO Find a way to test this if the unit test is executed from a jar.
      System.err.println("Unit test executed from jar file, no stable method available for testing the parsing of " +
          "directory locations.");
    }
  }

  public void assertContainsPath(String message, String path, String paths) throws URISyntaxException {
    File testFile;
    if (path.startsWith("file:/")) {
      testFile = new File(new URI(path));
    } else {
      testFile = new File(path);
    }
    String[] parts = paths.split(System.getProperty("path.separator"));
    for (String part : parts) {
      if (testFile.equals(new File(part))) {
        return;
      }
    }
    fail(message);
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
    assertTrue("No IllegalArgumentException found as cause in stacktrace.",
            parsedString.contains("\nCaused by: java.lang.IllegalArgumentException:"));
  }
}
