package fitnesse.wikitext.test;

import fitnesse.wikitext.parser.SourcePage;
import util.Maybe;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class TestSourcePage implements SourcePage {
    public String content;
    public HashMap<String, String> properties = new HashMap<String, String>();
    public SourcePage includedPage;
    public String targetPath;
    public String url;

    public TestSourcePage withContent(String content) {
        this.content = content;
        return this;
    }

    public TestSourcePage withProperty(String key, String value) {
        properties.put(key, value);
        return this;
    }

    public TestSourcePage withIncludedPage(SourcePage includedPage) {
        this.includedPage = includedPage;
        return this;
    }

    public TestSourcePage withTarget(String targetPath) {
        this.targetPath = targetPath;
        return this;
    }

    public TestSourcePage withUrl(String url) {
        this.url = url;
        return this;
    }

    public String getName() { return null; }
    public String getFullName() { return null; }
    public String getPath() { return null; }

    public String getFullPath() { return null; }

    public String getContent() { return content; }
    public boolean targetExists(String wikiWordPath) { return targetPath != null; }
    public String makeFullPathOfTarget(String wikiWordPath) { return targetPath; }
    public String findParentPath(String targetName) { return null; }

    public Maybe<SourcePage> findIncludedPage(String pageName) {
        return includedPage != null ? new Maybe<SourcePage>(includedPage) : Maybe.<SourcePage>nothingBecause("missing");
    }

    public Collection<SourcePage> getAncestors() { return new ArrayList<SourcePage>(); }
    public Collection<SourcePage> getChildren() { return null; }

    public boolean hasProperty(String propertyKey) {
        return properties.containsKey(propertyKey);
    }

    public String getProperty(String propertyKey) {
        return properties.containsKey(propertyKey) ? properties.get(propertyKey) : "";
    }

    public String makeUrl(String wikiWordPath) {
        return url;
    }

    public int compareTo(SourcePage other) {
        return getName().compareTo(other.getName());
    }
}
