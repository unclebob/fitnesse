package fitnesse.testsystems.slim.tables;

import fitnesse.wikitext.MarkUpSystem;
import fitnesse.wikitext.parser.Maybe;
import fitnesse.wikitext.ParsingPage;
import fitnesse.wikitext.SourcePage;

import java.util.Collection;
import java.util.HashMap;

/**
 * Helper class to translate wiki markup in symbols to a Slim-readable html String
 */

class WikiSymbolTranslateUtil {

  String getHtmlFor(String input) {
    return MarkUpSystem.make().parse(new ParsingPage(new DummySourcePage()), input).translateToHtml();
  }

  private static class DummySourcePage implements SourcePage {
    public String content;
    public HashMap<String, String> properties = new HashMap<>();
    public SourcePage includedPage;
    public String targetPath;

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
      return includedPage != null ? new Maybe<>(includedPage) : Maybe.nothingBecause("missing");
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
      return properties.getOrDefault(propertyKey, "");
    }

    @Override
    public int compareTo(SourcePage other) {
      return getName().compareTo(other.getName());
    }
  }
}
