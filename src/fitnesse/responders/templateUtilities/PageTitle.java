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
  private String pageTags;

  public PageTitle(WikiPagePath pagePath) {
    pagePath = pagePath.clone();
    List<String> names = pagePath.getNames();
    link = PathParser.render(pagePath);
    if (names.size() > 0) {
      title = names.get(names.size() - 1);
      
      pagePath.removeNameFromEnd();
      while (pagePath.getNames().size() > 0) {
        names = pagePath.getNames();
        BreadCrumb crumb = new BreadCrumb(names.get(names.size() - 1), PathParser.render(pagePath));
        breadCrumbs.add(crumb);
        pagePath.removeNameFromEnd();
      }
      Collections.reverse(breadCrumbs);
    } else {
    	title = "root";
    }
  }

  public PageTitle() {
  }

  public PageTitle(String pageType) {
    this.setPageType(pageType);
    this.title = pageType;
  }

  public PageTitle(String pageType, WikiPagePath wikiPagePath) {
    this(wikiPagePath);
    this.setPageType(pageType);
  }

  public PageTitle(String pageType, WikiPagePath wikiPagePath, String pageTags) {
    this(wikiPagePath);
    this.setPageType(pageType);
    this.setPageTags(pageTags);
  }

  public PageTitle(String path, String separator) {
    String[] crumbs = path.split(separator);
    String crumb;
    String trail = "";
    for (int i = 0; i < crumbs.length - 1; i++) {
      crumb = crumbs[i];
      breadCrumbs.add(new BreadCrumb(crumb, trail + crumb));
      trail = trail + crumb + separator;
    }
    if (crumbs.length > 0) {
      crumb = crumbs[crumbs.length - 1];
      title = crumb;
      link = trail + crumb;
    }
  }

  public PageTitle(String pageType, String path, String separator) {
    this(path, separator);
    this.setPageType(pageType);
  }

  public PageTitle(String pageType, String path, String separator, String pageTags) {
    this(path, separator);
    this.setPageType(pageType);
    this.setPageTags(pageTags);
  }
  
  public PageTitle notLinked() {
    link = null;
    return this;
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

  public void setPageType(String pageType) {
    this.pageType = pageType;
  }
  
  public String getPageTags() {
    return pageTags;
  }
  
  public void setPageTags(String pageTags) {
    if(pageTags == null) return;
    if(pageTags == "") return;
    this.pageTags = pageTags;
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
