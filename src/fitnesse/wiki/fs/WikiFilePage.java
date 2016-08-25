package fitnesse.wiki.fs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import org.apache.commons.lang.StringUtils;

import fitnesse.wiki.*;
import fitnesse.wikitext.parser.*;
import util.FileUtil;

import static fitnesse.util.StringUtils.isBlank;
import static java.lang.String.format;

/**
 * With this page all content is saved in one file: WikiPageName.wiki.
 * Sub wiki's are stored as WikiPageMame/SubWiki.wiki.
 * This format should eventually replace the {@link FileSystemPage}.
 */
public class WikiFilePage extends BaseWikitextPage implements FileBasedWikiPage {
  public static final String FILE_EXTENSION = ".wiki";
  public static final String ROOT_FILE_NAME = "_root" + FILE_EXTENSION;
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

  private WikiFilePage(WikiFilePage page, String versionName) {
    this(page.path, page.getName(), (page.isRoot() ? null : page.getParent()), versionName,
      page.versionsController, page.subWikiPageFactory, page.getVariableSource());
  }

  @Override
  public WikiPage addChildPage(final String childName) {
    return new WikiFilePage(new File(getFileSystemPath(), childName + FILE_EXTENSION), childName, this, null, this.versionsController, this.subWikiPageFactory, this.getVariableSource());
  }

  private File getSubWikiFolder() {
    return path;
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
      versionsController.delete(path, getFileSystemPath());
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
    return versionsController.history(path);
  }

  @Override
  public WikiPage getVersion(final String versionName) {
    try {
      versionsController.getRevisionData(versionName, path);
    } catch (IOException e) {
      throw new WikiPageLoadException(format("Could not load version %s for page at %s", versionName, path.getPath()), e);
    }
    return new WikiFilePage(this, versionName);
  }

  @Override
  public VersionInfo commit(final PageData data) {
    resetCache();
    try {
      return versionsController.makeVersion(new WikiFilePageVersion(data));
    } catch (IOException e) {
      throw new WikiPageLoadException(e);
    }
  }

  @Override
  public File getFileSystemPath() {
    if (ROOT_FILE_NAME.equals(path.getName())) {
      return path.getParentFile();
    } else {
      String pathStr = this.path.getPath();
      return new File(pathStr.substring(0, pathStr.length() - FILE_EXTENSION.length()));
    }
  }

  @Override
  protected void resetCache() {
    super.resetCache();
    pageData = null;
  }

  private PageData getDataVersion() throws IOException {
    FileVersion[] versions = versionsController.getRevisionData(versionName, path);
    FileVersion fileVersion = versions[0];
    String content = "";
    WikiPageProperty properties = defaultPageProperties();

    if (fileVersion != null) {
      try {
        String fileContent = loadContent(fileVersion);

        final ParsingPage parsingPage = makeParsingPage(this);
        final Symbol syntaxTree = Parser.make(parsingPage, fileContent, WIKI_FILE_PARSING_PROVIDER).parse();
        if (!syntaxTree.getChildren().isEmpty()) {
          final Symbol maybeFrontMatter = syntaxTree.getChildren().get(0);
          if (maybeFrontMatter.isType(FrontMatter.symbolType)) {
            properties = mergeWikiPageProperties(properties, maybeFrontMatter);
            if (syntaxTree.getChildren().size() > 1) {
              content = fileContent.substring(maybeFrontMatter.getEndOffset());
            }
          } else {
            content = fileContent;
          }
        }
        properties.setLastModificationTime(fileVersion.getLastModificationTime());

      } catch (IOException e) {
        throw new WikiPageLoadException(e);
      }
    }
    pageData = new PageData(content, properties);
    return pageData;
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
        } else {
          WikiPageProperty symLinks = properties.set(key, value);
          for (int i = 2; i < keyValue.getChildren().size(); i++) {
            final Symbol subProperty = keyValue.getChildren().get(i);
            assert subProperty.isType(FrontMatter.keyValueSymbolType);
            String linkName = subProperty.getChildren().get(0).getContent();
            String linkPath = subProperty.getChildren().get(1).getContent();
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

  private String loadContent(final FileVersion fileVersion) throws IOException {
    try (InputStream content = fileVersion.getContent()) {
      return FileUtil.toString(content);
    }
  }

  private String propertiesYaml(final WikiPageProperty pageProperties) {
    final WikiPageProperty defaultProperties = defaultPageProperties();
    final List<String> lines = new ArrayList<>();
    for (String key : pageProperties.keySet()) {
      if (isBooleanProperty(key)) {
        if (!defaultProperties.has(key)) {
          lines.add(key);
        }
      } else if (!WikiPageProperty.LAST_MODIFIED.equals(key)) {
        final StringBuilder builder = new StringBuilder();
        builder.append(key);
        if (!isBlank(pageProperties.get(key))) {
          builder.append(": ").append(pageProperties.get(key));
        }
        final WikiPageProperty subProperty = pageProperties.getProperty(key);
        for (String pageName: subProperty.keySet()) {
          builder.append("\n  ").append(pageName).append(": ").append(subProperty.get(pageName));
        }
        lines.add(builder.toString());
      }
    }
    for (String key : defaultProperties.keySet()) {
        if (isBooleanProperty(key) && !pageProperties.has(key)) {
          lines.add(key + ": no");
        }
    }
    Collections.sort(lines);
    return lines.isEmpty() ? "" : "---\n" + StringUtils.join(lines, '\n') + "\n---\n";
  }

  private class WikiFilePageVersion implements FileVersion {
    private final PageData data;

    public WikiFilePageVersion(final PageData data) {
      this.data = new PageData(data);
    }

    @Override
    public File getFile() {
      return path;
    }

    @Override
    public InputStream getContent() throws IOException {
      String yaml = propertiesYaml(data.getProperties());
      final String content = yaml + data.getContent();
      return new ByteArrayInputStream(content.getBytes(FileUtil.CHARENCODING));
    }

    @Override
    public String getAuthor() {
      return data.getAttribute(WikiPageProperty.LAST_MODIFYING_USER);
    }

    @Override
    public Date getLastModificationTime() {
      return data.getProperties().getLastModificationTime();
    }
  }


}
