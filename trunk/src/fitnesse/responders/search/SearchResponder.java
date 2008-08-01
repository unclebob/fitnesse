// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.search;

import fitnesse.components.Searcher;

public class SearchResponder extends ResultResponder
{
	private Searcher searcher;

	private String getSearchString()
	{
		return (String) request.getInput("searchString");
	}

	private String getSearchType()
	{
		String searchType = (String) request.getInput("searchType");
		searchType = searchType.toLowerCase();

		if(searchType.indexOf("title") != -1)
			return "Title";
		else
			return "Content";
	}

	protected String getPageFooterInfo(int hits) throws Exception
	{
		return "Found " + hits + " results for your search.";
	}

	protected String getTitle() throws Exception
	{
		return getSearchType() + " Search Results for '" + getSearchString() + "'";
	}

	protected void startSearching() throws Exception
	{
		String searchString = getSearchString();
		if(!searchString.equals(""))
		{
			loadSearcher(searchString);
			String searchType = getSearchType();
			if("Title".equals(searchType))
				searcher.searchTitles(this);
			else
				searcher.searchContent(this);
		}
	}

	protected boolean shouldRespondWith404()
	{
		return false;
	}

	public void setSearcher(Searcher searcher)
	{
		this.searcher = searcher;
	}

	private void loadSearcher(String searchString) throws Exception
	{
		if(searcher == null)
			searcher = new Searcher(searchString, root);
	}
}
