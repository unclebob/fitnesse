package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import util.Maybe;

import java.util.HashMap;

public class LineToken extends ContentToken {
    public static final TokenType HeaderLine = TokenType.HeaderLine;
    public static final TokenType CenterLine = TokenType.CenterLine;
    public static final TokenType NoteLine = TokenType.NoteLine;

    private interface Renderer { HtmlTag render(String content); }
    private static final HashMap<String, Renderer> renderers;

    static {
        renderers = new HashMap<String, Renderer>();

        Renderer headerRenderer = new Renderer() {
            public HtmlTag render(String content) {
                return new HtmlTag("h" + content);
            }
        };
        for (int i = 1; i < 7; i++) renderers.put(Integer.toString(i), headerRenderer);

        Renderer centerRenderer = new Renderer() {
            public HtmlTag render(String content) {
                HtmlTag html = new HtmlTag("div");
                html.addAttribute("class", "centered");
                return html;
            }
        };
        renderers.put("c", centerRenderer);
        renderers.put("C", centerRenderer);

        renderers.put("note", new Renderer() {
            public HtmlTag render(String content) {
                return HtmlUtil.makeSpanTag(content, "");
            }
        });
    }

    private final TokenType type;

    public LineToken(String content, TokenType type) {
        super(content);
        this.type = type;
    }

    public Maybe<String> render(Scanner scanner) {
        scanner.moveNext();
        if (scanner.getCurrent().getType() != TokenType.Whitespace) return Maybe.noString;
        String body = new Translator().translate(scanner, TokenType.Newline);
        if (scanner.isEnd()) return Maybe.noString;
        HtmlTag html = renderers.get(getContent()).render(getContent());
        html.add(body.trim());
        return new Maybe<String>(html.html());
    }

    public TokenType getType() { return type; }
}
