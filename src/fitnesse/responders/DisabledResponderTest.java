package fitnesse.responders;

import static org.junit.Assert.assertEquals;
import static util.RegexTestCase.assertHasRegexp;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import org.junit.Before;
import org.junit.Test;

public class DisabledResponderTest {

  private FitNesseContext context;

  @Before
  public void setUp() {
    context = FitNesseUtil.makeTestContext();
  }

  @Test
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
