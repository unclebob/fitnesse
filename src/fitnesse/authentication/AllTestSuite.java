// Copyright (C) 2003,2004 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.authentication;

import junit.framework.*;
import fitnesse.testutil.TestSuiteMaker;

public class AllTestSuite
{
	public static Test suite()
	{
		return TestSuiteMaker.makeSuite("authentication", new Class[] {
			AuthenticatorTest.class,
			SecureOperationTest.class,
			MultiUserAuthenticatorTest.class,
			HashingCipherTest.class,
			PasswordTest.class,
			PasswordFileTest.class
		});
	}
}

