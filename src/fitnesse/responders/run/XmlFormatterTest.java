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
    Assert.assertEquals("2009_04/13_15_21_43.xml", XmlFormatter.makeResultFileName());
  }

}
