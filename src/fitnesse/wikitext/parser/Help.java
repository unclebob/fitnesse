package fitnesse.wikitext.parser;

import fitnesse.wiki.PageData;
import util.Maybe;

import java.util.List;

public class Help extends SymbolType implements Rule, Translation {
    private static final String editableOption = "-editable";

    public Help() {
        super("Help");
        wikiMatcher(new Matcher().string("!help"));
        wikiRule(this);
        htmlTranslation(this);
    }
    
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        List<Symbol> lookAhead = parser.peek(new SymbolType[] {SymbolType.Whitespace, SymbolType.Text});
        if (lookAhead.size() != 0 ) {
            String option = lookAhead.get(1).getContent();
            if (option.equals(editableOption)) {
                current.putProperty(editableOption, "");
                parser.moveNext(2);
            }
        }
        return new Maybe<Symbol>(current);
    }

    public String toTarget(Translator translator, Symbol symbol) {
        String helpText = translator.getPage().getProperty(PageData.PropertyHELP);
        String editText = helpText.length() > 0 ? "edit" : "edit help text";
        if (symbol.hasProperty(editableOption)) {
          helpText += " <a href=\"" + translator.getPage().getFullPath() + "?properties\">(" + editText + ")</a>";
        }
        return helpText;
    }
}
