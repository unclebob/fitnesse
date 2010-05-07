package fitnesse.wikitext.translator;

import fitnesse.wikitext.parser.*;

import java.util.HashMap;

public abstract class Translator {

    private SourcePage currentPage;
    protected abstract HashMap<SymbolType, Translation> getTranslations();

    public Translator(SourcePage currentPage) {
        this.currentPage = currentPage;
    }

    public SourcePage getPage() { return currentPage; }

    public String translateTree(Symbol syntaxTree) {
        StringBuilder result = new StringBuilder();
        for (Symbol symbol : syntaxTree.getChildren()) {
            result.append(translate(symbol));
        }
        return result.toString();
    }

    public String translate(Symbol symbol) {
        if (getTranslations().containsKey(symbol.getType())) {
            return getTranslations().get(symbol.getType()).toTarget(this, symbol);
        }
        else {
            StringBuilder result = new StringBuilder(symbol.getContent());
            for (Symbol child: symbol.getChildren()) {
                result.append(translate(child));
            }
            return result.toString();
        }
    }

    public String formatMessage(String message) {
        return translate(new Symbol(SymbolType.Meta).add(message));
    }
}
