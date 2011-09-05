// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import java.util.LinkedList;
import java.util.List;

public class HtmlTag extends HtmlElement {
  public LinkedList<HtmlElement> childTags = new LinkedList<HtmlElement>();
  protected List<Attribute> attributes = new LinkedList<Attribute>();
  protected String tagName = "youreIt";
  public String tail;
  public String head;
  public boolean isInline;

  public HtmlTag(String tagName) {
    this.tagName = tagName;
  }

  public HtmlTag(String tagName, String content) {
    this(tagName);
    add(content);
  }

  public HtmlTag(String tagName, HtmlElement child) {
    this(tagName);
    add(child);
  }

  public String tagName() {
    return tagName;
  }

  public String html() {
    return html(0);
  }

  public String htmlInline() {
    isInline = true;
    return html(0);
  }

  public String html(int depth) {
    return new HtmlFormatter(depth).format();
  }

  private boolean hasChildren() {
    return childTags.size() > 0;
  }

  public void add(String s) {
    add(new RawHtml(s));
  }

  public void add(HtmlElement element) {
    childTags.add(element);
  }

  public void addAttribute(String key, String value) {
    attributes.add(new Attribute(key, value));
  }

  public void use(String s) {
    use(new RawHtml(s));
  }

  public void use(HtmlElement element) {
    childTags.clear();
    add(element);
  }

  public String getAttribute(String key) {
    for (Attribute attribute : attributes) {
      if (key != null && key.equals(attribute.name))
        return attribute.value;
    }
    return null;
  }

  protected String makeIndent(int depth) {
    String indent = "";
    for (int i = 0; i < depth; i++)
      indent+='\t';
    return indent;
  }

  public static class Attribute {
    public String name;
    public String value;

    public Attribute(String name, String value) {
      this.name = name;
      this.value = value;
    }
  }

  private class HtmlFormatter {
    private int depth;

    public HtmlFormatter(int depth) {
      this.depth = depth;
    }

    public String format() {
      return makeTabs() + makeHead()
        + makeTag() + makeAttributes() + makeTagEnd()
        + makeChildren()
        + makeEndTag()
        + makeTail() + makeLineEnd();
    }

    private String makeEndTag() {
      return hasChildren() ? "</" + tagName() + ">" : "";
    }

    private String makeLineEnd() {
      return isInline ? "" : endl;
    }

    private String makeTail() {
      return tail == null ? "" : tail;
    }

    private String makeChildren() {
      String children = "";
      if (hasChildren()) {
        boolean addedTag = false;
        boolean lastAddedWasNonTag = false;
        int i1 = 0;
        for (HtmlElement element : childTags) {
          if (element instanceof HtmlTag) {
            if ((i1 == 0 || lastAddedWasNonTag) && !isInline)
              children += endl;
            children += ((HtmlTag) element).html(depth + 1);
            addedTag = true;
            lastAddedWasNonTag = false;
          } else {
            children += element.html();
            lastAddedWasNonTag = true;
          }
          i1++;
        }

        boolean tagWasAdded = addedTag;
        if (tagWasAdded && !isInline) children += makeTabs();
      }
      return children;
    }

    private String makeTagEnd() {
      return hasChildren() ? ">" : "/>";
    }

    private String makeAttributes() {
      String attributes = "";
      for (Attribute attribute : HtmlTag.this.attributes) {
        attributes += " " + attribute.name + "=\"" + attribute.value + "\"";
      }
      return attributes;
    }

    private String makeTag() {
      return "<" + tagName();
    }

    private String makeHead() {
      return head == null ? "" : head;
    }

    private String makeTabs() {
      return makeIndent(depth);
    }
  }
}
