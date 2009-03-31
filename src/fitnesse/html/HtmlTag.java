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
    StringBuffer buffer = new StringBuffer();
    addTabs(depth, buffer);

    if (head != null) buffer.append(head);

    buffer.append("<").append(tagName());
    addAttributes(buffer);

    if (hasChildren()) {
      buffer.append(">");
      boolean tagWasAdded = addChildHtml(buffer, depth);
      if (tagWasAdded && !isInline) addTabs(depth, buffer);
      buffer.append("</").append(tagName()).append(">");
    } else
      buffer.append("/>");

    if (tail != null) buffer.append(tail);
    if (!isInline) buffer.append(endl);

    return buffer.toString();
  }

  private void addAttributes(StringBuffer buffer) {
    for (Attribute attribute : attributes) {
      buffer.append(" ").append(attribute.name).append("=\"").append(attribute.value).append("\"");
    }
  }

  protected void addTabs(int depth, StringBuffer buffer) {
    for (int i = 0; i < depth; i++)
      buffer.append('\t');
  }

  private boolean addChildHtml(StringBuffer buffer, int depth) {
    boolean addedTag = false;
    boolean lastAddedWasNonTag = false;
    int i = 0;
    for (HtmlElement element : childTags) {
      if (element instanceof HtmlTag) {
        if ((i == 0 || lastAddedWasNonTag) && !isInline)
          buffer.append(endl);
        buffer.append(((HtmlTag) element).html(depth + 1));
        addedTag = true;
        lastAddedWasNonTag = false;
      } else {
        buffer.append(element.html());
        lastAddedWasNonTag = true;
      }
      i++;
    }

    return addedTag;
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

  public static class Attribute {
    public String name;
    public String value;

    public Attribute(String name, String value) {
      this.name = name;
      this.value = value;
    }
  }
}
