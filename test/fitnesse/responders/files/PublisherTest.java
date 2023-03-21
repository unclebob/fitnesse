package fitnesse.responders.files;

import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageProperty;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PublisherTest {

  @Before public void SetUp() {
    root = InMemoryPage.makeRoot("root");
    paths = "";
  }

  @Test public void plainTopPage() {
    assertTopPage("plain content", "plain content");
  }

  @Test public void topPageSibling() {
    assertTopPage("<a href=\"TestSibling.html\">TestSibling</a>", "TestSibling");
  }

  @Test public void linkWithAnchor() {
    assertTopPage("<a href=\"TestSibling.html#anchor\">link</a>", "[[link][TestSibling#anchor]]");
  }

  @Test public void linkToRoot() {
    assertTopPage("<a href=\"root.html\">root</a>", "[[root][root]]");
  }

  @Test public void missingPage() {
    assertTopPage("MissingPage", "MissingPage");
  }

  @Test public void externalLink() {
    assertTopPage("<a href=\"http://test.org\">http://test.org</a>", "http://test.org");
  }

  @Test public void plainChildPage() {
    assertChildPage("my child", "my child");
  }

  @Test public void childPageSibling() {
    assertChildPage("<a href=\"../TestParent/TestSibling.html\">TestSibling</a>", "TestSibling");
  }

  @Test public void childAbsoluteLinkSlash() {
    assertChildPage("<a href=\"../TestParent.html\">link</a>", "!-<a href=\"/TestParent\">link</a>-!");
  }

  @Test public void childAbsoluteLinkDot() {
    assertChildPage("<a href=\"../TestParent.html\">.TestParent</a>", ".TestParent");
  }

  @Test public void childAbsolutePath() {
    assertChildPage("<a href=\"../TestParent/TestSibling.html\">link</a>", "!-<a href=\".TestParent.TestSibling\">link</a>-!");
  }

  @Test public void headerAndFooter() {
    WikiPageUtil.addPage(root, PathParser.parse("PageHeader"), "header");
    WikiPageUtil.addPage(root, PathParser.parse("PageFooter"), "footer");
    assertTopPage("header+body", "+body");
    Assert.assertTrue(content, content.contains("f*footer*f"));
  }

  @Test public void skipSpecialPages() {
    WikiPageUtil.addPage(root, PathParser.parse("PageHeader"), "header");
    WikiPageUtil.addPage(root, PathParser.parse("PageFooter"), "footer");
    WikiPageUtil.addPage(root, PathParser.parse("files"), "files");
    Publisher publisher = new Publisher(TEMPLATE, "out", root.getPageCrawler(), this::writer);
    publisher.traverse(root);
    Assert.assertEquals("out/root.html", paths);
  }

  @Test public void symbolicPage() {
    WikiPage pageOne = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), ">SymPage");
    WikiPage pageTwo = WikiPageUtil.addPage(root, PathParser.parse("PageTwo"), "");
    PageData data = pageOne.getData();
    data.getProperties().set(SymbolicPage.PROPERTY_NAME).set("SymPage", pageTwo.getName());
    pageOne.commit(data);
    assertPublishes("<a href=\"PageTwo.html\">&gt;SymPage</a>", "PageOne", "", pageOne);
  }

  @Test public void frontPageCopiedToIndex() {
    WikiPageUtil.addPage(root, PathParser.parse("FrontPage"), "stuff");
    Publisher publisher = new Publisher(TEMPLATE, "out", root.getPageCrawler(), this::writer);
    publisher.traverse(root);
    Assert.assertEquals("out/root.htmlout/FrontPage.htmlout/index.html", paths);
  }

  private void assertChildPage(String expected, String pageContent) {
    WikiPage parent = WikiPageUtil.addPage(root, PathParser.parse("TestParent"), "");
    WikiPageUtil.addPage(parent, PathParser.parse("TestSibling"), "");
    WikiPage page = WikiPageUtil.addPage(parent, PathParser.parse("TestPage"), pageContent);
    assertPublishes(expected, "TestParent/TestPage", "../", page);
    Assert.assertTrue(content, content.contains("t*TestParent.TestPage*t"));
    Assert.assertTrue(content, content.contains("c*<li><a href=\"../TestParent.html\">TestParent</a></li>\n<li>TestPage</li>\n*c"));
  }

  private void assertTopPage(String expected, String pageContent) {
    WikiPageUtil.addPage(root, PathParser.parse("TestSibling"), "");
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("TestPage"), pageContent);
    PageData data = page.getData();
    data.setAttribute(WikiPageProperty.HELP, "sos");
    page.commit(data);
    page.getData().setAttribute(WikiPageProperty.HELP, "sos");
    assertPublishes(expected, "TestPage", "", page);
    Assert.assertTrue(content, content.contains("t*TestPage*t"));
    Assert.assertTrue(content, content.contains("h*sos*h"));
    Assert.assertTrue(content, content.contains("c*<li>TestPage</li>\n*c"));
  }

  private void assertPublishes(String pageContent, String pageName, String prefix, WikiPage page) {
    Publisher publisher = new Publisher(TEMPLATE, "out", root.getPageCrawler(), this::writer);
    publisher.traverse(page);
    Assert.assertEquals("out/" + pageName + ".html", paths);
    Assert.assertTrue(content, content.contains("b*" + pageContent + "*b"));
    Assert.assertTrue(content, content.contains("<link href=\"" + prefix + "files/path\">"));
    Assert.assertTrue(content, content.contains("<script src=\"" + prefix + "files/path\">"));
  }

  private void writer(String content, String path) {
    this.content = content;
    this.paths += path;
  }

  private static final String TEMPLATE =
    "t*$title*t <link href=\"files/path\"> <script src=\"files/path\"> h*$helpText*h " +
    "c*#foreach($breadCrumb in $pageTitle.BreadCrumbs)<li><a href=\"$breadCrumb.Link\">$breadCrumb.Name</a></li>\n#end" +
    "<li>$pageTitle.Title</li>\n" +
    "*c b*$content*b f*$footerContent*f";
  private String content;
  private String paths;
  private WikiPage root;
}
