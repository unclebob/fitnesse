package fitnesse.html;

import fitnesse.testutil.RegexTestCase;

public class ChunkedResultsListingUtilTest extends RegexTestCase
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
