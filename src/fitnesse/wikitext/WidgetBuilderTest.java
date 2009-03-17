// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.wikitext;

import java.util.concurrent.atomic.AtomicBoolean;

import junit.framework.TestCase;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageDummy;
import fitnesse.wikitext.widgets.AnchorDeclarationWidget;
import fitnesse.wikitext.widgets.AnchorMarkerWidget;
import fitnesse.wikitext.widgets.BoldWidget;
import fitnesse.wikitext.widgets.CenterWidget;
import fitnesse.wikitext.widgets.ClasspathWidget;
import fitnesse.wikitext.widgets.CollapsableWidget;
import fitnesse.wikitext.widgets.EmailWidget;
import fitnesse.wikitext.widgets.HeaderWidget;
import fitnesse.wikitext.widgets.HruleWidget;
import fitnesse.wikitext.widgets.ItalicWidget;
import fitnesse.wikitext.widgets.ListWidget;
import fitnesse.wikitext.widgets.MockWidgetRoot;
import fitnesse.wikitext.widgets.NoteWidget;
import fitnesse.wikitext.widgets.ParentWidget;
import fitnesse.wikitext.widgets.PreformattedWidget;
import fitnesse.wikitext.widgets.StrikeWidget;
import fitnesse.wikitext.widgets.TableWidget;
import fitnesse.wikitext.widgets.TextWidget;
import fitnesse.wikitext.widgets.VirtualWikiWidget;
import fitnesse.wikitext.widgets.WidgetRoot;
import fitnesse.wikitext.widgets.WikiWordWidget;

public class WidgetBuilderTest extends TestCase {
  private WikiPage mockSource;

  public void setUp() throws Exception {
    mockSource = new WikiPageDummy();
  }

  public void tearDown() throws Exception {
  }

  public void testEmptyPage() throws Exception {
    ParentWidget widget = new WidgetRoot(null, mockSource);
    assertNotNull(widget);
    assertEquals(0, widget.numberOfChildren());
  }

  public void testSimpleText() throws Exception {
    ParentWidget page = new WidgetRoot("Hello, World!", mockSource);
    assertNotNull(page);
    assertEquals(1, page.numberOfChildren());
    WikiWidget widget = page.nextChild();
    testWidgetClassAndText(widget, TextWidget.class, "Hello, World!");
  }

  public void testSimpleWikiWord() throws Exception {
    ParentWidget page = new WidgetRoot("WikiWord", mockSource);
    WikiWidget widget = page.nextChild();
    testWidgetClassAndText(widget, WikiWordWidget.class, "WikiWord");
  }

  public void testTextThenWikiWord() throws Exception {
    ParentWidget page = new WidgetRoot("text WikiWord more Text", mockSource);
    assertEquals(3, page.numberOfChildren());
    WikiWidget widget1 = page.nextChild();
    WikiWidget widget2 = page.nextChild();
    WikiWidget widget3 = page.nextChild();
    testWidgetClassAndText(widget1, TextWidget.class, "text ");
    testWidgetClassAndText(widget2, WikiWordWidget.class, "WikiWord");
    testWidgetClassAndText(widget3, TextWidget.class, " more Text");
  }

  public void testWikiWord_Text_WikiWord() throws Exception {
    ParentWidget page = new WidgetRoot("WikiWord more WordWiki", mockSource);
    assertEquals(3, page.numberOfChildren());
    WikiWidget widget1 = page.nextChild();
    WikiWidget widget2 = page.nextChild();
    WikiWidget widget3 = page.nextChild();
    testWidgetClassAndText(widget1, WikiWordWidget.class, "WikiWord");
    testWidgetClassAndText(widget2, TextWidget.class, " more ");
    testWidgetClassAndText(widget3, WikiWordWidget.class, "WordWiki");
  }

  public void testItalic_text_WikiWord() throws Exception {
    ParentWidget page = new WidgetRoot("''italic'' text WikiWord", mockSource);
    assertEquals(3, page.numberOfChildren());
    WikiWidget widget1 = page.nextChild();
    WikiWidget widget2 = page.nextChild();
    WikiWidget widget3 = page.nextChild();
    assertEquals(ItalicWidget.class, widget1.getClass());
    testWidgetClassAndText(widget2, TextWidget.class, " text ");
    testWidgetClassAndText(widget3, WikiWordWidget.class, "WikiWord");
  }

