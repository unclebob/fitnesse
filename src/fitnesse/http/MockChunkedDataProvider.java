package fitnesse.http;

public class MockChunkedDataProvider implements ChunkedDataProvider {

  @Override
  public void startSending(boolean includeDecoration) {
    // Nothing to send.
    
  }

}
