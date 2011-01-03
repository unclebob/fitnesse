package fitnesse.wikitext.test;

import org.junit.Test;

public class StyleTest {
    @Test public void scansParenthesisStyle() throws Exception {
        ParserTestHelper.assertScansTokenType("!style_x(my text)", "Style", true);
        ParserTestHelper.assertScansTokenType("!style_style(my text)", "Style", true);
        ParserTestHelper.assertScansTokenType("!style(Hi)", "Style", false);
        ParserTestHelper.assertScansTokenType("!style_(Hi)", "Style", false);
        ParserTestHelper.assertScansTokenType("!style_myStyle(hi))", "Style", true);
    }

    @Test public void scansBraceStyle() throws Exception {
        ParserTestHelper.assertScansTokenType("!style_x{my text}", "Style", true);
        ParserTestHelper.assertScansTokenType("!style_style{my text}", "Style", true);
        ParserTestHelper.assertScansTokenType("!style{Hi}", "Style", false);
        ParserTestHelper.assertScansTokenType("!style_{Hi}", "Style", false);
        ParserTestHelper.assertScansTokenType("!style_myStyle{hi}}", "Style", true);
    }

    @Test public void scansBracketStyle() throws Exception {
        ParserTestHelper.assertScansTokenType("!style_x[my text]", "Style", true);
        ParserTestHelper.assertScansTokenType("!style_style[my text]", "Style", true);
        ParserTestHelper.assertScansTokenType("!style[Hi]", "Style", false);
        ParserTestHelper.assertScansTokenType("!style_[Hi]", "Style", false);
        ParserTestHelper.assertScansTokenType("!style_myStyle[hi]]", "Style", true);
    }

    @Test public void translatesStyle() {
        ParserTestHelper.assertTranslatesTo("!style_myStyle(wow zap)", "<span class=\"myStyle\">wow zap</span>");
        ParserTestHelper.assertTranslatesTo("!style_myStyle[wow zap]", "<span class=\"myStyle\">wow zap</span>");
        ParserTestHelper.assertTranslatesTo("!style_myStyle[)]", "<span class=\"myStyle\">)</span>");
        ParserTestHelper.assertTranslatesTo("!style_myStyle{wow zap}", "<span class=\"myStyle\">wow zap</span>");
    }

    @Test public void ignoresMismatchedStyle() {
        ParserTestHelper.assertTranslatesTo("!style_myStyle[stuff)", "!style_myStyle[stuff)");
    }

    @Test public void translatesNestedStyle() {
        ParserTestHelper.assertTranslatesTo("!style_myStyle(!style_otherStyle(stuff))",
          "<span class=\"myStyle\"><span class=\"otherStyle\">stuff</span></span>");
    }

    @Test public void translatesOverlappedStyle() {
        ParserTestHelper.assertTranslatesTo("!style_red(!style_blue{a)}",
          "!style_red(<span class=\"blue\">a)</span>");
    }
}
