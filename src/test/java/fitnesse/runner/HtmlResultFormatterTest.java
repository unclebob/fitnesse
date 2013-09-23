// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.runner;

import static org.junit.Assert.assertNotNull;
import static util.RegexTestCase.assertSubString;

import java.io.InputStream;

import fitnesse.FitNesseContext;
import fitnesse.testsystems.TestSummary;
import fitnesse.testutil.FitNesseUtil;
import org.junit.Before;
import org.junit.Test;
import util.StreamReader;

public class HtmlResultFormatterTest {
  private HtmlResultFormatter formatter;

  @Before
  public void setUp() throws Exception {
    FitNesseContext context = FitNesseUtil.makeTestContext(null);
    formatter = new HtmlResultFormatter(context, "somehost.com:8080", "FitNesse");
  }

  @Test
  public void testUsage() throws Exception {
    formatter.acceptResult(new PageResult("PageOne", new TestSummary(1, 0, 0, 0), "page one"));
    formatter.acceptResult(new PageResult("PageTwo", new TestSummary(0, 1, 0, 0), "page two"));
    formatter.acceptFinalCount(new TestSummary(1, 1, 0, 0));

    String html = getHtml();

    assertSubString("PageOne", html);
    assertSubString("page one", html);
    assertSubString("pass", html);

    assertSubString("PageTwo", html);
    assertSubString("page two", html);
    assertSubString("fail", html);
  }

  private String getHtml() throws Exception {
    InputStream input = formatter.getResultStream();
    assertNotNull(input);

    int bytes = formatter.getByteCount();
    String html = new StreamReader(input).read(bytes);
    return html;
  }
}
