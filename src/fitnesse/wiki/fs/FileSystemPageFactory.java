package fitnesse.wiki.fs;

import fitnesse.ConfigurationParameter;
import fitnesse.components.ComponentFactory;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.SymbolicPage;
import fitnesse.wiki.SystemVariableSource;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wiki.WikiPagePath;
import fitnesse.wiki.WikiPageProperties;
import fitnesse.wiki.WikiPageProperty;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wikitext.parser.VariableSource;
import fitnesse.wiki.VariableTool;
import fitnesse.wikitext.parser.WikiWordPath;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class FileSystemPageFactory implements WikiPageFactory {
  private final FileSystem fileSystem;
  private final VersionsController versionsController;
  private final VariableSource variableSource;

  public FileSystemPageFactory() {
    fileSystem = new DiskFileSystem();
    versionsController = new ZipFileVersionsController();
    variableSource = new SystemVariableSource(new Properties());
  }

  public FileSystemPageFactory(Properties properties) {
    fileSystem = new DiskFileSystem();
    versionsController = (VersionsController) new ComponentFactory(properties).createComponent(
            ConfigurationParameter.VERSIONS_CONTROLLER_CLASS, ZipFileVersionsController.class);
    versionsController.setHistoryDepth(Integer.parseInt(properties.getProperty(ConfigurationParameter.VERSIONS_CONTROLLER_DAYS.getKey(), "14")));
    variableSource = new SystemVariableSource(properties);
  }

  public FileSystemPageFactory(FileSystem fileSystem, VersionsController versionsController, VariableSource variableSource) {
    this.fileSystem = fileSystem;
    this.versionsController = versionsController;
    this.variableSource = variableSource;
  }

  @Override
  // TODO: RootPath should be a File?
  public FileSystemPage makeRootPage(String rootPath, String rootPageName) {
    return new FileSystemPage(rootPath, rootPageName, versionsController, new FileSystemSubWikiPageFactory(rootPath != null ? new File(rootPath) : null), variableSource);
  }

  VersionsController getVersionsController() {
    return versionsController;
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
      final File thisDir = new File(page.getFileSystemPath());
      final List<WikiPage> children = new LinkedList<WikiPage>();
      if (fileSystem.exists(thisDir)) {
        final String[] subFiles = fileSystem.list(thisDir);
        for (final String subFile : subFiles) {
          if (fileIsValid(subFile, thisDir)) {
            children.add(getChildPage(page, subFile));
          }
        }
      }
      return children;
    }

    protected List<WikiPage> getSymlinkChildren(WikiPage page) {
      List<WikiPage> children = new LinkedList<WikiPage>();
      WikiPageProperties props = page.getData().getProperties();
      WikiPageProperty symLinksProperty = props.getProperty(SymbolicPage.PROPERTY_NAME);
      if (symLinksProperty != null) {
        for (String linkName : symLinksProperty.keySet()) {
          WikiPage linkedPage = createSymbolicPage(page, symLinksProperty, linkName);
          if (linkedPage != null && !children.contains(linkedPage))
            children.add(linkedPage);
        }
      }
      return children;
    }

    @Override
    public WikiPage getChildPage(FileSystemPage page, String childName) {
      final File file = new File(page.getFileSystemPath(), childName);
      if (hasContentChild(file)) {
        return new FileSystemPage(childName, page);
      } else if (hasHtmlChild(file)) {
        return new ExternalSuitePage(file, childName, page, fileSystem);
      } else if (fileIsValid(childName, new File(page.getFileSystemPath()))) {
        // Empty directories that have Wiki format are considered pages as well.
        return new FileSystemPage(childName, page);
      } else {
        return createSymbolicPage(page, page.readOnlyData().getProperties().getProperty(SymbolicPage.PROPERTY_NAME), childName);
      }
    }

    private boolean hasContentChild(File path) {
      for (String child : fileSystem.list(path)) {
        if (child.equals("content.txt")) return true;
      }
      return false;
    }

    private boolean hasHtmlChild(File path) {
      if (path.getName().endsWith(".html")) return true;
      for (String child : fileSystem.list(path)) {
        if (hasHtmlChild(new File(path, child))) return true;
      }
      return false;
    }

    private boolean fileIsValid(final String filename, final File dir) {
      return WikiWordPath.isWikiWord(filename) && fileSystem.exists(new File(dir, filename));
    }

    private WikiPage createSymbolicPage(WikiPage page, WikiPageProperty symLinkProperty, String linkName) {
      if (symLinkProperty == null)
        return null;
      String linkPath = symLinkProperty.get(linkName);
      if (linkPath == null)
        return null;
      return makePage(linkPath, linkName, page);
    }

    public WikiPage makePage(String linkPath, String linkName, WikiPage parent) {
      if (linkPath.startsWith("file:"))
        return createExternalSymbolicLink(linkPath, linkName, parent);
      else
        return createInternalSymbolicPage(linkPath, linkName, parent);
    }

    private WikiPage createExternalSymbolicLink(String linkPath, String linkName, WikiPage parent) {
      // And this:
      String fullPagePath = new VariableTool(variableSource).replace(linkPath);
      File file = WikiPageUtil.resolveFileUri(fullPagePath, rootPath);
      File parentDirectory = file.getParentFile();
      if (parentDirectory.exists()) {
        if (file.isDirectory()) {
          WikiPage externalRoot = FileSystemPageFactory.this.makeRootPage(parentDirectory.getPath(), file.getName());
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

}
