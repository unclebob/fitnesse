package fitnesse.wiki.fs;

import fitnesse.wikitext.parser.WikiWordPath;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fitnesse.wiki.BaseWikiPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PageType;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.WikiWordPath;

public class ExternalSuitePage extends BaseWikiPage {
  private static final long serialVersionUID = 1L;
  public static final String HTML = ".html";

  private String path;
  private FileSystem fileSystem;

  public ExternalSuitePage(String path, String name, BaseWikiPage parent, FileSystem fileSystem) {
    super(name, parent);
    this.path = path;
    this.fileSystem = fileSystem;
  }

  public String getFileSystemPath() {
    return path;
  }

  @Override
  public WikiPage addChildPage(String name) {
    return null;
  }

  @Override
  public boolean hasChildPage(String pageName) {
    return getNormalChildPage(pageName) != null;
  }

  @Override
  public void removeChildPage(String name) {
  }

  @Override
  public PageData getData() {
    return makePageData();
  }

  @Override
  public ReadOnlyPageData readOnlyData() {
    return getData();
  }

  @Override
  public Collection<VersionInfo> getVersions() {
    return Collections.emptySet();
  }

  @Override
  public PageData getDataVersion(String versionName) {
    return null;
  }

  @Override
  public VersionInfo commit(PageData data) {
    return null;
  }

  @Override
  protected List<WikiPage> getNormalChildren() {
    return findChildren();
  }

  @Override
  protected WikiPage getNormalChildPage(String name) {
    for (WikiPage child : findChildren()) {
      if (child.getName().equals(name)) {
        return child;
      }
    }
    return null;
  }

  private List<WikiPage> findChildren() {
    List<WikiPage> children = new ArrayList<WikiPage>();
    for (String child : fileSystem.list(getFileSystemPath())) {
      String childPath = getFileSystemPath() + "/" + child;
      if (child.endsWith(HTML)) {
        children.add(new ExternalTestPage(childPath,
                WikiWordPath.makeWikiWord(child.replace(HTML, "")), parent, fileSystem));
      } else if (hasHtmlChild(childPath)) {
        children.add(new ExternalSuitePage(childPath,
                WikiWordPath.makeWikiWord(child), parent, fileSystem));
      }
    }
    return children;
  }

  private Boolean hasHtmlChild(String path) {
    if (path.endsWith(HTML)) return true;
    for (String child : fileSystem.list(path)) {
      if (hasHtmlChild(path + "/" + child)) return true;
    }
    return false;
  }

  private PageData makePageData() {
    PageData pageData = new PageData(this);
    pageData.setContent("!contents");
    pageData.removeAttribute(PageData.PropertyEDIT);
    pageData.removeAttribute(PageData.PropertyPROPERTIES);
    pageData.removeAttribute(PageData.PropertyVERSIONS);
    pageData.removeAttribute(PageData.PropertyREFACTOR);
    pageData.setAttribute(PageType.SUITE.toString(), Boolean.toString(true));
    return pageData;

  }
}
