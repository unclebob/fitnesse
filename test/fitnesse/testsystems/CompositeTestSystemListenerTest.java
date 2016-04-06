package fitnesse.testsystems;

import java.io.IOException;

import fitnesse.testsystems.CompositeTestSystemListener.CompositeIOException;
import org.junit.Test;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

public class CompositeTestSystemListenerTest {

  @Test
  public void shouldRunHandlersEvenIfOneThrowsException() throws IOException {
    CompositeTestSystemListener compositeListener = new CompositeTestSystemListener();

    TestSystemListener throwingListener = mock(TestSystemListener.class);
    TestSystemListener behavingListener = mock(TestSystemListener.class);

    IOException exceptionToBeThrown = new IOException("dummy");

    doThrow(exceptionToBeThrown).when(throwingListener).testOutputChunk(anyString());

    compositeListener.addTestSystemListener(throwingListener);
    compositeListener.addTestSystemListener(behavingListener);

    try {
      compositeListener.testOutputChunk("Chunk");
      fail("An exception should have been raised");
    } catch (IOException e) {
      assertThat(e, is(exceptionToBeThrown));
      assertThat(e.getMessage(), is("dummy"));
    }

    verify(throwingListener).testOutputChunk("Chunk");
    verify(behavingListener).testOutputChunk("Chunk");
  }


  @Test
  public void shouldReportMultipleExceptions() throws IOException {
    CompositeTestSystemListener compositeListener = new CompositeTestSystemListener();

    TestSystemListener throwingListener = mock(TestSystemListener.class);
    TestSystemListener anotherThrowingListener = mock(TestSystemListener.class);
    TestSystemListener behavingListener = mock(TestSystemListener.class);

    doThrow(IOException.class).when(throwingListener).testOutputChunk(anyString());
    doThrow(IOException.class).when(anotherThrowingListener).testOutputChunk(anyString());

    compositeListener.addTestSystemListener(throwingListener);
    compositeListener.addTestSystemListener(anotherThrowingListener);
    compositeListener.addTestSystemListener(behavingListener);

    try {
      compositeListener.testOutputChunk("Chunk");
      fail("An exception should have been raised");
    } catch (IOException e) {
      assertThat(e.getMessage(), is("2 test system listeners threw exceptions"));
      assertThat(((CompositeIOException) e).getCauses(), hasSize(2));
    }

    verify(throwingListener).testOutputChunk("Chunk");
    verify(anotherThrowingListener).testOutputChunk("Chunk");
    verify(behavingListener).testOutputChunk("Chunk");
  }

  @Test
  public void failingListsenersAreRemoved() throws IOException {
    CompositeTestSystemListener compositeListener = new CompositeTestSystemListener();

    TestSystemListener throwingListener = mock(TestSystemListener.class);
    TestSystemListener behavingListener = mock(TestSystemListener.class);

    doThrow(IOException.class).when(throwingListener).testOutputChunk(anyString());

    compositeListener.addTestSystemListener(throwingListener);
    compositeListener.addTestSystemListener(behavingListener);

    try {
      compositeListener.testOutputChunk("Chunk");
      fail("An exception should have been raised");
    } catch (IOException e) {
    }

    assertThat(compositeListener.listeners(), not(contains(throwingListener)));
    assertThat(compositeListener.listeners(), contains(behavingListener));
  }
}
