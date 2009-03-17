// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext.widgets;

import java.util.regex.Pattern;

import fitnesse.wikitext.WikiWidget;

public class EmailWidgetTest extends WidgetTestCase {
  protected String getRegexp() {
    return EmailWidget.REGEXP;
  }

  public void testEmailRegularExpression() throws Exception {
    assertTrue("Match 1", Pattern.matches(EmailWidget.REGEXP, "ppagel@objectmentor.com"));
    assertTrue("Match 2", Pattern.matches(EmailWidget.REGEXP, "ppagel123@objectmentor.com"));
    assertTrue("Match 3", Pattern.matches(EmailWidget.REGEXP, "1@2.com"));
    assertTrue("Match 4", Pattern.matches(EmailWidget.REGEXP, "1342534532@2.3.com"));
    assertFalse("Match 5", Pattern.matches(EmailWidget.REGEXP, "#!^@@@.()"));
    assertFalse("Match 6", Pattern.matches(EmailWidget.REGEXP, "abc@@@.()"));
    assertFalse("Match 7", Pattern.matches(EmailWidget.REGEXP, "abc@efg.()"));
  }

  public void testEmailRendering() throws Exception {
    WikiWidget email = new EmailWidget(null, "ppagel@objectmentor.com");
    assertEquals("<a href=\"mailto:ppagel@objectmentor.com\">ppagel@objectmentor.com</a>", email.render());
  }

}
