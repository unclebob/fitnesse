package fitnesse.wiki;

public class PagePointer
{
	public WikiPage root;
	public WikiPagePath path;

	public PagePointer(WikiPage root, WikiPagePath path)
	{
		this.root = root;
		this.path = path;
	}

	public WikiPage getPage() throws Exception
	{
		return root.getPageCrawler().getPage(root, path);
	}
}
