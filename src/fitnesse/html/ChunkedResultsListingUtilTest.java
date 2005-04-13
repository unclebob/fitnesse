package fitnesse.html;

import fitnesse.testutil.RegexTest;

public class ChunkedResultsListingUtilTest extends RegexTest
{
	public void testOpeningTag()
	{
		assertEquals("<table id=\"myTable\" cellspacing=\"0\" cellpadding=\"0\" border=\"0\" class=\"dirListing\">", ChunkedResultsListingUtil.getTableOpenHtml("myTable"));
	}

	public void testClosingTag()
	{
		assertEquals("</table>", ChunkedResultsListingUtil.getTableCloseHtml());
	}

}
