package fitnesse.wikitext.test;

import fitnesse.wikitext.parser.SourcePage;
import util.Maybe;

import java.util.Collection;
import java.util.HashMap;

public class TestSourcePage implements SourcePage {
    public String content;
    public HashMap<String, String> properties = new HashMap<String, String>();

    public TestSourcePage withContent(String content) {
        this.content = content;
        return this;
    }

    public TestSourcePage withProperty(String key, String value) {
        properties.put(key, value);
        return this;
    }

    public String getName() {
        return null;
    }

    public String getFullName() {
        return null;
    }

    public String getPath() {
        return null;
    }

    public String getContent() { return content; }

    public boolean targetExists(String wikiWordPath) {
        return false;
    }

    public String makeFullPathOfTarget(String wikiWordPath) {
        return null;
    }

    public String findParentPath(String targetName) {
        return null;
    }

    public Maybe<SourcePage> findIncludedPage(String pageName) {
        return null;
    }

    public Collection<SourcePage> getAncestors() {
        return null;
    }

    public Collection<SourcePage> getChildren() {
        return null;
    }

    public boolean hasProperty(String propertyKey) {
        return properties.containsKey(propertyKey);
    }

    public String getProperty(String propertyKey) {
        return properties.containsKey(propertyKey) ? properties.get(propertyKey) : "";
    }
}
