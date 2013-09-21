package fitnesse.wiki.fs;

import fitnesse.components.ComponentFactory;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;

import java.util.Properties;

public class FileSystemPageFactory implements WikiPageFactory {
  private FileSystem fileSystem;
  private VersionsController versionsController;

  public FileSystemPageFactory() {
    fileSystem = new DiskFileSystem();
    versionsController = new ZipFileVersionsController();
  }

  public FileSystemPageFactory(Properties properties) {
    fileSystem = new DiskFileSystem();
    versionsController = (VersionsController) new ComponentFactory(properties).createComponent(
            ComponentFactory.VERSIONS_CONTROLLER_CLASS, ZipFileVersionsController.class);
    versionsController.setHistoryDepth(Integer.parseInt(properties.getProperty(ComponentFactory.VERSIONS_CONTROLLER_DAYS, "14")));
  }

  public FileSystemPageFactory(FileSystem fileSystem, VersionsController versionsController) {
    this.fileSystem = fileSystem;
    this.versionsController = versionsController;
  }

  @Override
  // TODO: RootPath should be a File()?
  public WikiPage makeRootPage(String rootPath, String rootPageName) {
    return new FileSystemPage(rootPath, rootPageName, fileSystem, versionsController);
  }

  VersionsController getVersionsController() {
    return versionsController;
  }
}
