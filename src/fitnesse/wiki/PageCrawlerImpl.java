package fitnesse.wiki;

import java.util.*;
import java.util.regex.Pattern;
import fitnesse.wikitext.widgets.WikiWordWidget;
import fitnesse.components.FitNesseTraversalListener;

public class PageCrawlerImpl implements PageCrawler
{
	private PageCrawlerDeadEndStrategy deadEndStrategy;

	protected PageCrawlerImpl()
	{
	}

	public WikiPage getPage(WikiPage context, WikiPagePath path) throws Exception
	{
		if(path == null)
			return null;

		if(path.isEmpty())
			return context;

		String firstPathElement = path.getFirst();
		WikiPagePath restOfPath = path.getRest();
		if(firstPathElement.equals(WikiPagePath.ROOT))
			return getPage(getRoot(context), restOfPath);

		WikiPage childPage = context.getChildPage(firstPathElement);
		if(childPage != null)
			return getPage(childPage, restOfPath);
		else
			return getPageAfterDeadEnd(context, firstPathElement, restOfPath);
	}

	protected WikiPage getPageAfterDeadEnd(WikiPage context, String first, WikiPagePath rest) throws Exception
	{
		rest.addNameToFront(first);
		if(deadEndStrategy != null)
			return deadEndStrategy.getPageAfterDeadEnd(context, rest, this);
		else
			return null;
	}

	public void setDeadEndStrategy(PageCrawlerDeadEndStrategy strategy)
	{
		deadEndStrategy = strategy;
	}

	public boolean pageExists(WikiPage context, WikiPagePath path) throws Exception
	{
		return getPage(context, path) != null;
	}

	public WikiPagePath getFullPathOfChild(WikiPage parent, WikiPagePath childPath) throws Exception
	{
		WikiPagePath fullPathOfChild;
		if(childPath.isAbsolute())
			fullPathOfChild = childPath.relativePath();
		else
		{
			WikiPagePath absolutePathOfParent = new WikiPagePath(parent);
			fullPathOfChild = absolutePathOfParent.append(childPath);
		}
		return fullPathOfChild;
	}

	public WikiPagePath getFullPath(WikiPage page) throws Exception
	{
		return new WikiPagePath(page);
	}

	public WikiPage addPage(WikiPage context, WikiPagePath path, String content) throws Exception
	{
		WikiPage page = addPage(context, path);
		if(page != null)
		{
			PageData data = new PageData(page);
			data.setContent(content);
			page.commit(data);
		}
		return page;
	}

	public WikiPage addPage(WikiPage context, WikiPagePath path) throws Exception
	{
		return getOrMakePage(context, path.getNames());
	}

	private WikiPage getOrMakePage(WikiPage context, List namePieces) throws Exception
	{
		String first = (String) namePieces.get(0);
		List rest = namePieces.subList(1, namePieces.size());
		WikiPage current;
		if(context.getChildPage(first) == null)
			current = context.addChildPage(first);
		else
			current = context.getChildPage(first);
		if(rest.size() == 0)
			return current;
		return getOrMakePage(current, rest);
	}

	public String getRelativeName(WikiPage base, WikiPage page) throws Exception
	{
		StringBuffer qualName = new StringBuffer();
		for(WikiPage p = page; !isRoot(p) && p != base; p = p.getParent())
		{
			if(p != page)
				qualName.insert(0, ".");
			qualName.insert(0, p.getName());
		}
		return qualName.toString();
	}

	//TODO this doesn't belong here
	public static WikiPage getInheritedPage(String pageName, WikiPage context) throws Exception
	{
		List ancestors = WikiPageUtil.getAncestorsStartingWith(context);
		for(Iterator iterator = ancestors.iterator(); iterator.hasNext();)
		{
			WikiPage ancestor = (WikiPage) iterator.next();
			WikiPage namedPage = ancestor.getChildPage(pageName);
			if(namedPage != null)
				return namedPage;
		}
		return null;
	}

	public boolean isRoot(WikiPage page) throws Exception
	{
		WikiPage parent = page.getParent();
		return parent == null || parent == page;
	}

	public WikiPage getRoot(WikiPage page) throws Exception
	{
		if(isRoot(page))
			return page;
		else
			return getRoot(page.getParent());
	}

	public void traverse(WikiPage context, FitNesseTraversalListener listener) throws Exception
	{
		if(context.getClass() == SymbolicPage.class)
			return;
		listener.processPage(context);
		List children = context.getChildren();
		for(Iterator iterator = children.iterator(); iterator.hasNext();)
		{
			WikiPage wikiPage = (WikiPage) iterator.next();
			traverse(wikiPage, listener);
		}
	}
}
