package fitnesse.responders.run.formatters;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;
import org.junit.After;
import fitnesse.responders.testHistory.TestHistory;
import fitnesse.responders.run.TestSummary;

public class XmlFormatterTest {
  @Before
  public void setUp() {
    XmlFormatter.setTestTime("4/13/2009 15:21:43");
  }

  @After
  public void tearDown() {
    XmlFormatter.clearTestTime();
  }

  @Test
  public void makeFileName() throws Exception {
    XmlFormatter formatter = new XmlFormatter(null, null, null);
    TestSummary summary = new TestSummary(1, 2, 3, 4);
    Assert.assertEquals(
      "20090413152143_1_2_3_4.xml", 
      TestHistory.makeResultFileName(summary, formatter.getTime()));
  }

}