  public void testWikiWordInsideItalic() throws Exception {
    testWikiWordInParentWidget("''WikiWord''", ItalicWidget.class, "WikiWord", 1);
  }

  public void testWikiWordInsideBold() throws Exception {
    testWikiWordInParentWidget("'''WikiWord'''", BoldWidget.class, "WikiWord", 1);
  }

  public void testMultiLineWidget() throws Exception {
    ParentWidget page = new WidgetRoot("{{{\npreformatted\n}}}", mockSource);
    assertEquals(1, page.numberOfChildren());
    assertEquals(PreformattedWidget.class, page.nextChild().getClass());
  }

  public void testEmailWidget() throws Exception {
    ParentWidget page = new WidgetRoot("someone@somewhere.com", mockSource);
    assertEquals(1, page.numberOfChildren());
    assertEquals(EmailWidget.class, page.nextChild().getClass());
  }

  public void testHrule() throws Exception {
    ParentWidget page = new WidgetRoot("-----", mockSource);
    WikiWidget widget = page.nextChild();
    assertEquals(HruleWidget.class, widget.getClass());
  }

  public void testAnchorsMarker() throws Exception {
    ParentWidget page = new WidgetRoot(".#name ", mockSource);
    WikiWidget widget = page.nextChild();
    assertEquals(AnchorMarkerWidget.class, widget.getClass());
  }

  public void testAnchorsDeclaration() throws Exception {
    ParentWidget page = new WidgetRoot("!anchor name ", mockSource);
    WikiWidget widget = page.nextChild();
    assertEquals(AnchorDeclarationWidget.class, widget.getClass());

  }

  public void testWikiWordInsideHeader() throws Exception {
    testWikiWordInParentWidget("!1 WikiWord\n", HeaderWidget.class, "WikiWord", 1);
  }

  public void testWikiWordInsideCenter() throws Exception {
    testWikiWordInParentWidget("!c WikiWord\n", CenterWidget.class, "WikiWord", 1);
  }

  public void testTable() throws Exception {
    ParentWidget page = new WidgetRoot("|a|b|\n|c|d|\n", mockSource);
    assertEquals(1, page.numberOfChildren());
    WikiWidget widget = page.nextChild();
    assertEquals(TableWidget.class, widget.getClass());
  }

  public void testList() throws Exception {
    ParentWidget page = new WidgetRoot(" *Item1\n", mockSource);
    assertEquals(1, page.numberOfChildren());
    assertEquals(ListWidget.class, page.nextChild().getClass());
  }

  public void testClasspath() throws Exception {
    ParentWidget page = new WidgetRoot("!path something", mockSource);
    assertEquals(1, page.numberOfChildren());
    assertEquals(ClasspathWidget.class, page.nextChild().getClass());
  }

  public void testStrike() throws Exception {
    testWikiWordInParentWidget("--WikiWord--", StrikeWidget.class, "WikiWord", 1);
  }

  public void testNoteWidget() throws Exception {
    ParentWidget page = new WidgetRoot("!note something", mockSource);
    assertEquals(1, page.numberOfChildren());
    assertEquals(NoteWidget.class, page.nextChild().getClass());
  }

  public void testCollapsableWidget() throws Exception {
    ParentWidget page = new WidgetRoot("!* title\ncontent\n*!\n", mockSource);
    assertEquals(1, page.numberOfChildren());
    assertEquals(CollapsableWidget.class, page.nextChild().getClass());
  }

  public void testNullPointerError() throws Exception {
    String wikiText = "''\nsome text that should be in italics\n''";
    ParentWidget root = new WidgetRoot(wikiText, new WikiPageDummy());

    try {
      root.render();
    }
    catch (Exception e) {
      fail("should be no exception\n" + e);
    }
  }

