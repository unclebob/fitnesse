// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.responders.files;

import junit.framework.*;
import fitnesse.testutil.TestSuiteMaker;

public class AllTestSuite
{
	public static Test suite()
	{
		return TestSuiteMaker.makeSuite("files", new Class[] {
			FileResponderTest.class,
			UploadResponderTest.class,
			CreateDirectoryResponderTest.class,
			DeleteFileResponderTest.class,
			RenameFileResponderTest.class,
			RenameFileConfirmationResponderTest.class,
			DeleteConfirmationResponderTest.class,
			DirectoryResponderTest.class
		});
	}
}
