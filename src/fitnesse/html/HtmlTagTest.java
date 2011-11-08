// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HtmlTagTest {
  public static String endl = HtmlElement.endl;
  private HtmlTag tag;

  @Before
  public void setUp() throws Exception {
    tag = new HtmlTag("aTag");
  }

  @Test
  public void testEmpty() throws Exception {
    assertEquals("<aTag/>" + endl, tag.html());
  }

  @Test
  public void givenNonNullHead_HeadIsPrepended() throws Exception {
    tag.head = "head";
    assertEquals("head<aTag/>" + endl, tag.html());
  }

  @Test
  public void givenNonNullTail_TailIsAppended() throws Exception {
    tag.tail = "tail";
    assertEquals("<aTag/>tail" +endl, tag.html());
  }

  @Test
  public void testWithText() throws Exception {
    tag.add("some text");
    assertEquals("<aTag>some text</aTag>" + endl, tag.html());
  }

  @Test
  public void testEmbeddedTag() throws Exception {
    tag.add(new HtmlTag("innertag"));

    String expected = "<aTag>" + endl +
      "\t<innertag/>" + endl +
      "</aTag>" + endl;

    assertEquals(expected, tag.html());
  }

  @Test
  public void testAttribute() throws Exception {
    tag.addAttribute("key", "value");
    assertEquals("<aTag key=\"value\"/>" + endl, tag.html());
  }

  @Test
  public void testCombination() throws Exception {
    tag.addAttribute("mykey", "myValue");
    HtmlTag inner = new HtmlTag("inner");
    inner.add(new HtmlTag("beforetext"));
    inner.add("inner text");
    inner.add(new HtmlTag("aftertext"));
    tag.add(inner);

    String expected = "<aTag mykey=\"myValue\">" + endl +
      "\t<inner>" + endl +
      "\t\t<beforetext/>" + endl +
      "inner text" + endl +
      "\t\t<aftertext/>" + endl +
      "\t</inner>" + endl +
      "</aTag>" + endl;

    assertEquals(expected, tag.html());
  }

  @Test
  public void testNoEndTabWithoutChildrenTags() throws Exception {
    HtmlTag subtag = new HtmlTag("subtag");
    subtag.add("content");
    tag.add(subtag);

    String expected = "<aTag>" + endl +
      "\t<subtag>content</subtag>" + endl +
      "</aTag>" + endl;

    assertEquals(expected, tag.html());
  }

  @Test
  public void whenInline_noLineBreaksOrTabsAreGeneratedForChildren() throws Exception {
    HtmlTag subtag = new HtmlTag("child");
    subtag.add("content");
    tag.add(subtag);
    assertEquals("<aTag>\t<child>content</child>" + endl + "</aTag>", tag.htmlInline());
  }

  @Test
  public void testTwoChildren() throws Exception {
    tag.add(new HtmlTag("tag1"));
    tag.add(new HtmlTag("tag2"));

    String expected = "<aTag>" + endl +
      "\t<tag1/>" + endl +
      "\t<tag2/>" + endl +
      "</aTag>" + endl;

    assertEquals(expected, tag.html());
  }

  @Test
  public void testUse() throws Exception {
    tag.add("original");
    tag.use("new");
    assertEquals("<aTag>new</aTag>" + endl, tag.html());
  }

  @Test
  public void comment() throws Exception {
    HtmlComment comment = new HtmlComment("the comment");
    assertEquals("<!--the comment-->" +endl, comment.html());
  }
}
