// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Pattern;

import util.RegexTestCase;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wikitext.WidgetBuilder;

public class PreformattedWidgetTest extends RegexTestCase {
  public void testRegexp() throws Exception {
    Pattern pattern = Pattern.compile(PreformattedWidget.REGEXP, Pattern.DOTALL);
    assertTrue("match1", pattern.matcher("{{{preformatted}}}").matches());
    assertTrue("match2", pattern.matcher("{{{{preformatted}}}}").matches());
    assertFalse("match3", pattern.matcher("{{ {not preformatted}}}").matches());
    assertTrue("match4", pattern.matcher("{{{\npreformatted\n}}}").matches());
  }

  public void testHtml() throws Exception {
    PreformattedWidget widget = new PreformattedWidget(new MockWidgetRoot(), "{{{preformatted text}}}");
    assertEquals("<pre>preformatted text</pre>", widget.render());
  }

  public void testMultiLine() throws Exception {
    PreformattedWidget widget = new PreformattedWidget(new MockWidgetRoot(), "{{{\npreformatted text\n}}}");
    assertEquals("<pre><br/>preformatted text<br/></pre>", widget.render());
  }

  public void testAsWikiText() throws Exception {
    PreformattedWidget widget = new PreformattedWidget(new MockWidgetRoot(), "{{{preformatted text}}}");
    assertEquals("{{{preformatted text}}}", widget.asWikiText());
  }

  public void testThatLiteralsWorkInPreformattedText() throws Exception {
    ParentWidget root = new WidgetRoot("{{{abc !-123-! xyz}}}", new WikiPageDummy(), WidgetBuilder.htmlWidgetBuilder);
    String text = root.render();
    assertEquals("<pre>abc 123 xyz</pre>", text);
  }

  public void testThatVariablesWorkInPreformattedText() throws Exception {
    ParentWidget root = new WidgetRoot("!define X {123}\n{{{abc ${X} xyz}}}", new WikiPageDummy(), WidgetBuilder.htmlWidgetBuilder);
    String text = root.render();
    assertSubString("<pre>abc 123 xyz</pre>", text);
  }
}
