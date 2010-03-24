package fitnesse.html;

public class HtmlText extends HtmlElement {
    private String text;

    public HtmlText(String text) { this.text = text; }

    @Override
    public String html() {
        return text
                .replaceAll("\r", "")
                .replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"", "&quot;");
    }
}
