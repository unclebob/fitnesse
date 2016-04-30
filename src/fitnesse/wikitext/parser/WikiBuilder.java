package fitnesse.wikitext.parser;

import java.util.ArrayList;
import java.util.Collection;

public class WikiBuilder implements Translation {
    private interface WikiStringBuilder {
        void build(Translator translator, Symbol symbol, StringBuilder wikiString);
    }

    private Collection<WikiStringBuilder> builders = new ArrayList<>();

    public WikiBuilder content() {
        builders.add(new WikiStringBuilder() {
            @Override
            public void build(Translator translator, Symbol symbol, StringBuilder wikiString) {
                wikiString.append(symbol.getContent());
            }
        });
        return this;
    }

    public WikiBuilder child(final int child) {
        builders.add(new WikiStringBuilder() {
            @Override
            public void build(Translator translator, Symbol symbol, StringBuilder wikiString) {
                wikiString.append(translator.translate(symbol.childAt(child)));
            }
        });
        return this;
    }

    public WikiBuilder children(final String separator) {
        builders.add(new WikiStringBuilder() {
            @Override
            public void build(Translator translator, Symbol symbol, StringBuilder wikiString) {
                int count = 0;
                for (Symbol child: symbol.getChildren()) {
                    if (count > 0) wikiString.append(separator);
                    wikiString.append(translator.translate(child));
                    count++;
                }
            }
        });
        return this;
    }

    public WikiBuilder text(final String text) {
        builders.add(new WikiStringBuilder() {
            @Override
            public void build(Translator translator, Symbol symbol, StringBuilder wikiString) {
                wikiString.append(text);
            }
        });
        return this;
    }

    public WikiBuilder property(final String key, final String value, final String text) {
        builders.add(new WikiStringBuilder() {
            @Override
            public void build(Translator translator, Symbol symbol, StringBuilder wikiString) {
                if (symbol.getProperty(key, "*none*").equals(value)) wikiString.append(text);
            }
        });
        return this;
    }

    public WikiBuilder property(final String key) {
        builders.add(new WikiStringBuilder() {
            @Override
            public void build(Translator translator, Symbol symbol, StringBuilder wikiString) {
                if (symbol.hasProperty(key)) wikiString.append(symbol.getProperty(key));
            }
        });
        return this;
    }

    @Override
    public String toTarget(Translator translator, Symbol symbol) {
        StringBuilder result = new StringBuilder();
        for (WikiStringBuilder builder: builders) {
            builder.build(translator, symbol, result);
        }
        return result.toString();
    }
}
