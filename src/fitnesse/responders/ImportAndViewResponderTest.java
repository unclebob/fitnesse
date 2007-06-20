package fitnesse.responders;

import fitnesse.FitNesseContext;
import fitnesse.http.*;
import fitnesse.testutil.FitNesseUtil;
import fitnesse.wiki.*;
import junit.framework.TestCase;

public class ImportAndViewResponderTest extends TestCase
{
	private WikiImporterTest testData;
	private ImportAndViewResponder responder;

	public void setUp() throws Exception
	{
		testData = new WikiImporterTest();
		testData.createRemoteRoot();
		testData.createLocalRoot();

		FitNesseUtil.startFitnesse(testData.remoteRoot);

		responder = new ImportAndViewResponder();
	}

	public void tearDown() throws Exception
	{
		FitNesseUtil.stopFitnesse();
	}

	public void testRedirect() throws Exception
	{
		Response response = getResponse();

		assertEquals(303, response.getStatus());
		assertEquals("PageTwo", response.getHeader("Location"));
	}

	private Response getResponse() throws Exception
	{
		FitNesseContext context = new FitNesseContext(testData.localRoot);
		MockRequest request = new MockRequest();
		request.setResource("PageTwo");
		return responder.makeResponse(context, request);
	}

	public void testPageContentIsUpdated() throws Exception
	{
		PageData data = testData.pageTwo.getData();
		WikiPageProperties props = data.getProperties();

		WikiImportProperty importProps = new WikiImportProperty("http://localhost:" + FitNesseUtil.port + "/PageTwo");
		importProps.addTo(props);
		testData.pageTwo.commit(data);

		getResponse();

		data = testData.pageTwo.getData();
		assertEquals("page two", data.getContent());
	}
}
