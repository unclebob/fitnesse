package fitnesse.junit;

import fitnesse.junit.JavaFormatter.FolderResultsRepository;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.xml.sax.InputSource;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;
import java.io.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JavaFormatterFolderResultsRepositoryTest {
 
  private static final String TEST_NAME = "testName";

  @Rule
  public TemporaryFolder temporaryFolder = new TemporaryFolder();
  
  private FolderResultsRepository repository;
  
  @Before
  public void prepareFolder() throws Exception {
    String tempDir = temporaryFolder.getRoot().getAbsolutePath();
    repository = new JavaFormatter.FolderResultsRepository(tempDir);
  }

  @Test
  public void usesTestNameAsHeadingWithinValidHtmlStructure() throws Exception {
    repository.open(TEST_NAME);
    repository.close();
    
    String heading = evaluateXPathAgainstOutputHtml("/html/body/header/h2/text()");
    assertEquals(TEST_NAME, heading);
  }

  @Test
  public void usesUtf8Encoding() throws Exception {
    repository.open(TEST_NAME);
    repository.write("someContent\u263a");
    repository.close();
    
    String outputHtml = readOutputHtml("utf8");
    assertTrue(outputHtml.contains("someContent\u263a"));
    assertTrue(outputHtml.contains("<meta http-equiv='Content-Type' content='text/html;charset=UTF-8'/>"));
  }

  private String evaluateXPathAgainstOutputHtml(String expression) throws Exception {
    XPath xpath = XPathFactory.newInstance().newXPath();
    InputSource inputSource = new InputSource(getHtmlOutputStream());
    return xpath.evaluate(expression, inputSource);
  }

  private String readOutputHtml(String encoding) throws Exception {
    Reader reader = new InputStreamReader(getHtmlOutputStream(), encoding);
    StringBuilder result = new StringBuilder();
    char[] buffer = new char[1000];
    while (reader.read(buffer) > 0) {
      result.append(buffer);
    }
    return result.toString();
  }

  private InputStream getHtmlOutputStream() throws FileNotFoundException {
    File outputHtml = new File(temporaryFolder.getRoot(), TEST_NAME + ".html");
    return new FileInputStream(outputHtml);
  }

}
