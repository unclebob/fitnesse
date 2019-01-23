package fitnesse.testsystems.slim.tables;

import fitnesse.wikitext.parser.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Helper class to translate wiki markup in symbols to a Slim-readable html String
 */

class WikiSymbolTranslateUtil {

  String getHtmlFor(String input) {
    SourcePage page = new DummySourcePage();
    Symbol list = Parser.make(new ParsingPage(page), input).parse();
    return new HtmlTranslator(page, new ParsingPage(page)).translateTree(list);
  }

  private class DummySourcePage implements SourcePage {
    public String content;
    public HashMap<String, String> properties = new HashMap<>();
    public SourcePage includedPage;
    public String targetPath;
    public String url;

    @Override
    public String getName() {
      return null;
    }

    @Override
    public String getFullName() {
      return "fullname";
    }

    @Override
    public String getPath() {
      return null;
    }

    @Override
    public String getFullPath() {
      return null;
    }

    @Override
    public String getContent() {
      return content;
    }

    @Override
    public boolean targetExists(String wikiWordPath) {
      return targetPath != null;
    }

    @Override
    public String makeFullPathOfTarget(String wikiWordPath) {
      return targetPath;
    }

    @Override
    public String findParentPath(String targetName) {
      return null;
    }

    @Override
    public Maybe<SourcePage> findIncludedPage(String pageName) {
      return includedPage != null ? new Maybe<>(includedPage) : Maybe.<SourcePage>nothingBecause("missing");
    }

    @Override
    public Collection<SourcePage> getChildren() {
      return null;
    }

    @Override
    public boolean hasProperty(String propertyKey) {
      return properties.containsKey(propertyKey);
    }

    @Override
    public String getProperty(String propertyKey) {
      return properties.containsKey(propertyKey) ? properties.get(propertyKey) : "";
    }

    @Override
    public String makeUrl(String wikiWordPath) {
      return url;
    }

    @Override
    public int compareTo(SourcePage other) {
      return getName().compareTo(other.getName());
    }

    @Override
    public List<Symbol> getSymbols(final SymbolType symbolType) {
      return Collections.emptyList();
    }
  }
}
