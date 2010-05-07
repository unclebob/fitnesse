package fitnesse.wikitext.translator;

import fitnesse.html.HtmlTag;
import fitnesse.wikitext.parser.Symbol;

public class ListBuilder implements Translation {
    private String listTag;

    public ListBuilder(String listTag) { this.listTag = listTag; }

    public String toTarget(Translator translator, Symbol symbol) {
        HtmlTag list = new HtmlTag(listTag);
        for (Symbol child: symbol.getChildren()) {
            list.add(new HtmlTag("li", translator.translate(child)));
        }
        return list.html();
    }
}
