// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import fitnesse.testutil.FitNesseUtil;

public class VirtualWikiWidgetTest extends WidgetTestCase {
  public void setUp() throws Exception {
  }

  public void tearDown() throws Exception {
  }

  public void testRegexp() throws Exception {
    assertMatch("!virtualwiki http://localhost:" + FitNesseUtil.port + "/SomePage");
    assertNoMatch("!virtualwiki SomeName");
  }

  public void testPieces() throws Exception {
    String text = "!virtualwiki http://localhost:" + FitNesseUtil.port + "/SomePage.ChildPage";
    VirtualWikiWidget widget = new VirtualWikiWidget(new MockWidgetRoot(), text);
    assertEquals("http://localhost:" + FitNesseUtil.port + "/SomePage.ChildPage", widget.getRemoteUrl());
  }

  public void testHtml() throws Exception {
    String text = "!virtualwiki http://localhost:" + FitNesseUtil.port + "/SomePage.ChildPage";
    VirtualWikiWidget widget = new VirtualWikiWidget(new MockWidgetRoot(), text);
    String html = widget.render();
    assertHasRegexp("deprecated", html);
  }

  protected String getRegexp() {
    return VirtualWikiWidget.REGEXP;
  }
}
