package fitnesse.html;

public class HtmlText extends HtmlElement {
    private String text;

    public HtmlText(String text) { this.text = text; }

    @Override
    public String html() {
        return HtmlUtil.escapeHTML(text);
    }
}
