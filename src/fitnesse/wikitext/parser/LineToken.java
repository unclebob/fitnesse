package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import util.Maybe;

import java.util.HashMap;

public class LineToken extends Token {

    private interface Renderer { HtmlTag render(String content); }
    private static final HashMap<TokenType, Renderer> renderers;

    static {
        renderers = new HashMap<TokenType, Renderer>();

        renderers.put(TokenType.HeaderLine, new Renderer() {
            public HtmlTag render(String content) {
                return new HtmlTag("h" + content);
            }
        });

        renderers.put(TokenType.CenterLine, new Renderer() {
            public HtmlTag render(String content) {
                HtmlTag html = new HtmlTag("div");
                html.addAttribute("class", "centered");
                return html;
            }
        });

        renderers.put(TokenType.NoteLine, new Renderer() {
            public HtmlTag render(String content) {
                return HtmlUtil.makeSpanTag("note", "");
            }
        });
    }

    public Maybe<String> render(Scanner scanner) {
        scanner.moveNext();
        if (!scanner.isType(TokenType.Whitespace)) return Maybe.noString;
        String body = new Translator(getPage()).translateIgnoreFirst(scanner, TokenType.Newline);
        if (scanner.isEnd()) return Maybe.noString;
        HtmlTag html = renderers.get(getType()).render(getContent().substring(1));
        html.add(body.trim());
        return new Maybe<String>(html.html());
    }
}
