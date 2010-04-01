package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.parser.Symbol;

public class HtmlBuilder implements Translation {
    private String tagName;
    private String attributeName;
    private int bodyIndex = -1;
    private String cssClass;

    public HtmlBuilder tag(String htmlTag) {
        this.tagName = htmlTag;
        return this;
    }

    public HtmlBuilder cssClass(String cssClass) {
        this.cssClass = cssClass;
        return this;
    }

    public HtmlBuilder body(int bodyIndex) {
        this.bodyIndex = bodyIndex;
        return this;
    }

    public HtmlBuilder attribute(String attributeName) {
        this.attributeName = attributeName;
        return this;
    }

    public HtmlTag toHtml(Translator translator, Symbol symbol) {
        HtmlTag result = new HtmlTag(tagName);
        if (bodyIndex > -1) {
            result.add(translator.translate(symbol.childAt(bodyIndex)));
        }
        if (cssClass != null) {
            result.addAttribute("class", cssClass);
        }
        if (attributeName != null) {
             result.addAttribute(attributeName, translator.translate(symbol.childAt(0)));
        }
        return result;
    }
}
