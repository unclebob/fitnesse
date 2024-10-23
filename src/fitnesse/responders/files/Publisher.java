package fitnesse.responders.files;

import fitnesse.html.template.PageTitle;
import fitnesse.util.StringTransform;
import fitnesse.wiki.PageCrawler;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.RecentChanges;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageProperty;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wikitext.parser.TextMaker;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;

import java.io.File;
import java.io.StringWriter;
import java.util.function.BiConsumer;

public class Publisher {
  private final String HTML_PATH_SEPARATOR = "/";
  private final String ROOT_PAGE_NAME_LINK = PathParser.ROOT_PAGE_NAME + "\"";
  private final String PAGE_EXTENSION = ".html";
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
      String content = pageContent(page);
      writer.accept(content, path);
      if (page.getFullPath().toString().equals(WikiPageUtil.FRONT_PAGE)) {
        writer.accept(content, destinationPath("index"));
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      result.append(e).append("<br>");
    }
    for (WikiPage child: page.getChildren()) {
      if (child.getName().equals(PathParser.FILES)) continue;
      if (child.getName().equals(WikiPageUtil.PAGE_HEADER)) continue;
      if (child.getName().equals(WikiPageUtil.PAGE_FOOTER)) continue;
      if (child.getName().equals(RecentChanges.RECENT_CHANGES)) continue;
      if (child.isSymbolicPage()) continue;
      result.append(traverse(child));
    }
    return result.toString();
  }

  private String pageContent(WikiPage page) {
    return fixSources(page, fixLinks(page, fixMissing(renderPage(page))));
  }

  private String renderPage(WikiPage page) {
    VelocityContext context = new VelocityContext();
    context.put("content", WikiPageUtil.makePageHtml(page));
    context.put("title", PathParser.render(page.getFullPath()));
    context.put("pageTitle", new PageTitle(page.getFullPath()).notLinked());
    context.put("footerContent", WikiPageUtil.getFooterPageHtml(page));
    context.put("helpText", page.getData().getProperties().get(WikiPageProperty.HELP));
    StringWriter output = new StringWriter();
    Velocity.evaluate(context, output, "publish", template);
    return output.toString();
  }

  private String fixMissing(String input) {
    StringTransform transform = new StringTransform(input);
    while (transform.find("<a title=\"create page\"")) {
      transform.skipOver("[?]</a>");
    }
    return transform.getOutput();
  }

  private String fixLinks(WikiPage page, String input) {
    StringTransform transform = new StringTransform(input);
    long depth = page.getFullPath().toString().chars().filter(c -> c == '.').count();
    while (transform.find(" href=\"")) {
      transform.copy();
      fixRootLink(transform, depth);
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

  private void fixRootLink(StringTransform transform, long depth) {
    if (transform.startsWith(ROOT_PAGE_NAME_LINK)) {
      transform.insert(PathParser.ROOT_PAGE_NAME + PAGE_EXTENSION + "\"");
      transform.skipOver(ROOT_PAGE_NAME_LINK);
    }
  }
  private void fixWikiWord(StringTransform transform, long depth) {
    int wikiWordStart = transform.getCurrent();
    if (transform.startsWith("/") || transform.startsWith(".")) {
      wikiWordStart++;
    }
    int wikiWordLength = TextMaker.findWikiWordLength(transform.from(wikiWordStart));
    if (wikiWordLength == 0) return;
    WikiPagePath path = PathParser.parse(transform.from(wikiWordStart).substring(0, wikiWordLength));
    WikiPage target = crawler.getPage(path);
    if (target == null) return;
    WikiPage targetPage = target.getRealPage();
    for (int i = 0; i < depth; i++) transform.insert("../");
    transform.insert(targetPage.getFullPath().toString().replace('.', '/') + PAGE_EXTENSION);
    transform.skipTo(wikiWordStart + wikiWordLength);
  }

  private void fixFiles(StringTransform transform, long depth) {
    if (!transform.startsWith("files/")) return;
    for (int i = 0; i < depth; i++) transform.insert("../");
  }

  private String destinationPath(WikiPage page) {
    String pagePath = page.getFullPath().toString().replace(".", HTML_PATH_SEPARATOR);
    return destinationPath(pagePath);
  }

  private String destinationPath(String pagePath) {
    return destination + HTML_PATH_SEPARATOR + (pagePath.length() > 0 ? pagePath : PathParser.ROOT_PAGE_NAME) + PAGE_EXTENSION;
  }

  private final String template;
  private final String destination;
  private final PageCrawler crawler;
  private final BiConsumer<String, String> writer;
}
