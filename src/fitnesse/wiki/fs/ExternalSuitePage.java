package fitnesse.wiki.fs;

import fitnesse.wiki.*;
import fitnesse.wikitext.VariableSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fitnesse.util.Clock;

public class ExternalSuitePage extends BaseWikitextPage {
  public static final String HTML = ".html";

  private File path;
  private FileSystem fileSystem;

  public ExternalSuitePage(File path, String name, WikiPage parent, FileSystem fileSystem, VariableSource variableSource) {
    super(name, parent, variableSource);
    this.path = path;
    this.fileSystem = fileSystem;
  }

  @Override
  public WikiPage addChildPage(String name) {
    return null;
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
    List<WikiPage> children = new ArrayList<>();
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

  private boolean hasHtmlChild(File path) {
    return hasHtmlChild(fileSystem, path);
  }

  static boolean hasHtmlChild(FileSystem fileSystem, File path) {
    if (path.getName().endsWith(HTML)) return true;
    for (String child : fileSystem.list(path)) {
      if (hasHtmlChild(fileSystem, new File(path, child))) return true;
    }
    return false;
  }

  private PageData makePageData() {
    WikiPageProperties properties = new WikiPageProperties();
    properties.set(PageType.SUITE.toString());
    properties.set(WikiPageProperty.WHERE_USED);
    properties.set(WikiPageProperty.RECENT_CHANGES);
    properties.set(WikiPageProperty.FILES);
    properties.set(WikiPageProperty.VERSIONS);
    properties.set(WikiPageProperty.SEARCH);
    properties.setLastModificationTime(Clock.currentDate());
    return new PageData("!contents", properties);
  }
}
