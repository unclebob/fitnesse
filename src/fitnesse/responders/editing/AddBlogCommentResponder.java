package fitnesse.responders.editing;

import fitnesse.*;
import fitnesse.responders.NotFoundResponder;
import fitnesse.http.*;
import fitnesse.wiki.*;

public class AddBlogCommentResponder implements Responder
{
	private final String nl = "\n";

	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		final String pageName = request.getResource();

		PageCrawler crawler = context.root.getPageCrawler();
		WikiPage page = crawler.getPage(context.root, PathParser.parse(pageName));
		if(page != null)
		{
			appendRequestedCommentToPage(page, request);
			SimpleResponse response = new SimpleResponse();
			response.redirect(pageName);
			return response;
		}
		else
		{
			return new NotFoundResponder().makeResponse(context, request);
		}
	}

	private void appendRequestedCommentToPage(WikiPage page, Request request)
	  throws Exception
	{
		PageData data = page.getData();
		String content = data.getContent();
		String newContent = content + nl +
		  "!* " + request.getInput("date") + ", " +
		  request.getInput("bloggerName") + ", " +
		  request.getInput("subject") + nl +
		  request.getInput("comment") + nl + "*!" + nl;
		data.setContent(newContent);
		page.commit(data);
	}
}
