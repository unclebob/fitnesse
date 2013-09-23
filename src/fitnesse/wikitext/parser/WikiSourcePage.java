package fitnesse.wikitext.parser;

import fitnesse.wiki.*;
import util.Maybe;
import util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WikiSourcePage implements SourcePage {
    private static final Logger LOG = Logger.getLogger(WikiSourcePage.class.getName());

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
            return "." + StringUtil.join(Arrays.asList(pathElements), ".");
        }
        return "." + targetName;
    }

    public Maybe<SourcePage> findIncludedPage(String pageName) {
        PageCrawler crawler = page.getPageCrawler();
        WikiPagePath pagePath = PathParser.parse(pageName);
        if (pagePath == null) {
          return Maybe.nothingBecause("Page include failed because the page " + pageName + " does not have a valid WikiPage name.\n");
        }

        WikiPage includedPage = crawler.getSiblingPage(pagePath);
        if (includedPage == null) {
            return Maybe.nothingBecause("Page include failed because the page " + pageName + " does not exist.\n");
        }
        else if (isParentOf(includedPage))
           return Maybe.nothingBecause( "Error! Cannot include parent page (" + pageName + ").\n");
        else {
            return new Maybe<SourcePage>(new WikiSourcePage(includedPage));
        }
    }

    public Collection<SourcePage> getAncestors() {
        ArrayList<SourcePage> ancestors = new ArrayList<SourcePage>();
        for (WikiPage ancestor = page.getParent(); ancestor != null && ancestor != page; ancestor = ancestor.getParent()) {
            ancestors.add(new WikiSourcePage(ancestor));
            if (ancestor.isRoot()) break;
        }
        return ancestors;
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
