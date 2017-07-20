package fitnesse.testsystems.slim;

import fitnesse.testrunner.WikiPageDescriptor;
import fitnesse.testsystems.ConsoleExecutionLogListener;
import fitnesse.wiki.PathParser;
import fitnesse.wiki.WikiPage;
import fitnesse.wiki.WikiPageUtil;
import fitnesse.wiki.fs.InMemoryPage;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SslSlimClientBuilderTest {



  private WikiPage root;

  @Before
  public void setUp() throws Exception {
    root = InMemoryPage.makeRoot("RooT");
  }

  @Test
  public void sslParametersCanBeFound() throws ClassNotFoundException {
    Class.forName("fitnesse.socketservice.SslParametersWiki");
  }

  @Test
  public void StartAndConnectToSlimClientWithSslAgentWiki() throws Exception {
	  executeAndCheck("fitnesse.socketservice.SslParametersAgent", "fitnesse.socketservice.SslParametersWiki");
  }

  @Test
  public void StartAndConnectToSlimClientWithoutSsl() throws Exception {
	  executeAndCheck("false", "fitnesse.socketservice.SslParametersWiki");
  }

  @Test
  public void StartAndConnectToSlimClientWithSslWikiWiki() throws Exception {
	  executeAndCheck("fitnesse.socketservice.SslParametersWiki", "fitnesse.socketservice.SslParametersWiki");
  }

  public void executeAndCheck(String agentParameters, String clientParameters) throws Exception {
    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"),
        "!define TEST_SYSTEM {slim}\n" +
        "!define SLIM_PORT {0}\n" +
        "!define SLIM_SSL {"+ agentParameters +"}\n" +
        "!define slim.timeout {10}\n" +
        "!define slim.pool.size {1}\n" +
        "!define wiki.protocol.ssl.parameter.class {"+ clientParameters + "}\n"
    );
    WikiPageDescriptor descriptor = new WikiPageDescriptor(testPage, false, false, "build/classes/test", "classes");
    descriptor.getExecutionLogListener().addExecutionLogListener(new ConsoleExecutionLogListener());
    SlimClientBuilder clientBuilder = new SlimClientBuilder(descriptor);
    String testSystemName = clientBuilder.getTestSystemName();
    assertEquals("slim:" + "fitnesse.slim.SlimService", testSystemName);

    SlimCommandRunningClient client = clientBuilder.build();

    boolean isConnected;

    client.start();

    try{
      isConnected = client.isConnected();
    }finally{
        client.bye();
        client.kill();
    }

    assertTrue("Got connected to client", isConnected);
  }
}


