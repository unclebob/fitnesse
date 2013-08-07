package fitnesse.wikitext.parser;

import fitnesse.wiki.*;
import util.Maybe;
import util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class WikiSourcePage implements SourcePage {
    private WikiPage page;

    public WikiSourcePage(WikiPage page) { this.page = page; }

    public String getName() { return page.getName(); }

    public String getFullName() {
        try {
            return page.getPageCrawler().getFullPath().toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String getPath() {
        try {
            return page.getPageCrawler().getFullPath().parentPath().toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String getFullPath() {
        try {
            return page.getPageCrawler().getFullPath().toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String getContent() {
        try {
            return page.getData().getContent();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public boolean targetExists(String wikiWordPath) {
        WikiPagePath pathOfWikiWord = PathParser.parse(wikiWordPath);
        try {
            WikiPage parentPage = page.getParent();
            return parentPage.getPageCrawler().getPage(pathOfWikiWord) != null;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String makeFullPathOfTarget(String wikiWordPath) {
        WikiPagePath pathOfWikiWord = PathParser.parse(wikiWordPath);
        if (pathOfWikiWord == null) throw new IllegalArgumentException("Can't parse path: " + wikiWordPath);
        try {
            WikiPage parentPage = page.getParent();
            return PathParser.render(parentPage.getPageCrawler().getFullPathOfChild(pathOfWikiWord));
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String findParentPath(String targetName) {
        String[] pathElements = targetName.split("\\.");
        String target = pathElements[0];
        PageCrawler crawler = page.getPageCrawler();
        try {
            WikiPage ancestor = crawler.findAncestorWithName(target);
            if (ancestor != null) {
                pathElements[0] = PathParser.render(ancestor.getPageCrawler().getFullPath());
                return "." + StringUtil.join(Arrays.asList(pathElements), ".");
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return "." + targetName;
    }

    public Maybe<SourcePage> findIncludedPage(String pageName) {
        PageCrawler crawler = page.getPageCrawler();
        WikiPagePath pagePath = PathParser.parse(pageName);
        if (pagePath == null) {
          return Maybe.nothingBecause("Page include failed because the page " + pageName + " does not have a valid WikiPage name.\n");
        }
        try {
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
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public Collection<SourcePage> getAncestors() {
        ArrayList<SourcePage> ancestors = new ArrayList<SourcePage>();
        try {
            for (WikiPage ancestor = page.getParent(); ancestor != null && ancestor != page; ancestor = ancestor.getParent()) {
                ancestors.add(new WikiSourcePage(ancestor));
                if (ancestor.isRoot()) break;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return ancestors;
    }

    public Collection<SourcePage> getChildren() {
        ArrayList<SourcePage> children = new ArrayList<SourcePage>();
        try {
            for (WikiPage child: page.getChildren()) {
                children.add(new WikiSourcePage(child));
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return children;
    }

    public boolean hasProperty(String propertyKey) {
        try {
            return page.getData().getProperties().has(propertyKey);
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String getProperty(String propertyKey) {
        try {
            String propertyValue = page.getData().getAttribute(propertyKey);
            return propertyValue != null ? propertyValue.trim() : "";
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String makeUrl(String wikiWordPath) {
        return makeFullPathOfTarget(wikiWordPath) ;
    }

    private boolean isParentOf(WikiPage possibleParent) {
        try {
            for (WikiPage candidate = page; candidate.getParent() != candidate; candidate = candidate.getParent()) {
                if (possibleParent == candidate)
                  return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public int compareTo(SourcePage other) {
        return getName().compareTo(other.getName());
    }
}
