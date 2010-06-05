package fitnesse.junit;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Test;

import util.TimeMeasurement;

import fitnesse.responders.run.TestSummary;

public class JUnitXmlTestListenerIntegrationTest {
  String htmlOutputDir=new File(System.getProperty("java.io.tmpdir"), "fitnesse").getAbsolutePath();
  String xmlOutputDir=new File(System.getProperty("java.io.tmpdir"), "fitnesse-xml").getAbsolutePath();
  String fitNesseRootDir=".";
  JUnitXMLTestListener xmlTestListener=new JUnitXMLTestListener(xmlOutputDir);

  String expectedCorrectResultPattern=
        "<testsuite errors=\"0\" skipped=\"0\" tests=\"1\" time=\"[0-9\\.]*\" failures=\"0\""+
        " name=\"FitNesse.SuiteAcceptanceTests.SuiteSlimTests.MultiByteCharsInSlim\">"+
        "<properties></properties>"+
        "<testcase classname=\"FitNesse.SuiteAcceptanceTests.SuiteSlimTests.MultiByteCharsInSlim\""+ 
        " time=\"[0-9\\.]*\" name=\"FitNesse.SuiteAcceptanceTests.SuiteSlimTests.MultiByteCharsInSlim\">"+
        "</testcase></testsuite>";    
  @Test  
  public void checkJunitXmlTestListenerPrintsXmlFiles() throws Exception{
    JUnitHelper helper = new JUnitHelper(fitNesseRootDir,htmlOutputDir,xmlTestListener);
    helper.assertTestPasses("FitNesse.SuiteAcceptanceTests.SuiteSlimTests.MultiByteCharsInSlim");
    
    File expectedFile=new File(new File(xmlOutputDir),"TEST-FitNesse.SuiteAcceptanceTests.SuiteSlimTests.MultiByteCharsInSlim.xml");
    Assert.assertTrue("file exists", expectedFile.exists());
    String contents=readContents(expectedFile);
    Assert.assertTrue("file contents are "+contents, Pattern.matches(expectedCorrectResultPattern, contents));
  }

  @Test
  public void failuresAreRecordedCorrectly() throws Exception{
    xmlTestListener.recordTestResult("testName", new TestSummary(1,2,0,0), 100);
    Assert.assertEquals(readContents(new File(xmlOutputDir,"TEST-testName.xml")),
        "<testsuite errors=\"0\" skipped=\"0\" tests=\"1\" time=\"0.1\" failures=\"1\" name=\"testName\"><properties></properties><testcase classname=\"testName\" time=\"0.1\" name=\"testName\"><failure type=\"java.lang.AssertionError\" message=\" exceptions: 0 wrong: 2\"></failure></testcase></testsuite>");    
  }
  @Test
  public void exceptionsAreRecordedCorrectly() throws Exception{
    xmlTestListener.recordTestResult("testName", new TestSummary(1,2,0,1), 100);
    Assert.assertEquals(readContents(new File(xmlOutputDir,"TEST-testName.xml")),
        "<testsuite errors=\"1\" skipped=\"0\" tests=\"1\" time=\"0.1\" failures=\"0\" name=\"testName\"><properties></properties><testcase classname=\"testName\" time=\"0.1\" name=\"testName\"><failure type=\"java.lang.AssertionError\" message=\" exceptions: 1 wrong: 2\"></failure></testcase></testsuite>");    
  }
  private String readContents(File expectedFile) throws IOException {
    BufferedReader reader=new BufferedReader(new FileReader(expectedFile));
    StringBuffer buffer=new StringBuffer();
    String line=reader.readLine();
    while (line!=null){
      buffer.append(line);
      line=reader.readLine();
    }
    reader.close();
    return buffer.toString();
  }
}
