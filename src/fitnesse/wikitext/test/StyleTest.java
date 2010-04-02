package fitnesse.wikitext.test;

import fitnesse.html.HtmlElement;
import fitnesse.wikitext.test.ParserTest;
import fitnesse.wikitext.parser.SymbolType;
import org.junit.Test;

public class StyleTest {
    @Test public void scansParenthesisStyle() throws Exception {
        ParserTest.assertScansTokenType("!style_x(my text)", SymbolType.Style, true);
        ParserTest.assertScansTokenType("!style_style(my text)", SymbolType.Style, true);
        ParserTest.assertScansTokenType("!style(Hi)", SymbolType.Style, false);
        ParserTest.assertScansTokenType("!style_(Hi)", SymbolType.Style, false);
        ParserTest.assertScansTokenType("!style_myStyle(hi))", SymbolType.Style, true);
    }

    @Test public void scansBraceStyle() throws Exception {
        ParserTest.assertScansTokenType("!style_x{my text}", SymbolType.Style, true);
        ParserTest.assertScansTokenType("!style_style{my text}", SymbolType.Style, true);
        ParserTest.assertScansTokenType("!style{Hi}", SymbolType.Style, false);
        ParserTest.assertScansTokenType("!style_{Hi}", SymbolType.Style, false);
        ParserTest.assertScansTokenType("!style_myStyle{hi}}", SymbolType.Style, true);
    }

    @Test public void scansBracketStyle() throws Exception {
        ParserTest.assertScansTokenType("!style_x[my text]", SymbolType.Style, true);
        ParserTest.assertScansTokenType("!style_style[my text]", SymbolType.Style, true);
        ParserTest.assertScansTokenType("!style[Hi]", SymbolType.Style, false);
        ParserTest.assertScansTokenType("!style_[Hi]", SymbolType.Style, false);
        ParserTest.assertScansTokenType("!style_myStyle[hi]]", SymbolType.Style, true);
    }

    @Test public void translatesStyle() {
        ParserTest.assertTranslatesTo("!style_myStyle(wow zap)", "<span class=\"myStyle\">wow zap</span>" + HtmlElement.endl);
        ParserTest.assertTranslatesTo("!style_myStyle[wow zap]", "<span class=\"myStyle\">wow zap</span>" + HtmlElement.endl);
        ParserTest.assertTranslatesTo("!style_myStyle[)]", "<span class=\"myStyle\">)</span>" + HtmlElement.endl);
        ParserTest.assertTranslatesTo("!style_myStyle{wow zap}", "<span class=\"myStyle\">wow zap</span>" + HtmlElement.endl);
    }

    @Test public void ignoresMismatchedStyle() {
        ParserTest.assertTranslatesTo("!style_myStyle[stuff)", "!style_myStyle[stuff)");
    }

    @Test public void translatesNestedStyle() {
        ParserTest.assertTranslatesTo("!style_myStyle(!style_otherStyle(stuff))",
                "<span class=\"myStyle\"><span class=\"otherStyle\">stuff</span>" + HtmlElement.endl + "</span>" + HtmlElement.endl);
    }
}
