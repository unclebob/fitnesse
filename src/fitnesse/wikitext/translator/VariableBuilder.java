package fitnesse.wikitext.translator;

import fitnesse.html.HtmlUtil;
import fitnesse.wikitext.parser.Symbol;
import util.Maybe;

public class VariableBuilder implements Translation {

    public String toHtml(Translator translator, Symbol symbol) {
        String name = symbol.childAt(0).getContent();
        Maybe<String> variable = new VariableFinder(translator, symbol).findVariable(name);
        return variable.isNothing()
                ? HtmlUtil.metaText("undefined variable: " + name)
                : variable.getValue();
    }

}
