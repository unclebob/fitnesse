package fitnesse.wikitext.parser;

import org.junit.Test;

public class EvaluatorTest {
    @Test public void scansEvaluators() {
        ParserTestHelper.assertScansTokenType("${=3+4=}", "Evaluator", true);
    }

    @Test public void translatesEvaluators() {
        ParserTestHelper.assertTranslatesTo("${= 8 =}", "8");
        ParserTestHelper.assertTranslatesTo("${=42.24=}", "42.24");
        ParserTestHelper.assertTranslatesTo("${=1.2E+3=}", "1200");
        ParserTestHelper.assertTranslatesTo("${=-123=}", "-123");
        ParserTestHelper.assertTranslatesTo("${=%d:3.2=}", "3");
        ParserTestHelper.assertTranslatesTo("${==}", "");
        ParserTestHelper.assertTranslatesTo("${= =}", "");
        ParserTestHelper.assertTranslatesTo("${=3+4=}", "7");
        ParserTestHelper.assertTranslatesTo("${=abort=}", " <span class=\"fail\">invalid expression: abort</span> ");
        ParserTestHelper.assertTranslatesTo("${=''body''", "${=<i>body</i>");
    }
}
