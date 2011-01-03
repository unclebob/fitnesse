package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import util.Maybe;

public class Link extends SymbolType implements Rule, Translation {
    public static final Link symbolType = new Link();
    public static final String ImageProperty = "image";
    public static final String Left = "left";
    public static final String Right = "right";

    public Link() {
        super("Link");
        wikiMatcher(new Matcher().string("http://"));
        wikiMatcher(new Matcher().string("https://"));
        wikiRule(this);
        htmlTranslation(this);
    }
    
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol targetList = parser.parseToEnds(-1,
                SymbolProvider.linkTargetProvider,
                new SymbolType[] {SymbolType.Newline, SymbolType.Whitespace});
        return new Maybe<Symbol>(current.add(targetList));
    }
    public String toTarget(Translator translator, Symbol symbol) {
        String target = symbol.getContent() + translator.translate(symbol.childAt(0));
        return buildLink(translator, target, symbol);
    }

    public String buildLink(Translator translator, String body, Symbol link) {
        Reference reference = new Reference(translator.translate(link.childAt(0)));
        String prefix = link.getContent();
        HtmlTag tag;
        if (link.hasProperty(Link.ImageProperty) || reference.isImage()) {
            tag = new HtmlTag("img");
            tag.addAttribute("src", reference.makeUrl(prefix));
            String imageClass = link.getProperty(Link.ImageProperty);
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
