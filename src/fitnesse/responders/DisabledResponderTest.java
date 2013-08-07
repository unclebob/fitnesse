package fitnesse.responders;

import util.RegexTestCase;
import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;

public class DisabledResponderTest extends RegexTestCase {

	 private FitNesseContext context;

	  public void setUp() {
	    context = FitNesseUtil.makeTestContext();
	  }

	  public void testResponse() throws Exception {
	    Responder responder = new DisabledResponder();
	    SimpleResponse response = (SimpleResponse) responder.makeResponse(context, new MockRequest());

	    assertEquals(200, response.getStatus());

	    String body = response.getContent();

	    assertHasRegexp("<html>", body);
	    assertHasRegexp("<body", body);
	    assertHasRegexp("that this action should be disabled.", body);
	  }

	 

}
