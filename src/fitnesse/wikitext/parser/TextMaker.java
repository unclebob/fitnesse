package fitnesse.wikitext.parser;

import fitnesse.wikitext.widgets.WikiWordWidget;

import java.util.regex.Pattern;

public class TextMaker {
    public static final String eMailPattern = "[\\w-_.]+@[\\w-_.]+\\.[\\w-_.]+";

    private VariableSource variableSource;

    public TextMaker(VariableSource variableSource) {
        this.variableSource = variableSource;
    }

    public SymbolMatch make(SymbolProvider provider, String text) {
        if (provider.matchesFor(SymbolType.WikiWord)) {
            int length = new WikiWordPath().findLength(text);
            if (length > 0) {
                Symbol wikiWord = new Symbol(SymbolType.WikiWord, text.substring(0, length));
                wikiWord.evaluateVariables(new String[] {WikiWordWidget.REGRACE_LINK}, variableSource);
                return new SymbolMatch(wikiWord, length);
            }
        }
        if (provider.matchesFor(SymbolType.EMail) && isEmailAddress(text)) {
            return new SymbolMatch(new Symbol(SymbolType.EMail, text), text.length());
        }
        return new SymbolMatch(new Symbol(SymbolType.Text, text), text.length());
    }

    private boolean isEmailAddress(String text) {
        return text.indexOf("@") > 0 && Pattern.matches(eMailPattern, text);
    }
}
