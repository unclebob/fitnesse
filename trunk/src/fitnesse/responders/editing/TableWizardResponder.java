// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.editing;

import fitnesse.components.*;
import fitnesse.wiki.WikiPage;

public class TableWizardResponder extends EditResponder
{
	protected String createPageContent() throws Exception
	{
		String textAreaContent = (String) request.getInput("text");
		String fixtureName = (String) request.getInput("fixture");
		String template = createFixtureTableTemplate(fixtureName);
		if(!template.equals(""))
			template = "\n" + template;
		return textAreaContent + template;
	}

	private String createFixtureTableTemplate(String fixtureName) throws Exception
	{
		String commandLine = createCommandLine(page, fixtureName);
		CommandRunner runner = new CommandRunner(commandLine, null);
		runner.run();
		return runner.getOutput() + runner.getError();
	}

	protected String createCommandLine(WikiPage page, String fixtureName) throws Exception
	{
		String classpath = new ClassPathBuilder().getClasspath(page);

		return "java -cp " + classpath + " fitnesse.FixtureTemplateCreator " + fixtureName;
	}
}
