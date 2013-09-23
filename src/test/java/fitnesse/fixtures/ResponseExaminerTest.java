package fitnesse.fixtures;

import org.junit.Test;
import org.junit.Before;
import static org.junit.Assert.assertEquals;
import fitnesse.http.MockResponseSender;
import fitnesse.http.SimpleResponse;

public class ResponseExaminerTest {
  private ResponseExaminer examiner;

  @Before
  public void setup() throws Exception {
    examiner = new ResponseExaminer();
    FitnesseFixtureContext.sender = new MockResponseSender();
    FitnesseFixtureContext.sender.send("&lt;hello".getBytes());
    FitnesseFixtureContext.response = new SimpleResponse();
    FitnesseFixtureContext.response.setStatus(42);
  }

  @Test
  public void shouldBeAbleToExtractContents() throws Exception {
    examiner.type = "contents";
    examiner.extractValueFromResponse();
    assertEquals("<hello", examiner.getValue());
  }

  @Test
  public void shouldBeAbleToExtractFullContents() throws Exception {
    examiner.type = "fullContents";
    examiner.extractValueFromResponse();
    assertEquals("&amp;lt;hello", examiner.getValue());
  }

  @Test
  public void shouldBeAbleToExtractStatus() throws Exception {
    examiner.type = "status";
    examiner.extractValueFromResponse();
    assertEquals("42", examiner.getValue());
  }

  @Test
  public void shouldBeAbleToExtractHeaders() throws Exception {
    FitnesseFixtureContext.sender = new MockResponseSender();
    FitnesseFixtureContext.sender.send("Headers \r\n\r\n bleh".getBytes());
    examiner.type = "headers";
    examiner.extractValueFromResponse();
    assertEquals("Headers \r\n", examiner.getValue());
  }
}
