package fitnesse.responders;

import fitnesse.wiki.*;
import fitnesse.FitNesseContext;
import fitnesse.testutil.RegexTest;
import fitnesse.http.*;

public class ChunkingResponderTest extends RegexTest
{

	private Exception exception;
	private Response response;
	private FitNesseContext context;
	private WikiPage root = new MockWikiPage();
	private ChunkingResponder responder = new ChunkingResponder()
	{
		protected void doSending() throws Exception
		{
			throw exception;
		}
	};

	protected void setUp() throws Exception
	{
		context = new FitNesseContext();
		context.root = root;
	}

	public void testException() throws Exception
	{
  	exception = new Exception("test exception");
		response = responder.makeResponse(context, new MockRequest());
		String result = new MockResponseSender(response).sentData();
		assertSubString("test exception", result);
	}
}