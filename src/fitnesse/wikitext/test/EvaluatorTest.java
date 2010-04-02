package fitnesse.wikitext.test;

import fitnesse.wikitext.test.ParserTest;
import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;

public class EvaluatorTest {
    @Test public void scansEvaluators() {
        ParserTest.assertScansTokenType("${=3+4=}", SymbolType.Evaluator, true);
    }

    @Test public void translatesEvaluators() {
        ParserTest.assertTranslatesTo("${=3+4=}", "7");
        ParserTest.assertTranslatesTo("${=abort=}", "<span class=\"meta\">invalid expression: abort</span>");
    }
}
