package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.parser.Symbol;

public class LinkBuilder implements Translation {
    public String toHtml(Translator translator, Symbol symbol) {
        String target = symbol.getContent() + translator.translate(symbol.childAt(0));
        return buildLink(translator, target, symbol);
    }

    public String buildLink(Translator translator, String body, Symbol link) {
        String reference = translator.translate(link.childAt(0));
        String prefix = link.getContent();
        HtmlTag tag = new HtmlTag("a", body);
        tag.addAttribute("href", reference.startsWith("files/") ? "/" + reference : prefix + reference);
        return tag.htmlInline();

    }
}
