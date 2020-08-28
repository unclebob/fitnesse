// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HtmlTagTest {
  public static final String endl = HtmlElement.endl;
  private HtmlTag tag;

  @Before
  public void setUp() throws Exception {
    tag = new HtmlTag("aTag");
  }

  @Test
  public void testEmpty() {
    assertEquals("<aTag/>" + endl, tag.html());
  }

  @Test
  public void testWithText() {
    tag.add("some text");
    assertEquals("<aTag>some text</aTag>" + endl, tag.html());
  }

  @Test
  public void testEmbeddedTag() {
    tag.add(new HtmlTag("innertag"));

    String expected = "<aTag>" + endl +
      "\t<innertag/>" + endl +
      "</aTag>" + endl;

    assertEquals(expected, tag.html());
  }

  @Test
  public void testAttribute() {
    tag.addAttribute("key", "value");
    assertEquals("<aTag key=\"value\"/>" + endl, tag.html());
  }

  @Test
  public void testCombination() {
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
  public void testNoEndTabWithoutChildrenTags() {
    HtmlTag subtag = new HtmlTag("subtag");
    subtag.add("content");
    tag.add(subtag);

    String expected = "<aTag>" + endl +
      "\t<subtag>content</subtag>" + endl +
      "</aTag>" + endl;

    assertEquals(expected, tag.html());
  }

  @Test
  public void whenInline_noLineBreaksOrTabsAreGeneratedForChildren() {
    HtmlTag subtag = new HtmlTag("child");
    subtag.add("content");
    tag.add(subtag);
    assertEquals("<aTag><child>content</child></aTag>", tag.htmlInline());
  }

  @Test
  public void testTwoChildren() {
    tag.add(new HtmlTag("tag1"));
    tag.add(new HtmlTag("tag2"));

    String expected = "<aTag>" + endl +
      "\t<tag1/>" + endl +
      "\t<tag2/>" + endl +
      "</aTag>" + endl;

    assertEquals(expected, tag.html());
  }

  @Test
  public void testUse() {
    tag.add("original");
    tag.use("new");
    assertEquals("<aTag>new</aTag>" + endl, tag.html());
  }
}