  public void testVirtualWikiWidget() throws Exception {
    ParentWidget page = new WidgetRoot("!virtualwiki http://localhost/FrontPage", mockSource);
    assertEquals(1, page.numberOfChildren());
    assertEquals(VirtualWikiWidget.class, page.nextChild().getClass());
  }

// TODO MdM A Test that reveals FitNesse's weakness for parsing large wiki documents.

  public void _testLargeTable() throws Exception {
    StringBuffer buffer = new StringBuffer();
    for (int i = 0; i < 1000; i++)
      buffer.append("|'''bold'''|''italic''|!c centered|\n");

    try {
      ParentWidget root = new WidgetRoot(buffer.toString(), new WikiPageDummy());
      root.render();
    }
    catch (StackOverflowError e) {
      fail("Got error with big table: " + e);
    }
  }

  private void testWikiWordInParentWidget(String input, Class<?> expectedClass, String wikiWordText, int subChildren) throws Exception {
    ParentWidget page = new WidgetRoot(input, mockSource);
    assertEquals(1, page.numberOfChildren());
    WikiWidget widget = page.nextChild();
    assertEquals(expectedClass, widget.getClass());

    ParentWidget iWidget = (ParentWidget) widget;
    assertEquals(subChildren, iWidget.numberOfChildren());
    WikiWidget childWidget = iWidget.nextChild();
    testWidgetClassAndText(childWidget, WikiWordWidget.class, wikiWordText);
  }

  private void testWidgetClassAndText(WikiWidget widget, Class<?> expectedClass, String expectedText) {
    assertEquals(expectedClass, widget.getClass());
    if (widget instanceof TextWidget)
      assertEquals(expectedText, ((TextWidget) widget).getText());
  }

  public void testConcurrentAddWidgets() throws Exception {
    WidgetBuilder widgetBuilder = new WidgetBuilder(new Class[]{BoldWidget.class});
    String text = "'''bold text'''";
    ParentWidget parent = new BoldWidget(new MockWidgetRoot(), "'''bold text'''");
    AtomicBoolean failFlag = new AtomicBoolean();
    failFlag.set(false);

    //This is our best attempt to get a race condition
    //by creating large number of threads.
    for (int i = 0; i < 1000; i++) {
      WidgetBuilderThread widgetBuilderThread = new WidgetBuilderThread(widgetBuilder, text, parent, failFlag);
      Thread thread = new Thread(widgetBuilderThread);
      try {
        thread.start();
      } catch (OutOfMemoryError e) {
        break;
      }
    }
    assertEquals(false, failFlag.get());
  }
  //Parsing Line Breaks used to be very slow because it was done with the LineBreakWidget
  //which used regular expressions.  Now we just have the text widget replace line ends
  // with <br/>.  This is much faster.  This test is here to make sure it stays fast.
  public void testParsingManyLineBreaksIsFast() throws Exception {
    StringBuffer b = new StringBuffer();
    for (int i = 0; i < 100; i++)
      b.append("****************************************************************\n");
    for (int i = 0; i < 100; i++)
      b.append("****************************************************************\r");
    for (int i = 0; i < 100; i++)
      b.append("****************************************************************\r\n");

    long start = System.currentTimeMillis();
    ParentWidget root = new WidgetRoot(b.toString(), mockSource);
    String html = root.childHtml();
    long stop = System.currentTimeMillis();

    StringBuffer expected = new StringBuffer();
    for (int i = 0; i < 300; i++)
      expected.append("****************************************************************<br/>");

    assertEquals(expected.toString(), html);
    long duration = stop - start;
    assertTrue(String.format("parsing took %s ms.", duration), duration < 500);
  }

  class WidgetBuilderThread implements Runnable {
    WidgetBuilder widjetBuilder = null;
    String text = null;
    ParentWidget parent = null;
    AtomicBoolean failFlag = null;

    public WidgetBuilderThread(WidgetBuilder widjetBuilder, String text, ParentWidget parent, AtomicBoolean failFlag) {
      this.widjetBuilder = widjetBuilder;
      this.text = text;
      this.parent = parent;
      this.failFlag = failFlag;
    }

    public void run() {
      try {
        this.widjetBuilder.addChildWidgets(this.text, this.parent);
      }
      catch (Exception e) {
        this.failFlag.set(true);
      }
    }
  }
}

