package fitnesse.wikitext.parser;

import fitnesse.wikitext.shared.LastModifiedHtml;

public class LastModified extends SymbolType implements Translation {
    public LastModified() {
        super("LastModified");
        wikiMatcher(new Matcher().string("!lastmodified"));
        htmlTranslation(this);
    }

    @Override
    public String toTarget(Translator translator, Symbol symbol) {
        return LastModifiedHtml.write(translator.getPage());
    }
}
