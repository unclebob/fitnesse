package fitnesse.wikitext.parser;

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
    }

    private Maybe<String> findVariableInPages(String name) {
        Maybe<String> localVariable = page.findVariable(name);
        if (!localVariable.isNothing()) return new Maybe<String>(localVariable.getValue());
        return lookInParentPages(name);
    }

    private Maybe<String> lookInParentPages(String name) {
        for (SourcePage sourcePage: page.getPage().getAncestors()) {
            if (!page.inCache(sourcePage)) {
                Parser.make(page.copyForPage(sourcePage), sourcePage.getContent()).parse();
                // todo: make this a method on ParsingPage
                page.putVariable(sourcePage, "", Maybe.noString);
            }
            Maybe<String> result = page.findVariable(sourcePage, name);
            if (!result.isNothing()) return result;
            //page.putVariable(sourcePage, name, result);
        }
        return Maybe.noString;
    }
}
