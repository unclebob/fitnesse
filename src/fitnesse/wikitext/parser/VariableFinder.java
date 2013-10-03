package fitnesse.wikitext.parser;

import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import fitnesse.FitNesseVersion;
import util.Maybe;

public class VariableFinder implements VariableSource {
  private ParsingPage page;

  public VariableFinder(ParsingPage page) {
    this.page = page;
  }

  public Maybe<String> findVariable(String name) {
    Maybe<String> result = findSpecialVariableValue(name);
    if (!result.isNothing()) return result;

    result = findVariableInPages(name);
    if (!result.isNothing()) return result;

    return findVariableInContext(name);
  }

  public Maybe<String> findSpecialVariableValue(String key) {
    final FitNesseContext context = getFitNesseContext();
    final SourcePage sourcePage = page.getPage();
    final SourcePage namedSourcePage = page.getNamedPage();
    String value;
    if (key.equals("RUNNING_PAGE_NAME"))
      value = sourcePage.getName();
    else if (key.equals("RUNNING_PAGE_PATH"))
      value = sourcePage.getPath();
    else if (key.equals("PAGE_NAME"))
      value = namedSourcePage.getName();
    else if (key.equals("PAGE_PATH"))
      value = namedSourcePage.getPath();
    else if (key.equals("FITNESSE_PORT"))
      value = Integer.toString(context != null ? context.port : -1);
    else if (key.equals("FITNESSE_ROOTPATH"))
      value = context != null ? context.rootPath : "";
    else if (key.equals("FITNESSE_VERSION"))
      value = new FitNesseVersion().toString();
    else
      return Maybe.noString;
    return new Maybe<String>(value);
  }


  private Maybe<String> findVariableInPages(String name) {
    Maybe<String> localVariable = page.findVariable(name);
    if (!localVariable.isNothing()) return new Maybe<String>(localVariable.getValue());
    return lookInParentPages(name);
  }

  private Maybe<String> findVariableInContext(String name) {
    final FitNesseContext context = getFitNesseContext();
    if (context != null) {
      String val = context.getProperty(name);
      if (val != null) return new Maybe<String>(val);
    }
    return Maybe.noString;
  }

  private FitNesseContext getFitNesseContext() {
    // Make this fail safe for unit tests
    final FitNesse fitnesse = FitNesse.FITNESSE_INSTANCE;
    return fitnesse != null ? fitnesse.getContext() : null;
  }

  private Maybe<String> lookInParentPages(String name) {
    for (SourcePage sourcePage : page.getPage().getAncestors()) {
      if (!page.inCache(sourcePage)) {
        Parser.make(page.copyForPage(sourcePage), sourcePage.getContent()).parse();
        // todo: make this a method on ParsingPage
        page.putVariable(sourcePage, "", Maybe.noString);
      }
      Maybe<String> result = page.findVariable(sourcePage, name);
      if (!result.isNothing()) return result;
    }
    return Maybe.noString;
  }
}
