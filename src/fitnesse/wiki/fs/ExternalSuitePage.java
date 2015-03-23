package fitnesse.wiki.fs;

import fitnesse.wiki.WikiPageProperties;
import fitnesse.wikitext.parser.VariableSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fitnesse.wiki.BaseWikiPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PageType;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.util.Clock;

public class ExternalSuitePage extends BaseWikiPage {
  public static final String HTML = ".html";

  private File path;
  private FileSystem fileSystem;

  public ExternalSuitePage(File path, String name, BaseWikiPage parent, FileSystem fileSystem, VariableSource variableSource) {
    super(name, parent, variableSource);
    this.path = path;
    this.fileSystem = fileSystem;
  }

  @Override
  public WikiPage addChildPage(String name) {
    return null;
  }

  @Override
  public boolean hasChildPage(String pageName) {
    return getChildPage(pageName) != null;
  }

  @Override
  public void removeChildPage(String name) {
  }

  @Override
  public PageData getData() {
    return makePageData();
  }

  @Override
  public Collection<VersionInfo> getVersions() {
    return Collections.emptySet();
  }

  @Override
  public WikiPage getVersion(String versionName) {
    return null;
  }

  @Override
  public VersionInfo commit(PageData data) {
    return null;
  }

  @Override
  public List<WikiPage> getChildren() {
    return findChildren();
  }

  @Override
  public WikiPage getChildPage(String name) {
    for (WikiPage child : findChildren()) {
      if (child.getName().equals(name)) {
        return child;
      }
    }
    return null;
  }

  private List<WikiPage> findChildren() {
    List<WikiPage> children = new ArrayList<WikiPage>();
    for (String child : fileSystem.list(path)) {
      File childPath = new File(path, child);
      if (child.endsWith(HTML)) {
        children.add(new ExternalTestPage(childPath,
                child.replace(HTML, ""), this, fileSystem, getVariableSource()));
      } else if (hasHtmlChild(childPath)) {
        children.add(new ExternalSuitePage(childPath,
                child, this, fileSystem, getVariableSource()));
      }
    }
    return children;
  }

  private Boolean hasHtmlChild(File path) {
    if (path.getName().endsWith(HTML)) return true;
    for (String child : fileSystem.list(path)) {
      if (hasHtmlChild(new File(path, child))) return true;
    }
    return false;
  }

  private PageData makePageData() {
    WikiPageProperties properties = new WikiPageProperties();
    properties.set(PageType.SUITE.toString());
    properties.set(PageData.PropertyWHERE_USED);
    properties.set(PageData.PropertyRECENT_CHANGES);
    properties.set(PageData.PropertyFILES);
    properties.set(PageData.PropertyVERSIONS);
    properties.set(PageData.PropertySEARCH);
    properties.setLastModificationTime(Clock.currentDate());
    return new PageData("!contents", properties);
  }
}
