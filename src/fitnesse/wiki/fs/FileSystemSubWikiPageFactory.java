package fitnesse.wiki.fs;

import java.io.File;
import java.util.*;

import fitnesse.util.StringUtils;
import fitnesse.wiki.*;
import fitnesse.wikitext.parser.VariableSource;

class FileSystemSubWikiPageFactory implements SubWikiPageFactory {

  private final File rootPath;
  private final FileSystem fileSystem;
  private final VariableSource variableSource;
  private final WikiPageFactory factory;

  public FileSystemSubWikiPageFactory(File rootPath, FileSystem fileSystem, VariableSource variableSource,
                                      WikiPageFactory factory) {
    this.rootPath = rootPath;
    this.fileSystem = fileSystem;
    this.variableSource = variableSource;
    this.factory = factory;
  }

  @Override
  public List<WikiPage> getChildren(FileBasedWikiPage page) {
    List<WikiPage> children = new ArrayList<>();
    children.addAll(getNormalChildren(page));
    children.addAll(getSymlinkChildren(page));
    return children;
  }

  private Set<WikiPage> getNormalChildren(FileBasedWikiPage page) {
    final File thisDir = page.getFileSystemPath();
    final Set<WikiPage> children = new TreeSet<>();
    if (fileSystem.exists(thisDir)) {
      final String[] subFiles = fileSystem.list(thisDir);
      for (String subFile : subFiles) {
        WikiPage maybeChildPage = getChildPage(page, subFile);
        if (maybeChildPage != null) {
          children.add(maybeChildPage);
        }
      }
    }
    return children;
  }

  private List<WikiPage> getSymlinkChildren(WikiPage page) {
    List<WikiPage> children = new LinkedList<>();
    WikiPageProperty props = page.getData().getProperties();
    WikiPageProperty symLinksProperty = props.getProperty(SymbolicPage.PROPERTY_NAME);
    if (symLinksProperty != null) {
      for (String linkName : symLinksProperty.keySet()) {
        WikiPage linkedPage = createSymbolicPage(page, linkName);
        if (linkedPage != null && !children.contains(linkedPage))
          children.add(linkedPage);
      }
    }
    return children;
  }

  @Override
  public WikiPage getChildPage(FileBasedWikiPage page, String childName) {
    File parent = page.getFileSystemPath();

    WikiPage childPage = makeChildPage(new File(parent, childName), childName, page);
    if (childPage == null) {
      childPage = createSymbolicPage(page, childName);
    }
    return childPage;
  }

  private WikiPage makeChildPage(File path, String childName, FileBasedWikiPage parent) {
     return factory.makePage(path, childName, parent, variableSource);
  }

  private WikiPage createSymbolicPage(WikiPage page, String linkName) {
    WikiPageProperty symLinkProperty = page.getData().getProperties().getProperty(SymbolicPage.PROPERTY_NAME);
    if (symLinkProperty == null)
      return null;
    String linkPath = symLinkProperty.get(linkName);
    if (linkPath == null)
      return null;

    if (linkPath.startsWith("file:"))
      return createExternalSymbolicLink(linkPath, linkName, page);
    else
      return createInternalSymbolicPage(linkPath, linkName, page);
  }

  private WikiPage createExternalSymbolicLink(String linkPath, String linkName, WikiPage parent) {
    String fullPagePath = new VariableTool(variableSource).replace(linkPath);
    File file = WikiPageUtil.resolveFileUri(fullPagePath, rootPath);
    File parentDirectory = file.getParentFile();
    if (fileSystem.exists(parentDirectory)) {
      WikiPage externalRoot = makeChildPage(file, file.getName(), null);
      if (externalRoot != null) {
        return new SymbolicPage(linkName, externalRoot, parent);
      }
    }
    return null;
  }

  protected WikiPage createInternalSymbolicPage(String linkPath, String linkName, WikiPage parent) {
    WikiPagePath path = PathParser.parse(linkPath);
    if (path == null) {
      return null;
    }
    WikiPage start = (path.isRelativePath()) ? parent.getParent() : parent;  //TODO -AcD- a better way?
    WikiPage wikiPage = start.getPageCrawler().getPage(path);
    if (wikiPage != null)
      wikiPage = new SymbolicPage(linkName, wikiPage, parent);
    return wikiPage;
  }
}
