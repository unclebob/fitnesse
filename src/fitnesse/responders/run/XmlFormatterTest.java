package fitnesse.responders.run;

import org.junit.Test;
import org.junit.Before;
import org.junit.Assert;

public class XmlFormatterTest {
  @Before
  public void setup() {
    XmlFormatter.setTestTime("4/13/2009 15:21:43");
  }

  @Test
  public void makeFileName() throws Exception {
    TestSummary summary = new TestSummary(1, 2, 3, 4);
    Assert.assertEquals("20090413152143_1_2_3_4.xml", XmlFormatter.makeResultFileName(summary));
  }

}
