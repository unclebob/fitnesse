package fitnesse.responders;

import fitnesse.*;
import fitnesse.components.XmlWriter;
import fitnesse.wiki.*;
import fitnesse.util.XmlUtil;
import fitnesse.http.*;
import org.w3c.dom.*;
import java.io.ByteArrayOutputStream;

public class RssResponder implements Responder
{
	protected Element channelElement;
	private String resource;
	private WikiPage contextPage;

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		Document rssDocument = buildRssHeader();

		resource = request.getResource();
		contextPage = context.root.getPageCrawler().getPage(context.root, PathParser.parse(resource));
		XmlUtil.addTextNode(rssDocument, channelElement, "title", "FitNesse:");
		WikiPage page = context.root.getChildPage("RecentChanges");
		buildItemReport(page, rssDocument, request.getResource());
		SimpleResponse response = responseFrom(rssDocument);
		return response;
	}

	private void buildItemReport(WikiPage page, Document rssDocument, String resource) throws Exception
	{
		if(page != null)
		{
			PageData data = page.getData();
			String recentChanges = data.getContent();
			String lines[] = recentChanges.split("\n");
			for(int i = 0; i < lines.length; i++)
			{
				String fields[] = lines[i].split("\\|");
				String title = fields[1];
				String author = fields[2];
				String pubDate = fields[3];

				if (shouldReportItem(resource, title))
					buildItem(rssDocument, title, author, pubDate);
			}
		}
	}

	private boolean shouldReportItem(String resource, String title)
	{
		return !exists(resource) || title.startsWith(resource);
	}

	private void buildItem(Document rssDocument, String title, String author, String pubDate) throws Exception
	{
		Element itemElement1 = rssDocument.createElement("item");
		XmlUtil.addTextNode(rssDocument, itemElement1, "title", title);
		XmlUtil.addTextNode(rssDocument, itemElement1, "author", author);
		XmlUtil.addTextNode(rssDocument, itemElement1, "pubDate", pubDate);
		buildLink(rssDocument, itemElement1, title);

		String description = makeDescription(author, pubDate);
		XmlUtil.addTextNode(rssDocument, itemElement1, "description", description);
		Element itemElement = itemElement1;
		channelElement.appendChild(itemElement);
	}

	private void buildLink(Document rssDocument, Element itemElement1, String pageName ) throws Exception
	{
		String prefix = "http://localhost/";
		if(contextPage != null)
		{
			PageData data = contextPage.getData();
			String prefixVariable = data.getVariable("RSS_PREFIX");
			prefix = prefixVariable == null ? prefix : prefixVariable;
		}
		String link = prefix + pageName;

		XmlUtil.addTextNode(rssDocument, itemElement1, "link", link);
	}

	private String makeDescription(String author, String pubDate)
	{
		String description;
		String authoredBy = "";
		if (exists(author))
			authoredBy = author + ":";
		description = authoredBy + pubDate;
		return description;
	}

	private boolean exists(String author)
	{
		return author != null && author.length() > 0;
	}

	private SimpleResponse responseFrom(Document rssDocument) throws Exception
	{
		byte[] bytes = toByteArray(rssDocument);
		SimpleResponse response = new SimpleResponse();
		response.setContent(bytes);
		response.setContentType("text/xml");
		return response;
	}

	private byte[] toByteArray(Document rssDocument) throws Exception
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		XmlWriter writer = new XmlWriter(os);
		writer.write(rssDocument);
		writer.close();
		byte[] bytes = os.toByteArray();
		return bytes;
	}

	private Document buildRssHeader() throws Exception
	{
		Document rssDocument = XmlUtil.newDocument();
		Element rssDocumentElement = rssDocument.createElement("rss");
		rssDocument.appendChild(rssDocumentElement);
		channelElement = rssDocument.createElement("channel");
		rssDocumentElement.setAttribute("version", "2.0");
		rssDocumentElement.appendChild(channelElement);
		return rssDocument;
	}

}
