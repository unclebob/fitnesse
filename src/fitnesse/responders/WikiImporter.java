package fitnesse.responders;

import fitnesse.wiki.*;
import fitnesse.http.*;
import fitnesse.util.XmlUtil;
import fitnesse.components.FitNesseTraversalListener;
import java.util.*;
import java.net.*;
import org.w3c.dom.Document;

public class WikiImporter implements XmlizerPageHandler, FitNesseTraversalListener
{
	private String remoteHostname;
	private int remotePort;
	private WikiPagePath localPath;
	private WikiPagePath remotePath = new WikiPagePath();
	private WikiPagePath relativePath = new WikiPagePath();
	private WikiImporterClient importerClient;
	protected int importCount = 0;
	protected int unmodifiedCount = 0;

	public static String remoteUsername;
	public static String remotePassword;
	private List<WikiPagePath> orphans = new LinkedList<WikiPagePath>();
	private HashSet<WikiPagePath> pageCatalog;
	private PageCrawler crawler;
	private boolean shouldDeleteOrphans = true;
	private WikiPagePath contextPath;

	public WikiImporter()
	{
		this.importerClient = new NullWikiImporterClient();
		this.localPath = new WikiPagePath();
	}

	public void importWiki(WikiPage page) throws Exception
	{
		catalogLocalTree(page);

		Document remotePageTreeDocument = getPageTree();
		new PageXmlizer().deXmlizeSkippingRootLevel(remotePageTreeDocument, page, this);

		filterOrphans(page);
		if(shouldDeleteOrphans)
			removeOrphans(page);
	}

	private void removeOrphans(WikiPage context) throws Exception
	{
		for(Iterator iterator = orphans.iterator(); iterator.hasNext();)
		{
			WikiPagePath path = (WikiPagePath) iterator.next();
			WikiPage wikiPage = crawler.getPage(context, path);
			if(wikiPage != null)
				wikiPage.getParent().removeChildPage(wikiPage.getName());
		}
	}

	private void filterOrphans(WikiPage context) throws Exception
	{
		for(Iterator iterator = pageCatalog.iterator(); iterator.hasNext();)
		{
			WikiPagePath wikiPagePath = (WikiPagePath) iterator.next();
			WikiPage unrecognizedPage = crawler.getPage(context, wikiPagePath);
			PageData data = unrecognizedPage.getData();
			WikiImportProperty importProps = WikiImportProperty.createFrom(data.getProperties());
			if(importProps != null && !importProps.isRoot())
			{
				orphans.add(wikiPagePath);
			}
		}
	}

	private void catalogLocalTree(WikiPage page) throws Exception
	{
		crawler = page.getPageCrawler();
		contextPath = crawler.getFullPath(page);
		pageCatalog = new HashSet<WikiPagePath>();
		page.getPageCrawler().traverse(page, this);
		pageCatalog.remove(contextPath);
	}

	public void enterChildPage(WikiPage childPage, Date lastModified) throws Exception
	{
		if(pageCatalog != null)
		{
			pageCatalog.remove(relativePath(childPage));
		}
		remotePath.addName(childPage.getName());
		relativePath.addName(childPage.getName());
		localPath.addName(childPage.getName());

		WikiPageProperties props = childPage.getData().getProperties();
		WikiImportProperty importProps = WikiImportProperty.createFrom(props);
		if(importProps != null)
		{
			Date lastRemoteModification = importProps.getLastRemoteModificationTime();
			if(lastModified.after(lastRemoteModification))
				importRemotePageContent(childPage);
			else
				unmodifiedCount++;
		}
		else
			importRemotePageContent(childPage);
	}

	private WikiPagePath relativePath(WikiPage childPage) throws Exception
	{
		return crawler.getFullPath(childPage).subtract(contextPath);
	}

