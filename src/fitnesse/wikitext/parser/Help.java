package fitnesse.wikitext.parser;

import fitnesse.wiki.WikiPageProperty;

import java.util.List;

public class Help extends SymbolType implements Rule, Translation {
    private static final String editableOption = "-editable";

    public Help() {
        super("Help");
        wikiMatcher(new Matcher().string("!help"));
        wikiRule(this);
        htmlTranslation(this);
    }

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        List<Symbol> lookAhead = parser.peek(new SymbolType[] {SymbolType.Whitespace, SymbolType.Text});
        if (!lookAhead.isEmpty()) {
            String option = lookAhead.get(1).getContent();
            if (option.equals(editableOption)) {
                current.putProperty(editableOption, "");
                parser.moveNext(2);
            }
        }
        return new Maybe<>(current);
    }

    @Override
    public String toTarget(Translator translator, Symbol symbol) {
        String helpText = translator.getPage().getProperty(WikiPageProperty.HELP);
        String editText = helpText.isEmpty() ? "edit help text" : "edit";
        if (symbol.hasProperty(editableOption)) {
          helpText += " <a href=\"" + translator.getPage().getFullPath() + "?properties\">(" + editText + ")</a>";
        }
        return helpText;
    }
}
