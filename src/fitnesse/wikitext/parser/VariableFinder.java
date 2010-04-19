package fitnesse.wikitext.parser;

import fitnesse.FitNesseContext;
import fitnesse.wiki.WikiPage;
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

        String value = System.getenv(name);
        if (value != null) return new Maybe<String>(value);

        value = System.getProperty(name);
        if (value != null) return new Maybe<String>(value);

        return Maybe.noString;


        /*try {
            String oldValue = page.getPage().getData().getVariable(name);
            return oldValue == null ? Maybe.noString : new Maybe<String>(oldValue);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }*/
    }

    private Maybe<String> findVariableInPages(String name) {
        Maybe<String> localVariable = page.findVariable(name);
        if (!localVariable.isNothing()) return new Maybe<String>(localVariable.getValue());
        return lookInParentPages(name);
    }

    private Maybe<String> lookInParentPages(String name) {
        try {
            for (WikiPage wikiPage = getParent(page.getPage()); wikiPage != null; wikiPage = getParent(wikiPage)) {
                if (!page.inCache(wikiPage)) {
                    Parser.make(page.copyForPage(wikiPage), wikiPage.getData().getContent()).parse();
                }
                Maybe<String> result = page.findVariable(wikiPage, name);
                if (!result.isNothing()) return result;

                if (wikiPage.getPageCrawler().isRoot(wikiPage)) break;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        return Maybe.noString;
    }

    private WikiPage getParent(WikiPage child) {
        if (child == null) return null;
        try {
            WikiPage parent = child.getParent();
            if (parent == child) return null;
            return parent;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
    }
}
