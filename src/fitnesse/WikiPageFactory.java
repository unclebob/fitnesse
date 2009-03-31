package fitnesse;

import fitnesse.wiki.WikiPage;
import fitnesse.wiki.FileSystemPage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Properties;

public class WikiPageFactory {
  private Class<?> wikiPageClass = FileSystemPage.class;

  public WikiPage makeRootPage(String rootPath, String rootPageName, ComponentFactory componentFactory) throws Exception {
    try {
      Constructor<?> constructorMethod = wikiPageClass.getConstructor(String.class, String.class, ComponentFactory.class);
      return (WikiPage) constructorMethod.newInstance(rootPath, rootPageName, componentFactory);
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
