package fitnesse.wiki.fs;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fitnesse.util.Clock;
import fitnesse.wiki.*;
import fitnesse.wikitext.parser.VariableSource;

public class ExternalTestPage extends BaseWikitextPage {
  private FileSystem fileSystem;
  private File path;

  public ExternalTestPage(File path, String name, WikiPage parent, FileSystem fileSystem, VariableSource variableSource) {
    super(name, parent, variableSource);
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
  public List<WikiPage> getChildren() {
    return Collections.emptyList();
  }

  @Override
  public WikiPage getChildPage(String name) {
    return null;
  }

  @Override
  public WikiPage addChildPage(String name) {
    return null;
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
  public WikiPage getVersion(String versionName) {
    return null;
  }

  @Override
  public String getHtml() {
    try {
      return fileSystem.getContent(path);
    } catch (IOException e) {
      throw new WikiPageLoadException("Unable to fetch page content", e);
    }
  }

  private PageData makePageData() {
    String content;
    try {
      content = fileSystem.getContent(path);
    } catch (IOException e) {
      throw new WikiPageLoadException("Unable to fetch page content", e);
    }

    WikiPageProperties properties = new WikiPageProperties();
    if (content.contains("<table")) {
      properties.set(PageType.TEST.toString());
    }
    properties.set(PageData.PropertyWHERE_USED);
    properties.set(PageData.PropertyRECENT_CHANGES);
    properties.set(PageData.PropertyFILES);
    properties.set(PageData.PropertyVERSIONS);
    properties.set(PageData.PropertySEARCH);
    properties.setLastModificationTime(Clock.currentDate());
    return new PageData("!-" + content + "-!", properties);
  }

}
