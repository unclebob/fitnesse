package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class StyleTokenTest {
    @Test public void scansParenthesisStyle() throws Exception {
        ParserTest.assertScans("!style_x(my text)", "StyleToken=x,TextToken=my text,DelimiterToken=)");
        ParserTest.assertScans("!style_style(my text)", "StyleToken=style,TextToken=my text,DelimiterToken=)");
        ParserTest.assertScans("!style(Hi)", "TextToken=!style(Hi,DelimiterToken=)");
        ParserTest.assertScans("!style_(Hi)", "TextToken=!style_(Hi,DelimiterToken=)");
        ParserTest.assertScans("!style_myStyle(hi))", "StyleToken=myStyle,TextToken=hi,DelimiterToken=),DelimiterToken=)");
    }

    @Test public void scansBraceStyle() throws Exception {
        ParserTest.assertScans("!style_x{my text}", "StyleToken=x,TextToken=my text,DelimiterToken=}");
        ParserTest.assertScans("!style_style{my text}", "StyleToken=style,TextToken=my text,DelimiterToken=}");
        ParserTest.assertScans("!style{Hi}", "TextToken=!style{Hi,DelimiterToken=}");
        ParserTest.assertScans("!style_{Hi}", "TextToken=!style_{Hi,DelimiterToken=}");
        ParserTest.assertScans("!style_myStyle{hi}}", "StyleToken=myStyle,TextToken=hi,DelimiterToken=},DelimiterToken=}");
    }

    @Test public void scansBracketStyle() throws Exception {
        ParserTest.assertScans("!style_x[my text]", "StyleToken=x,TextToken=my text,DelimiterToken=]");
        ParserTest.assertScans("!style_style[my text]", "StyleToken=style,TextToken=my text,DelimiterToken=]");
        ParserTest.assertScans("!style[Hi]", "TextToken=!style[Hi,DelimiterToken=]");
        ParserTest.assertScans("!style_[Hi]", "TextToken=!style_[Hi,DelimiterToken=]");
        ParserTest.assertScans("!style_myStyle[hi]]", "StyleToken=myStyle,TextToken=hi,DelimiterToken=],DelimiterToken=]");
    }

    @Test public void translatesStyle() {
        ParserTest.assertTranslates("!style_myStyle(wow zap)", "<span class=\"myStyle\">wow zap</span>" + HtmlElement.endl);
        ParserTest.assertTranslates("!style_myStyle[wow zap]", "<span class=\"myStyle\">wow zap</span>" + HtmlElement.endl);
        ParserTest.assertTranslates("!style_myStyle[)]", "<span class=\"myStyle\">)</span>" + HtmlElement.endl);
        ParserTest.assertTranslates("!style_myStyle{wow zap}", "<span class=\"myStyle\">wow zap</span>" + HtmlElement.endl);
    }

    @Test public void ignoresMismatchedStyle() {
        ParserTest.assertTranslates("!style_myStyle[stuff)", "!style_myStyle[stuff)");
    }

    @Test public void translatesNestedStyle() {
        ParserTest.assertTranslates("!style_myStyle(!style_otherStyle(stuff))",
                "<span class=\"myStyle\"><span class=\"otherStyle\">stuff</span>" + HtmlElement.endl + "</span>" + HtmlElement.endl);
    }
}
