package fitnesse.responders.editing;

import fitnesse.responders.*;
import fitnesse.*;
import fitnesse.wiki.PathParser;
import fitnesse.http.*;

public class BlogResponderTest extends ResponderTest
{
	// Return an instance of the Responder being tested.
	protected Responder responderInstance()
	{
		return new BlogResponder();
	}

	public void testHtml() throws Exception
	{
		crawler.addPage(root, PathParser.parse("MyPage"));
		request.setResource("MyPage");
		SimpleResponse response = (SimpleResponse)responder.makeResponse(context, request);
		assertEquals(200, response.getStatus());

		String content = response.getContent();
		assertSubString("<title>BLOG MyPage</title>", content);
		assertSubString(">MyPage</a>", content);

		assertSubString("<form", content);
		assertSubString("method=\"post\"", content);
		assertSubString("action=\"MyPage\"", content);

		assertSubString("<input type=\"hidden\" name=\"responder\" value=\"addBlogComment\"/>", content);
		assertSubString("<input type=\"hidden\" name=\"date\"", content);
		assertSubString("<input type=\"text\" name=\"bloggerName\" value=\"\"/>", content);
		assertSubString("<input type=\"text\" name=\"subject\" value=\"\" size=\"80\"/>", content);
		assertSubString("<textarea name=\"comment\" cols=\"80\" rows=\"20\"></textarea>", content);
		assertSubString("<input type=\"submit\" name=\"blog\" value=\"Append Comment\"/>", content);
	}


	public void testInvalidPage() throws Exception
	{
  	request.setResource("NonExistentPage");
		SimpleResponse response = (SimpleResponse)responder.makeResponse(context, request);
		String content = response.getContent();
		assertEquals(404, response.getStatus());
	}
}
