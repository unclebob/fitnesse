//[acd] EvaluatorWidget: Test module
package fitnesse.wikitext.widgets;

import fitnesse.wiki.*;

public class EvaluatorWidgetTest extends WidgetTestCase
{
   private WikiPage root;
   private PageCrawler crawler;
   private WikiPage page;
   private WidgetRoot widgetRoot;

   public void setUp() throws Exception
   {
      root = InMemoryPage.makeRoot("root");
      crawler = root.getPageCrawler();
      
      String content =    "!define ONE {1}\n"
      						+ "!define TWO {2}\n"
      						+ "!define FMT {%03x}\n"
      						+ "!define FmtCOLON {%03X:}"
      						;
      
      page = crawler.addPage(root, PathParser.parse("MyPage"), content);
      widgetRoot = new WidgetRoot("", page);
      
    }

   public void tearDown() throws Exception
   {
   }

   @Override
   protected String getRegexp ()
   {
      return EvaluatorWidget.REGEXP;
   }

   public void testMatches() throws Exception
   {
      assertMatches("${=X=}");
      assertMatches("${=xyz=}");
      assertMatches("${=  X  =}");
      assertMatches("${= 1 + 1 =}");
      assertMatches("${= ${ONE} + ${TWO} =}");
      assertMatches("${=%d:2.3=}");
      assertMatches("${= %02X : 27 =}");

      assertMatches("${=%30s:123=}");
      assertMatches("${=%-30s:123=}");

      assertMatches("${= %d : 3.2           =}");
      assertMatches("${= %03o : 18 =}");
   }
  
   public void testSimpleTermOneDigit () throws Exception
   {  assertEquals("8", new EvaluatorWidget(widgetRoot, "${= 8 =}").render());
   }
   public void testSimpleTermMultiDigit () throws Exception
   {  assertEquals("42", new EvaluatorWidget(widgetRoot, "${= 42 =}").render());
   }
   public void testSimpleTermMultiDigitDecimal () throws Exception
   {  assertEquals("42.24", new EvaluatorWidget(widgetRoot, "${= 42.24 =}").render());
   }
   public void testSimpleTermScientific () throws Exception
   {  assertEquals("1200", new EvaluatorWidget(widgetRoot, "${= 1.2E+3 =}").render());
   }
   public void testSimpleTermSigned () throws Exception
   {  assertEquals("-123", new EvaluatorWidget(widgetRoot, "${= -123 =}").render());
   }

   public void testFormatting () throws Exception
   {
   	assertEquals("3", new EvaluatorWidget(widgetRoot, "${=%d:3.2           =}").render());
   	assertEquals("3", new EvaluatorWidget(widgetRoot, "${= %d :3.2           =}").render());
   	assertEquals("3", new EvaluatorWidget(widgetRoot, "${= %d:3.2           =}").render());
   	assertEquals("3", new EvaluatorWidget(widgetRoot, "${=%d :3.2           =}").render());
   	assertSubString("invalid expression: %3 d :3.2           ", new EvaluatorWidget(widgetRoot, "${=%3 d :3.2           =}").render());
   	assertEquals("022", new EvaluatorWidget(widgetRoot, "${=%03o: 18 =}").render());
   	assertEquals("01b", new EvaluatorWidget(widgetRoot, "${=%03x: 27 =}").render());
   	assertEquals("01C", new EvaluatorWidget(widgetRoot, "${=%03X: 28 =}").render());
   	assertEquals("0.4041", new EvaluatorWidget(widgetRoot, "${=%5.4f: 0.8082 / 2 =}").render());
   }
   
   public void testAddition () throws Exception
   {  assertEquals("3", new EvaluatorWidget(widgetRoot, "${= 1 + 2 =}").render());
   }
   public void testAdditionWithNegativeUnarySigns () throws Exception
   {  assertEquals("-3", new EvaluatorWidget(widgetRoot, "${= -1 + -2 =}").render());
   }
   public void testAdditionWithMixedSigns () throws Exception
   {  assertEquals("-1", new EvaluatorWidget(widgetRoot, "${= 1 + -2 =}").render());
   }
   public void testSubtraction () throws Exception
   {  assertEquals("2", new EvaluatorWidget(widgetRoot, "${= 3 - 1 =}").render());
   }
   public void testMultiplication () throws Exception
   {  assertEquals("12", new EvaluatorWidget(widgetRoot, "${= 3 * 4 =}").render());
   }
   public void testDivision () throws Exception
   {  assertEquals("2.5", new EvaluatorWidget(widgetRoot, "${= 5 / 2 =}").render());
   }
   public void testExponent () throws Exception
   {  EvaluatorWidget eval = new EvaluatorWidget(widgetRoot, "${=%d: 3^3 =}");
      assertEquals("27", eval.render());
   }
   public void testSine () throws Exception
   {  assertEquals("1.8509", new EvaluatorWidget(widgetRoot, "${=%5.4f: 1 + sin 45 =}").render());
   }
   public void testCosine () throws Exception
   {  assertEquals("1.1543", new EvaluatorWidget(widgetRoot, "${=%5.4f: 1 + cos 30 =}").render());
   }
   public void testTangent () throws Exception
   {  assertEquals("-5.4053", new EvaluatorWidget(widgetRoot, "${=%5.4f: 1 + tan 30 =}").render());
   }
   public void testParentheses () throws Exception
   {  assertEquals("9", new EvaluatorWidget(widgetRoot, "${= (1 + 2) * 3 =}").render());
   }
   public void testNoParentheses () throws Exception
   {  assertEquals("7", new EvaluatorWidget(widgetRoot, "${= 1 + 2 * 3 =}").render());
   }

   public void testInvalidExpression() throws Exception
   {
      EvaluatorWidget eval = new EvaluatorWidget(widgetRoot, "${= x =}");
      assertSubString("invalid expression:  x ", eval.render());
   }

   public void testVariableSubstitutionPlain () throws Exception
   {
      WikiPage page2 = crawler.addPage( page, 
      											 PathParser.parse("MyVarSubPage"),
      											   "~vs1:${=${ONE}=}~\n"
      											 + "~vs2:${=${ONE}+${TWO}=}~\n"
      											 + "~vs3:${= ${ONE} + ${TWO} * ${TWO} =}~\n"
      											 + "~vs4:${=(${ONE} + ${TWO}) * ${TWO}=}~\n"
      											);
      
      String result = page2.getData().getHtml();
      assertSubString("~vs1:1~", result);
      assertSubString("~vs2:3~", result);
      assertSubString("~vs3:5~", result);
      assertSubString("~vs4:6~", result);
   }
   
   
   public void testRenderTwice() throws Exception
   {
      EvaluatorWidget eval = new EvaluatorWidget(widgetRoot, "${= 2 + 2 =}");
      assertEquals("4", eval.render());
      assertEquals("4", eval.render());
   }

   public void testAsWikiText() throws Exception
   {
      EvaluatorWidget eval = new EvaluatorWidget(widgetRoot, "${= 1 + 2 * 3 / 4 =}");
      assertEquals("${= 1 + 2 * 3 / 4 =}", eval.asWikiText());
   }
}
