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
            return page.getPageCrawler().getFullPath(page).toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String getPath() {
        try {
            return page.getPageCrawler().getFullPath(page).parentPath().toString();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public String getFullPath() {
        try {
            return page.getPageCrawler().getFullPath(page).toString();
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
            return parentPage.getPageCrawler().getPage(parentPage, pathOfWikiWord) != null;
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
            return PathParser.render(parentPage.getPageCrawler().getFullPathOfChild(parentPage, pathOfWikiWord));
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
            WikiPage ancestor = crawler.findAncestorWithName(page, target);
            if (ancestor != null) {
                pathElements[0] = PathParser.render(crawler.getFullPath(ancestor));
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
        crawler.setDeadEndStrategy(new VirtualEnabledPageCrawler());
        WikiPagePath pagePath = PathParser.parse(pageName);
        try {
            WikiPage includedPage = crawler.getSiblingPage(page, pagePath);
            if (includedPage == null) {
                if (page instanceof ProxyPage) {
                    ProxyPage proxy = (ProxyPage) page;
                    String host = proxy.getHost();
                    int port = proxy.getHostPort();
                    try {
                        ProxyPage remoteIncludedPage = new ProxyPage("RemoteIncludedPage", null, host, port, pagePath);
                        return new Maybe<SourcePage>(new WikiSourcePage(remoteIncludedPage));
                    }
                    catch (Exception e) {
                        return Maybe.nothingBecause("Remote page \" + host + \":\" + port + \"/\" + pageName + \" does not exist.\n");
                    }
                } else {
                    return Maybe.nothingBecause("Page include failed because the page " + pageName + " does not exist.\n");
                }
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
                if (ancestor.getPageCrawler().isRoot(ancestor)) break;
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
            if (page.hasExtension(VirtualCouplingExtension.NAME)) {
                VirtualCouplingExtension extension = (VirtualCouplingExtension) page.getExtension(VirtualCouplingExtension.NAME);
                WikiPage virtualCoupling = extension.getVirtualCoupling();
                for (WikiPage child: virtualCoupling.getChildren()) {
                    children.add(new WikiSourcePage(child));
                }
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
        if (!(page instanceof ProxyPage))
            return makeFullPathOfTarget(wikiWordPath) ;

        ProxyPage proxy = (ProxyPage) page;
        String remoteURLOfPage = proxy.getThisPageUrl();
        String nameOfThisPage = proxy.getName();
        int startOfThisPageName = remoteURLOfPage.lastIndexOf(nameOfThisPage);
        String remoteURLOfParent = remoteURLOfPage.substring(0, startOfThisPageName);
        return remoteURLOfParent + wikiWordPath;
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
