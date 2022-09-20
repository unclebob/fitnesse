package fitnesse.responders.files;

import fitnesse.wiki.PageData;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class PublisherTest {

  @Before public void SetUp() {
    root = InMemoryPage.makeRoot("root");
  }

  @Test public void topPage() {
    assertTopPage("plain content", "plain content");
    assertTopPage("<a href=\"TestSibling.html\">TestSibling</a>", "TestSibling");
    assertTopPage("<a href=\"TestSibling.html#anchor\">link</a>", "[[link][TestSibling#anchor]]");
    assertTopPage("<a href=\"http://test.org\">http://test.org</a>", "http://test.org");
  }

  @Test public void childPage() {
    assertChildPage("my child", "my child");
    assertChildPage("<a href=\"../TestParent/TestSibling.html\">TestSibling</a>", "TestSibling");
    assertChildPage("<a href=\"../TestParent.html\">link</a>", "!-<a href=\"/TestParent\">link</a>-!");
  }

  @Test public void headerAndFooter() {
    WikiPageUtil.addPage(root, PathParser.parse("PageHeader"), "header");
    WikiPageUtil.addPage(root, PathParser.parse("PageFooter"), "footer");
    assertTopPage("header+body", "+body");
    Assert.assertTrue(content, content.contains("f*footer*f"));
  }

  @Test public void symbolicPage() {
    WikiPage pageOne = WikiPageUtil.addPage(root, PathParser.parse("PageOne"), ">SymPage");
    WikiPage pageTwo = WikiPageUtil.addPage(root, PathParser.parse("PageTwo"), "");
    PageData data = pageOne.getData();
    data.getProperties().set(SymbolicPage.PROPERTY_NAME).set("SymPage", pageTwo.getName());
    pageOne.commit(data);
    assertPublishes("<a href=\"PageTwo.html\">&gt;SymPage</a>", "PageOne", "", pageOne);
  }

  private void assertChildPage(String expected, String pageContent) {
    WikiPage parent = WikiPageUtil.addPage(root, PathParser.parse("TestParent"), "");
    WikiPageUtil.addPage(parent, PathParser.parse("TestSibling"), "");
    WikiPage page = WikiPageUtil.addPage(parent, PathParser.parse("TestPage"), pageContent);
    assertPublishes(expected, "TestParent/TestPage", "../", page);
    Assert.assertTrue(content, content.contains("t*TestParent.TestPage*t"));
  }

  private void assertTopPage(String expected, String pageContent) {
    WikiPageUtil.addPage(root, PathParser.parse("TestSibling"), "");
    WikiPage page = WikiPageUtil.addPage(root, PathParser.parse("TestPage"), pageContent);
    assertPublishes(expected, "TestPage", "", page);
    Assert.assertTrue(content, content.contains("t*TestPage*t"));
  }

  private void assertPublishes(String pageContent, String pageName, String prefix, WikiPage page) {
    Publisher publisher = new Publisher(TEMPLATE, "out", root.getPageCrawler(), this::writer);
    publisher.traverse(page);
    Assert.assertEquals("out/" + pageName + ".html", path);
    Assert.assertTrue(content, content.contains("b*" + pageContent + "*b"));
    Assert.assertTrue(content, content.contains("<link href=\"" + prefix + "css/fitnesse_wiki.css\">"));
  }

  private void writer(String content, String path) {
    this.content = content;
    this.path = path;
  }

  private static final String TEMPLATE = "t*$title$*t <link href=\"css/fitnesse_wiki.css\"> b*$body$*b f*$footer$*f";

  private String content;
  private String path;
  private WikiPage root;
}
