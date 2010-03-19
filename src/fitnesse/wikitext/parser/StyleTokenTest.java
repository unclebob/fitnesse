package fitnesse.wikitext.parser;

import fitnesse.html.HtmlElement;
import org.junit.Test;

public class StyleTokenTest {
    @Test public void scansParenthesisStyle() throws Exception {
        ParserTest.assertScans("!style_x(my text)", "Style=x,Word=my,Whitespace= ,Word=text,CloseParenthesis=)");
        ParserTest.assertScans("!style_style(my text)", "Style=style,Word=my,Whitespace= ,Word=text,CloseParenthesis=)");
        ParserTest.assertScans("!style(Hi)", "Text=!,Word=style,Text=(,Word=Hi,CloseParenthesis=)");
        ParserTest.assertScans("!style_(Hi)", "Text=!,Word=style_,Text=(,Word=Hi,CloseParenthesis=)");
        ParserTest.assertScans("!style_myStyle(hi))", "Style=myStyle,Word=hi,CloseParenthesis=),CloseParenthesis=)");
    }

    @Test public void scansBraceStyle() throws Exception {
        ParserTest.assertScans("!style_x{my text}", "Style=x,Word=my,Whitespace= ,Word=text,CloseBrace=}");
        ParserTest.assertScans("!style_style{my text}", "Style=style,Word=my,Whitespace= ,Word=text,CloseBrace=}");
        ParserTest.assertScans("!style{Hi}", "Text=!,Word=style,Text={,Word=Hi,CloseBrace=}");
        ParserTest.assertScans("!style_{Hi}", "Text=!,Word=style_,Text={,Word=Hi,CloseBrace=}");
        ParserTest.assertScans("!style_myStyle{hi}}", "Style=myStyle,Word=hi,CloseBrace=},CloseBrace=}");
    }

    @Test public void scansBracketStyle() throws Exception {
        ParserTest.assertScans("!style_x[my text]", "Style=x,Word=my,Whitespace= ,Word=text,CloseBracket=]");
        ParserTest.assertScans("!style_style[my text]", "Style=style,Word=my,Whitespace= ,Word=text,CloseBracket=]");
        ParserTest.assertScans("!style[Hi]", "Text=!,Word=style,Text=[,Word=Hi,CloseBracket=]");
        ParserTest.assertScans("!style_[Hi]", "Text=!,Word=style_,Text=[,Word=Hi,CloseBracket=]");
        ParserTest.assertScans("!style_myStyle[hi]]", "Style=myStyle,Word=hi,CloseBracket=],CloseBracket=]");
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
