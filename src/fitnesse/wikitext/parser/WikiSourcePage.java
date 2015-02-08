package fitnesse.wikitext.parser;

import fitnesse.wiki.*;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class WikiSourcePage implements SourcePage {
    private WikiPage page;

    public WikiSourcePage(WikiPage page) { this.page = page; }

    public String getName() { return page.getName(); }

    public String getFullName() {
        return page.getPageCrawler().getFullPath().toString();
    }

    public String getPath() {
        return page.getPageCrawler().getFullPath().parentPath().toString();
    }

    public String getFullPath() {
        return page.getPageCrawler().getFullPath().toString();
    }

    public String getContent() {
        return page.getData().getContent();
    }

    public boolean targetExists(String wikiWordPath) {
        WikiPagePath pathOfWikiWord = PathParser.parse(wikiWordPath);
        WikiPage parentPage = page.getParent();
        return parentPage.getPageCrawler().getPage(pathOfWikiWord) != null;
    }

    public String makeFullPathOfTarget(String wikiWordPath) {
        WikiPagePath pathOfWikiWord = PathParser.parse(wikiWordPath);
        if (pathOfWikiWord == null) throw new IllegalArgumentException("Can't parse path: " + wikiWordPath);
        WikiPage parentPage = page.getParent();
        return PathParser.render(parentPage.getPageCrawler().getFullPathOfChild(pathOfWikiWord));
    }

    public String findParentPath(String targetName) {
        String[] pathElements = targetName.split("\\.");
        String target = pathElements[0];
        PageCrawler crawler = page.getPageCrawler();
        WikiPage ancestor = crawler.findAncestorWithName(target);
        if (ancestor != null) {
            pathElements[0] = PathParser.render(ancestor.getPageCrawler().getFullPath());
          return "." + StringUtils.join(Arrays.asList(pathElements), ".");
        }
        return "." + targetName;
    }

    public Maybe<SourcePage> findIncludedPage(String pageName) {
        PageCrawler crawler = page.getPageCrawler();
        WikiPagePath pagePath = PathParser.parse(pageName);
        if (pagePath == null) {
          return Maybe.nothingBecause("Page include failed because the page " + pageName + " does not have a valid WikiPage name.");
        }

        WikiPage includedPage = crawler.getSiblingPage(pagePath);
        if (includedPage == null) {
            return Maybe.nothingBecause("Page include failed because the page " + pageName + " does not exist.");
        }
        else if (isParentOf(includedPage))
           return Maybe.nothingBecause("Error! Cannot include parent page (" + pageName + ").");
        else {
            return new Maybe<SourcePage>(new WikiSourcePage(includedPage));
        }
    }


  public Collection<SourcePage> getChildren() {
        ArrayList<SourcePage> children = new ArrayList<SourcePage>();
        for (WikiPage child: page.getChildren()) {
            children.add(new WikiSourcePage(child));
        }
        return children;
    }

    public boolean hasProperty(String propertyKey) {
        return page.getData().getProperties().has(propertyKey);
    }

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

    public String makeUrl(String wikiWordPath) {
        return makeFullPathOfTarget(wikiWordPath) ;
    }

    private boolean isParentOf(WikiPage possibleParent) {
        for (WikiPage candidate = page; candidate.getParent() != candidate; candidate = candidate.getParent()) {
            if (possibleParent == candidate)
              return true;
        }
        return false;
    }

    public int compareTo(SourcePage other) {
        return getName().compareTo(other.getName());
    }
}
