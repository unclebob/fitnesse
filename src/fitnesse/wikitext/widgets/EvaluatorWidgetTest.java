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
      page = crawler.addPage(root, PathParser.parse("MyPage"));
      widgetRoot = new WidgetRoot("", page);
      
      widgetRoot.addChildWidgets("!define ONE {1}\n!define TWO {2}");
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
   {  EvaluatorWidget eval = new EvaluatorWidget(widgetRoot, "${= 3^3 =}");
      assertHasRegexp("^26.9+[0-9]$", eval.render());
   }
   public void testSine () throws Exception
   {  assertSubString("1.8509", new EvaluatorWidget(widgetRoot, "${= 1 + sin 45 =}").render());
   }
   public void testCosine () throws Exception
   {  assertSubString("1.1542", new EvaluatorWidget(widgetRoot, "${= 1 + cos 30 =}").render());
   }
   public void testTangent () throws Exception
   {  assertSubString("-5.4053", new EvaluatorWidget(widgetRoot, "${= 1 + tan 30 =}").render());
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
