package fitnesse.responders.editing;

import fitnesse.*;
import fitnesse.responders.NotFoundResponder;
import fitnesse.html.*;
import fitnesse.http.*;
import fitnesse.wiki.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BlogResponder implements Responder
{
	public Response makeResponse(FitNesseContext context, Request request) throws Exception
	{
		String pageName = request.getResource();
		PageCrawler crawler = context.root.getPageCrawler();
		if(crawler.pageExists(context.root, PathParser.parse(pageName)))
			return makeResponseForValidPage(context, pageName);
		else
      return new NotFoundResponder().makeResponse(context, request);
	}

	private Response makeResponseForValidPage(FitNesseContext context, String pageName)
	  throws Exception
	{
		SimpleResponse response = new SimpleResponse();
		HtmlPage blogPage = makeBlogPage(context, pageName);
		response.setContent(blogPage.html());
		return response;
	}

	private HtmlPage makeBlogPage(FitNesseContext context, String pageName)
	  throws Exception
	{
		HtmlPage page = context.htmlPageFactory.newPage();
		page.title.use("BLOG " + pageName);
		page.header.use(HtmlUtil.makeBreadCrumbsWithPageType(pageName, "Add BLOG comment."));
		page.main.use(makeBlogForm(pageName));
		return page;
	}

	private HtmlTag makeBlogForm(String pageName)
	{
		HtmlTag form = new HtmlTag("form");
		form.addAttribute("method", "post");
		form.addAttribute("action", pageName);
		form.add(HtmlUtil.makeInputTag("hidden", "responder", "addBlogComment"));
		form.add(HtmlUtil.makeInputTag("hidden", "date", makeDateString()));
		form.add("Your Name:");
		form.add(HtmlUtil.makeInputTag("text", "bloggerName", ""));
		form.add("Subject:");
		form.add(makeSubjectField());
		form.add(HtmlUtil.BR);
		form.add(makeCommentField());
		form.add(HtmlUtil.BR);
		form.add(HtmlUtil.makeInputTag("submit", "blog", "Append Comment"));
		return form;
	}

	private String makeDateString()
	{
		Date today = new Date();
		SimpleDateFormat f = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss");
		String dateString = f.format(today);
		return dateString;
	}

	private HtmlTag makeSubjectField()
	{
		HtmlTag subjectField = HtmlUtil.makeInputTag("text", "subject", "");
		subjectField.addAttribute("size", "80");
		return subjectField;
	}

	private HtmlTag makeCommentField()
	{
		HtmlTag commentField = new HtmlTag("textarea");
		commentField.addAttribute("name", "comment");
		commentField.addAttribute("cols", "80");
		commentField.addAttribute("rows", "20");
		commentField.add("");
		return commentField;
	}
}
