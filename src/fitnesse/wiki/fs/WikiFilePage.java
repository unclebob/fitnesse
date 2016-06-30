package fitnesse.wiki.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import fitnesse.wiki.*;
import fitnesse.wikitext.parser.*;
import util.FileUtil;

import static fitnesse.wiki.fs.FileSystemPage.propertiesFilename;

/**
 * With this page all content is saved in one file: WikiPageName.wiki.
 * Sub wiki's are stored as WikiPageMame/SubWiki.wiki.
 * This format should eventually replace the {@link FileSystemPage}.
 */
public class WikiFilePage extends BaseWikitextPage implements FileBasedWikiPage {

  private static final SymbolProvider WIKI_FILE_PARSING_PROVIDER = new SymbolProvider( new SymbolType[] {
    FrontMatter.symbolType, SymbolType.Text});

  private final File path;
  private final VersionsController versionsController;
  private final SubWikiPageFactory subWikiPageFactory;
  private final String versionName;
  private PageData pageData;

  protected WikiFilePage(final File path, final String name, final WikiPage parent,
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
    final WikiPage childPage = getChildPage(name);
    if (childPage != null) {
      childPage.remove();
    }
  }

  @Override
  public void remove() {
    super.remove();
  }

  @Override
  public List<WikiPage> getChildren() {
    return subWikiPageFactory.getChildren(this);
  }

  @Override
  public PageData getData() {
    if (pageData == null) {
      try {
        pageData = getDataVersion();
      } catch (IOException e) {
        throw new WikiPageLoadException("Could not load page data for page " + path.getPath(), e);
      }
    }
    return new PageData(pageData);
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

  private PageData getDataVersion() throws IOException {
    FileVersion[] versions = versionsController.getRevisionData(versionName, wikiFile());
    FileVersion fileVersion = versions[0];
    String content = "";
    WikiPageProperty properties = null;

    try {
      content = loadContent(fileVersion);

      final ParsingPage parsingPage = makeParsingPage(this);
      final Symbol syntaxTree = Parser.make(parsingPage, content, WIKI_FILE_PARSING_PROVIDER).parse();
      if (syntaxTree.getChildren().size() == 2) {
        final Symbol maybeFrontMatter = syntaxTree.getChildren().get(0);
        final Symbol maybeContent = syntaxTree.getChildren().get(1);
        if (maybeFrontMatter.isType(FrontMatter.symbolType)) {
          properties = mergeWikiPageProperties(defaultPageProperties(), toWikiPageProperties(maybeFrontMatter));
          content = maybeContent.getContent();
        }
      }
    } catch (IOException e) {
      throw new WikiPageLoadException(e);
    }

    if (properties == null) {
      properties = defaultPageProperties();
    }
    return new PageData(content, properties);
  }

  private WikiPageProperty mergeWikiPageProperties(final WikiPageProperty properties, final WikiPageProperty updates) {
    return properties;
  }

  private WikiPageProperty toWikiPageProperties(final Symbol frontMatter) {
    return new WikiPageProperty();
  }

  private File wikiFile() {
    return new File(getFileSystemPath().getPath() + ".wiki");
  }

  private String loadContent(final FileVersion fileVersion) throws IOException {
    try (InputStream content = fileVersion.getContent()) {
      return FileUtil.toString(content);
    }
  }

  }
