package fitnesse.wikitext.parser;

import org.junit.Test;

public class EvaluatorTokenTest {
    @Test public void scansEvaluators() {
        ParserTest.assertScansTokenType("${=3+4=}", SymbolType.Evaluator, true);
    }

    @Test public void translatesEvaluators() {
        ParserTest.assertTranslates("${=3+4=}", "7");
        ParserTest.assertTranslates("${=abort=}", "<span class=\"meta\">invalid expression: abort</span>");
    }
}
