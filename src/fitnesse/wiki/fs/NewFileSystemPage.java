package fitnesse.wiki.fs;

import java.io.File;
import java.util.Collection;
import java.util.List;

import fitnesse.wiki.BaseWikitextPage;
import fitnesse.wiki.PageData;
import fitnesse.wiki.VersionInfo;
import fitnesse.wiki.WikiPage;
import fitnesse.wikitext.parser.VariableSource;

public class NewFileSystemPage extends BaseWikitextPage implements FileBasedWikiPage {

  private final File path;
  private final VersionsController versionsController;
  private final SubWikiPageFactory subWikiPageFactory;
  private final String versionName;

  public NewFileSystemPage(final File path, final String name, final WikiPage parent,
                           final String versionName, final VersionsController versionsController,
                           final SubWikiPageFactory subWikiPageFactory, final VariableSource variableSource) {
    super(name, parent, variableSource);
    this.path = path;
    this.versionsController = versionsController;
    this.subWikiPageFactory = subWikiPageFactory;
    this.versionName = versionName;
  }

  @Override
  public WikiPage addChildPage(final String childName) {
    return null;
  }

  private File getSubWikiFolder() {
    return path;
  }

  @Override
  public boolean hasChildPage(final String childName) {
    return false;
  }

  @Override
  public WikiPage getChildPage(final String childName) {
    return subWikiPageFactory.getChildPage(this, childName);
  }

  @Override
  public void removeChildPage(final String name) {

  }

  @Override
  public List<WikiPage> getChildren() {
    return null;
  }

  @Override
  public PageData getData() {
    return null;
  }

  @Override
  public Collection<VersionInfo> getVersions() {
    return null;
  }

  @Override
  public WikiPage getVersion(final String versionName) {
    return null;
  }

  @Override
  public VersionInfo commit(final PageData data) {
    return null;
  }

  @Override
  public File getFileSystemPath() {
    return path;
  }
}
