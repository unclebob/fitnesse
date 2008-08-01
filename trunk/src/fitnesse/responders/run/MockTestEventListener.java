package fitnesse.responders.run;

import fitnesse.wiki.PageData;

public class MockTestEventListener implements TestEventListener
{
	public boolean gotPreTestNotification;
	public boolean gotPreSuiteNotification;

	public void notifyPreTest(TestResponder testResponder, PageData data)
	{
		gotPreTestNotification = true;
	}

	public void notifyPreSuite(TestResponder testResponder, PageData data) throws Exception
	{
		gotPreSuiteNotification = true;
	}
}
