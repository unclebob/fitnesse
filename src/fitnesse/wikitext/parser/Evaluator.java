package fitnesse.wikitext.parser;

import fitnesse.wikitext.shared.Names;
import fitnesse.wikitext.shared.ToHtml;

public class Evaluator extends SymbolType implements Rule {
    public static final Evaluator symbolType = new Evaluator();

    public Evaluator() {
        super("Evaluator");
        wikiMatcher(new Matcher().string("${="));
        wikiRule(this);
        htmlTranslation(Translate.with(ToHtml::expression).child(0));
    }

    @Override
    public Maybe<Symbol> parse(Symbol current, Parser parser) {
        Symbol body = parser.parseTo(SymbolType.CloseEvaluator);
        if (parser.atEnd()) return new Maybe<>(Symbol.listOf(current.asText(), body));
        current.copyVariables(new String[] {Names.FORMAT_LOCALE}, parser.getVariableSource());
        return new Maybe<>(current.add(body));
    }
}
