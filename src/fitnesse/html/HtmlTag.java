// Copyright (C) 2003-2009 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the CPL Common Public License version 1.0.
package fitnesse.html;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class HtmlTag extends HtmlElement implements Iterable<HtmlElement> {
  private final LinkedList<HtmlElement> childTags = new LinkedList<>();
  private final List<Attribute> attributes = new LinkedList<>();
  private final String tagName;
  private boolean isInline;

  public static HtmlTag name(String tagName) { return new HtmlTag(tagName); }
  public HtmlTag attribute(String name, String value) { addAttribute(name, value); return this; }
  public HtmlTag body(String body) { add(body); return this; }
  public HtmlTag child(HtmlElement child) { add(child); return this; }
  public HtmlTag text(String text) { add(new HtmlText(text)); return this; }

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

  @Override
  public String html() {
    return html(0);
  }

  public String htmlInline() {
    return htmlInline(0);
  }

  public String htmlInline(int depth) {
    isInline = true;
    return html(depth);
  }

  public String html(int depth) {
    return new HtmlFormatter(depth).format();
  }

  private boolean hasChildren() {
    return !childTags.isEmpty();
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
    StringBuilder indent = new StringBuilder();
    for (int i = 0; i < depth; i++)
      indent.append('\t');
    return indent.toString();
  }

  @Override
  public Iterator<HtmlElement> iterator() {
    return childTags.iterator();
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
    private final int depth;
    private boolean childTagWasMade;
    private boolean lastMadeChildWasNotTag;
    private boolean firstElement;

    public HtmlFormatter(int depth) {
      this.depth = depth;
    }

    public String format() {
      return makeTabs()
        + makeTag() + makeAttributes() + makeTagEnd()
        + makeChildren()
        + makeEndTag()
        + makeLineEnd();
    }

    private String makeEndTag() {
      return hasChildren() ? "</" + tagName() + ">" : "";
    }

    private String makeLineEnd() {
      return isInline ? "" : endl;
    }

    private String makeChildren() {
      String children = "";
      if (hasChildren()) {
        children = makeChildrenWithoutTrailingIndent();
        if (childTagWasMade && !isInline) children += makeTabs();
      }
      return children;
    }

    private String makeChildrenWithoutTrailingIndent() {
      StringBuilder children = new StringBuilder(64);
      childTagWasMade = false;
      lastMadeChildWasNotTag = false;
      firstElement = true;
      for (HtmlElement element : childTags) {
        children.append(makeChildFromElement(element));
        firstElement = false;
      }
      return children.toString();
    }

    private String makeChildFromElement(HtmlElement element) {
      boolean childIsTag = element instanceof HtmlTag;
      String child = childIsTag ?
        makeChildFromTag((HtmlTag) element) :
        element.html();

      prepareForNextElement(childIsTag);
      return child;
    }

    private void prepareForNextElement(boolean childIsTag) {
      childTagWasMade |= childIsTag;
      lastMadeChildWasNotTag = !childIsTag;
    }

    private String makeChildFromTag(HtmlTag element) {
      return (childShouldStartWithNewLine() ? endl : "")
        + (isInline ? element.htmlInline(depth) : element.html(depth + 1));
    }

    private boolean childShouldStartWithNewLine() {
      return (firstElement || lastMadeChildWasNotTag) && !isInline;
    }

    private String makeTagEnd() {
      return hasChildren() ? ">" : "/>";
    }

    private String makeAttributes() {
      StringBuilder attributes = new StringBuilder();
      for (Attribute attribute : HtmlTag.this.attributes) {
        attributes.append(" ").append(attribute.name).append("=\"").append(attribute.value).append("\"");
      }
      return attributes.toString();
    }

    private String makeTag() {
      return "<" + tagName();
    }

    private String makeTabs() {
      return isInline ? "" : makeIndent(depth);
    }
  }
}
