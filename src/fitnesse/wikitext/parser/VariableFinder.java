package fitnesse.wikitext.parser;

import fitnesse.FitNesse;
import fitnesse.FitNesseContext;
import util.Maybe;

public class VariableFinder implements VariableSource {
  private ParsingPage page;

  public VariableFinder(ParsingPage page) {
    this.page = page;
  }

  public Maybe<String> findVariable(String name) {
    Maybe<String> result = page.getSpecialVariableValue(name);
    if (!result.isNothing()) return result;

    result = findVariableInPages(name);
    if (!result.isNothing()) return result;

    return findVariableInContext(name);
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
      //page.putVariable(sourcePage, name, result);
    }
    return Maybe.noString;
  }
}
