/*
 * Copyright (c) 2004 Rick Mugridge, University of Auckland, NZ
 * Released under the terms of the GNU General Public License version 2 or later.
 *
 */
package fitLibrary;
import junit.framework.TestCase;
import fitLibrary.ExtendedCamelCase;

public class ExtendedCamelCaseTest extends TestCase {
    public void testJustCamel() {
        check("two words","twoWords");
        check("three wee words","threeWeeWords");
    }
    public void testExtendedCamel() {
        check("\" hi \"","quoteHiQuote");
        check("!#$%age","bangHashDollarPercentAge");
        check("&'()*","ampersandSingleQuoteLeftParenthesisRightParenthesisStar");
        check("+,-./:","plusCommaMinusDotSlashColon");
        check(";=?","semicolonEqualsQuestion");
        check("@[]\\","atLeftSquareBracketRightSquareBracketBackslash");
        check("^`{}~","caretBackquoteLeftBraceRightBraceTilde");
        check("cost $","costDollar");
        check("cost$","costDollar");
        check("!","bang");
        check("!!","bangBang");
        check("meet @","meetAt");
        check("rick@mugridge.com","rickAtMugridgeDotCom");
        check("","blank");
    }
    public void testLeadingDigit() {
        check("2 words","twoWords");
    }
    public void testJavaKeyword() {
        check("static","static_");
        check("return","return_");
        check("null","null_");
    }
    public void testUnicode() {
        check("\u216C","u216C");
        check("\u216D\uFFFE","u216DuFFFE");
        check("\uFFFF","uFFFF");
        check("\u0041b","Ab");
    }
    private void check(String in, String out) {
        assertEquals(out,ExtendedCamelCase.camel(in));
    }
}
