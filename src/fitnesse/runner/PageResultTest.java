package fitnesse.runner;

import junit.framework.TestCase;
import fit.Counts;

public class PageResultTest extends TestCase
{
	public void testToString() throws Exception
	{
		PageResult result = new PageResult("PageTitle", new Counts(1, 2, 3, 4), "content");
		assertEquals("PageTitle\n1 right, 2 wrong, 3 ignored, 4 exceptions\ncontent", result.toString());
	}
	
	public void testParse() throws Exception
	{
		Counts counts = new Counts(1, 2, 3, 4);
		PageResult result = new PageResult("PageTitle", counts, "content");
		PageResult parsedResult = PageResult.parse(result.toString());
		assertEquals("PageTitle", parsedResult.title());
		assertEquals(counts, parsedResult.counts());
		assertEquals("content", parsedResult.content());
	}
}
