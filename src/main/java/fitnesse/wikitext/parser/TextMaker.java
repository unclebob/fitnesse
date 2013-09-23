package fitnesse.wikitext.parser;

import java.util.regex.Pattern;

public class TextMaker {
    public static final String eMailPattern = "[\\w-_.]+@[\\w-_.]+\\.[\\w-_.]+";

    private VariableSource variableSource;
    private SourcePage sourcePage;

    public TextMaker(VariableSource variableSource, SourcePage sourcePage) {
        this.variableSource = variableSource;
        this.sourcePage = sourcePage;
    }

    public SymbolMatch make(ParseSpecification specification, String text) {
        if (specification.matchesFor(WikiWord.symbolType)) {
            int length = new WikiWordPath().findLength(text);
            if (length > 0) {
                Symbol wikiWord = new Symbol(new WikiWord(sourcePage), text.substring(0, length));
                wikiWord.evaluateVariables(new String[] {WikiWord.REGRACE_LINK}, variableSource);
                return new SymbolMatch(wikiWord, length);
            }
        }
        if (specification.matchesFor(SymbolType.EMail) && isEmailAddress(text)) {
            return new SymbolMatch(SymbolType.EMail, text);
        }
        return new SymbolMatch(SymbolType.Text, text);
    }

    private boolean isEmailAddress(String text) {
        return text.indexOf("@") > 0 && Pattern.matches(eMailPattern, text);
    }
}
