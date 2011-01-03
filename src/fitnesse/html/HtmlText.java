package fitnesse.html;

import fitnesse.wikitext.Utils;

public class HtmlText extends HtmlElement {
    private String text;

    public HtmlText(String text) { this.text = text; }

    @Override
    public String html() {
        return Utils.escapeHTML(text);
    }
}
