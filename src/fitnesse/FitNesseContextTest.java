package fitnesse;

import junit.framework.TestCase;

public class FitNesseContextTest extends TestCase {
  public void testShouldReportPortOfMinusOneIfNotInitialized() {
    FitNesseContext.globalContext = null;
    assertEquals(-1, FitNesseContext.getPort());
  }

  public void testShouldHavePortSetAfterFitNesseObjectConstructed() throws Exception {
    FitNesseContext context = new FitNesseContext();
    context.port = 9988;
    new FitNesse(context, false);
    assertEquals(9988, FitNesseContext.getPort());
  }
}
