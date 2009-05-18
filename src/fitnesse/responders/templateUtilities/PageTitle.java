package fitnesse.responders.templateUtilities;

import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPagePath;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;

public class PageTitle {
  private String title;
  private String link;
  private List<BreadCrumb> breadCrumbs = new ArrayList<BreadCrumb>();
  private String pageType;

  public PageTitle(WikiPagePath pagePath) {
    List<String> names = pagePath.getNames();
    title = names.get(names.size() - 1);
    link = PathParser.render(pagePath);

    pagePath.removeNameFromEnd();
    while (pagePath.getNames().size() > 0) {
      names = pagePath.getNames();
      BreadCrumb crumb = new BreadCrumb(names.get(names.size() - 1),PathParser.render(pagePath));
      breadCrumbs.add(crumb);
      pagePath.removeNameFromEnd();
    }
    Collections.reverse(breadCrumbs);
  }

  public PageTitle() {
  }

  public PageTitle(String pageType) {
    this.pageType = pageType;
    this.title = pageType;
  }

  public PageTitle(String pageType, WikiPagePath wikiPagePath) {
    this(wikiPagePath);
    this.pageType = pageType;
  }

  public String getTitle() {
    return title;
  }

  public String getLink() {
    return link;
  }

  public List<BreadCrumb> getBreadCrumbs() {
    return breadCrumbs;
  }

  public String getPageType() {
    return pageType;
  }

  public class BreadCrumb {
    private String name;
    private String link;

    public BreadCrumb(String name, String link) {
      this.name = name;
      this.link = link;
    }

    public String getName() {
      return name;
    }

    public String getLink() {
      return link;
    }
  }
}
