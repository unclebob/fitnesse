package fitnesse;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.Test;

import fitnesse.testutil.FitNesseUtil;

public class FitNesseContextTest {

  @Test
  public void testGetRootPagePath() {
    FitNesseContext context = FitNesseUtil.makeTestContext();
    assertEquals(context.rootPath + File.separator + FitNesseUtil.base, context.getRootPagePath());
  }

}
