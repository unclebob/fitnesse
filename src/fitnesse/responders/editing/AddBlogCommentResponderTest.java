package fitnesse.responders.editing;

import fitnesse.Responder;
import fitnesse.http.SimpleResponse;
import fitnesse.responders.ResponderTest;
import fitnesse.wiki.*;

public class AddBlogCommentResponderTest extends ResponderTest
{
	private WikiPagePath blogPagePath;

	// Return an instance of the Responder being tested.
	protected Responder responderInstance()
	{
		return new AddBlogCommentResponder();
	}

	public void setUp() throws Exception
	{
		super.setUp();
		blogPagePath = PathParser.parse("BlogPage");
		request.addInput("page", "BlogPage");
		request.addInput("date", "today");
		request.addInput("bloggerName", "Bob");
		request.addInput("subject", "the subject");
		request.addInput("comment", "the comment");
		request.setResource("BlogPage");
	}

	public void testAppendComment() throws Exception
	{
		crawler.addPage(root, blogPagePath, "the text\n");

		SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
		assertEquals(303, response.getStatus());
		assertHasRegexp("Location: BlogPage", response.makeHttpHeaders());

		WikiPage page = crawler.getPage(root, blogPagePath);
		PageData data = page.getData();
		String content = data.getContent();
		System.out.println("content = " + content);
		assertHasRegexp("the text", content);
		assertHasRegexp("\\!\\* today, Bob, the subject", content);
		assertHasRegexp("the comment", content);
		assertHasRegexp("\\*\\!", content);
	}

	public void testBadPage() throws Exception
	{
		SimpleResponse response = (SimpleResponse) responder.makeResponse(context, request);
		assertEquals(404, response.getStatus());
	}
}
