package fitnesse.wiki;

import fitnesse.wikitext.parser.Maybe;
import fitnesse.wikitext.SourcePage;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class WikiSourcePage implements SourcePage {
    private final WikiPage page;

    public WikiSourcePage(WikiPage page) { this.page = page; }

    @Override
    public String getName() { return page.getName(); }

    @Override
    public String getFullName() {
        return page.getFullPath().toString();
    }

    @Override
    public String getPath() {
        return page.getFullPath().parentPath().toString();
    }

    @Override
    public String getFullPath() {
        return page.getFullPath().toString();
    }

    @Override
    public String getContent() {
        return page.getData().getContent();
    }

    @Override
    public boolean targetExists(String wikiWordPath) {
        WikiPagePath pathOfWikiWord = PathParser.parse(wikiWordPath);
        WikiPage parentPage = page.getParent();
        return parentPage.getPageCrawler().getPage(pathOfWikiWord) != null;
    }

    @Override
    public String makeFullPathOfTarget(String wikiWordPath) {
        WikiPagePath pathOfWikiWord = PathParser.parse(wikiWordPath);
        if (pathOfWikiWord == null) throw new IllegalArgumentException("Can't parse path: " + wikiWordPath);
        WikiPage parentPage = page.getParent();
        return PathParser.render(parentPage.getPageCrawler().getFullPathOfChild(pathOfWikiWord));
    }

    @Override
    public String findParentPath(String targetName) {
        String[] pathElements = targetName.split("\\.");
        String target = pathElements[0];
        PageCrawler crawler = page.getPageCrawler();
        WikiPage ancestor = crawler.findAncestorWithName(target);
        if (ancestor != null) {
            pathElements[0] = PathParser.render(ancestor.getFullPath());
          return "." + StringUtils.join(Arrays.asList(pathElements), ".");
        }
        return "." + targetName;
    }

    @Override
    public Maybe<SourcePage> findIncludedPage(String pageName) {
        PageCrawler crawler = page.getPageCrawler();
        WikiPagePath pagePath = PathParser.parse(pageName);
        if (pagePath == null) {
          return Maybe.nothingBecause("Page include failed because the page " + pageName + " does not have a valid wiki page name.");
        }

        WikiPage includedPage = crawler.getSiblingPage(pagePath);
        if (includedPage == null) {
            return Maybe.nothingBecause("Page include failed because the page " + pageName + " does not exist.");
        }
        else if (isParentOf(includedPage))
           return Maybe.nothingBecause("Error! Cannot include parent page (" + pageName + ").");
        else {
            return new Maybe<>(new WikiSourcePage(includedPage));
        }
    }


  @Override
  public Collection<SourcePage> getChildren() {
        ArrayList<SourcePage> children = new ArrayList<>();
        for (WikiPage child: page.getChildren()) {
            children.add(new WikiSourcePage(child));
        }
        return children;
    }

    @Override
    public boolean hasProperty(String propertyKey) {
        return page.getData().getProperties().has(propertyKey);
    }

    @Override
    public String getProperty(String propertyKey) {
        String propertyValue = page.getData().getAttribute(propertyKey);
        return propertyValue != null ? propertyValue.trim() : "";
    }

    public boolean hasSymbolicLinkChild(String childName){
        if(page.getData().getProperties().has(SymbolicPage.PROPERTY_NAME)){
             return page.getData().getProperties().getProperty(SymbolicPage.PROPERTY_NAME).keySet().contains(childName);
        }
        return false;
    }

    private boolean isParentOf(WikiPage possibleParent) {
        for (WikiPage candidate = page; candidate.getParent() != candidate; candidate = candidate.getParent()) {
            if (possibleParent == candidate)
              return true;
        }
        return false;
    }

    @Override
    public int compareTo(SourcePage other) {
        return getName().compareTo(other.getName());
    }
}
