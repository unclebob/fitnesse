package fitnesse.responders.files;

import fitnesse.html.template.PageTitle;
import fitnesse.util.StringTransform;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wikitext.parser.TextMaker;

import java.io.File;
import java.util.function.BiConsumer;

public class Publisher {
  //todo: downloads?

  public Publisher(String template, String destination, PageCrawler crawler, BiConsumer<String, String> writer) {
    this.template = template;
    this.destination = destination;
    this.crawler = crawler;
    this.writer = writer;
  }

  public String traverse(WikiPage page) {
    StringBuilder result = new StringBuilder();
    String path = destinationPath(page);
    result.append(path).append("<br>");
    try {
      writer.accept(pageContent(page), path);
    }
    catch (Exception e) {
      result.append(e).append("<br>");
    }
    for (WikiPage child: page.getChildren()) {
      if (child.getName().equals(PathParser.FILES)) continue;
      if (child.getName().equals(WikiPageUtil.PAGE_HEADER)) continue;
      if (child.getName().equals(WikiPageUtil.PAGE_FOOTER)) continue;
      //todo: skip others?
      if (child.isSymbolicPage()) continue;
      result.append(traverse(child));
    }
    return result.toString();
  }

  private String pageContent(WikiPage page) {
    return fixSources(page, fixLinks(page, replaceKeywords(page, template)));
  }

  private String replaceKeywords(WikiPage page, String input) {
    StringTransform transform = new StringTransform(input);
    String[][] keywords = new String[][] {
      { "title", PathParser.render(page.getFullPath()) },
      { "breadcrumbs", makeBreadCrumbs(page) },
      { "body", WikiPageUtil.makePageHtml(page) },
      { "footer", WikiPageUtil.getFooterPageHtml(page) },
      { "", ""}
    };
    while (transform.find("$")) {
      for (String[] keyword: keywords) {
        if (keyword[0].length() == 0) transform.copy();
        else if (transform.startsWith(keyword[0] + "$")) {
          transform.insert(keyword[1]);
          transform.skip(keyword[0].length() + 1);
          break;
        }
      }
    }
    return transform.getOutput();
  }

  private String makeBreadCrumbs(WikiPage page) {
    PageTitle pt = new PageTitle(page.getFullPath());
    StringBuilder breadcrumbs = new StringBuilder();
    for (PageTitle.BreadCrumb breadcrumb : pt.getBreadCrumbs()) {
      breadcrumbs.append("<li><a href=\"").append(breadcrumb.getLink()).append("\">").append(breadcrumb.getName()).append("</a></li>\n");
    }
    breadcrumbs.append("<li>").append(page.getName()).append("</li>\n");
    return breadcrumbs.toString();
  }

  private String fixLinks(WikiPage page, String input) {
    StringTransform transform = new StringTransform(input);
    long depth = page.getFullPath().toString().chars().filter(c -> c == '.').count();
    while (transform.find(" href=\"")) {
      transform.copy();
      fixWikiWord(transform, depth);
      fixFiles(transform, depth);
    }
    return transform.getOutput();
  }

  private String fixSources(WikiPage page, String input) {
    StringTransform transform = new StringTransform(input);
    long depth = page.getFullPath().toString().chars().filter(c -> c == '.').count();
    while (transform.find(" src=\"")) {
      transform.copy();
      fixFiles(transform, depth);
    }
    return transform.getOutput();
  }

  private void fixWikiWord(StringTransform transform, long depth) {
    int wikiWordStart = transform.getCurrent();
    if (transform.startsWith("/") || transform.startsWith(".")) {
      wikiWordStart++;
    }
    int wikiWordLength = TextMaker.findWikiWordLength(transform.from(wikiWordStart));
    if (wikiWordLength == 0) return;
    WikiPagePath path = PathParser.parse(transform.from(wikiWordStart).substring(0, wikiWordLength));
    WikiPage targetPage = crawler.getPage(path).getRealPage();
    for (int i = 0; i < depth; i++) transform.insert("../");
    transform.insert(targetPage.getFullPath().toString().replace('.', '/') + ".html");
    transform.skipTo(wikiWordStart + wikiWordLength);
  }

  private void fixFiles(StringTransform transform, long depth) {
    if (!transform.startsWith("files/")) return;
    for (int i = 0; i < depth; i++) transform.insert("../");
  }

  private String destinationPath(WikiPage page) {
    String pagePath = page.getFullPath().toString().replace(".", File.separator);
    return destination + File.separator + (pagePath.length() > 0 ? pagePath : "root") + ".html";
  }

  private final String template;
  private final String destination;
  private final PageCrawler crawler;
  private final BiConsumer<String, String> writer;
}
