package fitnesse.testsystems.slim;

import java.io.File;
import java.io.IOException;

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
  public static void main(String[] args) throws IOException {
	    	try{
	    		SslSlimClientBuilderTest test = new SslSlimClientBuilderTest();
	    		test.setUp();
	    		System.out.println("----1 ---");
	    		test.StartAndConnectToSlimClientWithoutSsl();
	    		System.out.println("----2 ---");
	    		test.StartAndConnectToSlimClientWithSslAgentWiki();
	    		System.out.println("----3 ---");
	    	}catch (Exception e){
	    		e.printStackTrace();
	    		System.out.println("Haling test: " + e.getMessage());
	    	}
}



  @Test
  public void StartAndConnectToSlimClientWithSslAgentWiki() throws Exception {
	  executeAndCheck("FitNesseAgent", "fitnesse.socketservice.SslParametersAgent", "FitNesseWiki", "fitnesse.socketservice.SslParametersWiki");
  }
  @Test
  public void StartAndConnectToSlimClientWithoutSsl() throws Exception {
	  executeAndCheck(null, "false", null, "fitnesse.socketservice.SslParametersWiki");
  }

  @Test
  public void StartAndConnectToSlimClientWithSslWikiWiki() throws Exception {
	  executeAndCheck("FitNesseWiki", "fitnesse.socketservice.SslParametersWiki", "FitNesseWiki", "fitnesse.socketservice.SslParametersWiki");
  }

  public void executeAndCheck(String agentName, String agentParameters, String clientName, String clientParameters) throws Exception {
	    WikiPage testPage = WikiPageUtil.addPage(root, PathParser.parse("TestPage"), 
	    		"!define TEST_SYSTEM {slim}\n" +
	    		"!define SLIM_SSL {"+ agentParameters +"}\n" +
	    		"!define slim.timeout {10}\n" +
	    		"!define slim.pool.size {1}\n" +
	    		"!define wiki.protocol.ssl.parameter.class {"+ clientParameters + "}\n"
    );
	    WikiPageDescriptor descriptor = new WikiPageDescriptor(testPage, false, false, "test-classes" + File.pathSeparator + "classes");	    descriptor.getExecutionLogListener().addExecutionLogListener(new ConsoleExecutionLogListener());
	    System.out.print("----------------------------------------------------\n");
	    System.out.print("SLIM_SSL: " + descriptor.getVariable("SLIM_SSL") + "\n");
	    System.out.print("slim.timeout: " + descriptor.getVariable("slim.timeout") + "\n");
	    System.out.print("wiki.protocol.ssl.parameter.class: " + descriptor.getVariable("wiki.protocol.ssl.parameter.class") + "\n");
	    System.out.print("TEST_SYSTEM: " + descriptor.getVariable("TEST_SYSTEM") + "\n");
	    SlimClientBuilder clientBuilder = new SlimClientBuilder(descriptor);
	    String testSystemName = clientBuilder.getTestSystemName();
	    assertEquals("slim:" + "fitnesse.slim.SlimService", testSystemName);
	    
	    SlimCommandRunningClient client = clientBuilder.build();
	    
	    boolean isConnected;
	    String myName;
	    String peerName;

	    client.start();
	 
	    try{
	    	isConnected = client.isConnected();
	    	myName = client.getMyName();
	    	peerName = client.getPeerName();
	    }finally{
	        client.bye();
	        client.kill();
	  	}
	    
	    assertTrue("Got connected to client:", isConnected);
	    System.out.print("My   Name is: " + myName +"\n");
	    System.out.print("Peer Name is: " + peerName+"\n");
	    assertEquals("Client Name", clientName, myName);
	    assertEquals("Agent Name", agentName, peerName);

	  }
}
  

