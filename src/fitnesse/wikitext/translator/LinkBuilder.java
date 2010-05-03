package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.parser.LinkRule;
import fitnesse.wikitext.parser.Symbol;

public class LinkBuilder implements Translation {
    public String toHtml(Translator translator, Symbol symbol) {
        String target = symbol.getContent() + translator.translate(symbol.childAt(0));
        return buildLink(translator, target, symbol);
    }

    public String buildLink(Translator translator, String body, Symbol link) {
        Reference reference = new Reference(translator.translate(link.childAt(0)));
        String prefix = link.getContent();
        HtmlTag tag;
        if (link.hasProperty(LinkRule.ImageProperty) || reference.isImage()) {
            tag = new HtmlTag("img");
            tag.addAttribute("src", reference.makeUrl(prefix));
            String imageClass = link.getProperty(LinkRule.ImageProperty);
            if (imageClass.length() > 0) tag.addAttribute("class", imageClass);
        }
        else {
            tag = new HtmlTag("a", body);
            tag.addAttribute("href", reference.makeUrl(prefix));
        }
        return tag.htmlInline();
    }
    
    private class Reference {
        private String reference;

        public Reference(String reference) { this.reference = reference; }

        public boolean isImage() {
            return reference.toLowerCase().endsWith(".jpg") || reference.toLowerCase().endsWith(".gif");
        }

        public String makeUrl(String prefix) {
            return reference.startsWith("files/") ? "/" + reference : prefix + reference;
        }
    }
}
