// Copyright (C) 2003,2004,2005 by Object Mentor, Inc. All rights reserved.
// Released under the terms of the GNU General Public License version 2 or later.
package fitnesse.authentication;

public class TransparentCipher implements PasswordCipher
{
	public String encrypt(String password) throws Exception
	{
		return password;
	}
}
