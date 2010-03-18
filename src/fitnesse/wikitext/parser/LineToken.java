package fitnesse.wikitext.parser;

import fitnesse.html.HtmlTag;
import fitnesse.html.HtmlUtil;
import util.Maybe;

import java.util.HashMap;

public class LineToken extends ContentToken {

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

    public LineToken(String content) { super(content); }
    public LineToken() { this(""); }

    public TokenMatch makeMatch(ScanString input) {
        if (input.startsLine() && input.startsWith("!")) {
            int blank = input.find(new char[] {' '}, 1);
            if (blank > 1) {
                String content = input.substring(1, blank);
                if (renderers.containsKey(content)) {
                    return new TokenMatch(new LineToken(content), content.length() + 2);
                }
            }
        }
        return TokenMatch.noMatch;
    }

    public Maybe<String> render(Scanner scanner) {
        String body = new Translator().translate(scanner, new NewlineToken());
        if (scanner.isEnd()) return Maybe.noString;
        HtmlTag html = renderers.get(getContent()).render(getContent());
        html.add(body.trim());
        return new Maybe<String>(html.html());
    }

    public boolean sameAs(Token other) {
        return other instanceof LineToken && ((LineToken)other).getContent().equals(getContent());
    }
}
