package fitnesse.wiki.fs;

import fitnesse.ConfigurationParameter;
import fitnesse.components.ComponentFactory;
import fitnesse.wiki.BaseWikiPage;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wiki.WikiPageFactoryRegistry;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wiki.WikiPageProperty;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wikitext.parser.VariableSource;
import fitnesse.wiki.VariableTool;
import fitnesse.wikitext.parser.Maybe;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class FileSystemPageFactory implements WikiPageFactory<FileSystemPage>, WikiPageFactoryRegistry {
  private final FileSystem fileSystem;
  private final VersionsController versionsController;
  private final VariableSource variableSource;
  private final List<WikiPageFactory> wikiPageFactories = new ArrayList<WikiPageFactory>();

  public FileSystemPageFactory() {
    fileSystem = new DiskFileSystem();
    versionsController = new ZipFileVersionsController();
    variableSource = new SystemVariableSource(new Properties());
    initializeWikiPageFactories();
  }

  public FileSystemPageFactory(Properties properties) {
    fileSystem = new DiskFileSystem();
    versionsController = (VersionsController) new ComponentFactory(properties).createComponent(
            ConfigurationParameter.VERSIONS_CONTROLLER_CLASS, ZipFileVersionsController.class);
    versionsController.setHistoryDepth(Integer.parseInt(properties.getProperty(ConfigurationParameter.VERSIONS_CONTROLLER_DAYS.getKey(), "14")));
    variableSource = new SystemVariableSource(properties);
    initializeWikiPageFactories();
  }

  public FileSystemPageFactory(FileSystem fileSystem, VersionsController versionsController, VariableSource variableSource) {
    this.fileSystem = fileSystem;
    this.versionsController = versionsController;
    this.variableSource = variableSource;
    initializeWikiPageFactories();
  }

  private void initializeWikiPageFactories() {
    registerWikiPageFactory(this);
    registerWikiPageFactory(new ExternalSuitePageFactory(fileSystem, variableSource));
  }

  @Override
  public void registerWikiPageFactory(WikiPageFactory wikiPageFactory) {
    wikiPageFactories.add(wikiPageFactory);
  }

  @Override
  public boolean supports(File path) {
    for (String child : fileSystem.list(path)) {
      if (child.equals(FileSystemPage.contentFilename)) return true;
    }
    return false;
  }

  @Override
  public FileSystemPage makePage(File path, String pageName, FileSystemPage parent) {
    if (parent != null) {
      return new FileSystemPage(path, pageName, parent);
    } else {
      Maybe<String> rootPath = variableSource.findVariable("FITNESSE_ROOTPATH");
      return new FileSystemPage(path, pageName, versionsController, new FileSystemSubWikiPageFactory(new File(rootPath.getValue())), variableSource);
    }
  }

  VersionsController getVersionsController() {
    return versionsController;
  }

  private WikiPage makeChildPage(File path, String childName, FileSystemPage page) {
    for (WikiPageFactory factory : wikiPageFactories) {
      if (factory.supports(path)) {
        return factory.makePage(path, childName, page);
      }
    }
    // Fall back:
    if (fileIsValid(path)) {
      // Empty directories that have Wiki format are considered pages as well.
      return makePage(path, childName, page);
    }
    return null;
  }

  private boolean fileIsValid(final File path) {
    return fileSystem.isDirectory(path) && PathParser.isSingleWikiWord(path.getName());
  }

  protected class FileSystemSubWikiPageFactory implements SubWikiPageFactory {

    private final File rootPath;

    public FileSystemSubWikiPageFactory(File rootPath) {
      this.rootPath = rootPath;
    }

    public List<WikiPage> getChildren(FileSystemPage page) {
      List<WikiPage> children = getNormalChildren(page);
      children.addAll(getSymlinkChildren(page));
      return children;
    }

    private List<WikiPage> getNormalChildren(FileSystemPage page) {
      final File thisDir = page.getFileSystemPath();
      final List<WikiPage> children = new LinkedList<WikiPage>();
      if (fileSystem.exists(thisDir)) {
        final String[] subFiles = fileSystem.list(thisDir);
        for (final String subFile : subFiles) {
          if (fileIsValid(new File(thisDir, subFile))) {
            children.add(getChildPage(page, subFile));
          }
        }
      }
      return children;
    }

    protected List<WikiPage> getSymlinkChildren(BaseWikiPage page) {
      List<WikiPage> children = new LinkedList<WikiPage>();
      WikiPageProperties props = page.getData().getProperties();
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
    public WikiPage getChildPage(FileSystemPage page, String childName) {
      File parent = page.getFileSystemPath();

      WikiPage childPage = makeChildPage(new File(parent, childName), childName, page);
      if (childPage == null) {
        childPage = createSymbolicPage(page, childName);
      }
      return childPage;
    }

    private WikiPage createSymbolicPage(BaseWikiPage page, String linkName) {
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

    private WikiPage createExternalSymbolicLink(String linkPath, String linkName, BaseWikiPage parent) {
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

    protected WikiPage createInternalSymbolicPage(String linkPath, String linkName, BaseWikiPage parent) {
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

}
