package fitnesse.responders.run;

import fitnesse.html.*;
import fitnesse.testutil.RegexTest;
import fitnesse.components.CommandRunner;
import fitnesse.wiki.MockWikiPage;
import fitnesse.responders.run.TestHtmlFormatter;
import fitnesse.responders.run.*;
import fit.Counts;


public class TestHtmlFormatterTest extends RegexTest
{
	private HtmlPage page;
	private TestHtmlFormatter formatter;

	public void setUp() throws Exception
	{
		page = new HtmlPageFactory().newPage();
		formatter = new TestHtmlFormatter(page);
	}

	public void tearDown() throws Exception
	{
	}

	public void testHead() throws Exception
	{
		String head = formatter.head();

		assertSubString("<div id=\"test-summary\">Running Tests ...</div>", head);
	}

	public void testTestSummary() throws Exception
	{
		String summary = formatter.testSummary(new Counts(4, 0, 0, 0));
		assertSubString("<script>document.getElementById(\"test-summary\").innerHTML =", summary);
		assertSubString("<strong>Assertions:</strong> 4 right, 0 wrong, 0 ignored, 0 exceptions", summary);
		assertSubString("document.getElementById(\"test-summary\").className = \"pass\"", summary);

		summary = formatter.testSummary(new Counts(4, 1, 0, 0));
		assertSubString("<strong>Assertions:</strong> 4 right, 1 wrong, 0 ignored, 0 exceptions", summary);
		assertSubString("document.getElementById(\"test-summary\").className = \"fail\"", summary);
	}

	public void testExecutionStatusHtml() throws Exception
	{
		ExecutionLog log = new ExecutionLog(new MockWikiPage(), new CommandRunner());
		String status = formatter.executionStatus(log);

		assertSubString("<div id=\"execution-status\">", status);
	}

	public void testTail() throws Exception
	{
		String tail = formatter.tail();

		assertSubString("</html>", tail);
	}
}
