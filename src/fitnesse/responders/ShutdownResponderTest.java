package fitnesse.responders;

import junit.framework.TestCase;
import fitnesse.*;
import fitnesse.authentication.AlwaysSecureOperation;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.http.*;

public class ShutdownResponderTest extends TestCase
{
	private FitNesseContext context;
	private FitNesse fitnesse;
	private boolean doneShuttingDown;

	protected void setUp() throws Exception
	{
		context = new FitNesseContext();
		context.port = FitNesseUtil.port;
		fitnesse = new FitNesse(context);
		fitnesse.start();
		context.fitnesse = fitnesse;
	}

	protected void tearDown() throws Exception
	{
		fitnesse.stop();
	}

	public void testFitNesseGetsShutdown() throws Exception
	{
		ShutdownResponder responder = new ShutdownResponder();
		responder.makeResponse(context, new MockRequest());
    Thread.sleep(200);
		assertFalse(fitnesse.isRunning());
	}

	public void testShutdownCalledFromServer() throws Exception
	{
		Thread thread = new Thread(){
			public void run()
			{
				try
				{
					RequestBuilder request = new RequestBuilder("/?responder=shutdown");
					ResponseParser.performHttpRequest("localhost", FitNesseUtil.port, request);
					doneShuttingDown = true;
				}
				catch(Exception e)
				{
					e.printStackTrace();
				}
			}
		};
		thread.start();

		Thread.sleep(500);

		assertTrue(doneShuttingDown);
		assertFalse(fitnesse.isRunning());
	}

	public void testIsSecure() throws Exception
	{
		assertTrue((new ShutdownResponder().getSecureOperation() instanceof AlwaysSecureOperation) == true);
	}
}
