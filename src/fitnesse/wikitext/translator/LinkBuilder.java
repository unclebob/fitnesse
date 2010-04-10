package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.parser.Symbol;

public class LinkBuilder implements Translation {
    public String toHtml(Translator translator, Symbol symbol) {
        return buildLink(symbol.getContent() + symbol.childAt(0).getContent(), symbol);
    }

    public String buildLink(String body, Symbol symbol) {
        String reference = symbol.childAt(0).getContent();
        String url = symbol.getContent() + reference;
        HtmlTag tag = new HtmlTag("a", body);
        tag.addAttribute("href", reference.startsWith("files/") ? "/" + reference : url);
        return tag.htmlInline();

    }
}