	void importRemotePageContent(WikiPage localPage) throws Exception
	{
		try
		{
			Document doc = getXmlDocument("data");
			PageData remoteData = new PageXmlizer().deXmlizeData(doc);

			WikiPageProperties remoteProps = remoteData.getProperties();
			remoteProps.remove("Edit");

			WikiImportProperty importProperty = new WikiImportProperty(remoteUrl());
			Date lastModificationTime = remoteProps.getLastModificationTime();
			importProperty.setLastRemoteModificationTime(lastModificationTime);
			importProperty.addTo(remoteProps);
			localPage.commit(remoteData);

			importerClient.pageImported(localPage);
		}
		catch(AuthenticationRequiredException e)
		{
			throw e;
		}
		catch(Exception e)
		{
			importerClient.pageImportError(localPage, e);
		}
		importCount++;
	}

	public String remoteUrl()
	{
		String remotePathName = PathParser.render(remotePath);
		return "http://" + remoteHostname + ":" + remotePort + "/" + remotePathName;
	}

	public void exitPage()
	{
		remotePath.pop();
		relativePath.pop();
		localPath.pop();
	}

	public Document getPageTree() throws Exception
	{
		return getXmlDocument("pages");
	}

	private Document getXmlDocument(String documentType) throws Exception
	{
		String remotePathName = PathParser.render(remotePath);
		RequestBuilder builder = new RequestBuilder("/" + remotePathName);
		builder.addInput("responder", "proxy");
		builder.addInput("type", documentType);
		builder.setHostAndPort(remoteHostname, remotePort);
		if(remoteUsername != null)
			builder.addCredentials(remoteUsername, remotePassword);

		ResponseParser parser = ResponseParser.performHttpRequest(remoteHostname, remotePort, builder);

		if(parser.getStatus() == 404)
			throw new Exception("The remote resource, " + remoteUrl() + ", was not found.");
		if(parser.getStatus() == 401)
			throw new AuthenticationRequiredException(remoteUrl());

		String body = parser.getBody();
		return XmlUtil.newDocument(body);
	}

	public void setRemoteUsername(String username)
	{
		remoteUsername = username;
	}

	public void setRemotePassword(String password)
	{
		remotePassword = password;
	}

	public WikiPagePath getRelativePath()
	{
		return relativePath;
	}

	public WikiPagePath getLocalPath()
	{
		return localPath;
	}

	public String getRemoteHostname()
	{
		return remoteHostname;
	}

	public int getRemotePort()
	{
		return remotePort;
	}

	public WikiPagePath getRemotePath()
	{
		return remotePath;
	}

	public int getUnmodifiedCount()
	{
		return unmodifiedCount;
	}

	public int getImportCount()
	{
		return importCount;
	}

	public void parseUrl(String urlString) throws Exception
	{
		URL url = null;
		try
		{
			url = new URL(urlString);
		}
		catch(MalformedURLException e)
		{
			throw new MalformedURLException(urlString + " is not a valid URL.");
		}

		remoteHostname = url.getHost();
		remotePort = url.getPort();
		if(remotePort == -1)
			remotePort = 80;

		String path = url.getPath();
		if(path.startsWith("/"))
			path = path.substring(1);
		remotePath = PathParser.parse(path);

		if(remotePath == null)
			throw new MalformedURLException("The URL's resource path, " + path + ", is not a valid WikiWord.");
	}

	public void setWikiImporterClient(WikiImporterClient client)
	{
		importerClient = client;
	}

	public void setLocalPath(WikiPagePath path)
	{
		localPath = path;
	}

	public List<WikiPagePath> getOrphans()
	{
		return orphans;
	}

	public void processPage(WikiPage page) throws Exception
	{
		pageCatalog.add(relativePath(page));
	}

	public String getSearchPattern() throws Exception
	{
		return null;
	}

	public void setDeleteOrphanOption(boolean shouldDeleteOrphans)
	{
		this.shouldDeleteOrphans = shouldDeleteOrphans;
	}

	private static class NullWikiImporterClient implements WikiImporterClient
	{

		public void pageImported(WikiPage localPage)
		{
		}

		public void pageImportError(WikiPage localPage, Exception e)
		{
		}
	}

	public static class AuthenticationRequiredException extends Exception
	{
		public AuthenticationRequiredException(String message)
		{
			super(message);
		}
	}

}
