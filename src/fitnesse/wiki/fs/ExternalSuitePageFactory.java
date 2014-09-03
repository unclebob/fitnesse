package fitnesse.wiki.fs;

import java.io.File;

import fitnesse.wiki.BaseWikiPage;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageFactory;
import fitnesse.wikitext.parser.VariableSource;

public class ExternalSuitePageFactory implements WikiPageFactory<BaseWikiPage> {

  private final FileSystem fileSystem;
  private final VariableSource variableSource;

  public ExternalSuitePageFactory(FileSystem fileSystem, VariableSource variableSource) {
    this.fileSystem = fileSystem;
    this.variableSource = variableSource;
  }

  @Override
  public WikiPage makePage(File path, String pageName, BaseWikiPage parent) {
    return new ExternalSuitePage(path, pageName, parent, fileSystem, variableSource);
  }

  @Override
  public boolean supports(File path) {
    return hasHtmlChild(path);
  }

  private boolean hasHtmlChild(File path) {
    if (path.getName().endsWith(".html")) return true;
    for (String child : fileSystem.list(path)) {
      if (hasHtmlChild(new File(path, child))) return true;
    }
    return false;
  }

}
