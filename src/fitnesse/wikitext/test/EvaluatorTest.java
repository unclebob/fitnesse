package fitnesse.wikitext.test;

import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;

public class EvaluatorTest {
    @Test public void scansEvaluators() {
        ParserTest.assertScansTokenType("${=3+4=}", "Evaluator", true);
    }

    @Test public void translatesEvaluators() {
        ParserTest.assertTranslatesTo("${= 8 =}", "8");
        ParserTest.assertTranslatesTo("${=42.24=}", "42.24");
        ParserTest.assertTranslatesTo("${=1.2E+3=}", "1200");
        ParserTest.assertTranslatesTo("${=-123=}", "-123");
        ParserTest.assertTranslatesTo("${=%d:3.2=}", "3");
        ParserTest.assertTranslatesTo("${==}", "");
        ParserTest.assertTranslatesTo("${= =}", "");
        ParserTest.assertTranslatesTo("${=3+4=}", "7");
        ParserTest.assertTranslatesTo("${=abort=}", "<span class=\"meta\">invalid expression: abort</span>");
    }
}
