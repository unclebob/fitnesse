package fitnesse.wiki.fs;

import fitnesse.wiki.*;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fitnesse.wiki.BaseWikiPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.PageType;
import fitnesse.wiki.ReadOnlyPageData;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;

public class ExternalTestPage extends BaseWikiPage {
  private static final long serialVersionUID = 1L;
  private FileSystem fileSystem;
  private File path;

  public ExternalTestPage(File path, String name, BaseWikiPage parent, FileSystem fileSystem) {
    super(name, parent);
    this.path = path;
    this.fileSystem = fileSystem;
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
  protected List<WikiPage> getNormalChildren() {
    return Collections.emptyList();
  }

  @Override
  public WikiPage getNormalChildPage(String name) {
    return null;
  }

  @Override
  public WikiPage addChildPage(String name) {
    return null;
  }

  @Override
  public boolean hasChildPage(String pageName) {
    return false;
  }

  @Override
  public VersionInfo commit(PageData data) {
    return null;
  }

  @Override
  public Collection<VersionInfo> getVersions() {
    return Collections.emptySet();
  }

  @Override
  public PageData getDataVersion(String versionName) {
    return null;
  }

  private PageData makePageData() {
    PageData pageData = new PageData(this);
    String content;
    try {
      content = fileSystem.getContent(path);
    } catch (IOException e) {
      throw new RuntimeException("Unable to fetch page content", e);
    }
    pageData.setContent("!-" + content + "-!");
    pageData.removeAttribute(PageData.PropertyEDIT);
    pageData.removeAttribute(PageData.PropertyPROPERTIES);
    pageData.removeAttribute(PageData.PropertyVERSIONS);
    pageData.removeAttribute(PageData.PropertyREFACTOR);
    if (content.contains("<table")) {
      pageData.setAttribute(PageType.TEST.toString(), Boolean.toString(true));
    }
    return pageData;
  }

}
