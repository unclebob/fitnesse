package fitnesse.responders.editing;

import fitnesse.FitNesseContext;
import fitnesse.http.MockRequest;
import fitnesse.http.Response;
import fitnesse.http.SimpleResponse;
import fitnesse.testutil.FitNesseUtil;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentFilterResponderTest {
  private ContentFilter contentFilter;
  private ContentFilterResponder filter;
  private FitNesseContext context;
  private MockRequest request;

  @Before
  public void setUp() throws Exception {
    contentFilter = mock(ContentFilter.class);
    filter = new ContentFilterResponder(contentFilter);
    context = FitNesseUtil.makeTestContext();
    request = new MockRequest();
  }

  @Test
  public void passThroughIfContentIsAcceptable() throws Exception {
    when(contentFilter.isContentAcceptable(isNull(), anyString())).thenReturn(true);
    Response response = filter.makeResponse(context, request);
    assertThat(response, is(nullValue()));
  }

  @Test
  public void errorResponseIfContentIsNotAcceptable() throws Exception {
    when(contentFilter.isContentAcceptable(isNull(), anyString())).thenReturn(false);
    Response response = filter.makeResponse(context, request);

    assertThat(response, instanceOf(SimpleResponse.class));
    String content = ((SimpleResponse) response).getContent();
    assertThat(content + " does not contain 'Banned Content'", content, containsString("banned"));
  }

}
