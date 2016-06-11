package fitnesse.wiki.fs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import fitnesse.ConfigurationParameter;
import fitnesse.components.ComponentFactory;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wiki.WikiPageFactoryRegistry;
import fitnesse.wikitext.parser.Maybe;
import fitnesse.wikitext.parser.VariableSource;

public class FileSystemPageFactory implements WikiPageFactory<WikiPage>, WikiPageFactoryRegistry {
  private final FileSystem fileSystem;
  private final VersionsController versionsController;
  private final List<WikiPageFactory> wikiPageFactories = new ArrayList<>();
  private final InnerFileSystemPageFactory innerFileSystemPageFactory = new InnerFileSystemPageFactory();

  public FileSystemPageFactory() {
    fileSystem = new DiskFileSystem();
    versionsController = new ZipFileVersionsController();
    initializeWikiPageFactories();
  }

  public FileSystemPageFactory(Properties properties) {
    fileSystem = new DiskFileSystem();
    versionsController = new ComponentFactory(properties).createComponent(
            ConfigurationParameter.VERSIONS_CONTROLLER_CLASS, ZipFileVersionsController.class);
    initializeWikiPageFactories();
  }

  public FileSystemPageFactory(FileSystem fileSystem, VersionsController versionsController) {
    this.fileSystem = fileSystem;
    this.versionsController = versionsController;
    initializeWikiPageFactories();
  }

  private void initializeWikiPageFactories() {
    registerWikiPageFactory(innerFileSystemPageFactory);
    registerWikiPageFactory(new NewStyleFileSystemPageFactory());
    // Note: ExternalSuitePageFactory should be last in line: it traverses the remainder of the tree looking for .html files.
    registerWikiPageFactory(new ExternalSuitePageFactory(fileSystem));
  }

  @Override // from WikiPageFactoryRegistry
  public void registerWikiPageFactory(WikiPageFactory wikiPageFactory) {
    wikiPageFactories.add(wikiPageFactory);
  }

  @Override // from WikiPageFactory
  public boolean supports(File path) {
    for (WikiPageFactory factory : wikiPageFactories) {
      if (factory.supports(path)) {
        return true;
      }
    }
    return false;
  }

  @Override // from WikiPageFactory
  public WikiPage makePage(File path, String pageName, WikiPage parent, VariableSource variableSource) {
    if (parent != null) {
      for (WikiPageFactory factory : wikiPageFactories) {
        if (factory.supports(path)) {
          return factory.makePage(path, pageName, parent, variableSource);
        }
      }
    }
    if (parent == null || (parent instanceof FileSystemPage && fileIsValid(path)))
      return innerFileSystemPageFactory.makePage(path, pageName, (FileSystemPage) parent, variableSource);
    return null;
  }

  private boolean fileIsValid(final File path) {
    return fileSystem.isDirectory(path) && PathParser.isSingleWikiWord(path.getName());
  }

  VersionsController getVersionsController() {
    return versionsController;
  }

  /**
   * This is the class that does the sole handling of FileSystemPages
   */
  protected class InnerFileSystemPageFactory implements WikiPageFactory<FileSystemPage> {

    @Override
    public WikiPage makePage(final File path, final String pageName, final FileSystemPage parent, final VariableSource variableSource) {
      if (parent != null) {
        return new FileSystemPage(path, pageName, parent);
      } else {
        // Initialize fitnesse root path:
        Maybe<String> rootPath = variableSource.findVariable("FITNESSE_ROOTPATH");
        return new FileSystemPage(path, pageName, versionsController,
          new FileSystemSubWikiPageFactory(new File(rootPath.getValue()), fileSystem, variableSource, FileSystemPageFactory.this),
          variableSource);
      }
    }

    @Override
    public boolean supports(File path) {
      String[] children = fileSystem.list(path);
      for (String child : children) {
        if (child.equals(FileSystemPage.contentFilename)) return true;
      }
      return false;
    }
  }

  protected class NewStyleFileSystemPageFactory implements WikiPageFactory<WikiPage> {
    @Override
    public WikiPage makePage(final File path, final String pageName, final WikiPage parent, final VariableSource variableSource) {
      Maybe<String> rootPath = variableSource.findVariable("FITNESSE_ROOTPATH");
      return new NewFileSystemPage(path, pageName, parent, null, versionsController,
        new FileSystemSubWikiPageFactory(new File(rootPath.getValue()), fileSystem, variableSource, FileSystemPageFactory.this),
        variableSource);
    }

    @Override
    public boolean supports(File path) {
      File wikiFile = new File(path.getPath() + ".wiki");
      if (fileSystem.exists(wikiFile) && !fileSystem.isDirectory(wikiFile)) return true;
      return false;
    }
  }
}
