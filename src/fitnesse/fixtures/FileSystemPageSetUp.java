// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.fixtures;

import fitnesse.responders.ResponderFactory;
import fitnesse.wiki.FileSystemPage;
import fit.Fixture;

public class FileSystemPageSetUp extends Fixture
{
	public FileSystemPageSetUp() throws Exception
	{
		FitnesseFixtureContext.root = FileSystemPage.makeRoot(FitnesseFixtureContext.baseDir,"RooT");
		FitnesseFixtureContext.responderFactory = new ResponderFactory(FitnesseFixtureContext.baseDir);
	}
}
