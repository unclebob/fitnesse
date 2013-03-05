package fitnesse;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Properties;

import fitnesse.wiki.FileSystemPage;
import fitnesse.wiki.VersionsController;
import fitnesse.wiki.WikiPage;
import util.DiskFileSystem;
import util.FileSystem;

public class WikiPageFactory {
  private Class<?> wikiPageClass = FileSystemPage.class;
  private FileSystem fileSystem;

  public WikiPageFactory() {
    this(new DiskFileSystem());
  }

  public WikiPageFactory(FileSystem fileSystem) {
    this.fileSystem = fileSystem;
  }

  public WikiPage makeRootPage(String rootPath, String rootPageName, ComponentFactory componentFactory) throws Exception {
    try {
      Constructor<?> constructorMethod = wikiPageClass.getConstructor(String.class, String.class, FileSystem.class, VersionsController.class);
      return (WikiPage) constructorMethod.newInstance(rootPath, rootPageName, fileSystem, componentFactory.getVersionsController());
    } catch (NoSuchMethodException e) {
      Method makeRootMethod = wikiPageClass.getMethod("makeRoot", Properties.class);
      return (WikiPage) makeRootMethod.invoke(wikiPageClass, componentFactory.getProperties());
    }
  }

  public Class<?> getWikiPageClass() {
    return wikiPageClass;
  }

  public void setWikiPageClass(Class<?> wikiPageClass) {
    this.wikiPageClass = wikiPageClass;
  }
}
