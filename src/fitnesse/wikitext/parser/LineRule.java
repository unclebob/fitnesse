package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import fitnesse.wikitext.translator.Translator;
import util.Maybe;

import java.util.HashMap;

public class LineRule extends Rule {
    @Override
    public Maybe<Symbol> parse(Scanner scanner) {
        SymbolType type = scanner.getCurrentType();
        scanner.moveNext();
        if (!scanner.isType(SymbolType.Whitespace)) return Symbol.Nothing;
        Phrase body = new Parser(getPage()).parseIgnoreFirst(scanner, SymbolType.Newline);
        if (scanner.isEnd()) return Symbol.Nothing;
        return new Maybe<Symbol>(new Phrase(type).add(body));
    }

    private interface Renderer { HtmlTag render(String content); }
    private static final HashMap<SymbolType, Renderer> renderers;

    static {
        renderers = new HashMap<SymbolType, Renderer>();

        renderers.put(SymbolType.HeaderLine, new Renderer() {
            public HtmlTag render(String content) {
                return new HtmlTag("h" + content);
            }
        });

        renderers.put(SymbolType.CenterLine, new Renderer() {
            public HtmlTag render(String content) {
                HtmlTag html = new HtmlTag("div");
                html.addAttribute("class", "centered");
                return html;
            }
        });

        renderers.put(SymbolType.NoteLine, new Renderer() {
            public HtmlTag render(String content) {
                return HtmlUtil.makeSpanTag("note", "");
            }
        });
    }

    public Maybe<String> render(Scanner scanner) {
        scanner.moveNext();
        if (!scanner.isType(SymbolType.Whitespace)) return Maybe.noString;
        String body = new Translator(getPage()).translateIgnoreFirst(scanner, SymbolType.Newline);
        if (scanner.isEnd()) return Maybe.noString;
        HtmlTag html = null; //renderers.get(getType()).render(getContent().substring(1));
        html.add(body.trim());
        return new Maybe<String>(html.html());
    }
}
