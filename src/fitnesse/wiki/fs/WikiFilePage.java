package fitnesse.wiki.fs;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import javax.xml.crypto.dsig.keyinfo.KeyValue;

import fitnesse.wiki.*;
import fitnesse.wikitext.parser.*;
import util.FileUtil;

import static fitnesse.util.StringUtils.isBlank;
import static fitnesse.wiki.fs.FileSystemPage.propertiesFilename;
import static java.lang.String.format;

/**
 * With this page all content is saved in one file: WikiPageName.wiki.
 * Sub wiki's are stored as WikiPageMame/SubWiki.wiki.
 * This format should eventually replace the {@link FileSystemPage}.
 */
public class WikiFilePage extends BaseWikitextPage implements FileBasedWikiPage {
  public static final String FILE_EXTENSION = ".wiki";
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
    try {
      versionsController.delete(getFileSystemPath(), wikiFile());
    } catch (IOException e) {
      throw new WikiPageLoadException(format("Could not remove page %s", new WikiPagePath(this).toString()), e);
    }
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
          properties = mergeWikiPageProperties(defaultPageProperties(), maybeFrontMatter);
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

  private WikiPageProperty mergeWikiPageProperties(final WikiPageProperty properties, final Symbol frontMatter) {
    for (Symbol keyValue : frontMatter.getChildren()) {
      if (keyValue.isType(FrontMatter.keyValueSymbolType)) {
        String key = keyValue.getChildren().get(0).getContent();
        String value = keyValue.getChildren().get(1).getContent();
        if (isBooleanProperty(key)) {
          if (isBlank(value) || isTruthy(value)) {
            properties.set(key, value);
          } else if (isFalsy(value)) {
            properties.remove(key);
          }
        } else if (WikiPageProperty.HELP.equals(key)) {
          properties.set(key, value);
        } else if (SymbolicPage.PROPERTY_NAME.equals(key)) {
          WikiPageProperty symLinks = properties.set(SymbolicPage.PROPERTY_NAME);
          for (int i = 2; i < keyValue.getChildren().size(); i++) {
            final Symbol symLink = keyValue.getChildren().get(i);
            assert symLink.isType(FrontMatter.keyValueSymbolType);
            String linkName = symLink.getChildren().get(0).getContent();
            String linkPath = symLink.getChildren().get(1).getContent();
            symLinks.set(linkName, linkPath);
          }
        }
      }
    }
    return properties;
  }

  private boolean isBooleanProperty(final String key) {
    return qualifiesAs(key, PageData.PAGE_TYPE_ATTRIBUTES) ||
      qualifiesAs(key, PageData.NON_SECURITY_ATTRIBUTES) ||
      qualifiesAs(key, PageData.SECURITY_ATTRIBUTES);
  }

  private boolean isTruthy(final String value) {
    return qualifiesAs(value.toLowerCase(), new String[]{"y", "yes", "t", "true", "1"});
  }

  private boolean isFalsy(final String value) {
    return qualifiesAs(value.toLowerCase(), new String[]{"n", "no", "f", "false", "0"});
  }

  private boolean qualifiesAs(final String value, final String[] qualifiers) {
    for (String q : qualifiers) {
      if (q.equals(value)) return true;
    }
    return false;
  }

  private File wikiFile() {
    return new File(getFileSystemPath().getPath() + FILE_EXTENSION);
  }

  private String loadContent(final FileVersion fileVersion) throws IOException {
    try (InputStream content = fileVersion.getContent()) {
      return FileUtil.toString(content);
    }
  }

}
