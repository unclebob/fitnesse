package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlText;
import fitnesse.wikitext.parser.Symbol;

import java.util.ArrayList;
import java.util.List;

public class HtmlBuilder implements Translation {
    private interface TagBuilder {
        void build(Translator translator, Symbol symbol, HtmlTag tag);
    }

    private List<TagBuilder> builders = new ArrayList<TagBuilder>();
    private String tagName;
    private boolean inline;

    public HtmlBuilder(String tagName) {
        this.tagName = tagName;
    }

    public HtmlBuilder inline() {
        this.inline = true;
        return this;
    }

    public HtmlBuilder attribute(final String name, final String value) {
        builders.add(new TagBuilder() {
            public void build(Translator translator, Symbol symbol, HtmlTag tag) {
                tag.addAttribute(name, value);
            }
        });
        return this;
    }

    public HtmlBuilder attribute(String name, int index) { return attribute(name, index, ""); }
    
    public HtmlBuilder attribute(final String name, final int index, final String prefix) {
        builders.add(new TagBuilder() {
            public void build(Translator translator, Symbol symbol, HtmlTag tag) {
                tag.addAttribute(name, prefix +
                        (index < 0 ? symbol.getContent() : translator.translate(symbol.childAt(index))));
            }
        });
        return this;
    }

    public HtmlBuilder body(int index) { return body(index, ""); }

    public HtmlBuilder body(final int index, final String prefix) {
        builders.add(new TagBuilder() {
            public void build(Translator translator, Symbol symbol, HtmlTag tag) {
                tag.add(prefix + translator.translate(symbol.childAt(index)));
            }
        });
        return this;
    }

    public HtmlBuilder bodyContent() {
        builders.add(new TagBuilder() {
            public void build(Translator translator, Symbol symbol, HtmlTag tag) {
                tag.add(new HtmlText(symbol.getContent()));
            }
        });
        return this;
    }

    public String toTarget(Translator translator, Symbol symbol) {
        HtmlTag result = new HtmlTag(tagName);
        for (TagBuilder builder: builders) {
            builder.build(translator, symbol, result);
        }
        return inline ? result.htmlInline() : result.html();
    }
}
