package fitnesse.wiki;

public interface PageCrawlerDeadEndStrategy
{
	WikiPage getPageAfterDeadEnd(WikiPage context, WikiPagePath restOfPath, PageCrawler crawler) throws Exception;
}
