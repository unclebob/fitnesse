package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.Responder;
import fitnesse.http.MockRequest;
import fitnesse.http.Request;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertSame;

public class FilterResponderFactoryTest {
  private ResponderFactory factory;
  private MockRequest request;
  private FitNesseContext context;

  // Responses set my mockResponder/mockFilter
  private static Response expectedFilterResponse;
  private static Response expectedAnotherFilterResponse;
  private static Response expectedResponderResponse;

  @Before
  public void resetStaticVariables() {
    expectedFilterResponse = null;
    expectedAnotherFilterResponse = null;
    expectedResponderResponse = null;
  }

  @Before
  public void setUp() throws Exception {
    factory = new ResponderFactory("testDir");
    factory.addResponder("mock", MockResponder.class);
    factory.addFilter("mock", new MockFilter());

    request = new MockRequest();
    request.addInput("responder", "mock");
    context = FitNesseUtil.makeTestContext();
  }

  @Test
  public void canAddFiltersForAResponder() throws Exception {
    Responder responder = factory.makeResponder(request);
    expectedFilterResponse = new SimpleResponse();
    Response response = responder.makeResponse(context, request);

    assertSame(expectedFilterResponse, response);
  }

  @Test
  public void shouldCallResponderIfFilterReturnsNull() throws Exception {
    Responder responder = factory.makeResponder(request);
    expectedResponderResponse = new SimpleResponse();
    Response response = responder.makeResponse(context, request);

    assertSame(expectedResponderResponse, response);
  }

  @Test
  public void shouldExecuteMultipleFilters() throws Exception {
    factory.addFilter("mock", new AnotherMockFilter());
    Responder responder = factory.makeResponder(request);
    expectedAnotherFilterResponse = new SimpleResponse();
    Response response = responder.makeResponse(context, request);

    assertSame(expectedAnotherFilterResponse, response);
  }

  public static class MockFilter implements Responder {
    @Override
    public Response makeResponse(FitNesseContext context, Request request) throws Exception {
      return expectedFilterResponse;
    }
  }

  public static class AnotherMockFilter implements Responder {
    @Override
    public Response makeResponse(FitNesseContext context, Request request) throws Exception {
      return expectedAnotherFilterResponse;
    }
  }

  public static class MockResponder implements Responder {
    @Override
    public Response makeResponse(FitNesseContext context, Request request) throws Exception {
      return expectedResponderResponse;
    }
  }
}
