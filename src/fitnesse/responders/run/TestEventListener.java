package fitnesse.responders.run;

import fitnesse.wiki.PageData;

public interface TestEventListener
{
	void notifyPreTest(TestResponder testResponder, PageData data) throws Exception;
}
